// src/services/planificarService.js
import { Client } from "@stomp/stompjs";
import axios from "axios"; // 👈 nuevo  
/**
 * @typedef {import("../types/simulationRequest/SimulationRequest").SimulationRequest} SimulationRequest
 */
/**
 * @typedef {import("../types/simulationResponse/SolutionPayload").SolutionPayload} SolutionPayload
 */
const API_SIM_INIT = "/api/simulation-init";
const API_SIM_STOP = "/api/simulation-stop";
const SOCKET_URL =
  (window.location.protocol === "https:" ? "wss://" : "ws://") +
  window.location.host +
  "/ws";

let client = null;

/**
 * @param {SolutionPayload} onSolution         // SolutionResponse de /topic/simulator
 * @param {(status: any) => void} onStatus             // ProcessStatusResponse de /topic/simulator-status
 */
export function connectSimulatorWS(onSolution, onStatus) {
  if (client && client.active) return;

  client = new Client({
    brokerURL: SOCKET_URL,
    reconnectDelay: 5000,
    debug: () => {},
    onConnect: () => {
      console.log("STOMP conectado a", SOCKET_URL);

      client.subscribe("/topic/simulator", (message) => {
        const payload = JSON.parse(message.body);
        onSolution(payload);
      });

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
 * Iniciar simulación por HTTP
 * @param {SimulationRequest} request
 */
export async function sendSimulationRequest(request) {
  try {
    const { data } = await axios.post(API_SIM_INIT, request);
    return data; // GenericResponse
  } catch (error) {
    if (error.response && error.response.data) {
      throw new Error(error.response.data.message || "Error al iniciar simulación");
    }
    throw new Error("No se pudo conectar con el servidor de simulación");
  }
}

/** Pedir al back que detenga la simulación por HTTP */
export async function sendStopSimulation() {
  try {
    const { data } = await axios.post(API_SIM_STOP);
    return data; // GenericResponse
  } catch (error) {
    if (error.response && error.response.data) {
      throw new Error(error.response.data.message || "Error al detener simulación");
    }
    throw new Error("No se pudo conectar con el servidor de simulación");
  }
}

export function disconnectWS() {
  if (client) {
    client.deactivate();
    client = null;
  }
}