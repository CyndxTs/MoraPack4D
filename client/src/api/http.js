import axios from "axios";
import { API_BASE } from "../config";

// Error homogéneo para toda la app: siempre trae un mensaje legible
export class ApiError extends Error {
  constructor(mensaje, status = null) {
    super(mensaje);
    this.name = "ApiError";
    this.status = status;
  }
}

export const http = axios.create({ baseURL: API_BASE });

http.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (!error.response) {
      return Promise.reject(new ApiError("No se pudo conectar con el servidor."));
    }
    let data = error.response.data;
    // Si la petición pedía un blob/texto, el GenericResponse de error llega serializado
    if (data instanceof Blob) {
      try { data = JSON.parse(await data.text()); } catch { data = null; }
    } else if (typeof data === "string") {
      try { data = JSON.parse(data); } catch { /* texto plano */ }
    }
    const mensaje = (data && (data.mensaje || data.message)) || `Error ${error.response.status} en el servidor.`;
    return Promise.reject(new ApiError(mensaje, error.response.status));
  }
);

// Los endpoints devuelven GenericResponse { token, exito, mensaje } (o subclases). exito=false también es error.
function verificar(data) {
  if (data && typeof data === "object" && data.exito === false) {
    throw new ApiError(data.mensaje || "La operación no tuvo éxito.");
  }
  return data;
}

export async function post(url, body = {}, config) {
  const { data } = await http.post(url, body, config);
  return verificar(data);
}

export async function get(url, config) {
  const { data } = await http.get(url, config);
  return verificar(data);
}

// "TOK-abc" -> "abc" (id de transacción que usan los tópicos del WebSocket)
export const idDeToken = (token) => (token && token.startsWith("TOK-") ? token.substring(4) : token);

// Recursos con /listar y /filtrar paginados
export const listar = async (recurso, pagina = 0, tamanio = 30) =>
  (await post(`/${recurso}/listar`, { pagina, tamanio })).dtos ?? [];

export const filtrar = async (recurso, modelo, pagina = 0, tamanio = 30) =>
  (await post(`/${recurso}/filtrar`, { pagina, tamanio, modelo })).dtos ?? [];
