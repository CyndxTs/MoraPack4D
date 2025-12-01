// operationManager.js
// Uso desde cualquier página del mismo origen para notificar nuevos pedidos y escuchar eventos.
// Ejemplo: import { initOperationManager, notifyNewOrder, onEvent } from './operationManager.js'

import { listarParametros } from "./parametrosService";

let workerPort = null;
let sharedWorkerSupported = ('SharedWorker' in window);
let listeners = [];


export async function initOperationManager(options = {}) {
  const parametrosActuales = (await listarParametros()).dtos[0];
  console.log(parametrosActuales);

  if (sharedWorkerSupported) {
    try {
      const sw = new SharedWorker('/shared-worker.js');
      workerPort = sw.port;
      
      // 1) PRIMERO START
      workerPort.start();

      // 2) AHORA YA PUEDES ENVIAR MENSAJES
      workerPort.postMessage({
        type: 'init',
        wsEndpoint: options.wsEndpoint || undefined
      });

      workerPort.postMessage({
        type: 'set-parametros',
        parametros: parametrosActuales
      });

      workerPort.onmessage = handleWorkerMessage;

    } catch (e) {
      console.warn('SharedWorker init failed', e);
      sharedWorkerSupported = false;
    }
  }
}


function handleWorkerMessage(evt) {
  const msg = evt.data;
  // reenviamos a listeners
  listeners.forEach(fn => {
    try { fn(msg); } catch(e) {}
  });
}

export function onEvent(fn) {
  listeners.push(fn);
  return () => {
    listeners = listeners.filter(x => x !== fn);
  };
}

// Cuando ingresa un pedido en cualquier parte del frontend llama esto
export function notifyNewOrder({ fechaHoraISO } = {}) {
  if (workerPort) {
    workerPort.postMessage({ type: 'notify-new-order', fechaHora: fechaHoraISO });
  } else {
    // Fallback: hacer un POST directo para forzar la replanificación ahora (no recomendado)
    console.warn('notifyNewOrder: no hay workerPort. Llamando al endpoint directamente (fallback inmediato).');
    fetch('http://localhost:8080/api/operation-replanificate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ fechaHoraActual: fechaHoraISO || (new Date()).toISOString(), parametros: {} })
    }).then(r=>r.json()).then(j => {
      // informar listeners
      handleWorkerMessage({ data: { type: 'replanificacion-iniciada', payload: j, pendingOrders: 1 }});
    }).catch(e => handleWorkerMessage({ data: { type: 'error', message: 'Error fallback notifyNewOrder: ' + e.message }}));
  }
}

export function setParametros(p) {
  if (workerPort) workerPort.postMessage({ type: 'set-parametros', parametros: p });
}

export function getState() {
  return new Promise(resolve => {
    if (workerPort) {
      const listener = (m) => {
        if (m.type === 'state') {
          resolve(m.estado);
          off();
        }
      };
      const off = onEvent(listener);
      workerPort.postMessage({ type: 'get-state' });
      // timeout
      setTimeout(() => { off(); resolve(null); }, 2000);
    } else resolve(null);
  });
}
