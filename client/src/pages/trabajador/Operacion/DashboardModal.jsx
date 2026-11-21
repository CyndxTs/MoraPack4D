import React, { useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { Bar, BarChart, Cell, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { calcularVista } from "../../../scenario/vista";
import { formatFecha } from "../../../utils/fechas";
import { zonaCorta } from "../../../utils/zona";
import { formatPercent } from "../../../utils/format";
import "./dashboard.scss";

const COLOR_NIVEL = (ratio) => (ratio >= 0.8 ? "#FF6B7A" : ratio >= 0.5 ? "#eab308" : "#22c55e");

function Kpi({ label, value, hint, tono }) {
  return (
    <div className={`dash-kpi ${tono ? `dash-kpi--${tono}` : ""}`}>
      <span className="dash-kpi-label">{label}</span>
      <span className="dash-kpi-value">{value}</span>
      {hint && <span className="dash-kpi-hint">{hint}</span>}
    </div>
  );
}

// Resumen de la operación, por encima del mapa. Se cierra y se vuelve a abrir con el botón "Dashboard".
export default function DashboardModal({ escenario, nowMs, onClose }) {
  const navigate = useNavigate();
  const vista = useMemo(() => calcularVista(escenario, nowMs), [escenario, nowMs]);
  const m = escenario.metricas;

  const aeropuertosMasOcupados = useMemo(
    () => vista.airports
      .filter((a) => !a.esSede && a.capacidad > 0 && a.stockActual > 0)
      .sort((a, b) => b.ocupacion - a.ocupacion)
      .slice(0, 8)
      .map((a) => ({ codigo: a.code, ocupacion: Math.round(a.ocupacion * 1000) / 10, ratio: a.ocupacion })),
    [vista.airports]
  );

  const pedidos = [
    { nombre: "En proceso", valor: vista.orders.enProceso.length, color: "#78C6FF" },
    { nombre: "Entregados", valor: vista.orders.entregados.length, color: "#39D98A" },
    { nombre: "No atendidos", valor: escenario.noAtendidos.length, color: "#FF6B7A" },
  ];
  const hayPedidos = pedidos.some((p) => p.valor > 0);

  const ir = (ruta) => { onClose(); navigate(ruta); };

  return (
    <div className="dash-overlay" onClick={onClose}>
      <div className="dash-content" onClick={(e) => e.stopPropagation()}>
        <header className="dash-header">
          <div>
            <h3>Dashboard de la operación</h3>
            <span className="dash-subtitle">
              {escenario.recibidoEn
                ? `Última planificación recibida: ${formatFecha(escenario.recibidoEn, true)} (${zonaCorta()})`
                : "Aún no hay una planificación de la operación desde que el server arrancó."}
            </span>
          </div>
          <button className="dash-close" onClick={onClose} title="Cerrar">✕</button>
        </header>

        <section className="dash-kpis">
          <Kpi label="Pedidos en proceso" value={vista.orders.enProceso.length} />
          <Kpi label="Pedidos entregados" value={vista.orders.entregados.length} tono="ok" />
          <Kpi label="No atendidos" value={escenario.noAtendidos.length} tono={escenario.noAtendidos.length ? "danger" : null} />
          <Kpi label="Vuelos en el aire" value={vista.activeFlights.length} hint={`${vista.upcomingFlights.length} próximos`} />
          <Kpi label="Ocupación de flota" value={`${vista.fleet.percentage.toFixed(1)}%`} hint={`${vista.fleet.totalOccupied} / ${vista.fleet.totalMax} u.`} />
          <Kpi label="Rutas con vuelos activos" value={vista.routesNow.length} />
        </section>

        <section className="dash-grid">
          <div className="dash-card">
            <h4>Aeropuertos más ocupados</h4>
            {aeropuertosMasOcupados.length === 0 ? (
              <p className="dash-empty">Sin carga en aeropuertos en este momento.</p>
            ) : (
              <ResponsiveContainer width="100%" height={220}>
                <BarChart data={aeropuertosMasOcupados} margin={{ top: 8, right: 8, left: -16, bottom: 0 }}>
                  <XAxis dataKey="codigo" tick={{ fontSize: 11 }} />
                  <YAxis unit="%" tick={{ fontSize: 11 }} domain={[0, 100]} />
                  <Tooltip formatter={(v) => `${v}%`} />
                  <Bar dataKey="ocupacion" name="Ocupación" radius={[4, 4, 0, 0]}>
                    {aeropuertosMasOcupados.map((a) => <Cell key={a.codigo} fill={COLOR_NIVEL(a.ratio)} />)}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            )}
          </div>

          <div className="dash-card">
            <h4>Pedidos</h4>
            {!hayPedidos ? (
              <p className="dash-empty">No hay pedidos en la ventana de operación.</p>
            ) : (
              <div className="dash-pie">
                <ResponsiveContainer width="55%" height={200}>
                  <PieChart>
                    <Pie data={pedidos} dataKey="valor" nameKey="nombre" innerRadius={45} outerRadius={80} paddingAngle={2}>
                      {pedidos.map((p) => <Cell key={p.nombre} fill={p.color} />)}
                    </Pie>
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>
                <ul className="dash-legend">
                  {pedidos.map((p) => (
                    <li key={p.nombre}><span style={{ background: p.color }} />{p.nombre}<strong>{p.valor}</strong></li>
                  ))}
                </ul>
              </div>
            )}
          </div>

          <div className="dash-card">
            <h4>Calidad de la planificación</h4>
            <div className="dash-metric"><span>Utilización temporal</span><strong>{formatPercent(m?.utilizacionTemporal)}</strong></div>
            <div className="dash-metric"><span>Desviación espacial</span><strong>{formatPercent(m?.desviacionEspacial)}</strong></div>
            <div className="dash-metric"><span>Disposición operacional</span><strong>{formatPercent(m?.disposicionOperacional)}</strong></div>
            <p className="dash-note">Promedios de la última solución del algoritmo (menor es mejor).</p>
          </div>

          <div className="dash-card">
            <h4>Pedidos no atendidos</h4>
            {escenario.noAtendidos.length === 0 ? (
              <p className="dash-empty">Ninguno 🎉</p>
            ) : (
              <ul className="dash-list">
                {escenario.noAtendidos.slice(0, 6).map((p) => (
                  <li key={p.codigo}>
                    <strong>{p.codigo}</strong> → {p.codDestino} · {p.cantidadSolicitada} u.
                    <span className="dash-tag">{p.motivo === "EVENTO" ? "Evento" : "Sin ruta"}</span>
                  </li>
                ))}
                {escenario.noAtendidos.length > 6 && <li className="dash-more">y {escenario.noAtendidos.length - 6} más (ver panel lateral)</li>}
              </ul>
            )}
          </div>
        </section>

        <footer className="dash-footer">
          <div className="dash-links">
            <button onClick={() => ir("/trabajador/pedidos")}>Gestionar pedidos</button>
            <button onClick={() => ir("/trabajador/planes")}>Planes y eventos</button>
            <button onClick={() => ir("/trabajador/simulacion")}>Simulación</button>
          </div>
          <button className="btn green" onClick={onClose}>Ver la operación en el mapa</button>
        </footer>
      </div>
    </div>
  );
}
