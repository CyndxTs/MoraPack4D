import React, { useEffect, useId, useRef } from "react";
import "./gestion.scss";

/**
 * Piezas comunes de las pantallas de gestión (tablas): cabecera, barra de búsqueda y filtros plegables,
 * selector segmentado, modal con pie de acciones, zona para soltar archivos y confirmación.
 */

export function PaginaGestion({ titulo, descripcion, acciones, pestanas, children }) {
  return (
    <div className="gestion-scroll"><div className="gestion">
      <header className="gestion__cabecera">
        <div className="gestion__titulos">
          {pestanas}
          <h1>{titulo}</h1>
          {descripcion && <p>{descripcion}</p>}
        </div>
        {acciones && <div className="gestion__acciones">{acciones}</div>}
      </header>
      {children}
    </div></div>
  );
}

const IconoBuscar = () => (
  <svg viewBox="0 0 20 20" width="16" height="16" aria-hidden="true"><circle cx="8.5" cy="8.5" r="5.5" fill="none" stroke="currentColor" strokeWidth="1.8" /><path d="M13 13l4 4" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" /></svg>
);
const IconoFiltro = () => (
  <svg viewBox="0 0 20 20" width="16" height="16" aria-hidden="true"><path d="M3 5h14M6 10h8M8.5 15h3" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" /></svg>
);
export const IconoMas = () => (
  <svg viewBox="0 0 20 20" width="16" height="16" aria-hidden="true"><path d="M10 4v12M4 10h12" stroke="currentColor" strokeWidth="2" strokeLinecap="round" /></svg>
);

/**
 * Barra de búsqueda + botón "Filtros" (panel plegado por defecto) + chips de filtros activos.
 * 'activos': [{ clave, texto }] ; onQuitar(clave)
 */
export function BarraFiltros({ busqueda, onBusqueda, placeholder = "Buscar...", abierto, onAlternar, activos = [], onQuitar, onLimpiar, onAplicar, children, resumen }) {
  return (
    <div className="filtros">
      <div className="filtros__fila">
        {onBusqueda && (
          <label className="filtros__busqueda">
            <IconoBuscar />
            <input
              type="search"
              value={busqueda}
              placeholder={placeholder}
              onChange={(e) => onBusqueda(e.target.value)}
              onKeyDown={(e) => { if (e.key === "Enter") onAplicar?.(); }}
            />
          </label>
        )}
        {children && (
          <button type="button" className={`filtros__boton${abierto ? " is-abierto" : ""}`} aria-expanded={abierto} onClick={onAlternar}>
            <IconoFiltro /> Filtros{activos.length > 0 && <span className="filtros__cuenta">{activos.length}</span>}
          </button>
        )}
        {activos.length > 0 && (
          <div className="filtros__chips">
            {activos.map((a) => (
              <span key={a.clave} className="chip">
                {a.texto}
                <button type="button" aria-label={`Quitar filtro ${a.texto}`} onClick={() => onQuitar(a.clave)}>×</button>
              </span>
            ))}
            <button type="button" className="filtros__limpiar" onClick={onLimpiar}>Limpiar</button>
          </div>
        )}
        {resumen && <span className="filtros__resumen">{resumen}</span>}
      </div>
      {children && abierto && (
        <div className="filtros__panel">
          <div className="filtros__campos">{children}</div>
          <div className="filtros__pie">
            <button type="button" className="boton boton--fantasma" onClick={onLimpiar}>Limpiar</button>
            <button type="button" className="boton boton--primario" onClick={onAplicar}>Aplicar filtros</button>
          </div>
        </div>
      )}
    </div>
  );
}

// req: obligatorio (asterisco rojo) · opc: opcional (etiqueta tenue)
export function Campo({ label, hint, children, ancho, req, opc }) {
  const id = useId();
  return (
    <div className={`campo${ancho ? ` campo--${ancho}` : ""}`}>
      <label htmlFor={id}>{label}{req && <span className="campo__req" aria-hidden="true"> *</span>}{opc && <span className="campo__opc"> opcional</span>}</label>
      {React.isValidElement(children) && !children.props.id ? React.cloneElement(children, { id }) : children}
      {hint && <small>{hint}</small>}
    </div>
  );
}

