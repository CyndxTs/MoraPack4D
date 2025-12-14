importScripts("/stomp.umd.min.js");

let ports = [];
let stompClient = null;

let wsEndpoint = null;
let apiBaseUrl = null;

let parametros = null;
let pendingOrders = 0;
let fechaHoraPrimerPedido = null;
let listoParaReplanificar = false;
let replanTimer = null;
let log5minTimer = null;
let log10minTimer = null;

const REPLANIFICACION_MINUTOS = 15;

function connectWS() {
  if (stompClient && stompClient.active) return;

  stompClient = new StompJs.Client({
    brokerURL: wsEndpoint,
    reconnectDelay: 5000,
    debug: () => {},
    onConnect: () => {
      console.log("[SW][WS] Conectado");

      stompClient.subscribe("/topic/operation-status", msg => {
        const payload = JSON.parse(msg.body);
        broadcast({
          type: "operation-status",
          payload
        });
      });

      stompClient.subscribe("/topic/operation", msg => {
        const payload = JSON.parse(msg.body);
        broadcast({
          type: "operation-solution",
          payload
        });
      });
    },
    onStompError: frame => {
      console.error("[SW][WS] Error", frame.headers["message"]);
    }
  });

  stompClient.activate();
}


// -----------------------------------------------------------
function broadcast(msg) {
  ports.forEach(p => { try { p.postMessage(msg); } catch {} });
}

function logEstado(origen) {
  console.log(`[SW][${origen}]`, {
    listoParaReplanificar,
    fechaHoraPrimerPedido,
    pendingOrders,
    parametros
  });
}

// -----------------------------------------------------------
function fechaHoraPeru(extraMinutes = 0) {
  const now = new Date();
  const utc = now.getTime() + now.getTimezoneOffset() * 60000;
  const peru = new Date(utc - 5 * 3600000 + extraMinutes * 60000);
  const pad = n => n.toString().padStart(2, "0");
  return `${peru.getFullYear()}-${pad(peru.getMonth()+1)}-${pad(peru.getDate())} ${pad(peru.getHours())}:${pad(peru.getMinutes())}:${pad(peru.getSeconds())}`;
}

// -----------------------------------------------------------
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


// -----------------------------------------------------------
// TIMER
// -----------------------------------------------------------
function scheduleTimer(fromFechaPeru) {
  clearAllTimers();

  const base = new Date(fromFechaPeru.replace(" ", "T"));
  const target = new Date(base.getTime() + REPLANIFICACION_MINUTOS * 60000);
  const totalMs = Math.max(0, target - new Date());

  // ⏱ LOG 5 MINUTOS
  log5minTimer = setTimeout(() => {
    console.log("[SW][TIMER] 5 minutos transcurridos");
    logEstado("TIMER-5MIN");
  }, 5 * 60 * 1000);

  // ⏱ LOG 10 MINUTOS
  log10minTimer = setTimeout(() => {
    console.log("[SW][TIMER] 10 minutos transcurridos");
    logEstado("TIMER-10MIN");
  }, 10 * 60 * 1000);

  broadcast({
    type: "notification-global",
    variant: "info",
    message: "Iniciando replanificación (pedidos dentro de 15 minutos)"
  });

  // ⏱ TIMER FINAL 15 MIN
  replanTimer = setTimeout(runReplanification, totalMs);

  console.log("[SW][TIMER] Programado:", {
    desde: fromFechaPeru,
    ejecutaEn: fechaHoraPeru(REPLANIFICACION_MINUTOS),
    totalMs
  });

  logEstado("scheduleTimer");
}

function clearAllTimers() {
  if (replanTimer) clearTimeout(replanTimer);
  if (log5minTimer) clearTimeout(log5minTimer);
  if (log10minTimer) clearTimeout(log10minTimer);

  replanTimer = null;
  log5minTimer = null;
  log10minTimer = null;
}


// -----------------------------------------------------------
// REPLANIFICAR
// -----------------------------------------------------------
async function runReplanification() {
  if (!listoParaReplanificar) return;

  if (!parametros) {
    console.error("[SW] Replanificación cancelada: parametros = null");
    broadcast({
      type: "notification-global",
      variant: "danger",
      message: "No se puede replanificar: parámetros no cargados"
    });
    return;
  }

  broadcast({
    type: "notification-global",
    variant: "info",
    message: `Se están replanificando ${pendingOrders} pedidos`
  });

  logEstado("runReplanification-INICIO");

  try {
    const resp = await fetch(`${apiBaseUrl}/api/operation-replanificate`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        fechaHoraActual: fechaHoraUTC(),
        almacenarParametrizacion: false, // 🔴 NUEVO
        parametros
      })
    });

    const json = await resp.json();

    console.log("[SW][BACKEND][response]", json);

    if (!json.exito) {
      throw new Error(json.mensaje || "Error en replanificación");
    }

    broadcast({
      type: "notification-global",
      variant: "success",
      message: json.mensaje || "Replanificación iniciada"
    });

    // ⚠️ YA NO HAY PAYLOAD AQUÍ
    broadcast({
      type: "replanificacion-iniciada",
      token: json.token,
      pendingOrders
    });

    pendingOrders = 0;
    fechaHoraPrimerPedido = null;
    listoParaReplanificar = false;
    clearAllTimers();

    logEstado("runReplanification-FIN");

  } catch (e) {
    broadcast({
      type: "notification-global",
      variant: "danger",
      message: e.message || "Error durante la replanificación"
    });
    console.error("[SW] Error:", e);
  }
}


// -----------------------------------------------------------
// PORTS
// -----------------------------------------------------------
onconnect = e => {
  const port = e.ports[0];
  ports.push(port);

  port.onmessage = evt => {
    const msg = evt.data;

    switch (msg.type) {
      case "init":
        wsEndpoint = msg.wsEndpoint;
        apiBaseUrl = msg.apiBaseUrl;
        parametros = msg.parametros;
        console.log("[SW] INIT", parametros);
        connectWS();
        break;

      case "notify-new-order":
        if (!parametros) {
          console.warn("[SW] Pedido recibido pero parámetros aún no cargados");
          return;
        }

        pendingOrders++;
        if (!fechaHoraPrimerPedido) {
          fechaHoraPrimerPedido = fechaHoraPeru(); // ⬅️ SIEMPRE AHORA
          listoParaReplanificar = true;
          scheduleTimer(fechaHoraPrimerPedido);
        }
        logEstado("notify-new-order");
        break;

      case "set-parametros":
        parametros = msg.parametros;
        logEstado("set-parametros");
        break;
    }
  };

  port.start();
};
