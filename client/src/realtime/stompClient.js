import { Client } from "@stomp/stompjs";
import { WS_URL } from "../config";
import { leerSesion } from "../auth/session";
import { obtenerTokenAcceso } from "../auth/tokens";
import { escuchar } from "../auth/eventos";

// UN solo cliente STOMP para toda la pestaña. Las suscripciones se registran aquí y se
// re-suscriben solas cuando la conexión se recupera (antes cada servicio abría su propio socket).
// Autenticación: el token de acceso va en el CONNECT (el server lo valida solo ahí). Una vez conectado,
// la conexión sigue aunque el token venza o la sesión expire: las simulaciones en curso no se cortan.
// Se desconecta al cerrar sesión o al entrar con otra cuenta (el server autoriza las suscripciones por rol).

let client = null;
let conectado = false;
let cuentaConectada = null; // código del usuario con el que se abrió la conexión
let secuencia = 0;
const suscripciones = new Map(); // id -> { destino, handler, stomp }
const oyentesDeConexion = new Set();

function avisarConexion() {
  oyentesDeConexion.forEach((fn) => fn(conectado));
}

function asegurarCliente() {
  if (client) return;
  client = new Client({
    brokerURL: WS_URL,
    reconnectDelay: 3000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    debug: () => {},
    beforeConnect: async () => {
      const token = await obtenerTokenAcceso();
      cuentaConectada = leerSesion()?.usuario?.codigo ?? null;
      client.connectHeaders = token ? { Authorization: `Bearer ${token}` } : {};
      if (!token) await client.deactivate(); // sin sesión no se conecta (se reactiva al iniciar sesión)
    },
    onConnect: () => {
      conectado = true;
      suscripciones.forEach((s) => {
        s.stomp = client.subscribe(s.destino, s.handler);
      });
      avisarConexion();
    },
    onWebSocketClose: () => {
      if (conectado) {
        conectado = false;
        suscripciones.forEach((s) => (s.stomp = null));
        avisarConexion();
      }
    },
    onStompError: (frame) => {
      console.error("[STOMP]", frame.headers?.message, frame.body);
    },
  });
  if (leerSesion()) client.activate();

  // Login / logout / cambio de cuenta (también desde otra pestaña)
  escuchar("sesion", reconciliar);
  escuchar("sesion-externa", reconciliar);
  escuchar("logout", () => { cuentaConectada = null; client?.deactivate(); });
}

function reconciliar() {
  if (!client) return;
  const codigo = leerSesion()?.usuario?.codigo ?? null;
  if (!codigo) return; // sesión expirada: la conexión abierta se mantiene
  if (!client.active) {
    client.activate();
  } else if (cuentaConectada && cuentaConectada !== codigo) {
    // Otra cuenta (o código nuevo tras cambio de rol): reconectar con su token
    client.deactivate().then(() => client.activate());
  }
}

/**
 * Se suscribe a un tópico. 'callback(cuerpo, destinoReal)'; el destino real sirve con comodines
 * (p. ej. "/topic/importation-status-*" recibe "/topic/importation-status-<id>").
 * Devuelve la función para desuscribirse.
 */
export function suscribir(destino, callback) {
  asegurarCliente();
  const id = ++secuencia;
  const handler = (mensaje) => {
    let cuerpo = mensaje.body;
    try { cuerpo = JSON.parse(mensaje.body); } catch { /* texto */ }
    callback(cuerpo, mensaje.headers?.destination ?? destino);
  };
  const entrada = { destino, handler, stomp: conectado ? client.subscribe(destino, handler) : null };
  suscripciones.set(id, entrada);
  return () => {
    const e = suscripciones.get(id);
    if (!e) return;
    suscripciones.delete(id);
    try { e.stomp?.unsubscribe(); } catch { /* socket ya cerrado */ }
  };
}

export const estaConectado = () => conectado;

export function alCambiarConexion(fn) {
  oyentesDeConexion.add(fn);
  return () => oyentesDeConexion.delete(fn);
}

// Espera la conexión (máx. 'timeoutMs'); no falla si no llega: la app sigue y se re-suscribe luego
export function esperarConexion(timeoutMs = 4000) {
  asegurarCliente();
  if (conectado) return Promise.resolve(true);
  return new Promise((resolve) => {
    const t = setTimeout(() => { quitar(); resolve(false); }, timeoutMs);
    const quitar = alCambiarConexion((ok) => {
      if (ok) { clearTimeout(t); quitar(); resolve(true); }
    });
  });
}

// El estado de ejecución llega como objeto { estadoEjecucion, estadoFinalizacion } o como string suelto
export const normalizarEstado = (status) =>
  typeof status === "string"
    ? { estadoEjecucion: status, estadoFinalizacion: null }
    : { estadoEjecucion: status?.estadoEjecucion ?? null, estadoFinalizacion: status?.estadoFinalizacion ?? null, mensaje: status?.mensaje ?? null };
