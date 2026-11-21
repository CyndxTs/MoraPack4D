import React, { useState } from "react";
import { fechaServerALocal, formatFecha, formatHora } from "../utils/fechas";
import { zonaCorta } from "../utils/zona";
import "./detalle.scss";

// Paneles de detalle del panel lateral (vuelo, pedido, aeropuerto, ruta). Horas en la zona de la sesión.

const nivel = (pct) => (pct < 50 ? "ok" : pct < 80 ? "aviso" : "alerta");
const pct = (v, max) => (max > 0 ? Math.min(100, Math.round((v / max) * 100)) : 0);

function Medidor({ etiqueta, valor, maximo, unidad = "u." }) {
  const p = pct(valor, maximo);
  return (
    <div className="det-medidor">
      <div className="det-medidor__fila">
        <span>{etiqueta}</span>
        <strong>{valor} / {maximo} {unidad} <em className={`det-tono--${nivel(p)}`}>{p}%</em></strong>
      </div>
      <div className="det-medidor__barra"><span className={`det-fondo--${nivel(p)}`} style={{ width: `${p}%` }} /></div>
    </div>
  );
}

function Dato({ label, children }) {
  return <div className="det-dato"><span>{label}</span><strong>{children}</strong></div>;
}

const ciudad = (a, fallback) => a?.city || fallback || a?.code || "—";

/* ========== VUELO ========== */
export function FlightInfoPanel({ flight, getOrdersForFlight, nowMs }) {
  if (!flight) return null;
  const pedidos = getOrdersForFlight ? getOrdersForFlight(flight.code) : [];
  const avance = nowMs != null && flight.endMs > flight.startMs
    ? Math.max(0, Math.min(1, (nowMs - flight.startMs) / (flight.endMs - flight.startMs))) : null;
  const estado = avance == null ? null : avance <= 0 ? "Por despegar" : avance >= 1 ? "Aterrizó" : "En vuelo";

  return (
    <article className="det">
      <header className="det-cabecera">
        <div className="det-cabecera__fila">
          <span className="det-codigo">{flight.code}</span>
          {estado && <span className={`det-estado det-estado--${avance >= 1 ? "ok" : avance > 0 ? "info" : "neutro"}`}>{estado}</span>}
        </div>
        <div className="det-trayecto">
          <div className="det-punto">
            <strong>{flight.origin?.code ?? "—"}</strong>
            <span>{ciudad(flight.origin, flight.originName)}</span>
            <em>{formatHora(flight.startMs)}</em>
          </div>
          <div className="det-linea" aria-hidden="true">
            <span className="det-linea__avance" style={{ width: `${(avance ?? 0) * 100}%` }} />
            <svg className="det-linea__avion" style={{ left: `${(avance ?? 0) * 100}%` }} viewBox="0 0 24 24" width="18" height="18"><path d="M21 16v-2l-8-5V3.5a1.5 1.5 0 0 0-3 0V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5z" fill="currentColor" transform="rotate(90 12 12)" /></svg>
          </div>
          <div className="det-punto det-punto--fin">
            <strong>{flight.destination?.code ?? "—"}</strong>
            <span>{ciudad(flight.destination, flight.destinationName)}</span>
            <em>{formatHora(flight.endMs)}</em>
          </div>
        </div>
        <p className="det-nota">Salida {formatFecha(flight.startMs)} · llegada {formatFecha(flight.endMs)} ({zonaCorta()})</p>
      </header>

      <section className="det-seccion">
        <Medidor etiqueta="Carga del avión" valor={flight.capacity || 0} maximo={flight.planeCapacity || 0} />
      </section>

      {flight.rutas?.length > 0 && (
        <section className="det-seccion">
          <h4>Rutas que lo usan</h4>
          <div className="det-chips">{flight.rutas.map((r) => <span key={r}>{r}</span>)}</div>
        </section>
      )}

      <section className="det-seccion">
        <h4>Pedidos a bordo <span>{pedidos.length}</span></h4>
        {pedidos.length === 0 ? <p className="det-vacio">Vuelo sin carga comercial.</p> : (
          <ul className="det-lista">
            {pedidos.map((p, i) => (
              <li key={`${p.pedidoCodigo}-${i}`}><span>{p.pedidoCodigo}</span><strong>{p.cantidad} u.</strong></li>
            ))}
          </ul>
        )}
      </section>
    </article>
  );
}

