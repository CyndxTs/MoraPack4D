import { post, http, idDeToken, ApiError } from "./http";
import { toServerDateTime } from "../utils/fechas";

// ---------- Simulación ----------
// request: { fechaHoraInicio, fechaHoraFin, parametros, multiplicadorTemporal, saltoDeAlgoritmo }
export async function iniciarSimulacion(request) {
  const r = await post("/simulation-init", request);
  return idDeToken(r.token);
}
export const detenerSimulacion = (idTransaccion) => post("/simulation-stop", { idTransaccion });

// ---------- Operación ----------
// Sin 'parametros' el server usa los guardados por el administrador
export const replanificarOperacion = (parametros = null) =>
  post("/operation-replanificate", { fechaHoraActual: toServerDateTime(Date.now()), almacenarParametrizacion: false, parametros });

// Última solución de la operación en el server (null si aún no hay ninguna)
export async function obtenerOperacionActual() {
  const response = await http.post("/operation-current");
  if (response.status === 204 || !response.data) return null;
  return response.data.solucion ?? null;
}

export const purgarOperacion = () => post("/operation-purge");

// ---------- Exportación ----------
export const CARPETA_EXPORTACION = "exports";
// El server nombra el archivo "<prefijo>__<idTransaccion>.txt"
export const nombreDeReporte = (prefijo, idTransaccion) => `${prefijo}__${idTransaccion}.txt`;

export async function iniciarExportacion(idTransaccion, prefijo) {
  const r = await post("/exportation-init", { idTransaccion, prefijo });
  return idDeToken(r.token);
}

export async function previsualizarReporte(archivo) {
  const { data } = await http.post("/exportation-preview", archivo, { responseType: "text" });
  return data;
}

export async function descargarReporte(archivo) {
  const { data } = await http.post("/exportation-download", archivo, { responseType: "blob" });
  const url = window.URL.createObjectURL(data);
  const a = document.createElement("a");
  a.href = url;
  a.download = archivo.nombre;
  document.body.appendChild(a);
  a.click();
  a.remove();
  window.URL.revokeObjectURL(url);
}

export async function eliminarReporte(archivo) {
  try {
    await post("/exportation-delete", archivo);
  } catch (e) {
    if (!(e instanceof ApiError)) throw e; // si ya no existe, no importa
  }
}
