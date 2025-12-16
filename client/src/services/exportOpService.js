import { Client } from "@stomp/stompjs";
import axios from "axios";

const SOCKET_URL =
  (window.location.protocol === "https:" ? "wss://" : "ws://") +
  window.location.host +
  "/ws";

const API_URL = "/api";
const API_EXPORTATION_INIT = "/api/exportation-init";

let client = null;

/* ===============================
   HELPERS
================================ */
function cleanToken(token) {
  return token?.startsWith("TOK-") ? token.substring(4) : token;
}

/* ===============================
   EXPORTACIÓN
================================ */
export async function iniciarExportacion(idTransaccion, prefijo = "OpDiaria") {
  try {
    const request = {
      idTransaccion: cleanToken(idTransaccion),
      prefijo,
    };

    const { data } = await axios.post(API_EXPORTATION_INIT, request);

    if (data.exito === false) {
      throw new Error(data.mensaje || "Error iniciando exportación");
    }

    return data;
  } catch (e) {
    throw new Error(
      e.response?.data?.mensaje || e.message || "Error exportando"
    );
  }
}

/* ===============================
   DESCARGA
================================ */
export async function descargarExportacion(fileRequest) {
  const response = await axios.post(
    `${API_URL}/exportation-download`,
    fileRequest,
    { responseType: "blob" }
  );

  return response.data;
}

/* ===============================
   WEBSOCKET EXPORTACIÓN
================================ */
export function connectOperatorExportWS(
  idTransaccion,
  onSolution,
  onStatus
) {
  const cleanId = cleanToken(idTransaccion);

  // Si ya hay cliente, lo desactivamos para conectar al nuevo canal
  if (client) {
    console.log("[EXPORT WS] Reiniciando conexión para nuevo ID:", cleanId);
    client.deactivate();
    client = null;
  }

  client = new Client({
    brokerURL: SOCKET_URL,
    reconnectDelay: 5000,
    debug: () => {},
    onConnect: () => {
      console.log("[EXPORT WS] Conectado");

      client.subscribe(
        `/topic/exportation-${cleanId}`,
        (message) => onSolution(JSON.parse(message.body))
      );

      client.subscribe(
        `/topic/exportation-status-${cleanId}`,
        (message) => onStatus(JSON.parse(message.body))
      );
    },
  });

  client.activate();
}

export function disconnectExportWS() {
  if (client) {
    client.deactivate();
    client = null;
  }
}