export function Segmentado({ opciones, value, onChange, nombre }) {
  return (
    <div className="segmentado" role="radiogroup" aria-label={nombre}>
      {opciones.map((o) => (
        <button key={o.value} type="button" role="radio" aria-checked={value === o.value} className={value === o.value ? "is-activo" : ""} onClick={() => onChange(o.value)}>
          {o.label}
        </button>
      ))}
    </div>
  );
}

export function Modal({ titulo, descripcion, onCerrar, children, pie, ancho = "normal", bloqueado = false }) {
  const panel = useRef(null);
  useEffect(() => {
    const onKey = (e) => { if (e.key === "Escape" && !bloqueado) onCerrar(); };
    document.addEventListener("keydown", onKey);
    panel.current?.querySelector("input, select, textarea, button:not(.modal2__cerrar)")?.focus();
    return () => document.removeEventListener("keydown", onKey);
  }, [onCerrar, bloqueado]);
  return (
    <div className="modal2" role="presentation" onMouseDown={(e) => { if (e.target === e.currentTarget && !bloqueado) onCerrar(); }}>
      <div className={`modal2__panel modal2__panel--${ancho}`} role="dialog" aria-modal="true" aria-label={titulo} ref={panel}>
        <header className="modal2__cabecera">
          <div>
            <h2>{titulo}</h2>
            {descripcion && <p>{descripcion}</p>}
          </div>
          <button type="button" className="modal2__cerrar" onClick={onCerrar} aria-label="Cerrar" disabled={bloqueado}>×</button>
        </header>
        <div className="modal2__cuerpo">{children}</div>
        {pie && <footer className="modal2__pie">{pie}</footer>}
      </div>
    </div>
  );
}

export function ZonaArchivo({ archivo, onArchivo, ayuda, accept = ".txt" }) {
  const id = useId();
  const soltar = (e) => {
    e.preventDefault();
    const f = e.dataTransfer.files?.[0];
    if (f) onArchivo(f);
  };
  return (
    <div className={`zona-archivo${archivo ? " tiene-archivo" : ""}`} onDragOver={(e) => e.preventDefault()} onDrop={soltar}>
      <input id={id} type="file" accept={accept} onChange={(e) => onArchivo(e.target.files[0] ?? null)} />
      {archivo ? (
        <div className="zona-archivo__elegido">
          <strong>{archivo.name}</strong>
          <span>{(archivo.size / 1024).toFixed(1)} KB</span>
          <button type="button" className="boton boton--fantasma" onClick={() => onArchivo(null)}>Quitar</button>
        </div>
      ) : (
        <label htmlFor={id} className="zona-archivo__vacia">
          <strong>Arrastra el archivo aquí o haz clic para elegirlo</strong>
          {ayuda && <span>{ayuda}</span>}
        </label>
      )}
    </div>
  );
}

export function Confirmacion({ titulo, mensaje, accion, peligro = false, enviando = false, onConfirmar, onCerrar }) {
  return (
    <Modal titulo={titulo} onCerrar={onCerrar} ancho="angosto" bloqueado={enviando}
      pie={<>
        <button type="button" className="boton boton--fantasma" onClick={onCerrar} disabled={enviando}>Volver</button>
        <button type="button" className={`boton ${peligro ? "boton--peligro" : "boton--primario"}`} onClick={onConfirmar} disabled={enviando}>{enviando ? "Procesando..." : accion}</button>
      </>}>
      <p className="modal2__texto">{mensaje}</p>
    </Modal>
  );
}

