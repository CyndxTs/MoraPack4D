// src/services/planificarService.js
import { Client } from "@stomp/stompjs";
import axios from "axios";

const API_SIM_INIT = "/api/simulation-init";
const API_SIM_STOP = "/api/simulation-stop";

// Detecta si es local o prod para el WebSocket
const SOCKET_URL =
  (window.location.protocol === "https:" ? "wss://" : "ws://") +
  window.location.host +
  "/ws";

let client = null;

/**
 * 1. Conecta al servidor WebSocket (Solo conexión, sin suscribirse a nada aún)
 */
export function connectSimulatorWS(onConnected) {
  if (client && client.active) {
    if (onConnected) onConnected();
    return;
  }

  client = new Client({
    brokerURL: SOCKET_URL,
    reconnectDelay: 5000,
    // debug: (str) => console.log(str), // Descomenta para ver logs del socket
    onConnect: () => {
      console.log("✅ STOMP conectado a", SOCKET_URL);
      if (onConnected) onConnected();
    },
    onStompError: (frame) => {
      console.error("❌ Error STOMP:", frame.headers["message"], frame.body);
    },
  });

  client.activate();
}

/**
 * 2. Suscribe a una simulación ESPECÍFICA usando el ID de transacción
 * Retorna una función para desuscribirse limpiamente.
 */
export function subscribeToSimulation(idTransaccion, onSolution, onStatus) {
  if (!client || !client.connected) {
    console.warn("⚠️ No hay conexión STOMP activa para suscribirse.");
    return null;
  }

  console.log(`📡 Suscribiendo a canales: ${idTransaccion}`);

  // Canal de Datos (Solución)
  const solSub = client.subscribe(
    `/topic/simulation-${idTransaccion}`,
    (message) => {
      const payload = JSON.parse(message.body);
      onSolution(payload);
    }
  );

  // Canal de Estado (Status)
  const statSub = client.subscribe(
    `/topic/simulation-status-${idTransaccion}`,
    (message) => {
      const status = JSON.parse(message.body);
      onStatus(status);
    }
  );

  // Retornamos un objeto con la función 'unsubscribe' para limpiar después
  return {
    unsubscribe: () => {
      console.log(`🔌 Desuscribiendo de ${idTransaccion}`);
      solSub.unsubscribe();
      statSub.unsubscribe();
    },
  };
}

/**
 * 3. Inicia la simulación (POST) y retorna el TOKEN
 */
export async function sendSimulationRequest(request) {
  try {
    const { data } = await axios.post(API_SIM_INIT, request);
    return data; // Retorna { exito: true, token: "TOK-12345", ... }
  } catch (error) {
    throw new Error(error.response?.data?.message || "Error al iniciar simulación");
  }
}

/**
 * 4. Detiene la simulación (POST) enviando el ID
 */
export async function sendStopSimulation(idTransaccion) {
  if (!idTransaccion) return;
  try {
    // El backend espera ?idTransaccion=XYZ
    const { data } = await axios.post(API_SIM_STOP, null, {
      params: { idTransaccion },
    });
    return data;
  } catch (error) {
    console.warn("Error al detener:", error);
  }
}

export function disconnectWS() {
  if (client) {
    client.deactivate();
    client = null;
  }
}