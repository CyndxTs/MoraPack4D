import axios from "axios";
import { API_BASE } from "../config";
import { borrarSesion, guardarSesion, leerSesion } from "./session";
import { emitir } from "./eventos";

// Renovación del token de acceso (10 min) con el token de refresco. Una sola renovación a la vez por pestaña.
// Usa axios "puro" (sin los interceptores de la app) para no entrar en bucle.

const MARGEN_MS = 30000;
let enCurso = null;

export function refrescar() {
  if (enCurso) return enCurso;
  const s = leerSesion();
  if (!s) return Promise.resolve(null);
  enCurso = (async () => {
    try {
      const { data } = await axios.post(`${API_BASE}/auth/refrescar`, { tokenRefresco: s.tokenRefresco });
      if (!data?.tokenAcceso) throw new Error("Respuesta de renovación inválida");
      const actual = leerSesion();
      // Mientras tanto otra pestaña pudo cerrar sesión o entrar con otra cuenta
      if (!actual || actual.tokenRefresco !== s.tokenRefresco) return actual?.tokenAcceso ?? null;
      return guardarSesion(data, actual).tokenAcceso;
    } catch (e) {
      if (e.response?.status === 401) {
        const actual = leerSesion();
        if (actual && actual.tokenRefresco === s.tokenRefresco) {
          borrarSesion();
          emitir("sesion-expirada", e.response?.data?.mensaje);
        }
        return null;
      }
      throw e; // sin red: no se cierra la sesión
    } finally {
      enCurso = null;
    }
  })();
  return enCurso;
}

// Token vigente (lo renueva si le quedan menos de 30 s). null si no hay sesión.
export async function obtenerTokenAcceso() {
  const s = leerSesion();
  if (!s) return null;
  if (s.tokenAcceso && s.expiraAcceso && s.expiraAcceso - MARGEN_MS > Date.now()) return s.tokenAcceso;
  try {
    return await refrescar();
  } catch {
    return s.tokenAcceso ?? null;
  }
}
