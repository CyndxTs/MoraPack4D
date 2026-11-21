import { listar, filtrar, get, post } from "./http";
import { TAMANIO_CATALOGO } from "../config";

// ---------- Aeropuertos ----------
export const listarAeropuertos = (pagina, tamanio) => listar("aeropuertos", pagina, tamanio);
export const listarTodosLosAeropuertos = () => listar("aeropuertos", 0, TAMANIO_CATALOGO);
export const filtrarAeropuertos = (modelo, pagina, tamanio) => filtrar("aeropuertos", modelo, pagina, tamanio);

// ---------- Planes de vuelo ----------
export const listarPlanes = (pagina, tamanio) => listar("planes", pagina, tamanio);
export const listarTodosLosPlanes = () => listar("planes", 0, TAMANIO_CATALOGO);

// ---------- Eventos ----------
export const listarEventos = (pagina, tamanio) => listar("eventos", pagina, tamanio);

// ---------- Países ----------
export const listarPaises = () => listar("paises", 0, TAMANIO_CATALOGO);
export const filtrarPaises = (modelo, pagina, tamanio) => filtrar("paises", modelo, pagina, tamanio);

// ---------- Usuarios ----------
export const listarClientes = (pagina, tamanio) => listar("clientes", pagina, tamanio);
export const filtrarClientes = (modelo, pagina, tamanio) => filtrar("clientes", modelo, pagina, tamanio);
export const listarTrabajadores = (pagina, tamanio) => listar("trabajadores", pagina, tamanio);
export const filtrarTrabajadores = (modelo, pagina, tamanio) => filtrar("trabajadores", modelo, pagina, tamanio);
export const listarAdministradores = (pagina, tamanio) => listar("administradores", pagina, tamanio);
export const filtrarAdministradores = (modelo, pagina, tamanio) => filtrar("administradores", modelo, pagina, tamanio);

// ---------- Datos de la operación guardados en BD ----------
export const listarRutas = (pagina, tamanio) => listar("rutas", pagina, tamanio);
export const listarVuelos = (pagina, tamanio) => listar("vuelos", pagina, tamanio);
export const listarRegistros = (pagina, tamanio) => listar("registros", pagina, tamanio);
export const listarLotes = (pagina, tamanio) => listar("lotes", pagina, tamanio);
export const listarSegmentaciones = (pagina, tamanio) => listar("segmentaciones", pagina, tamanio);

// ---------- Parámetros (fila única) ----------
// Jackson puede serializar dMin/iMax/kMin/kMax/nMax/tMax también como dmin/imax/... -> se normaliza aquí
export async function obtenerParametros() {
  const { dtos } = await get("/parametros");
  const p = dtos?.[0];
  if (!p) return null;
  return {
    ...p,
    dMin: p.dMin ?? p.dmin,
    iMax: p.iMax ?? p.imax,
    kMin: p.kMin ?? p.kmin,
    kMax: p.kMax ?? p.kmax,
    nMax: p.nMax ?? p.nmax,
    tMax: p.tMax ?? p.tmax,
    codOrigenes: p.codOrigenes ?? [],
  };
}

// ---------- Pedidos ----------
export const listarPedidos = (pagina, tamanio) => listar("pedidos", pagina, tamanio);
export const filtrarPedidos = (modelo, pagina, tamanio) => filtrar("pedidos", modelo, pagina, tamanio);
// Solo OPERACION, ACTIVO y sin vuelos ya despegados
export const cancelarPedido = (codigo) => post("/pedidos/cancelar", { codigo });
