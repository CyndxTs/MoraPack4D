import { Client } from "@stomp/stompjs";
import { WS_URL } from "../config";

// UN solo cliente STOMP para toda la pestaña. Las suscripciones se registran aquí y se
// re-suscriben solas cuando la conexión se recupera (antes cada servicio abría su propio socket).

let client = null;
let conectado = false;
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
  client.activate();
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
    : { estadoEjecucion: status?.estadoEjecucion ?? null, estadoFinalizacion: status?.estadoFinalizacion ?? null };
