import { listarParametros } from "./parametrosService";

let workerPort = null;
let listeners = [];

// URLs dinámicas como en planificarService
const WS_URL =
  (window.location.protocol === "https:" ? "wss://" : "ws://") +
  window.location.host +
  "/ws";

const API_BASE_URL = window.location.origin;

export async function initOperationManager() {
  const { dtos } = await listarParametros();
  const parametros = dtos?.[0];

  if (!("SharedWorker" in window)) {
    console.warn("SharedWorker no soportado");
    return;
  }

  const worker = new SharedWorker("/shared-worker.js");
  workerPort = worker.port;
  workerPort.start();

  workerPort.postMessage({
    type: "init",
    wsEndpoint: WS_URL,
    apiBaseUrl: API_BASE_URL,
    parametros
  });

  workerPort.onmessage = e => {
    listeners.forEach(fn => fn(e.data));
  };
}

export function onEvent(fn) {
  listeners.push(fn);
  return () => {
    listeners = listeners.filter(l => l !== fn);
  };
}

export function notifyNewOrder({ fechaHoraISO } = {}) {
  console.log("[OM] notifyNewOrder:", fechaHoraISO);
  workerPort?.postMessage({
    type: "notify-new-order",
    fechaHora: fechaHoraISO
  });
}


export function setParametros(parametros) {
  workerPort?.postMessage({ type: "set-parametros", parametros });
}

export function getState() {
  return new Promise(resolve => {
    if (!workerPort) return resolve(null);

    const off = onEvent(msg => {
      if (msg.type === "state") {
        off();
        resolve(msg.estado);
      }
    });

    workerPort.postMessage({ type: "get-state" });
  });
}
