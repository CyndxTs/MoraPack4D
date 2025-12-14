// src/services/loaderService.js
import { Client } from "@stomp/stompjs";

const SOCKET_URL =
  (window.location.protocol === "https:" ? "wss://" : "ws://") +
  window.location.host +
  "/ws";

let client = null;
let connectPromise = null;

function ensureClient() {
  if (client && client.connected) {
    return Promise.resolve();
  }

  if (connectPromise) {
    return connectPromise;
  }

  connectPromise = new Promise((resolve, reject) => {
    client = new Client({
      brokerURL: SOCKET_URL,
      reconnectDelay: 1000,
      debug: (msg) => console.log("[STOMP]", msg),

      onConnect: () => {
        resolve();
      },

      onStompError: (frame) => {
        reject(new Error(frame.headers["message"] || "STOMP error"));
      },

      onWebSocketClose: () => {
        connectPromise = null;
      },
    });

    client.activate();
  });

  return connectPromise;
}

/**
 * Suscripción a progreso de una importación específica
 */
export async function subscribeImportation(importId, onProgress, onStatus) {
  await ensureClient();

  const progressTopic = `/topic/importation-${importId}`;
  const statusTopic = `/topic/importation-status-${importId}`;

  const progressSub = client.subscribe(progressTopic, (msg) => {
    console.log("RAW PROGRESS WS:", msg.body);
    onProgress(JSON.parse(msg.body));
  });

  const statusSub = client.subscribe(statusTopic, (msg) => {
    console.log("RAW STATUS WS:", msg.body);
    onStatus(JSON.parse(msg.body));
  });

  return () => {
    progressSub.unsubscribe();
    statusSub.unsubscribe();
  };
}

export function disconnectLoaderWS() {
  if (client) client.deactivate();
}
