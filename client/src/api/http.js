import axios from "axios";
import { API_BASE } from "../config";
import { leerSesion } from "../auth/session";
import { obtenerTokenAcceso, refrescar } from "../auth/tokens";
import { emitir, escuchar } from "../auth/eventos";

// Error homogéneo para toda la app: siempre trae un mensaje legible
export class ApiError extends Error {
  constructor(mensaje, status = null, codigo = null) {
    super(mensaje);
    this.name = "ApiError";
    this.status = status;
    this.codigo = codigo;
  }
}

export const http = axios.create({ baseURL: API_BASE });

// Las rutas de /auth no llevan token (salvo mi-cuenta)
const esAuthPublica = (url = "") => /^\/?auth\//.test(url) && !/^\/?auth\/mi-cuenta/.test(url);

http.interceptors.request.use(async (config) => {
  if (!esAuthPublica(config.url)) {
    const token = await obtenerTokenAcceso();
    if (token) config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

async function leerCuerpo(data) {
  // Si la petición pedía un blob/texto, el GenericResponse de error llega serializado
  if (data instanceof Blob) {
    try { return JSON.parse(await data.text()); } catch { return null; }
  }
  if (typeof data === "string") {
    try { return JSON.parse(data); } catch { return data; }
  }
  return data;
}

http.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (!error.response) {
      return Promise.reject(new ApiError("No se pudo conectar con el servidor."));
    }
    const { status, config } = error.response;
    const data = await leerCuerpo(error.response.data);
    const codigo = data && typeof data === "object" ? data.codigo : null;

    // Token de acceso vencido o invalidado (p. ej. cambio de rol): se renueva y se reintenta una vez
    if (status === 401 && (codigo === "TOKEN_EXPIRADO" || codigo === "TOKEN_INVALIDO") && !config._reintento) {
      try {
        const token = await refrescar();
        if (token) {
          config._reintento = true;
          config.headers.Authorization = `Bearer ${token}`;
          return http(config);
        }
      } catch { /* sin red */ }
    }
    // Sin sesión (expiró o nunca hubo): la petición queda en pausa y se reintenta al volver a iniciar sesión
    if (status === 401 && !leerSesion() && !esAuthPublica(config.url) && !config._esperoLogin) {
      emitir("requiere-login");
      return new Promise((resolve, reject) => {
        const quitar = escuchar("sesion", (sesion) => {
          if (!sesion) return;
          quitar();
          config._esperoLogin = true;
          delete config.headers.Authorization;
          http(config).then(resolve, reject);
        });
      });
    }
    const mensaje = (data && typeof data === "object" && (data.mensaje || data.message))
      || (status === 403 ? "No tienes permisos para esta acción." : `Error ${status} en el servidor.`);
    return Promise.reject(new ApiError(mensaje, status, codigo));
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
