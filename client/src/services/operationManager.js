import { Client } from "@stomp/stompjs";
import axios from "axios"; // <--- 1. IMPORTANTE: FALTABA ESTO
import { listarParametros } from "./parametrosService";

/**
 * @typedef {import("../types/simulationRequest/SimulationRequest").SimulationRequest} SimulationRequest
 */
/**
 * @typedef {import("../types/simulationResponse/SolutionPayload").SolutionPayload} SolutionPayload
 */

/* ===============================
   CONFIG
================================ */
const REPLANIFICACION_MINUTOS = 20;

const SOCKET_URL =
  (window.location.protocol === "https:" ? "wss://" : "ws://") +
  window.location.host +
  "/ws";

// 2. CORRECCIÓN: Usamos ruta relativa directamente.
// Axios usará el host actual automáticamente.
const API_OP_REPLANIFICATE = "/api/operation-replanificate";

/* ===============================
   ESTADO
================================ */
let client = null;
let parametros = null;
let listeners = [];

let pendingOrders = 0;
let fechaHoraPrimerPedido = null;
let listoParaReplanificar = false;

// Variables de Timers
let replanTimer = null;
let log5minTimer = null;
let log10minTimer = null;

// Flag para saber si esta pestaña es la que inició el timer
let isMasterTab = false; 

/* ===============================
   EVENTOS
================================ */
function broadcast(msg) {
  listeners.forEach((fn) => fn(msg));
}

export function onEvent(fn) {
  listeners.push(fn);
  return () => {
    listeners = listeners.filter((l) => l !== fn);
  };
}

/* ===============================
   FECHAS
================================ */
function fechaHoraUTC() {
  const d = new Date();
  const pad = (n) => n.toString().padStart(2, "0");

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
const tabChannel = new BroadcastChannel("om_sync_channel");

tabChannel.onmessage = (event) => {
  const { action, payload } = event.data;

  if (action === "NEW_ORDER_TRIGGERED") {
    console.log("[OM] Recibido aviso de pedido desde otra pestaña.");
    // isRemote = true. Esta pestaña NO hará la llamada a la API, solo UI.
    triggerLocalOrderLogic(payload, true);
  }
};

/* ===============================
   STOMP
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

  const targetTimeMs = startTimeMs + REPLANIFICACION_MINUTOS * 60000;
  const nowMs = Date.now();
  const totalMs = Math.max(0, targetTimeMs - nowMs);

  console.log(`[OM] Timer programado para ejecutarse en ${totalMs / 1000} s`);

  // Logs visuales
  log10minTimer = setTimeout(() => {
    console.log(`[OM] Han pasado 10 minutos`);
  }, 10 * 60 * 1000);

  log5minTimer = setTimeout(() => {
    console.log(`[OM] Han pasado ${REPLANIFICACION_MINUTOS} minutos`);
  }, REPLANIFICACION_MINUTOS * 60 * 1000);

  broadcast({
    type: "notification-global",
    variant: "info",
    message: `Iniciando replanificación (ejecución en ${REPLANIFICACION_MINUTOS} min)`,
  });

  // Ejecutamos la replanificación cuando el tiempo acabe
  replanTimer = setTimeout(runReplanification, totalMs);
}

/* ===============================
   API BACKEND (Service Layer)
================================ */
export async function sendReplanificationRequest(requestPayload) {
  try {
    const { data } = await axios.post(API_OP_REPLANIFICATE, requestPayload);

    if (data.exito === false) {
      throw new Error(data.mensaje || "La operación no tuvo éxito");
    }
    return data;
  } catch (error) {
    if (error.response && error.response.data) {
      throw new Error(
        error.response.data.message || "Error al procesar la replanificación"
      );
    }
    if (error.message && !error.response) {
      throw error;
    }
    throw new Error("No se pudo conectar con el servidor de operaciones");
  }
}

async function runReplanification() {
  console.log("Intentando replanificar...", { listoParaReplanificar, parametros });

  if (!listoParaReplanificar || !parametros) return;

  // 3. CORRECCIÓN LÓGICA:
  // Si esta pestaña NO es la "Master" (la que inició la acción),
  // no debe llamar a la API, solo limpiar su estado visual.
  if (!isMasterTab) {
      console.log("[OM] Saltando llamada API (Pestaña remota)");
      // Limpiamos estado visual local
      pendingOrders = 0;
      fechaHoraPrimerPedido = null;
      listoParaReplanificar = false;
      clearAllTimers();
      return; 
  }

  broadcast({
    type: "notification-global",
    variant: "info",
    message: `Se están replanificando ${pendingOrders} pedidos`,
  });

  try {
    const payload = {
      fechaHoraActual: fechaHoraUTC(),
      almacenarParametrizacion: true,
      parametros,
    };

    const json = await sendReplanificationRequest(payload);

    broadcast({
      type: "replanificacion-iniciada",
      token: json.token,
      pendingOrders,
    });

    // Reset de estado
    pendingOrders = 0;
    fechaHoraPrimerPedido = null;
    listoParaReplanificar = false;
    isMasterTab = false; // Reset del flag
    clearAllTimers();
  } catch (e) {
    broadcast({
      type: "notification-global",
      variant: "danger",
      message: e.message || "Error durante la replanificación",
    });
  }
}

/* ===============================
   PUBLIC API
================================ */

export function notifyNewOrder() {
  const now = Date.now();

  // 1. Ejecutar lógica LOCAL (esta pestaña es la Master)
  triggerLocalOrderLogic(now, false);

  broadcast({
    type: "order-created", // 👈 CLAVE
  });

  // 2. Avisar a las OTRAS pestañas
  tabChannel.postMessage({
    action: "NEW_ORDER_TRIGGERED",
    payload: now,
  });
}

function triggerLocalOrderLogic(timeReference, isRemote) {
  if (!parametros) {
    console.warn("[OM] Pedido recibido pero parámetros no cargados");
    return;
  }

  pendingOrders++;

  if (!fechaHoraPrimerPedido) {
    fechaHoraPrimerPedido = fechaHoraUTC();
    listoParaReplanificar = true;
    
    // Aquí decidimos quién es el responsable de llamar a la API al final del timer
    isMasterTab = !isRemote; 

    console.log(
      `[OM] Se comienza replanificación. Primer pedido: ${fechaHoraPrimerPedido}. MasterTab: ${isMasterTab}`
    );

    scheduleTimer(Date.now());
  }
}

/* ===============================
   REPLANIFICACIÓN MANUAL (BOTÓN)
================================ */
export function forceReplanification() {
  console.log("[OM] Replanificación manual solicitada");

  if (!parametros) {
    console.warn("[OM] No hay parámetros cargados");
    return;
  }

  if (!listoParaReplanificar) {
    console.warn("[OM] No hay pedidos pendientes");
    return;
  }

  // Esta pestaña pasa a ser la Master
  isMasterTab = true;

  // Cancelamos cualquier timer activo
  clearAllTimers();

  // Ejecutamos inmediatamente
  runReplanification();
}