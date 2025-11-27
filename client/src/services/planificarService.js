// src/services/simulationService.js
import { Client } from "@stomp/stompjs";
/**
 * @typedef {import("../types/simulationRequest/SimulationRequest").SimulationRequest} SimulationRequest
 */

const SOCKET_URL =
  (window.location.protocol === "https:" ? "wss://" : "ws://") +
  window.location.host +
  "/ws";

let client = null;

export function connectSimulatorWS(onSolution) {
  if (client && client.active) return;

  client = new Client({
    brokerURL: SOCKET_URL,
    reconnectDelay: 5000,
    debug: () => {}, 
    onConnect: () => {
      console.log("STOMP conectado a", SOCKET_URL);
      client.subscribe("/topic/simulator", (message) => {
        const solution = JSON.parse(message.body);
        onSolution(solution);
      });
    },
    onStompError: (frame) => {
      console.error("Error STOMP:", frame.headers["message"], frame.body);
    },
  });

  client.activate();
}

/**
 * @param {SimulationRequest} request
 */
export function sendSimulationRequest(request) {
  if (!client || !client.connected) {
    console.error("STOMP no conectado; no se puede enviar SimulationRequest");
    return;
  }

  client.publish({
    destination: "/app/obtenerSimulacion",
    body: JSON.stringify(request),
  });
}

export function disconnectWS() {
  if (client) {
    client.deactivate();
    client = null;
  }
}
