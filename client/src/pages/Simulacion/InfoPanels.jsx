import React, { useState } from "react";

// Helper: Fecha completa en una línea
const formatDateTimeSingleLine = (dateStr) => {
  if (!dateStr) return "-";
  if (typeof dateStr === "string" && dateStr.includes("/")) {
    return dateStr;
  }
  const dateObj = new Date(dateStr);
  if (isNaN(dateObj.getTime())) return dateStr;
  const day = dateObj.getDate().toString().padStart(2, "0");
  const month = (dateObj.getMonth() + 1).toString().padStart(2, "0");
  const year = dateObj.getFullYear();
  const hours = dateObj.getHours().toString().padStart(2, "0");
  const minutes = dateObj.getMinutes().toString().padStart(2, "0");
  return `${day}/${month}/${year} ${hours}:${minutes}`;
};

// Helper: Color de barra (Verde -> Amarillo -> Rojo)
const getProgressColor = (percentage) => {
  if (percentage < 50) return "#22c55e"; // Verde
  if (percentage < 80) return "#eab308"; // Amarillo
  return "#ef4444"; // Rojo
};

// Componente mini barra de progreso para reutilizar
const MiniProgressBar = ({ value, max }) => {
  const pct = max > 0 ? Math.min(100, Math.round((value / max) * 100)) : 0;
  const color = getProgressColor(pct);

  return (
    <div
      style={{
        width: "100%",
        height: "4px",
        background: "rgba(255,255,255,0.3)",
        borderRadius: "2px",
        marginTop: "4px",
        overflow: "hidden",
      }}
    >
      <div
        style={{
          width: `${pct}%`,
          height: "100%",
          background: color,
          transition: "width 0.3s ease",
        }}
      />
    </div>
  );
};

// Devuelve la cadena de vuelos
const buildFlightChainForLote = (lote, flights = []) => {
  if (!lote || !Array.isArray(lote.vuelos) || lote.vuelos.length === 0)
    return [];
  return lote.vuelos
    .map((codVuelo) => {
      const f = flights.find((fl) => fl.code === codVuelo);
      if (!f) return null;
      const originName = f.origin?.city || f.originName || f.origin?.code;
      const destName =
        f.destination?.city || f.destinationName || f.destination?.code;
      return {
        label: `${originName} → ${destName}`,
        times: `${formatDateTimeSingleLine(
          f.startTime
        )} - ${formatDateTimeSingleLine(f.endTime)}`,
      };
    })
    .filter(Boolean);
};

