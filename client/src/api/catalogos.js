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
// Usuarios (solo administrador): modelo.tipoUsuario filtra por rol
export const listarUsuarios = (pagina, tamanio) => listar("usuarios", pagina, tamanio);
export const filtrarUsuarios = (modelo, pagina, tamanio) => filtrar("usuarios", modelo, pagina, tamanio);
export const crearUsuario = (datos) => post("/usuarios/crear", datos);
export const cambiarRolUsuario = (codigo, rol) => post("/usuarios/cambiar-rol", { codigo, rol });
export const cambiarEstadoUsuario = (codigo, estado) => post("/usuarios/cambiar-estado", { codigo, estado });
export const eliminarUsuario = (codigo) => post("/usuarios/eliminar", { codigo });

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
