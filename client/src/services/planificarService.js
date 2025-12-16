import { Client } from "@stomp/stompjs";
import axios from "axios";

const API_SIM_INIT = "/api/simulation-init";
const API_SIM_STOP = "/api/simulation-stop";
const API_EXPORT_DOWNLOAD = "/api/exportation-download";
const API_EXPORT_PREVIEW = "/api/exportation-preview"; // <--- NUEVO
const API_EXPORT_DELETE = "/api/exportation-delete";   // <--- NUEVO

const SOCKET_URL =
  (window.location.protocol === "https:" ? "wss://" : "ws://") +
  window.location.host +
  "/ws";

let client = null;

export function connectSimulatorWS(onConnected) {
  if (client && client.active) {
    if (onConnected) onConnected();
    return;
  }

  client = new Client({
    brokerURL: SOCKET_URL,
    reconnectDelay: 5000,
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

export function subscribeToSimulation(idTransaccion, onSolution, onStatus, onExport) {
  if (!client || !client.connected) {
    console.warn("⚠️ No hay conexión STOMP activa para suscribirse.");
    return null;
  }

  const solSub = client.subscribe(`/topic/simulation-${idTransaccion}`, (m) => onSolution(JSON.parse(m.body)));
  const statSub = client.subscribe(`/topic/simulation-status-${idTransaccion}`, (m) => onStatus(JSON.parse(m.body)));
  
  // Canal de Exportación
  const exportSub = client.subscribe(`/topic/exportation-${idTransaccion}`, (m) => {
      const fileData = JSON.parse(m.body);
      if (onExport) onExport(fileData);
  });

  return {
    unsubscribe: () => {
      console.log(`🔌 Desuscribiendo de ${idTransaccion}`);
      solSub.unsubscribe();
      statSub.unsubscribe();
      exportSub.unsubscribe();
    },
  };
}

export async function sendSimulationRequest(request) {
  try {
    const { data } = await axios.post(API_SIM_INIT, request);
    return data;
  } catch (error) {
    throw new Error(error.response?.data?.message || "Error al iniciar simulación");
  }
}

export async function sendStopSimulation(idTransaccion) {
  if (!idTransaccion) return;
  try {
    const { data } = await axios.post(API_SIM_STOP, { idTransaccion });
    return data;
  } catch (error) {
    console.warn("Error al detener:", error);
    throw error; 
  }
}

// --- FUNCIONES DE REPORTE ---

export async function downloadExportationFile(fileRequest) {
  try {
    const response = await axios.post(API_EXPORT_DOWNLOAD, fileRequest, {
      responseType: 'blob',
    });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', fileRequest.nombre);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  } catch (error) {
    console.error("Error descargando reporte:", error);
    throw new Error("No se pudo descargar el archivo.");
  }
}

// Obtener el texto del archivo para previsualizarlo
export async function getExportationPreview(fileRequest) {
  try {
    const response = await axios.post(API_EXPORT_PREVIEW, fileRequest, {
      responseType: 'text', // Pedimos texto plano para mostrarlo en el modal
    });
    return response.data; // Retorna el contenido del archivo
  } catch (error) {
    console.error("Error obteniendo previsualización:", error);
    throw error;
  }
}

// Eliminar el archivo del servidor
export async function deleteExportationFile(fileRequest) {
  try {
    await axios.post(API_EXPORT_DELETE, fileRequest);
  } catch (error) {
    console.error("Error eliminando archivo:", error);
    // No lanzamos error crítico, solo log
  }
}

export function disconnectWS() {
  if (client) {
    client.deactivate();
    client = null;
  }
}