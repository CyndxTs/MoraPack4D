// src/services/loaderService.js
import { Client } from "@stomp/stompjs";

const SOCKET_URL =
  (window.location.protocol === "https:" ? "wss://" : "ws://") +
  window.location.host +
  "/ws";

let client = null;

function ensureClient() {
  return new Promise((resolve, reject) => {
    if (client && client.active) return resolve();

    client = new Client({
      brokerURL: SOCKET_URL,
      reconnectDelay: 1000,
      debug: () => {},
      onConnect: () => resolve(),
      onStompError: (frame) => {
        reject(new Error(frame.headers["message"] || "STOMP error"));
      },
    });

    client.activate();
  });
}

export async function subscribeLoader(callback) {
  await ensureClient();

  const subscription = client.subscribe("/topic/loader", (msg) => {
    callback(JSON.parse(msg.body));
  });

  return () => subscription.unsubscribe();
}

export function disconnectLoaderWS() {
  if (client) client.deactivate();
}