/* ========== VUELO ========== */
export function FlightInfoPanel({ flight, getOrdersForFlight }) {
  if (!flight) return null;

  const pedidos = getOrdersForFlight ? getOrdersForFlight(flight.code) : [];
  const capacidadTotal = flight.planeCapacity || 0;
  const cargaActual = flight.capacity || 0;
  const porcentaje =
    capacidadTotal > 0 ? Math.round((cargaActual / capacidadTotal) * 100) : 0;

  const originCity =
    flight.origin?.city || flight.originName || flight.origin?.code;
  const destCity =
    flight.destination?.city ||
    flight.destinationName ||
    flight.destination?.code;

  return (
    <div className="info-shell">
      <div className="info-shell-header flight-theme">
        <div className="header-top">
          <span className="header-type-badge">Vuelo</span>
          <span className="header-code">{flight.code}</span>
        </div>

        <h3 className="header-title">{originCity}</h3>
        <p className="header-subtitle" style={{ margin: 0, opacity: 0.7 }}>
          hacia
        </p>
        <h3 className="header-title" style={{ marginBottom: "16px" }}>
          {destCity}
        </h3>

        <div className="stats-grid">
          <div className="stat-box">
            <label>Salida:</label>
            <span>{formatDateTimeSingleLine(flight.startTime)}</span>
          </div>
          <div className="stat-box">
            <label>Llegada:</label>
            <span>{formatDateTimeSingleLine(flight.endTime)}</span>
          </div>
          <div className="stat-box">
            <label>Carga ({porcentaje}%):</label>
            <span>
              {cargaActual} / {capacidadTotal} u.
            </span>
            {/*  BARRA DE PROGRESO VISUAL */}
            <MiniProgressBar value={cargaActual} max={capacidadTotal} />
          </div>
        </div>
      </div>

      <div className="info-shell-body">
        {flight.rutas && flight.rutas.length > 0 && (
          <div style={{ marginBottom: "20px" }}>
            <h4 className="info-section-title">Ruta asociada</h4>
            <div style={{ display: "flex", flexWrap: "wrap", gap: "6px" }}>
              {flight.rutas.map((r, idx) => (
                <span
                  key={idx}
                  style={{
                    fontSize: "12px",
                    background: "#f1f5f9",
                    padding: "4px 10px",
                    borderRadius: "6px",
                    color: "#334155",
                    fontWeight: "600",
                    border: "1px solid #e2e8f0",
                  }}
                >
                  📍 {r}
                </span>
              ))}
            </div>
          </div>
        )}

        <h4 className="info-section-title">
          📦 Pedidos a bordo ({pedidos.length})
        </h4>
        {pedidos.length === 0 ? (
          <p className="info-empty-state">Vuelo sin carga comercial.</p>
        ) : (
          <ul className="info-list-modern">
            {pedidos.map((p, idx) => (
              <li key={`${p.pedidoCodigo}-${idx}`} className="info-list-item">
                <div className="info-item-row main">
                  <span className="flight-route-text">
                    Pedido {p.pedidoCodigo}
                  </span>
                  <span className="flight-code-badge">{p.cantidad} u.</span>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}

/* ========== PEDIDO ========== */
export function OrderInfoPanel({ order, flights = [], simNowMs }) {
  if (!order) return null;

  let isEntregado = false;
  let maxArrivalMs = 0;
  const allVuelosIds = new Set();

  if (order.segmentaciones) {
    order.segmentaciones.forEach((seg) => {
      seg.lotes.forEach((lote) => {
        if (lote.vuelos) lote.vuelos.forEach((v) => allVuelosIds.add(v));
      });
    });
  }

  if (allVuelosIds.size > 0) {
    let allArrived = true;
    allVuelosIds.forEach((vid) => {
      const f = flights.find((fl) => fl.code === vid);
      if (f) {
        if (f.endMs > maxArrivalMs) maxArrivalMs = f.endMs;
        if (!simNowMs || simNowMs < f.endMs) {
          allArrived = false;
        }
      } else {
        allArrived = false;
      }
    });
    isEntregado = allArrived;
  }

  const estadoTexto = isEntregado ? "Entregado" : "Planificado";

  return (
    <div className="info-shell">
      <div className="info-shell-header order-theme">
        <div className="header-top">
          <span className="header-type-badge">Pedido</span>
          <h3 className="header-title" style={{ fontSize: "20px" }}>
            {order.codigo}
          </h3>
        </div>
        <p className="header-subtitle">Cliente: {order.codCliente}</p>

        <div className="stats-grid">
          <div className="stat-box">
            <label>Destino:</label>
            <span>{order.codDestino}</span>
          </div>
          <div className="stat-box">
            <label>Cantidad:</label>
            <span>{order.cantidadSolicitada} u.</span>
          </div>
          <div className="stat-box">
            <label>Estado:</label>
            <span>{estadoTexto}</span>
          </div>
          <div className="stat-box">
            <label>Generado:</label>
            <span>{formatDateTimeSingleLine(order.fechaHoraGeneracion)}</span>
          </div>
        </div>
      </div>

      <div className="info-shell-body">
        <h4 className="info-section-title">
          Segmentaciones ({order.segmentaciones?.length || 0})
        </h4>
        {(order.segmentaciones || []).flatMap((s) => s.lotes).length > 0 ? (
          <ul className="info-list-modern">
            {(order.segmentaciones || [])
              .flatMap((s) => s.lotes)
              .map((lote, idx) => {
                const flightChain = buildFlightChainForLote(lote, flights);
                return (
                  <li key={idx} className="info-list-item">
                    <div className="info-item-row main">
                      <span className="flight-route-text">Lote {idx + 1}</span>
                      <span className="flight-code-badge">
                        {lote.loteTamanio} u.
                      </span>
                    </div>
                    {flightChain.length > 0 ? (
                      <div
                        style={{
                          marginTop: "8px",
                          paddingLeft: "8px",
                          borderLeft: "2px solid #e2e8f0",
                        }}
                      >
                        {flightChain.map((v, i) => (
                          <div
                            key={i}
                            style={{ fontSize: "11px", marginBottom: "4px" }}
                          >
                            <div style={{ fontWeight: 600, color: "#475569" }}>
                              ✈ {v.label}
                            </div>
                            <div style={{ color: "#94a3b8", fontSize: "10px" }}>
                              {v.times}
                            </div>
                          </div>
                        ))}
                      </div>
                    ) : (
                      <div
                        style={{
                          fontSize: "11px",
                          color: "#94a3b8",
                          fontStyle: "italic",
                          marginTop: "4px",
                        }}
                      >
                        En almacén / Sin vuelo asignado
                      </div>
                    )}
                  </li>
                );
              })}
          </ul>
        ) : (
          <p className="info-empty-state">Sin segmentaciones.</p>
        )}
      </div>
    </div>
  );
}

/* ========== AEROPUERTO ========== */
export function AirportInfoPanel({
  airport,
  vuelosSaliendo,
  vuelosLlegando,
  getOrdersForFlight,
}) {
  if (!airport) return null;
  const [tab, setTab] = useState("salidas");

  const stockActual = airport.stockActual ?? 0;
  const capacidad = airport.capacidad || 0;
  const ocupacion = capacidad > 0 ? (stockActual / capacidad) * 100 : 0;
  const tipoTexto = airport.esSede ? "Principal" : "Intermedio";

  const renderList = (lista, tipo) => {
    if (!lista || lista.length === 0)
      return <div className="info-empty-state">No hay vuelos.</div>;
    const sorted = [...lista].sort((a, b) =>
      tipo === "salidas" ? a.startMs - b.startMs : a.endMs - b.endMs
    );

    return (
      <ul className="info-list-modern">
        {sorted.map((f) => {
          const cityName =
            tipo === "salidas"
              ? f.destination?.city || f.destination?.code
              : f.origin?.city || f.origin?.code;
          return (
            <li key={f.code} className="info-list-item">
              <div className="info-item-row main">
                <span className="flight-route-text">
                  {tipo === "salidas" ? `→ ${cityName}` : `← ${cityName}`}
                </span>
                <span className="flight-code-badge">{f.code}</span>
              </div>
              <div className="info-item-row sub">
                <span>
                  {tipo === "salidas"
                    ? formatDateTimeSingleLine(f.startTime)
                    : formatDateTimeSingleLine(f.endTime)}
                </span>
                <span>
                  Carga: {f.capacity}/{f.planeCapacity}
                </span>
              </div>
            </li>
          );
        })}
      </ul>
    );
  };

  return (
    <div className="info-shell">
      <div className="info-shell-header header-airport">
        <div className="header-top">
          <span className="header-type-badge">Aeropuerto</span>
          <span className="header-code">{airport.code}</span>
        </div>
        <h3 className="header-title" style={{ fontSize: "28px" }}>
          {airport.city}
        </h3>
        <p className="header-subtitle">{airport.country}</p>

        <div className="stats-grid">
          <div className="stat-box">
            <label>Tipo:</label>
            <span>{tipoTexto}</span>
          </div>
          <div className="stat-box">
            <label>Capacidad:</label>
            <span>{capacidad} u.</span>
          </div>
          <div className="stat-box">
            <label>Stock ({Math.round(ocupacion)}%):</label>
            <span style={{ color: "#fff" }}>{stockActual} u.</span>
            {/*  BARRA DE PROGRESO VISUAL */}
            <MiniProgressBar value={stockActual} max={capacidad} />
          </div>
        </div>
      </div>

      <div className="detail-tabs">
        <button
          className={`detail-tab ${tab === "salidas" ? "active" : ""}`}
          onClick={() => setTab("salidas")}
        >
          Salidas ({vuelosSaliendo?.length || 0})
        </button>
        <button
          className={`detail-tab ${tab === "llegadas" ? "active" : ""}`}
          onClick={() => setTab("llegadas")}
        >
          Llegadas ({vuelosLlegando?.length || 0})
        </button>
      </div>

      <div className="info-shell-body">
        {tab === "salidas"
          ? renderList(vuelosSaliendo, "salidas")
          : renderList(vuelosLlegando, "llegadas")}
      </div>
    </div>
  );
}

/* ========== RUTA ========== */
export function RouteInfoPanel({ route }) {
  if (!route) return null;
  const vuelos = route.codVuelos || [];

  return (
    <div className="info-shell">
      <div className="info-shell-header route-theme">
        <div className="header-top">
          <span className="header-type-badge">Ruta</span>
          <span className="header-code">{route.codigo}</span>
        </div>

        <h3 className="header-title">{route.originCity || route.codOrigen}</h3>
        <p className="header-subtitle" style={{ margin: 0, opacity: 0.7 }}>
          hacia
        </p>
        <h3 className="header-title" style={{ marginBottom: "16px" }}>
          {route.destinationCity || route.codDestino}
        </h3>

        <div className="stats-grid">
          <div className="stat-box">
            <label>Tipo:</label>
            <span>{route.tipo}</span>
          </div>
          <div className="stat-box">
            <label>Distancia:</label>
            <span>
              {typeof route.distancia === "number"
                ? route.distancia.toFixed(0)
                : 0}{" "}
              km
            </span>
          </div>
          <div className="stat-box">
            <label>Vuelos:</label>
            <span>{vuelos.length}</span>
          </div>
        </div>
      </div>

      <div className="info-shell-body">
        <h4 className="info-section-title">Vuelos asociados</h4>
        {vuelos.length === 0 ? (
          <p className="info-empty-state">No hay vuelos.</p>
        ) : (
          <ul className="info-list-modern">
            {vuelos.map((v) => (
              <li key={v} className="info-list-item">
                <div className="info-item-row main">
                  <span className="flight-route-text">{v}</span>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
