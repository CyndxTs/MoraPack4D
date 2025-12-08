// src/pages/Simulacion/InfoPanels.jsx
import React from "react";

// ========== VUELO ==========
export function FlightInfoPanel({ flight, getOrdersForFlight }) {
  if (!flight) return null;

  const pedidos = getOrdersForFlight ? getOrdersForFlight(flight.code) : [];
  const ocupacion =
    flight.planeCapacity > 0
      ? Math.round((flight.capacity / flight.planeCapacity) * 100)
      : 0;

  return (
    <div className="info-shell">
      {/* HEADER */}
      <div className="info-shell-header">
        <div className="info-shell-left">
          <div className="info-chip info-chip--flight">Vuelo seleccionado</div>
          <h3 className="info-shell-title">
            {flight.originName} ({flight.origin.code}) →{" "}
            {flight.destinationName} ({flight.destination.code})
          </h3>
          <p className="info-shell-subtitle">{flight.code}</p>
        </div>

        <div className="info-shell-summary">
          <div className="info-summary-row">
            <span className="info-summary-label">Capacidad:</span>
            <span className="info-summary-value">
              {flight.capacity} / {flight.planeCapacity} u.
            </span>
          </div>
          <div className="info-summary-row">
            <span className="info-summary-label">Ocupación:</span>
            <span className="info-summary-value">{ocupacion}%</span>
          </div>
          <div className="info-summary-row">
            <span className="info-summary-label">Salida:</span>
            <span className="info-summary-value">{flight.startTime}</span>
          </div>
          <div className="info-summary-row">
            <span className="info-summary-label">Llegada:</span>
            <span className="info-summary-value">{flight.endTime}</span>
          </div>
        </div>
      </div>

      {/* BODY */}
      <div className="info-shell-body--two-cols">
        <div>
          <h4 className="info-section-title">Pedidos transportados</h4>
          {pedidos.length === 0 ? (
            <p className="info-empty">Este vuelo no transporta pedidos.</p>
          ) : (
            <ul className="info-list">
              {pedidos.map((p) => (
                <li key={`${p.pedidoCodigo}-${p.loteCodigo}`}>
                  Pedido {p.pedidoCodigo} · Lote {p.loteCodigo} · {p.cantidad}{" "}
                  u.
                </li>
              ))}
            </ul>
          )}
        </div>

        <div>
          <h4 className="info-section-title">Rutas que pasa</h4>
          {flight.rutas && flight.rutas.length > 0 ? (
            <ul className="info-list">
              {flight.rutas.map((r, idx) => (
                <li key={idx}>{r}</li>
              ))}
            </ul>
          ) : (
            <p className="info-empty">Este vuelo no tiene rutas asociadas.</p>
          )}
        </div>
      </div>
    </div>
  );
}

