import { replanificarOperacion } from "../api/escenarios";
import { REPLANIFICACION_DIFERIDA_MIN } from "../config";

/**
 * Replanificación automática de la operación.
 * Cuando se registran pedidos de OPERACION se programa una replanificación para dentro de
 * REPLANIFICACION_DIFERIDA_MIN minutos (si ya había una programada, se respeta la primera).
 * La programación vive en localStorage: todas las pestañas del navegador la ven y solo una la ejecuta.
 * Si otro navegador ya lanzó una, el server responde "Ya hay una replanificación en proceso" y se ignora.
 */

const KEY_PROGRAMADA = "morapack.replanificacion.programadaPara";
const KEY_LOCK = "morapack.replanificacion.lock";
const TAB_ID = `${Date.now()}-${Math.random().toString(36).slice(2)}`;
const oyentes = new Set();

const leer = () => {
  const v = Number(localStorage.getItem(KEY_PROGRAMADA));
  return Number.isFinite(v) && v > 0 ? v : null;
};
const avisar = () => oyentes.forEach((fn) => fn(leer()));

export const obtenerProgramada = () => leer();

export function suscribirProgramacion(fn) {
  oyentes.add(fn);
  return () => oyentes.delete(fn);
}

export function programarReplanificacion() {
  if (!leer()) {
    localStorage.setItem(KEY_PROGRAMADA, String(Date.now() + REPLANIFICACION_DIFERIDA_MIN * 60000));
    avisar();
  }
  return leer();
}

export function cancelarProgramacion() {
  localStorage.removeItem(KEY_PROGRAMADA);
  avisar();
}

export const esReplanificacionEnCurso = (error) => /ya hay una replanificaci/i.test(error?.message ?? "");

export async function replanificarAhora() {
  cancelarProgramacion();
  return replanificarOperacion();
}

// Lock corto entre pestañas: evita que dos pestañas del mismo navegador disparen a la vez
function tomarLock() {
  const ahora = Date.now();
  try {
    const actual = JSON.parse(localStorage.getItem(KEY_LOCK) || "null");
    if (actual && actual.hasta > ahora && actual.tab !== TAB_ID) return false;
    localStorage.setItem(KEY_LOCK, JSON.stringify({ tab: TAB_ID, hasta: ahora + 15000 }));
    return JSON.parse(localStorage.getItem(KEY_LOCK)).tab === TAB_ID;
  } catch {
    return true;
  }
}

/**
 * Arranca el vigilante (una vez por pestaña, mientras haya un trabajador con sesión).
 * onError(error) se llama si el server rechaza la replanificación por otro motivo.
 */
export function iniciarReplanificador({ onLanzada, onError } = {}) {
  const onStorage = (e) => { if (e.key === KEY_PROGRAMADA) avisar(); };
  window.addEventListener("storage", onStorage);
  const intervalo = setInterval(async () => {
    const t = leer();
    if (!t || Date.now() < t || !tomarLock()) return;
    cancelarProgramacion();
    try {
      await replanificarOperacion();
      onLanzada?.();
    } catch (e) {
      if (!esReplanificacionEnCurso(e)) onError?.(e);
    }
  }, 2000);
  return () => {
    clearInterval(intervalo);
    window.removeEventListener("storage", onStorage);
  };
}
