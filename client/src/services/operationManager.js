import { Client } from "@stomp/stompjs";
import { listarParametros } from "./parametrosService";

/**
 * @typedef {import("../types/simulationRequest/SimulationRequest").SimulationRequest} SimulationRequest
 */
/**
 * @typedef {import("../types/simulationResponse/SolutionPayload").SolutionPayload} SolutionPayload
 */

/**
 * @param {SolutionPayload} onSolution         // SolutionResponse de /topic/simulator
 * @param {(status: any) => void} onStatus             // ProcessStatusResponse de /topic/simulator-status
 */

/* ===============================
   CONFIG
================================ */
const REPLANIFICACION_MINUTOS = 5;

const SOCKET_URL =
  (window.location.protocol === "https:" ? "wss://" : "ws://") +
  window.location.host +
  "/ws";

// 2. API URL (AQUÍ ESTÁ LA CORRECCIÓN CLAVE)
// Esto detecta automáticamente si estás en localhost o en 1inf54...
const API_BASE_URL = 
  window.location.protocol + "//" + 
  window.location.hostname + 
  ":8080/api";

/* ===============================
   ESTADO
================================ */
let client = null;
let parametros = null;

let listeners = [];

let pendingOrders = 0;
let fechaHoraPrimerPedido = null;
let listoParaReplanificar = false;

let replanTimer = null;
let log5minTimer = null;
let log10minTimer = null;

/* ===============================
   EVENTOS
================================ */
function broadcast(msg) {
  listeners.forEach(fn => fn(msg));
}

export function onEvent(fn) {
  listeners.push(fn);
  return () => {
    listeners = listeners.filter(l => l !== fn);
  };
}

/* ===============================
   FECHAS
================================ */
function fechaHoraUTC() {
  const d = new Date();
  const pad = n => n.toString().padStart(2, "0");

  return (
    d.getUTCFullYear() + "-" +
    pad(d.getUTCMonth() + 1) + "-" +
    pad(d.getUTCDate()) + " " +
    pad(d.getUTCHours()) + ":" +
    pad(d.getUTCMinutes()) + ":" +
    pad(d.getUTCSeconds())
  );
}

/* ===============================
   SINCRONIZACIÓN ENTRE PESTAÑAS
================================ */
// Creamos un canal de radio para que las pestañas se hablen
const tabChannel = new BroadcastChannel('om_sync_channel');

tabChannel.onmessage = (event) => {
  const { action, payload } = event.data;
  
  if (action === 'NEW_ORDER_TRIGGERED') {
    console.log("[OM] Recibido aviso de pedido desde otra pestaña.");
    // Ejecutamos la lógica localmente para que aparezca el log y el timer visual
    // Pasamos true para indicar que es un evento remoto y no volver a emitir
    triggerLocalOrderLogic(payload, true); 
  }
};

/* ===============================
   STOMP (MISMO PATRÓN QUE FUNCIONA)
================================ */


export function connectOperatorWS(onSolution, onStatus) {
  if (client && client.active) return;

  client = new Client({
    brokerURL: SOCKET_URL,
    reconnectDelay: 5000,
    debug: () => {},
    onConnect: () => {
      console.log("STOMP conectado a", SOCKET_URL);

      client.subscribe("/topic/operation", (message) => {
        console.log("RAW WS MESSAGE:", message.body);
        const payload = JSON.parse(message.body);
        onSolution(payload);
      });

      client.subscribe("/topic/operation-status", (message) => {
        const status = JSON.parse(message.body);
        onStatus(status);
      });
    },
    onStompError: (frame) => {
      console.error("Error STOMP:", frame.headers["message"], frame.body);
    },
  });

  client.activate();
}

export function disconnectOperationWS() {
  if (client) {
    client.deactivate();
    client = null;
    console.log("[OM] WebSocket desconectado");
  }
}

/* ===============================
   INIT
================================ */
export async function initOperationManager() {
  const { dtos } = await listarParametros();
  if (!dtos || dtos.length === 0) {
    throw new Error("No se encontraron parámetros");
  }

  parametros = dtos[0];
}