// ========== PEDIDO ==========
export function OrderInfoPanel({ order }) {
  if (!order) return null;

  const totalSegmentos = order.segmentaciones?.length || 0;
  const totalLotes =
    order.segmentaciones?.reduce(
      (acc, seg) => acc + (seg.lotes?.length || 0),
      0
    ) || 0;
  const totalUnidades =
    order.segmentaciones?.reduce(
      (acc, seg) =>
        acc +
        (seg.lotes || []).reduce(
          (accLote, lote) => accLote + (lote.loteTamanio || 0),
          0
        ),
      0
    ) || 0;

  return (
    <div className="info-shell">
      {/* HEADER */}
      <div className="info-shell-header">
        <div className="info-shell-left">
          <div className="info-chip info-chip--order">Pedido seleccionado</div>
          <h3 className="info-shell-title">{order.codigo}</h3>
          <p className="info-shell-subtitle">
            Cliente {order.codCliente} · Destino {order.codDestino}
          </p>
        </div>

        <div className="info-shell-summary">
          <div className="info-summary-row">
            <span className="info-summary-label">Cantidad solicitada:</span>
            <span className="info-summary-value">
              {order.cantidadSolicitada} u.
            </span>
          </div>
          <div className="info-summary-row">
            <span className="info-summary-label">Estado:</span>
            <span className="info-summary-value">
              {order.fueAtendido ? "Atendido" : "Pendiente"}
            </span>
          </div>
          <div className="info-summary-row">
            <span className="info-summary-label">Segmentaciones:</span>
            <span className="info-summary-value">{totalSegmentos}</span>
          </div>
          <div className="info-summary-row">
            <span className="info-summary-label">Lotes:</span>
            <span className="info-summary-value">{totalLotes}</span>
          </div>
        </div>
      </div>

      {/* BODY */}
      <div className="info-shell-body--two-cols">
        <div>
          <h4 className="info-section-title">Segmentaciones y lotes</h4>

          {order.segmentaciones && order.segmentaciones.length > 0 ? (
            <ul className="info-list">
              {order.segmentaciones.map((seg) => (
                <li key={seg.codigo}>
                  <div>
                    <strong>{seg.codigo}</strong>{" "}
                    {seg.fechaHoraAplicacion && (
                      <span>({seg.fechaHoraAplicacion})</span>
                    )}
                  </div>
                  {(seg.lotes || []).map((lote) => (
                    <div key={lote.loteCodigo}>
                      • Lote {lote.loteCodigo} · {lote.loteTamanio} u. · Ruta{" "}
                      {lote.origenCode} → {lote.destinoCode}{" "}
                      {lote.vuelos?.length
                        ? `· Vuelos: ${lote.vuelos.join(", ")}`
                        : ""}
                    </div>
                  ))}
                </li>
              ))}
            </ul>
          ) : (
            <p className="info-empty">Este pedido no tiene segmentaciones.</p>
          )}
        </div>

        <div>
          <h4 className="info-section-title">Resumen</h4>
          <p className="info-section-subtitle">
            Distribución del pedido en la red.
          </p>
          <ul className="info-list">
            <li>
              Total unidades en lotes: <strong>{totalUnidades} u.</strong>
            </li>
            <li>
              Segmentaciones: <strong>{totalSegmentos}</strong>
            </li>
            <li>
              Lotes creados: <strong>{totalLotes}</strong>
            </li>
            <li>
              Estado:{" "}
              <strong>
                {order.fueAtendido ? "Atendido completamente" : "Pendiente"}
              </strong>
            </li>
          </ul>
        </div>
      </div>
    </div>
  );
}

