import { offsetHorasZona } from "../utils/zona";
import { post, http, idDeToken, ApiError } from "./http";
import { toServerDateTime } from "../utils/fechas";

// ---------- Simulación ----------
// request: { fechaHoraInicio, fechaHoraFin, parametros, multiplicadorTemporal, saltoDeAlgoritmo }
export async function iniciarSimulacion(request) {
  const r = await post("/simulation-init", { husoHorario: offsetHorasZona(), ...request });
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

// Replanificación automática (la programa el server según PARAMETROS): { programadaPara, enProceso }
export const obtenerProgramacion = () => post("/operation-schedule");
export const cancelarProgramacion = () => post("/operation-schedule-cancel");

export const esReplanificacionEnCurso = (error) => /ya hay una replanificaci/i.test(error?.message ?? "");

// ---------- Exportación ----------
export const CARPETA_EXPORTACION = "exports";
// El server nombra el archivo "<prefijo>__<idTransaccion>.txt"
export const nombreDeReporte = (prefijo, idTransaccion) => `${prefijo}__${idTransaccion}.txt`;

// El server genera "<prefijo>__<id>.txt" y ".pdf"; el PDF muestra las horas en el huso de la sesión
export const pdfDe = (nombreTxt) => nombreTxt?.replace(/\.txt$/, ".pdf");

export async function iniciarExportacion(idTransaccion, prefijo) {
  const r = await post("/exportation-init", { idTransaccion, prefijo, husoHorario: offsetHorasZona() });
  return idDeToken(r.token);
}

// PDF como blob (para la vista previa en un iframe)
export async function previsualizarPdf(archivo) {
  const { data } = await http.post("/exportation-preview", archivo, { responseType: "blob" });
  return data;
}

// Envío por correo en segundo plano: devuelve el id cuyo resultado llega por WebSocket
export async function enviarReportePorCorreo(nombre, correo) {
  const r = await post("/exportation-send", { nombre, correo });
  return idDeToken(r.token);
}

// Simulación en curso o terminada (para retomarla al volver a su pestaña)
export const obtenerEstadoSimulacion = (idTransaccion) => post("/simulation-state", { idTransaccion });

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
