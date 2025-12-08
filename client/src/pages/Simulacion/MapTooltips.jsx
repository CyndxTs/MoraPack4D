import React, { useState } from "react";

// === TOOLTIP AEROPUERTO (tabs Salidas / Llegadas, máx 2) ===
export function AirportTooltipContent({
  airport,
  vuelosQueSalen,
  vuelosQueLlegan,
  getOrdersForFlight,
}) {
  const [tab, setTab] = useState("salidas");

  const ocupPct = Math.min(
    100,
    Math.max(0, Math.round((airport.ocupacion ?? 0) * 100))
  );

  const isSalidas = tab === "salidas";
  const flightsSource = isSalidas ? vuelosQueSalen : vuelosQueLlegan;
  // solo 2 vuelos
  const flightsToShow = flightsSource.slice(0, 2);

  return (
    <div className="airport-popup">
      <div className="airport-popup__header">
        <span className="airport-popup__country">
          {airport.city} ({airport.code})
        </span>
        <span className="airport-popup__code">
          {airport.esSede ? "Principal" : "Intermedio"}
        </span>
      </div>

      <div className="airport-popup__city">{airport.country}</div>

      <div className="airport-popup__row">
        <span className="airport-popup__label">Capacidad</span>
        <span className="airport-popup__value">{airport.capacidad} u</span>
      </div>

      <div className="airport-popup__row">
        <span className="airport-popup__label">Stock actual</span>
        <span className="airport-popup__value">
          {airport.stockActual} u ({ocupPct}%)
        </span>
      </div>

      <div className="airport-popup__progress">
        <div
          className="airport-popup__progress-fill"
          style={{ width: `${ocupPct}%` }}
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
        <ul className="airport-popup__list">
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
                    {isSalidas ? v.startTime : v.endTime}
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

// === TOOLTIP AVIÓN (hover ordenado) ===
export function PlaneTooltipContent({ flight, getOrdersForFlight }) {
  const ocupPct =
    flight.planeCapacity > 0
      ? Math.round((flight.capacity * 100) / flight.planeCapacity)
      : 0;

  const pedidosVuelo = getOrdersForFlight(flight.code);
  const totalVuelo = pedidosVuelo.reduce(
    (sum, p) => sum + (p.cantidad || 0),
    0
  );

  return (
    <div className="plane-tooltip">
      <div className="plane-tooltip__route">
        {flight.origin.code} → {flight.destination.code}
      </div>

      <div className="plane-tooltip__code">{flight.code}</div>

      <div className="plane-tooltip__row">
        <span>Salida</span>
        <span>{flight.startTime}</span>
      </div>
      <div className="plane-tooltip__row">
        <span>Llegada</span>
        <span>{flight.endTime}</span>
      </div>
      <div className="plane-tooltip__row">
        <span>Capacidad</span>
        <span>
          {flight.capacity}/{flight.planeCapacity} u ({ocupPct}%)
        </span>
      </div>

      {/* 🔥 barra de progreso de ocupación */}
      <div className="plane-tooltip__progress">
        <div
          className="plane-tooltip__progress-fill"
          style={{ width: `${ocupPct}%` }}
        />
      </div>
    </div>
  );
}
