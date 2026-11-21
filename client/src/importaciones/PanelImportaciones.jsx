import React, { useEffect, useRef, useState } from "react";
import "./importaciones.scss";

const ETIQUETA = { PEDIDOS: "Pedidos", PLANES: "Planes de vuelo", EVENTOS: "Eventos", AEROPUERTOS: "Aeropuertos" };
const fmt = (n) => Number(n || 0).toLocaleString("es-PE");

function textoTarea(t) {
  if (t.estado === "ok") return "Importación terminada";
  if (t.estado === "error") return t.mensaje || "La importación falló";
  if (t.estado === "desconocido") return "Sin confirmación del server";
  if (/^Leyendo/i.test(t.proceso)) return `Leyendo el archivo · ${fmt(t.completado)} de ${fmt(t.total)} líneas`;
  if (/^Guardando/i.test(t.proceso)) return `${t.proceso}${t.lote > 1 || !["AEROPUERTOS", "EVENTOS"].includes(t.tipo) ? ` · lote ${Math.max(1, t.lote)}` : ""} · ${fmt(t.completado)} de ${fmt(t.total)}`;
  if (/^Cargando recursos/i.test(t.proceso)) return "Preparando la importación";
  return t.proceso || "En cola";
}

function Icono({ estado }) {
  if (estado === "cargando") return <span className="imp-icono imp-icono--girando" aria-hidden="true" />;
  if (estado === "ok") return <span className="imp-icono imp-icono--ok" aria-hidden="true">✓</span>;
  return <span className="imp-icono imp-icono--error" aria-hidden="true">!</span>;
}

function Item({ t }) {
  const [detalle, setDetalle] = useState(false);
  const pct = Math.round(t.general ?? 0);
  const pctTarea = t.total > 0 ? Math.min(100, Math.round((t.completado / t.total) * 100)) : 0;
  return (
    <li className={`imp-item imp-item--${t.estado}`}>
      <div className="imp-item__fila">
        <Icono estado={t.estado} />
        <div className="imp-item__textos">
          <span className="imp-item__nombre" title={t.nombre}>{t.nombre}</span>
          <span className="imp-item__tipo">{ETIQUETA[t.tipo] ?? t.tipo} · {t.estado === "error" ? "con error" : `${pct}%`}</span>
        </div>
      </div>
      <div className="imp-barra" role="progressbar" aria-valuemin={0} aria-valuemax={100} aria-valuenow={pct} aria-label={`Avance de ${t.nombre}`}>
        <span style={{ transform: `scaleX(${pct / 100})` }} />
      </div>
      <button type="button" className="imp-item__ver" aria-expanded={detalle} onClick={() => setDetalle((v) => !v)}>
        {detalle ? "Ocultar detalle" : "Ver detalle"}
      </button>
      {detalle && (
        <div className="imp-item__detalle">
          <span>{textoTarea(t)}</span>
          {t.estado === "cargando" && t.total > 0 && (
            <div className="imp-barra imp-barra--tarea"><span style={{ transform: `scaleX(${pctTarea / 100})` }} /></div>
          )}
        </div>
      )}
    </li>
  );
}

// Panel flotante abajo a la derecha. Se minimiza solo y desaparece cuando ya no hay nada importándose.
export default function PanelImportaciones({ tareas, onVaciar }) {
  const [minimizado, setMinimizado] = useState(false);
  const [saliendo, setSaliendo] = useState(false);
  const enCurso = tareas.filter((t) => t.estado === "cargando").length;
  const cantidad = useRef(tareas.length);

  // Una importación nueva vuelve a mostrar el panel abierto
  useEffect(() => {
    if (tareas.length > cantidad.current) { setMinimizado(false); setSaliendo(false); }
    cantidad.current = tareas.length;
  }, [tareas.length]);

  // Todo terminó: espera, se minimiza solo y luego sale
  useEffect(() => {
    if (tareas.length === 0 || enCurso > 0) return undefined;
    const t1 = setTimeout(() => setMinimizado(true), 1800);
    const t2 = setTimeout(() => setSaliendo(true), 2900);
    const t3 = setTimeout(() => { onVaciar(); setSaliendo(false); setMinimizado(false); }, 3300);
    return () => { clearTimeout(t1); clearTimeout(t2); clearTimeout(t3); };
  }, [tareas.length, enCurso, onVaciar]);

  if (tareas.length === 0) return null;
  const promedio = Math.round(tareas.reduce((s, t) => s + (t.estado === "cargando" ? t.general : 100), 0) / tareas.length);
  const titulo = enCurso > 0
    ? `Importando ${enCurso} ${enCurso === 1 ? "archivo" : "archivos"}`
    : tareas.some((t) => t.estado === "error") ? "Importación con errores" : "Importaciones terminadas";

  return (
    <section className={`imp-panel${minimizado ? " is-minimizado" : ""}${saliendo ? " is-saliendo" : ""}`} aria-live="polite" aria-label="Importaciones">
      <header className="imp-panel__cabecera">
        <span className="imp-panel__titulo">{titulo}</span>
        {enCurso > 0 && <span className="imp-panel__pct">{promedio}%</span>}
        <button type="button" className="imp-panel__min" onClick={() => setMinimizado((v) => !v)} aria-label={minimizado ? "Mostrar importaciones" : "Minimizar importaciones"}>
          <svg viewBox="0 0 16 16" width="14" height="14" aria-hidden="true"><path d="M4 6l4 4 4-4" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" /></svg>
        </button>
      </header>
      {minimizado && enCurso > 0 && <div className="imp-barra imp-barra--mini"><span style={{ transform: `scaleX(${promedio / 100})` }} /></div>}
      <div className="imp-panel__cuerpo">
        <ul className="imp-lista">{tareas.map((t) => <Item key={t.id} t={t} />)}</ul>
      </div>
    </section>
  );
}
