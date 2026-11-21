import { post, http, idDeToken, ApiError } from "./http";
import { esperarImportacion, prepararSeguimientoDeImportaciones } from "../realtime/importaciones";

// Tipos aceptados por /importation-init (un DTO) y /importation-init-file (un archivo)
// Los usuarios ya no se importan: se registran (cliente) o los crea el administrador uno a uno (/api/usuarios)
export const TIPO_DTO = {
  AEROPUERTO: "AEROPUERTO", PLAN: "PLAN", PEDIDO: "PEDIDO", PARAMETROS: "PARAMETROS", PAIS: "PAIS", EVENTO: "EVENTO",
};
export const TIPO_ARCHIVO = { AEROPUERTOS: "AEROPUERTOS", PLANES: "PLANES", PEDIDOS: "PEDIDOS", EVENTOS: "EVENTOS" };

// La importación corre en segundo plano en el server; el resultado llega por WebSocket.
// Devuelve { id, resultado } donde 'resultado' es una promesa con el StatusPayload final.
async function lanzar(enviar, onProgress) {
  await prepararSeguimientoDeImportaciones(); // nos suscribimos ANTES de pedir, para no perder mensajes
  const respuesta = await enviar();
  const id = idDeToken(respuesta.token);
  return { id, mensaje: respuesta.mensaje, resultado: esperarImportacion(id, onProgress) };
}

// Espera el final y lanza ApiError si el server reportó error
async function lanzarYEsperar(enviar, onProgress) {
  const { resultado } = await lanzar(enviar, onProgress);
  const estado = await resultado;
  if (estado.estadoFinalizacion === "ERRONEO") {
    throw new ApiError(estado.mensaje || "El servidor rechazó la importación. Revisa el formato o los datos.");
  }
  return estado;
}

export const importarDto = (tipoDto, dto, onProgress) =>
  lanzarYEsperar(() => post("/importation-init", { tipoDto, dto }), onProgress);

// request: { tipoArchivo, tipoEscenario?, fechaHoraInicio?, fechaHoraFin?, mes?, anio? }
export const importarArchivo = (archivo, request, onProgress) =>
  lanzarYEsperar(async () => {
    const form = new FormData();
    form.append("file", archivo);
    form.append("request", new Blob([JSON.stringify(request)], { type: "application/json" }));
    const { data } = await http.post("/importation-init-file", form);
    if (data?.exito === false) throw new ApiError(data.mensaje);
    return data;
  }, onProgress);

// Atajos
export const registrarPedido = (dto) => importarDto(TIPO_DTO.PEDIDO, dto);
export const guardarParametros = (dto) => importarDto(TIPO_DTO.PARAMETROS, dto);
export const registrarEvento = (dto) => importarDto(TIPO_DTO.EVENTO, dto);
export const registrarPais = (dto) => importarDto(TIPO_DTO.PAIS, dto);
export const registrarAeropuerto = (dto) => importarDto(TIPO_DTO.AEROPUERTO, dto);
export const registrarPlan = (dto) => importarDto(TIPO_DTO.PLAN, dto);
