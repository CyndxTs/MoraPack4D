import { formatFecha } from "../utils/fechas";
import React, { useState } from "react";

// Helper para obtener color según porcentaje
// Mismos umbrales que la leyenda: aeropuertos 50/80 %, aviones 50/90 %
const getProgressColor = (percentage, alto = 80) => {
  if (percentage < 50) return "var(--ok)";
  if (percentage < alto) return "var(--aviso)";
  return "var(--alerta)";
};

// === TOOLTIP AEROPUERTO ===
export function AirportTooltipContent({
  airport,
  vuelosQueSalen,
  vuelosQueLlegan,
  getOrdersForFlight,
  onOpenPanel,
}) {
  const [tab, setTab] = useState("salidas");

  const ocupPct = Math.min(
    100,
    Math.max(0, Math.round((airport.ocupacion ?? 0) * 100))
  );

  // Obtenemos el color dinámico
  const barColor = getProgressColor(ocupPct);

  const isSalidas = tab === "salidas";
  const flightsSource = isSalidas ? vuelosQueSalen : vuelosQueLlegan;
  const flightsToShow = (flightsSource || []).slice(0, 2);

  return (
    <div
      className="airport-popup"
      style={{ cursor: "pointer" }}
      onClick={() => onOpenPanel(airport)}
    >
      <div className="airport-popup__header">
        <span className="airport-popup__country">
          {airport.city} ({airport.code})
        </span>
        <span className="airport-popup__code">
          {airport.esSede ? "Sede" : "Destino"}
        </span>
      </div>

      <div className="airport-popup__city">{airport.country}</div>

      <div className="airport-popup__row">
        <span className="airport-popup__label">Capacidad: </span>
        <span className="airport-popup__value">{airport.capacidad} u</span>
      </div>

      <div className="airport-popup__row">
        <span className="airport-popup__label">Stock actual: </span>
        <span className="airport-popup__value">
          {airport.stockActual} u (<span style={{ color: barColor }}>{ocupPct}%</span>)
        </span>
      </div>

      {/* BARRA DE PROGRESO CON COLOR DINÁMICO */}
      <div className="airport-popup__progress">
        <div
          className="airport-popup__progress-fill"
          style={{
            width: `${ocupPct}%`,
            background: barColor,
          }}
        />
      </div>

      {/* Tabs Salidas / Llegadas */}
      <div className="airport-popup-tabs">
        <button
          type="button"
          className={`airport-popup-tab ${tab === "salidas" ? "active" : ""}`}
          onClick={(e) => {
            e.stopPropagation();
            setTab("salidas");
          }}
        >
          Salidas
        </button>
        <button
          type="button"
          className={`airport-popup-tab ${tab === "llegadas" ? "active" : ""}`}
          onClick={(e) => {
            e.stopPropagation();
            setTab("llegadas");
          }}
        >
          Llegadas
        </button>
      </div>

      <div className="airport-popup__section-title">
        {isSalidas ? "Próximas salidas" : "Próximas llegadas"}
      </div>

      {flightsToShow.length === 0 ? (
        <div className="airport-popup__footnote">
          {isSalidas
            ? "No hay salidas pendientes."
            : "No hay llegadas pendientes."}
        </div>
      ) : (
        <ul
          className="airport-popup__list"
          onClick={(e) => e.stopPropagation()}
        >
          {flightsToShow.map((v) => {
            const pedidosVuelo = getOrdersForFlight(v.code);
            const totalVuelo = pedidosVuelo.reduce(
              (sum, p) => sum + (p.cantidad || 0),
              0
            );

            return (
              <li key={v.code}>
                <div className="airport-popup__flight">
                  <div className="airport-popup__flight-main">
                    <span className="airport-popup__flight-code">{v.code}</span>
                    <span className="airport-popup__flight-dest">
                      {isSalidas
                        ? `→ ${v.destination?.code}`
                        : `← ${v.origin?.code}`}
                    </span>
                  </div>
                  <div className="airport-popup__flight-time">
                    {isSalidas ? "Sale: " : "Llega: "}
                    {formatFecha(isSalidas ? v.startMs : v.endMs)}
                  </div>
                  <div className="airport-popup__flight-orders">
                    {pedidosVuelo.length === 0 ? (
                      "Sin pedidos asignados"
                    ) : (
                      <div>
                        <div className="airport-popup__orders-title">
                          Pedidos (total {totalVuelo} u.)
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
}

// === TOOLTIP AVIÓN ===
export function PlaneTooltipContent({
  flight,
  getOrdersForFlight,
  onOpenPanel,
}) {
  const ocupPct =
    flight.planeCapacity > 0
      ? Math.round((flight.capacity * 100) / flight.planeCapacity)
      : 0;

  const barColor = getProgressColor(ocupPct, 90);

  return (
    <div
      className="plane-tooltip"
      onClick={() => onOpenPanel && onOpenPanel(flight)}
      style={{ cursor: "pointer" }}
    >
      <div className="plane-tooltip__route">
        {flight.origin.code} → {flight.destination.code}
      </div>

      <div className="plane-tooltip__code">{flight.code}</div>

      <div className="plane-tooltip__row">
        <span>Salida</span>
        <span>{formatFecha(flight.startMs)}</span>
      </div>
      <div className="plane-tooltip__row">
        <span>Llegada</span>
        <span>{formatFecha(flight.endMs)}</span>
      </div>
      <div className="plane-tooltip__row">
        <span>Capacidad</span>
        <span>
          {flight.capacity}/{flight.planeCapacity} u (<span style={{ color: barColor }}>{ocupPct}%</span>)
        </span>
      </div>

      {/*  BARRA DE PROGRESO DE AVIÓN CON COLOR */}
      <div className="plane-tooltip__progress">
        <div
          className="plane-tooltip__progress-fill"
          style={{
            width: `${ocupPct}%`,
            background: barColor,
          }}
        />
      </div>
    </div>
  );
}
