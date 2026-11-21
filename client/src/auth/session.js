import { SESION_STORAGE_KEY } from "../config";
import { emitir } from "./eventos";

// La sesión vive en localStorage: la comparten TODAS las pestañas del navegador (una sesión por dispositivo).
// Forma: { usuario, tokenAcceso, tokenRefresco, expiraAcceso, expiraSesion }  (expira* en epoch ms)
// La sesión dura 12 h desde el inicio (no se extiende); el token de acceso dura 10 min y se renueva solo.

export const ROLES = { CLIENTE: "CLIENTE", TRABAJADOR: "TRABAJADOR", ADMINISTRADOR: "ADMINISTRADOR" };

export const RUTA_INICIO_POR_ROL = {
  [ROLES.CLIENTE]: "/cliente",
  [ROLES.TRABAJADOR]: "/trabajador",
  [ROLES.ADMINISTRADOR]: "/admin",
};

export const ETIQUETA_ROL = { CLIENTE: "Cliente", TRABAJADOR: "Trabajador", ADMINISTRADOR: "Administrador" };

// Cada rol entra solo a su área (el administrador es sysadmin: no usa las pantallas del trabajador)
export const puedeEntrar = (usuario, roles) => !!usuario && roles.includes(usuario.tipoUsuario);

export function leerSesion() {
  try {
    const raw = localStorage.getItem(SESION_STORAGE_KEY);
    const s = raw ? JSON.parse(raw) : null;
    if (!s?.usuario?.correo || !s.tokenRefresco || !s.expiraSesion) return null;
    if (s.expiraSesion <= Date.now()) return null;
    return s;
  } catch {
    return null;
  }
}

// 'respuesta' = AuthenticationResponse del server
export function guardarSesion(respuesta, base = null) {
  const sesion = {
    usuario: respuesta.usuario ?? base?.usuario,
    tokenAcceso: respuesta.tokenAcceso ?? base?.tokenAcceso,
    tokenRefresco: respuesta.tokenRefresco ?? base?.tokenRefresco,
    expiraAcceso: respuesta.expiraAcceso ?? base?.expiraAcceso,
    expiraSesion: respuesta.expiraSesion ?? base?.expiraSesion,
  };
  try { localStorage.setItem(SESION_STORAGE_KEY, JSON.stringify(sesion)); } catch { /* modo privado */ }
  emitir("sesion", sesion);
  return sesion;
}

export function actualizarUsuarioEnSesion(usuario) {
  const actual = leerSesion();
  if (actual) guardarSesion({ usuario }, actual);
}

export function borrarSesion() {
  try { localStorage.removeItem(SESION_STORAGE_KEY); } catch { /* nada */ }
  emitir("sesion", null);
}