/* ===============================
   TIMER
================================ */
function clearAllTimers() {
  if (replanTimer) clearTimeout(replanTimer);
  if (log5minTimer) clearTimeout(log5minTimer);
  if (log10minTimer) clearTimeout(log10minTimer);

  replanTimer = null;
  log5minTimer = null;
  log10minTimer = null;
}

function scheduleTimer(startTimeMs) {
  clearAllTimers();

  // Calculamos el objetivo sumando 3 minutos al momento de inicio
  const targetTimeMs = startTimeMs + (REPLANIFICACION_MINUTOS * 60000);
  const nowMs = Date.now();
  
  // Calculamos la diferencia
  const totalMs = Math.max(0, targetTimeMs - nowMs);

  console.log(`[OM] Timer programado para ejecutarse en ${totalMs / 1000} segundos`);

  // Timers de log (opcional, ajustados para ser relativos)
  log5minTimer = setTimeout(() => {
    console.log("[OM] Han pasado 2 minutos");
  }, 2 * 60 * 1000); // Esto cuenta desde AHORA, no desde el inicio, ajustar si es necesario

  broadcast({
    type: "notification-global",
    variant: "info",
    message: `Iniciando replanificación (ejecución en ${REPLANIFICACION_MINUTOS} min)`
  });

  replanTimer = setTimeout(runReplanification, totalMs);
}

/* ===============================
   API BACKEND
================================ */
async function runReplanification() {
  console.log("Intentando replanificar...", { listoParaReplanificar, parametros });
  if (!listoParaReplanificar || !parametros) return;

  broadcast({
    type: "notification-global",
    variant: "info",
    message: `Se están replanificando ${pendingOrders} pedidos`
  });

  try {
    const resp = await fetch(`${API_BASE_URL}/operation-replanificate`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        fechaHoraActual: fechaHoraUTC(),
        almacenarParametrizacion: true,
        parametros
      })
    });

    const json = await resp.json();
    if (!json.exito) throw new Error(json.mensaje);

    broadcast({
      type: "replanificacion-iniciada",
      token: json.token,
      pendingOrders
    });

    pendingOrders = 0;
    fechaHoraPrimerPedido = null;
    listoParaReplanificar = false;
    clearAllTimers();

  } catch (e) {
    broadcast({
      type: "notification-global",
      variant: "danger",
      message: e.message || "Error durante la replanificación"
    });
  }
}

/* ===============================
   PUBLIC API
================================ */

// Esta función se llama cuando el usuario hace la acción
export function notifyNewOrder() {
  const now = Date.now();
  
  // 1. Ejecutar lógica en ESTA pestaña
  triggerLocalOrderLogic(now, false);

  // 2. Avisar a las OTRAS pestañas
  tabChannel.postMessage({
    action: 'NEW_ORDER_TRIGGERED',
    payload: now
  });
}

// Esta función contiene la lógica real (Logs + Timer)
function triggerLocalOrderLogic(timeReference, isRemote) {
  if (!parametros) {
    console.warn("[OM] Pedido recibido pero parámetros no cargados");
    return;
  }

  pendingOrders++;

  if (!fechaHoraPrimerPedido) {
    fechaHoraPrimerPedido = fechaHoraUTC(); // O usar timeReference para formatear
    listoParaReplanificar = true;

    console.log(
      `[OM] Se comienza replanificación. Primer pedido registrado: ${fechaHoraPrimerPedido}`
    );

    // Si es remoto (Planificación), solo queremos ver el log y el timer visual, 
    // pero idealmente solo UNA pestaña debería hacer el POST al backend.
    // Para simplificar, dejaremos que ambas inicien el timer, pero ten cuidado con duplicar el POST.
    
    // TRUCO: Si es remoto, podemos programar el timer solo visualmente o 
    // dejar que corra. Si ambas hacen POST, el backend recibirá dos peticiones.
    // Lo ideal: Solo la pestaña activa hace el POST.
    
    // Para tu caso de uso (ver los logs):
    scheduleTimer(Date.now()); 
  }
}