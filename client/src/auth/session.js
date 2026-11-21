import { SESION_STORAGE_KEY } from "../config";

// La sesión vive en localStorage: es compartida por TODAS las pestañas del navegador
// (una sola cuenta por navegador). Los ids de transacción (simulaciones) van por URL/pestaña.

export const ROLES = { CLIENTE: "CLIENTE", TRABAJADOR: "TRABAJADOR", ADMINISTRADOR: "ADMINISTRADOR" };

export const RUTA_INICIO_POR_ROL = {
  [ROLES.CLIENTE]: "/cliente",
  [ROLES.TRABAJADOR]: "/trabajador",
  [ROLES.ADMINISTRADOR]: "/admin",
};

export function leerSesion() {
  try {
    const raw = localStorage.getItem(SESION_STORAGE_KEY);
    const sesion = raw ? JSON.parse(raw) : null;
    return sesion?.usuario?.correo ? sesion : null;
  } catch {
    return null;
  }
}

export function guardarSesion(usuario) {
  const sesion = { usuario, iniciadaEn: Date.now() };
  localStorage.setItem(SESION_STORAGE_KEY, JSON.stringify(sesion));
  return sesion;
}

export function borrarSesion() {
  localStorage.removeItem(SESION_STORAGE_KEY);
}