// ========== AEROPUERTO ==========
export function AirportInfoPanel({
  airport,
  vuelosSaliendo,
  vuelosLlegando,
  getOrdersForFlight,
}) {
  if (!airport) return null;

  const stockActual = airport.stockActual ?? 0;
  const capacidad = airport.capacidad || 0;
  const ocupacion = capacidad > 0 ? (stockActual / capacidad) * 100 : 0;

  const buildPedidosSummary = (flight) => {
    if (!getOrdersForFlight) return null;
    const pedidos = getOrdersForFlight(flight.code) || [];
    if (pedidos.length === 0) return "0 pedidos";

    const total = pedidos.reduce((acc, p) => acc + (p.cantidad || 0), 0);
    return `${pedidos.length} pedidos · ${total} u.`;
  };

  const renderFlightList = (lista, tipo) => {
    if (!lista || lista.length === 0) {
      return (
        <p className="info-empty">No hay vuelos {tipo} para este aeropuerto.</p>
      );
    }

    return (
      <ul className="info-list">
        {lista.map((f) => (
          <li key={f.code}>
            <div>
              <strong>
                {f.origin.code} → {f.destination.code}
              </strong>{" "}
              ({f.code})
            </div>
            <div>
              {tipo === "saliendo" ? "Sale" : "Llega"}:{" "}
              {tipo === "saliendo" ? f.startTime : f.endTime}
            </div>
            <div>
              Capacidad: {f.capacity} / {f.planeCapacity} u.
            </div>
            {getOrdersForFlight && <div>{buildPedidosSummary(f)}</div>}
          </li>
        ))}
      </ul>
    );
  };

  return (
    <div className="info-shell">
      {/* HEADER */}
      <div className="info-shell-header">
        <div className="info-shell-left">
          <div className="info-chip info-chip--airport">
            Aeropuerto seleccionado
          </div>
          <h3 className="info-shell-title">
            {airport.city} ({airport.code})
          </h3>
          <p className="info-shell-subtitle">{airport.country}</p>
        </div>

        <div className="info-shell-summary">
          <div className="info-summary-row">
            <span className="info-summary-label">Tipo:</span>
            <span className="info-summary-value">
              {airport.esSede ? "Principal" : "Intermedio"}
            </span>
          </div>
          <div className="info-summary-row">
            <span className="info-summary-label">Capacidad total:</span>
            <span className="info-summary-value">{capacidad} unidades</span>
          </div>
          <div className="info-summary-row">
            <span className="info-summary-label">Stock actual:</span>
            <span className="info-summary-value">
              {stockActual} / {capacidad} u. ({Math.round(ocupacion)}%)
            </span>
          </div>
        </div>
      </div>

      {/* BODY */}
      <div className="info-shell-body--two-cols">
        <div>
          <h4 className="info-section-title">Vuelos saliendo</h4>
          {renderFlightList(vuelosSaliendo, "saliendo")}
        </div>

        <div>
          <h4 className="info-section-title">Vuelos llegando</h4>
          {renderFlightList(vuelosLlegando, "llegando")}
        </div>
      </div>
    </div>
  );
}

// ========== RUTA ==========
export function RouteInfoPanel({ route }) {
  if (!route) return null;

  const vuelos = route.codVuelos || [];

  return (
    <div className="info-shell">
      {/* HEADER */}
      <div className="info-shell-header">
        <div className="info-shell-left">
          <div className="info-chip info-chip--route">Ruta seleccionada</div>
          <h3 className="info-shell-title">
            {route.codOrigen} → {route.codDestino}
          </h3>
          <p className="info-shell-subtitle">{route.codigo}</p>
        </div>

        <div className="info-shell-summary">
          <div className="info-summary-row">
            <span className="info-summary-label">Tipo:</span>
            <span className="info-summary-value">{route.tipo}</span>
          </div>
          {typeof route.distancia === "number" && (
            <div className="info-summary-row">
              <span className="info-summary-label">Distancia:</span>
              <span className="info-summary-value">
                {route.distancia.toFixed(0)} km
              </span>
            </div>
          )}
          <div className="info-summary-row">
            <span className="info-summary-label">Vuelos asociados:</span>
            <span className="info-summary-value">{vuelos.length}</span>
          </div>
        </div>
      </div>

      {/* BODY */}
      <div className="info-shell-body--two-cols">
        <div>
          <h4 className="info-section-title">Vuelos</h4>
          {vuelos.length === 0 ? (
            <p className="info-empty">No hay vuelos para esta ruta.</p>
          ) : (
            <ul className="info-list">
              {vuelos.map((v) => (
                <li key={v}>Vuelo {v}</li>
              ))}
            </ul>
          )}
        </div>

        <div>
          <h4 className="info-section-title">Detalle de la ruta</h4>
          <p className="info-section-subtitle">
            Configuración básica de esta conexión.
          </p>
          <ul className="info-list">
            <li>
              Origen: <strong>{route.codOrigen}</strong>
            </li>
            <li>
              Destino: <strong>{route.codDestino}</strong>
            </li>
            <li>
              Tipo: <strong>{route.tipo}</strong>
            </li>
            {typeof route.distancia === "number" && (
              <li>
                Distancia aproximada:{" "}
                <strong>{route.distancia.toFixed(0)} km</strong>
              </li>
            )}
          </ul>
        </div>
      </div>
    </div>
  );
}