// Tabla de gestión: columnas [{ label, key, render?, alinear? }]
export function Tabla({ columnas, filas, cargando, vacio = "No hay registros.", clave = "codigo" }) {
  return (
    <div className="tabla">
      <table>
        <thead>
          <tr>{columnas.map((c) => <th key={c.key} className={c.alinear ? `a-${c.alinear}` : ""}>{c.label}</th>)}</tr>
        </thead>
        <tbody>
          {cargando ? (
            Array.from({ length: 6 }, (_, i) => (
              <tr key={i} className="tabla__esqueleto">{columnas.map((c) => <td key={c.key}><span /></td>)}</tr>
            ))
          ) : filas.length === 0 ? (
            <tr><td colSpan={columnas.length} className="tabla__vacia">{vacio}</td></tr>
          ) : (
            filas.map((f, i) => (
              <tr key={f[clave] ?? i}>
                {columnas.map((c) => <td key={c.key} className={c.alinear ? `a-${c.alinear}` : ""}>{c.render ? c.render(f) : (f[c.key] ?? "—")}</td>)}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}

export function Paginador({ pagina, hayMas, onCambiar, total }) {
  return (
    <div className="paginador">
      <button type="button" className="boton boton--fantasma" disabled={pagina <= 1} onClick={() => onCambiar(pagina - 1)}>Anterior</button>
      <span>Página {pagina}{total ? ` de ${total}` : ""}</span>
      <button type="button" className="boton boton--fantasma" disabled={!hayMas} onClick={() => onCambiar(pagina + 1)}>Siguiente</button>
    </div>
  );
}

// Etiqueta de estado con tono semántico: ok | aviso | alerta | info | neutro | ambar
export function Etiqueta({ tono = "neutro", children }) {
  return <span className={`etiqueta etiqueta--${tono}`}>{children}</span>;
}

/**
 * Selector con búsqueda. opciones: [{ value, label, detalle? }]. Navegable con teclado.
 */
export function Selector({ opciones, value, onChange, placeholder = "Buscar...", vacio = "Sin resultados", id, disabled }) {
  const [abierto, setAbierto] = React.useState(false);
  const [texto, setTexto] = React.useState("");
  const [activo, setActivo] = React.useState(0);
  const ref = useRef(null);
  const elegido = opciones.find((o) => o.value === value);

  React.useEffect(() => {
    if (!abierto) return undefined;
    const fuera = (e) => { if (!ref.current?.contains(e.target)) setAbierto(false); };
    document.addEventListener("mousedown", fuera);
    return () => document.removeEventListener("mousedown", fuera);
  }, [abierto]);

  const t = texto.trim().toLowerCase();
  const filtradas = React.useMemo(
    () => (t ? opciones.filter((o) => `${o.label} ${o.detalle ?? ""}`.toLowerCase().includes(t)) : opciones).slice(0, 80),
    [opciones, t],
  );

  const elegir = (o) => { onChange(o.value); setAbierto(false); setTexto(""); };
  const onKey = (e) => {
    if (e.key === "ArrowDown") { e.preventDefault(); setAbierto(true); setActivo((a) => Math.min(a + 1, filtradas.length - 1)); }
    else if (e.key === "ArrowUp") { e.preventDefault(); setActivo((a) => Math.max(a - 1, 0)); }
    else if (e.key === "Enter" && abierto && filtradas[activo]) { e.preventDefault(); elegir(filtradas[activo]); }
    else if (e.key === "Escape" && abierto) { e.stopPropagation(); setAbierto(false); }
  };

  return (
    <div className={`selector${abierto ? " is-abierto" : ""}`} ref={ref}>
      <input
        id={id}
        role="combobox"
        aria-expanded={abierto}
        autoComplete="off"
        disabled={disabled}
        value={abierto ? texto : (elegido?.label ?? "")}
        placeholder={elegido ? elegido.label : placeholder}
        onFocus={() => { setAbierto(true); setActivo(0); }}
        onChange={(e) => { setTexto(e.target.value); setActivo(0); setAbierto(true); }}
        onKeyDown={onKey}
      />
      <svg className="selector__flecha" viewBox="0 0 20 20" width="14" height="14" aria-hidden="true"><path d="M5 8l5 5 5-5" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" /></svg>
      {abierto && (
        <ul className="selector__lista" role="listbox">
          {filtradas.length === 0 && <li className="selector__vacio">{vacio}</li>}
          {filtradas.map((o, i) => (
            <li
              key={o.value}
              role="option"
              aria-selected={o.value === value}
              className={`${i === activo ? "is-activo" : ""}${o.value === value ? " is-elegido" : ""}`}
              onMouseDown={(e) => { e.preventDefault(); elegir(o); }}
              onMouseEnter={() => setActivo(i)}
            >
              <span>{o.label}</span>
              {o.detalle && <small>{o.detalle}</small>}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

// Interruptor encendido/apagado
export function Interruptor({ activo, onChange, etiqueta, descripcion, disabled }) {
  return (
    <label className={`interruptor${disabled ? " is-deshabilitado" : ""}`}>
      <span className="interruptor__texto">
        <strong>{etiqueta}</strong>
        {descripcion && <small>{descripcion}</small>}
      </span>
      <button type="button" role="switch" aria-checked={!!activo} disabled={disabled} className={`interruptor__control${activo ? " is-activo" : ""}`} onClick={() => onChange(!activo)}>
        <span />
      </button>
    </label>
  );
}
