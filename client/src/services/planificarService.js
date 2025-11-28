// src/services/planificarService.js
import { Client } from "@stomp/stompjs";
/**
 * @typedef {import("../types/simulationRequest/SimulationRequest").SimulationRequest} SimulationRequest
 */

const SOCKET_URL =
  (window.location.protocol === "https:" ? "wss://" : "ws://") +
  window.location.host +
  "/ws";

let client = null;

/**
 * @param {(solution: any) => void} onSolution         // SolutionResponse de /topic/simulator
 * @param {(status: any) => void} onStatus             // ProcessStatusResponse de /topic/simulator-status
 */
export function connectSimulatorWS(onSolution, onStatus) {
  if (client && client.active) return;

  client = new Client({
    brokerURL: SOCKET_URL,
    reconnectDelay: 5000,
    debug: () => {}, // pon console.log si quieres ver tráfico
    onConnect: () => {
      console.log("STOMP conectado a", SOCKET_URL);

      // Soluciones de planificación
      client.subscribe("/topic/simulator", (message) => {
        const solution = JSON.parse(message.body);
        onSolution(solution);
      });

      // Estados del proceso (INICIADO, COLAPSADO, FINALIZADO, etc.)
      client.subscribe("/topic/simulator-status", (message) => {
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

/**
 * Enviar SimulationRequest para iniciar la simulación
 * @param {SimulationRequest} request
 */
export function sendSimulationRequest(request) {
  if (!client || !client.connected) {
    console.error("STOMP no conectado; no se puede enviar SimulationRequest");
    return;
  }

  client.publish({
    destination: "/app/simulator-init", // 👈 nuevo mapping
    body: JSON.stringify(request),
  });
}

/** Pedir al back que detenga la simulación */
export function sendStopSimulation() {
  if (!client || !client.connected) {
    console.error("STOMP no conectado; no se puede enviar stop");
    return;
  }

  client.publish({
    destination: "/app/simulator-stop",
    body: "", // no necesita body
  });
}

export function disconnectWS() {
  if (client) {
    client.deactivate();
    client = null;
  }
}