/* ========== PEDIDO ========== */
export function OrderInfoPanel({ order, flights = [], simNowMs }) {
  if (!order) return null;
  const entregado = order.entregaMs != null && simNowMs != null && simNowMs >= order.entregaMs;
  const [tono, estado] = order.estado === "CANCELADO" ? ["neutro", "Cancelado"]
    : order.estado === "CANCELADO_POR_ERROR" ? ["alerta", "Sin ruta a tiempo"]
    : entregado ? ["ok", "Entregado"] : ["info", "Planificado"];
  const lotes = (order.segmentaciones || []).flatMap((s) => s.lotes || []);
  const vuelo = (cod) => flights.find((f) => f.code === cod);

  return (
    <article className="det">
      <header className="det-cabecera">
        <div className="det-cabecera__fila">
          <span className="det-codigo">{order.codigo}</span>
          <span className={`det-estado det-estado--${tono}`}>{estado}</span>
        </div>
        <div className="det-datos">
          <Dato label="Cliente">{order.codCliente}</Dato>
          <Dato label="Destino">{order.codDestino}</Dato>
          <Dato label="Cantidad">{order.cantidadSolicitada} u.</Dato>
          <Dato label={`Generado (${zonaCorta()})`}>{fechaServerALocal(order.fechaHoraGeneracion)}</Dato>
          {order.entregaMs != null && <Dato label="Llega a destino">{formatFecha(order.entregaMs)}</Dato>}
        </div>
      </header>

      <section className="det-seccion">
        <h4>Recorrido por lotes <span>{lotes.length}</span></h4>
        {lotes.length === 0 ? <p className="det-vacio">Sin lotes asignados todavía.</p> : lotes.map((lote, i) => {
          const tramo = (lote.vuelos || []).map(vuelo).filter(Boolean);
          return (
            <div key={i} className="det-lote">
              <div className="det-lote__cabecera"><span>Lote {i + 1}</span><strong>{lote.loteTamanio} u.</strong></div>
              {tramo.length === 0 ? <p className="det-vacio">En almacén, sin vuelo asignado.</p> : (
                <ol className="det-escalas">
                  {tramo.map((f, k) => {
                    const hecho = simNowMs != null && simNowMs >= f.endMs;
                    const enCurso = simNowMs != null && simNowMs >= f.startMs && simNowMs < f.endMs;
                    return (
                      <li key={f.code} className={hecho ? "is-hecho" : enCurso ? "is-actual" : ""}>
                        <span className="det-escalas__nodo" aria-hidden="true" />
                        <div>
                          <strong>{ciudad(f.origin)} → {ciudad(f.destination)}</strong>
                          <small>{formatFecha(f.startMs)} – {formatHora(f.endMs)} · {f.code}</small>
                        </div>
                        {k === tramo.length - 1 && <span className="det-escalas__fin" aria-hidden="true" />}
                      </li>
                    );
                  })}
                </ol>
              )}
            </div>
          );
        })}
      </section>
    </article>
  );
}

