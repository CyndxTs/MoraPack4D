import React, { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from "react";
import { lanzarImportacionDeArchivo, seguirImportacion } from "../api/importacion";
import { invalidarPaginado } from "../paginacion/usePaginado";
import { escuchar } from "../auth/eventos";
import { useNotify } from "../notifications/NotificationContext";
import PanelImportaciones from "./PanelImportaciones";

/**
 * Importaciones de archivos en segundo plano, al estilo de las subidas de Google Drive: se lanzan, el modal se
 * cierra y el avance se ve en un panel abajo a la derecha que sigue visible al cambiar de pantalla (y tras un F5,
 * mientras el server siga publicando). Al terminar se refresca la tabla del recurso importado.
 */
const ImportacionesContext = createContext({ iniciar: async () => {}, alTerminar: () => () => {} });

const STORAGE = "morapack.importaciones";
export const ETIQUETA_TIPO = { PEDIDOS: "Pedidos", PLANES: "Planes de vuelo", EVENTOS: "Eventos", AEROPUERTOS: "Aeropuertos" };
const RECURSO_TIPO = { PEDIDOS: "pedidos", PLANES: "planes", EVENTOS: "eventos", AEROPUERTOS: "aeropuertos" };
// Tipos que guardan todo al final (leer y guardar son etapas separadas)
const GUARDA_AL_FINAL = new Set(["AEROPUERTOS", "EVENTOS"]);

const leerGuardadas = () => {
  try { return JSON.parse(sessionStorage.getItem(STORAGE) || "[]"); } catch { return []; }
};
const guardar = (lista) => {
  try {
    const vivas = lista.filter((t) => t.estado === "cargando").map(({ id, nombre, tipo, inicio, general }) => ({ id, nombre, tipo, inicio, general }));
    sessionStorage.setItem(STORAGE, JSON.stringify(vivas));
  } catch { /* sin almacenamiento: solo se pierde el seguimiento tras un F5 */ }
};

// Avance general siempre creciente, aunque el server reinicie el contador en cada lote de 500
function avanzar(tarea, p) {
  const proceso = p?.proceso ?? "";
  const c = Number(p?.completado) || 0;
  const t = Math.max(1, Number(p?.total) || 1);
  const f = Math.min(1, c / t);
  const alFinal = GUARDA_AL_FINAL.has(tarea.tipo);
  let general = tarea.general;
  let leido = tarea.leido;
  let lote = tarea.lote;
  if (/^Cargando recursos/i.test(proceso)) general = Math.max(general, 1 + 3 * f);
  else if (/^Leyendo/i.test(proceso)) {
    leido = Math.max(leido, f);
    general = Math.max(general, 4 + (alFinal ? 61 : 91) * leido);
  } else if (/^Guardando/i.test(proceso)) {
    const nuevoLote = tarea.proceso !== proceso || c < (tarea.completado ?? 0);
    if (nuevoLote) lote += 1;
    if (alFinal) general = Math.max(general, 65 + 34 * f);
    else if (leido >= 1) general = Math.max(general, 95 + 4 * f);
  }
  return { ...tarea, general: Math.min(99, general), leido, lote, proceso, completado: c, total: Number(p?.total) || 0 };
}

export function ImportacionesProvider({ children }) {
  const notify = useNotify();
  const [tareas, setTareas] = useState([]);
  const oyentes = useRef(new Set());
  const seguidas = useRef(new Set());

  const actualizar = useCallback((id, fn) => {
    setTareas((prev) => {
      const sig = prev.map((t) => (t.id === id ? fn(t) : t));
      guardar(sig);
      return sig;
    });
  }, []);

  const seguir = useCallback((tarea) => {
    if (seguidas.current.has(tarea.id)) return;
    seguidas.current.add(tarea.id);
    seguirImportacion(tarea.id, (p) => actualizar(tarea.id, (t) => avanzar(t, p)))
      .then((fin) => {
        const error = fin.estadoFinalizacion === "ERRONEO";
        const desconocido = fin.estadoFinalizacion === "DESCONOCIDO";
        actualizar(tarea.id, (t) => ({
          ...t,
          estado: error ? "error" : desconocido ? "desconocido" : "ok",
          general: error ? t.general : 100,
          mensaje: fin.mensaje ?? null,
          finEn: Date.now(),
        }));
        if (error) notify("danger", fin.mensaje || `No se pudo importar ${tarea.nombre}.`);
        if (desconocido) notify("warning", `No se pudo confirmar el final de ${tarea.nombre}; revisa la tabla.`);
        invalidarPaginado(RECURSO_TIPO[tarea.tipo]);
        oyentes.current.forEach((fn) => fn(tarea.tipo, fin));
      })
      .finally(() => seguidas.current.delete(tarea.id));
  }, [actualizar, notify]);

  // Retomar las que seguían en curso antes de un F5
  useEffect(() => {
    const previas = leerGuardadas().map((t) => ({ ...t, estado: "cargando", leido: 0, lote: 0, proceso: "Reconectando...", completado: 0, total: 0, general: t.general ?? 0 }));
    if (previas.length) { setTareas(previas); previas.forEach(seguir); }
    return escuchar("logout", () => { setTareas([]); guardar([]); });
  }, [seguir]);

  const iniciar = useCallback(async ({ archivo, request, tipo }) => {
    const id = await lanzarImportacionDeArchivo(archivo, request);
    const tarea = { id, nombre: archivo.name, tipo, inicio: Date.now(), estado: "cargando", general: 0, leido: 0, lote: 0, proceso: "En cola", completado: 0, total: 0 };
    setTareas((prev) => { const sig = [...prev.filter((t) => t.estado === "cargando" || Date.now() - (t.finEn ?? 0) < 4000), tarea]; guardar(sig); return sig; });
    seguir(tarea);
    return id;
  }, [seguir]);

  const alTerminar = useCallback((fn) => {
    oyentes.current.add(fn);
    return () => oyentes.current.delete(fn);
  }, []);

  const limpiar = useCallback(() => setTareas((prev) => { const sig = prev.filter((t) => t.estado === "cargando"); guardar(sig); return sig; }), []);

  const value = useMemo(() => ({ iniciar, alTerminar }), [iniciar, alTerminar]);

  return (
    <ImportacionesContext.Provider value={value}>
      {children}
      <PanelImportaciones tareas={tareas} onVaciar={limpiar} />
    </ImportacionesContext.Provider>
  );
}

export const useImportaciones = () => useContext(ImportacionesContext);

// Ejecuta fn(tipo, estadoFinal) cuando termina una importación de alguno de los tipos dados
export function useAlTerminarImportacion(tipos, fn) {
  const { alTerminar } = useImportaciones();
  const ref = useRef(fn);
  ref.current = fn;
  const clave = [].concat(tipos).join(",");
  useEffect(() => alTerminar((tipo, fin) => { if (clave.split(",").includes(tipo)) ref.current(tipo, fin); }), [alTerminar, clave]);
}
