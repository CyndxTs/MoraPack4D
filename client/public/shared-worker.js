// ===========================================================
// shared-worker.js - SharedWorker con STOMP WebSocket
// ===========================================================

// === CONFIG ===
const WS_ENDPOINT_DEFAULT = "ws://localhost:8080/ws"; 
const REPLANIFY_ENDPOINT = "http://localhost:8080/api/operation-replanificate";
const REPLANIFICACION_MINUTOS = 15;

// STOMP UMD
importScripts("/stomp.umd.min.js");

// === ESTADO GLOBAL ===
let ports = [];
let stompClient = null;
let wsEndpoint = WS_ENDPOINT_DEFAULT;

let fechaHoraPrimerPedido = null;
let listoParaReplanificar = false;
let pendingOrders = 0;
let parametros = null;
let replanificacionTimer = null;

// ===========================================================
// Broadcast
// ===========================================================
function broadcast(msg) {
  ports.forEach(p => { try { p.postMessage(msg); } catch(e){} });
}

function parseSafeJson(s) {
  try { return JSON.parse(s); } catch { return s; }
}

// ===========================================================
// Conexión STOMP
// ===========================================================
function connectStomp() {
  if (stompClient && stompClient.active) return;

  const { Client } = StompJs;

  stompClient = new Client({
    webSocketFactory: () => new WebSocket(wsEndpoint),
    reconnectDelay: 5000,
    debug: () => {}
  });

  stompClient.onConnect = () => {
    console.log("[SW] Conectado STOMP");
    stompClient.subscribe("/topic/operator-status", m =>
      broadcast({ type: "operator-status", payload: parseSafeJson(m.body) })
    );
    stompClient.subscribe("/topic/operator", m =>
      broadcast({ type: "operator", payload: parseSafeJson(m.body) })
    );

    broadcast({ type: "stomp-connected" });
  };

  stompClient.onStompError = (e) => {
    console.error("[SW] STOMP error", e);
    broadcast({ type: "stomp-error", error: e });
  };

  stompClient.activate();
}

// ===========================================================
// Timer
// ===========================================================
function clearTimer() {
  if (replanificacionTimer) clearTimeout(replanificacionTimer);
  replanificacionTimer = null;
}

function scheduleTimerFrom(fechaIso) {
  clearTimer();
  if (!fechaIso) return;

  let fecha = new Date(fechaIso);
  let target = new Date(fecha.getTime() + REPLANIFICACION_MINUTOS * 60000);
  let ms = target - new Date();

  if (ms < 0) ms = 0;

  replanificacionTimer = setTimeout(onReplanificacionTimer, ms);

  broadcast({
    type: "timer-scheduled",
    targetIso: target.toISOString(),
    msRemaining: ms
  });
}

// ===========================================================
// EJECUTAR REPLANIFICACIÓN
// ===========================================================
async function onReplanificacionTimer() {
  if (!listoParaReplanificar) return;

  if (!parametros) {
    console.warn("[SW] No hay parámetros cargados, abortando replanificación.");
    return;
  }

  broadcast({
    type: "info",
    message: "Comenzando replanificación…",
    pendingOrders
  });

  try {
    const resp = await fetch(REPLANIFY_ENDPOINT, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        fechaHoraActual: fechaUTC5(REPLANIFICACION_MINUTOS),
        parametros
      })
    });

    if (!resp.ok) {
      broadcast({
        type: "error",
        message: "Error POST: " + resp.status
      });
      return;
    }

    const json = await resp.json();

    broadcast({
      type: "replanificacion-iniciada",
      payload: json
    });

    // RESET
    listoParaReplanificar = false;
    fechaHoraPrimerPedido = null;
    pendingOrders = 0;
    clearTimer();

  } catch (e) {
    broadcast({
      type: "error",
      message: "Error replanificando: " + e.message
    });
  }
}

// ===========================================================
// PUERTOS
// ===========================================================
onconnect = (e) => {
  const port = e.ports[0];
  ports.push(port);

  port.postMessage({
    type: "sw-init",
    estado: { fechaHoraPrimerPedido, listoParaReplanificar, pendingOrders }
  });

  port.onmessage = (evt) => {
    const msg = evt.data;

    switch (msg.type) {
      case "init":
        wsEndpoint = msg.wsEndpoint || WS_ENDPOINT_DEFAULT;
        connectStomp();
        break;

      case "notify-new-order":
        pendingOrders++;

        if (!fechaHoraPrimerPedido) {
          fechaHoraPrimerPedido = msg.fechaHora || new Date().toISOString();
          listoParaReplanificar = true;
          scheduleTimerFrom(fechaHoraPrimerPedido);
        }
        break;

      case "set-parametros":
        console.log("[SW] Parámetros actualizados:", msg.parametros);
        parametros = msg.parametros;
        break;

      case "force-replanify-now":
        fechaHoraPrimerPedido = new Date().toISOString();
        listoParaReplanificar = true;
        clearTimer();
        onReplanificacionTimer();
        break;

      case "disconnect-port":
        try { port.close(); } catch {}
        break;
    }
  };

  port.start();
};

// ===========================================================
// LIMPIEZA
// ===========================================================
setInterval(() => {
  ports = ports.filter(p => p && !p.closed);
}, 10000);

// ===========================================================
// FECHA: UTC-5 + minutos programados
// ===========================================================
function fechaUTC5(minutosExtra = 0) {
  const now = new Date();
  const t = now.getTime()
    + (5 * 3600 * 1000)   // UTC-5
    + (minutosExtra * 60000);

  const d = new Date(t);
  const pad = n => n.toString().padStart(2, "0");

  return (
    d.getFullYear() + "-" +
    pad(d.getMonth() + 1) + "-" +
    pad(d.getDate()) + " " +
    pad(d.getHours()) + ":" +
    pad(d.getMinutes()) + ":" +
    pad(d.getSeconds())
  );
}