/* ========== AEROPUERTO ========== */
export function AirportInfoPanel({ airport, vuelosSaliendo = [], vuelosLlegando = [], nowMs }) {
  const [tab, setTab] = useState("salidas");
  if (!airport) return null;
  const lista = tab === "salidas" ? vuelosSaliendo : vuelosLlegando;
  const ordenada = [...(lista || [])].sort((a, b) => (tab === "salidas" ? a.startMs - b.startMs : a.endMs - b.endMs));

  return (
    <article className="det">
      <header className="det-cabecera">
        <div className="det-cabecera__fila">
          <span className="det-codigo">{airport.code}</span>
          <span className={`det-estado det-estado--${airport.esSede ? "info" : "neutro"}`}>{airport.esSede ? "Sede" : "Aeropuerto"}</span>
        </div>
        <h3 className="det-titulo">{airport.city}</h3>
        <p className="det-nota">{airport.country}{airport.continent ? ` · ${airport.continent}` : ""}</p>
      </header>

      <section className="det-seccion">
        <Medidor etiqueta="Almacén" valor={airport.stockActual ?? 0} maximo={airport.capacidad || 0} />
      </section>

      <section className="det-seccion">
        <div className="det-pestanas" role="tablist">
          <button type="button" role="tab" aria-selected={tab === "salidas"} className={tab === "salidas" ? "is-activo" : ""} onClick={() => setTab("salidas")}>Salidas <span>{vuelosSaliendo?.length || 0}</span></button>
          <button type="button" role="tab" aria-selected={tab === "llegadas"} className={tab === "llegadas" ? "is-activo" : ""} onClick={() => setTab("llegadas")}>Llegadas <span>{vuelosLlegando?.length || 0}</span></button>
        </div>
        {ordenada.length === 0 ? <p className="det-vacio">No hay vuelos.</p> : (
          <ul className="det-tablero">
            {ordenada.map((f) => {
              const otro = tab === "salidas" ? f.destination : f.origin;
              const t = tab === "salidas" ? f.startMs : f.endMs;
              const pasado = nowMs != null && nowMs >= t;
              return (
                <li key={f.code} className={pasado ? "is-pasado" : ""}>
                  <time>{formatHora(t)}</time>
                  <span className="det-tablero__lugar"><strong>{otro?.code}</strong> {ciudad(otro)}</span>
                  <span className="det-tablero__carga">{f.capacity}/{f.planeCapacity}</span>
                </li>
              );
            })}
          </ul>
        )}
      </section>
    </article>
  );
}

/* ========== RUTA ========== */
export function RouteInfoPanel({ route, flights = [] }) {
  if (!route) return null;
  const vuelos = (route.codVuelos || []).map((c) => flights.find((f) => f.code === c) || { code: c });
  return (
    <article className="det">
      <header className="det-cabecera">
        <div className="det-cabecera__fila">
          <span className="det-codigo">{route.codigo}</span>
          {route.tipo && <span className="det-estado det-estado--neutro">{route.tipo === "INTERCONTINENTAL" ? "Intercontinental" : route.tipo === "INTRACONTINENTAL" ? "Intracontinental" : route.tipo}</span>}
        </div>
        <h3 className="det-titulo">{route.originCity || route.codOrigen} → {route.destinationCity || route.codDestino}</h3>
        <div className="det-datos">
          <Dato label="Distancia">{typeof route.distancia === "number" ? `${Math.round(route.distancia).toLocaleString("es")} km` : "—"}</Dato>
          <Dato label="Vuelos">{vuelos.length}</Dato>
        </div>
      </header>
      <section className="det-seccion">
        <h4>Vuelos de la ruta</h4>
        {vuelos.length === 0 ? <p className="det-vacio">No hay vuelos.</p> : (
          <ol className="det-escalas">
            {vuelos.map((f) => (
              <li key={f.code}>
                <span className="det-escalas__nodo" aria-hidden="true" />
                <div>
                  <strong>{f.origin ? `${ciudad(f.origin)} → ${ciudad(f.destination)}` : f.code}</strong>
                  {f.startMs != null && <small>{formatFecha(f.startMs)} – {formatHora(f.endMs)} · {f.code}</small>}
                </div>
              </li>
            ))}
          </ol>
        )}
      </section>
    </article>
  );
}
