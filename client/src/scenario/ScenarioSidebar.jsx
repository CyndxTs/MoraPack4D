import React, { useState } from "react";
import hideIcon from "../assets/icons/hide-sidebar.png";
import { AirportInfoPanel, FlightInfoPanel, OrderInfoPanel, RouteInfoPanel } from "./InfoPanels";
import { getAirportOccupancyClass } from "./vista";

const vacio = (texto) => <div style={{ padding: "20px", textAlign: "center", color: "#999" }}>{texto}</div>;

// Panel lateral común a Operación y Simulación (antes había dos copias casi idénticas)
export default function ScenarioSidebar({
  collapsed, setCollapsed, pinned, setPinned,
  tab, setTab,
  nowMs, escenario, vista,
  selected, onClearSelection, seleccionDetalle,
  filtros, setFiltro,
  onFlightClick, onOrderClick, onAirportClick, onRouteClick,
  getAirportLabel, getOrdersForFlight,
}) {
  const [flightViewMode, setFlightViewMode] = useState("ACTIVE");
  const isDetailView = !!selected;

  // ---------- Vuelos ----------
  const flightsToShow = flightViewMode === "ACTIVE" ? vista.activeFlights : vista.upcomingFlights;
  const filteredFlights = flightsToShow.filter((f) =>
    (!filtros.vueloCodigo || f.code.toUpperCase().includes(filtros.vueloCodigo.toUpperCase()))
    && (!filtros.vueloOrigen || f.origin?.code === filtros.vueloOrigen)
    && (!filtros.vueloDestino || f.destination?.code === filtros.vueloDestino));

  // ---------- Pedidos ----------
  const baseOrders = filtros.pedidoEstado === "ENTREGADOS" ? vista.orders.entregados
    : filtros.pedidoEstado === "TODOS" ? vista.orders.generados
    : vista.orders.enProceso;
  const filteredOrders = baseOrders.filter((p) =>
    (!filtros.pedidoCodigo || p.codigo.toUpperCase().includes(filtros.pedidoCodigo.toUpperCase()))
    && (!filtros.pedidoDestino || p.codDestino === filtros.pedidoDestino));
  const noAtendidos = escenario.noAtendidos.filter((p) =>
    (!filtros.pedidoCodigo || p.codigo.toUpperCase().includes(filtros.pedidoCodigo.toUpperCase()))
    && (!filtros.pedidoDestino || p.codDestino === filtros.pedidoDestino));

  // ---------- Aeropuertos ----------
  const texto = filtros.aeropuertoTexto.toLowerCase();
  const visibleAirports = vista.airports.filter((ap) =>
    (!filtros.soloSedes || ap.esSede)
    && (!texto || (ap.code || "").toLowerCase().includes(texto) || (ap.city || "").toLowerCase().includes(texto)));

  const renderDetail = () => {
    if (!seleccionDetalle) return null;
    switch (selected.type) {
      case "airport":
        return <AirportInfoPanel airport={seleccionDetalle.airport} vuelosSaliendo={seleccionDetalle.salen} vuelosLlegando={seleccionDetalle.llegan} getOrdersForFlight={getOrdersForFlight} />;
      case "flight":
        return <FlightInfoPanel flight={seleccionDetalle} getOrdersForFlight={getOrdersForFlight} />;
      case "order":
        return <OrderInfoPanel order={seleccionDetalle} flights={escenario.flights} simNowMs={nowMs} />;
      case "route":
        return <RouteInfoPanel route={seleccionDetalle} />;
      default:
        return null;
    }
  };

  const Tab = ({ id, label, count }) => (
    <button className={`sidebar-tab ${tab === id ? "active" : ""}`} onClick={() => setTab(id)}>
      <span>{label}</span>
      <span className="sidebar-tab-badge">{count}</span>
    </button>
  );

  return (
    <aside className={`sidebar ${collapsed ? "collapsed" : ""}`}>
      <div className="sidebar-header">
        <span className="sidebar-title">{isDetailView ? "Detalle" : "MoraPack"}</span>
        <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
          <button
            className={`pin-button ${pinned ? "pin-button--on" : ""}`}
            onClick={() => setPinned(!pinned)}
            title={pinned ? "Desanclar" : "Fijar panel (abre el detalle al seleccionar)"}
          >
            {pinned ? "📌" : "📍"}
          </button>
          <img src={hideIcon} alt="Ocultar/Mostrar" className="hide-icon" onClick={() => setCollapsed(!collapsed)} />
        </div>
      </div>

      {!collapsed && (
        <div className="sidebar-content">
          {isDetailView ? (
            <div className="sidebar-detail-view">
              <button className="detail-back-btn" onClick={onClearSelection}>← Volver a listas</button>
              {renderDetail() ?? vacio("El elemento ya no está en el escenario.")}
            </div>
          ) : (
            <>
              <div className="sidebar-tabs">
                <Tab id="flights" label="Vuelos" count={vista.activeFlights.length} />
                <Tab id="orders" label="Pedidos" count={vista.orders.enProceso.length} />
                <Tab id="airports" label="Aeropuertos" count={visibleAirports.length} />
                <Tab id="routes" label="Rutas" count={vista.routesNow.length} />
              </div>

              {tab === "flights" && (
                <>
                  <div className="flight-toggle-row">
                    <button className={`flight-toggle-btn ${flightViewMode === "ACTIVE" ? "active" : ""}`} onClick={() => setFlightViewMode("ACTIVE")}>
                      Activos ({vista.activeFlights.length})
                    </button>
                    <button className={`flight-toggle-btn ${flightViewMode === "UPCOMING" ? "active" : ""}`} onClick={() => setFlightViewMode("UPCOMING")}>
                      Próximos ({vista.upcomingFlights.length})
                    </button>
                  </div>
                  <div className="filters-row">
                    <input type="text" className="filter-select" placeholder="Buscar código..." value={filtros.vueloCodigo} onChange={(e) => setFiltro("vueloCodigo", e.target.value)} />
                    <select className="filter-select" value={filtros.vueloOrigen} onChange={(e) => setFiltro("vueloOrigen", e.target.value)}>
                      <option value="">Origen</option>
                      {[...new Set(flightsToShow.map((f) => f.origin?.code).filter(Boolean))].sort().map((c) => <option key={c} value={c}>{getAirportLabel(c)}</option>)}
                    </select>
                    <select className="filter-select" value={filtros.vueloDestino} onChange={(e) => setFiltro("vueloDestino", e.target.value)}>
                      <option value="">Destino</option>
                      {[...new Set(flightsToShow.map((f) => f.destination?.code).filter(Boolean))].sort().map((c) => <option key={c} value={c}>{getAirportLabel(c)}</option>)}
                    </select>
                  </div>
                  <div className="active-flights-list">
                    {filteredFlights.length === 0
                      ? vacio(flightViewMode === "ACTIVE" ? "No hay vuelos activos" : "No hay vuelos próximos")
                      : filteredFlights.map((flight) => (
                        <div key={flight.code} className="flight-card" onClick={() => onFlightClick(flight)}>
                          <div className="flight-card-header">
                            <span className="flight-route">{flight.origin.city} ➝ {flight.destination.city}</span>
                            <span className="flight-code">{flight.code}</span>
                          </div>
                          <div className="flight-card-body">
                            <div className="flight-card-row">
                              <span className="flight-label">{flightViewMode === "ACTIVE" ? "Progreso" : "Salida"}</span>
                              <span className="flight-value">{flightViewMode === "ACTIVE" ? `${Math.round((flight.progress || 0) * 100)}%` : flight.startTime}</span>
                            </div>
                            <div className="flight-card-row">
                              <span className="flight-label">Carga</span>
                              <span className="flight-value">{flight.capacity} / {flight.planeCapacity}</span>
                            </div>
                          </div>
                        </div>
                      ))}
                  </div>
                </>
              )}

              {tab === "orders" && (
                <>
                  <div className="filters-row">
                    <input type="text" className="filter-select" placeholder="Buscar pedido..." value={filtros.pedidoCodigo} onChange={(e) => setFiltro("pedidoCodigo", e.target.value)} />
                    <select className="filter-select" value={filtros.pedidoDestino} onChange={(e) => setFiltro("pedidoDestino", e.target.value)}>
                      <option value="">Destino</option>
                      {[...new Set([...vista.orders.generados, ...escenario.noAtendidos].map((o) => o.codDestino))].sort().map((c) => <option key={c} value={c}>{getAirportLabel(c)}</option>)}
                    </select>
                    <select className="filter-select" value={filtros.pedidoEstado} onChange={(e) => setFiltro("pedidoEstado", e.target.value)}>
                      <option value="PENDIENTES">En proceso</option>
                      <option value="ENTREGADOS">Entregados</option>
                      <option value="TODOS">Todos</option>
                      <option value="NO_ATENDIDOS">No atendidos ({escenario.noAtendidos.length})</option>
                    </select>
                  </div>
                  <div className="orders-list">
                    {filtros.pedidoEstado === "NO_ATENDIDOS" ? (
                      noAtendidos.length === 0 ? vacio("Todos los pedidos fueron atendidos") : noAtendidos.map((p) => (
                        <div key={p.codigo} className="order-card order-card--fallido">
                          <div className="order-card-header">
                            <span className="order-code">{p.codigo}</span>
                            <span className="order-destination">{p.codDestino}</span>
                          </div>
                          <div className="order-card-body">
                            <div className="order-row">
                              <span className="order-label">Motivo: {p.motivo === "EVENTO" ? "Evento en un vuelo" : "Sin ruta a tiempo"}</span>
                              <span className="order-value">{p.cantidadSolicitada} u.</span>
                            </div>
                          </div>
                        </div>
                      ))
                    ) : filteredOrders.length === 0 ? vacio("No hay pedidos") : filteredOrders.map((pedido) => (
                      <div key={pedido.codigo} className="order-card" onClick={() => onOrderClick(pedido)}>
                        <div className="order-card-header">
                          <span className="order-code">{pedido.codigo}</span>
                          <span className="order-destination">{pedido.codDestino}</span>
                        </div>
                        <div className="order-card-body">
                          <div className="order-row">
                            <span className="order-label">Cliente: {pedido.codCliente}</span>
                            <span className="order-value">{pedido.cantidadSolicitada} u.</span>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                </>
              )}

              {tab === "airports" && (
                <div className="sidebar-section--airports">
                  <div className="filters-row">
                    <input type="text" className="filter-select" placeholder="Buscar código o ciudad..." value={filtros.aeropuertoTexto} onChange={(e) => setFiltro("aeropuertoTexto", e.target.value)} />
                  </div>
                  <div className="filters-row">
                    <label className="airports-toggle">
                      <input type="checkbox" checked={filtros.soloSedes} onChange={(e) => setFiltro("soloSedes", e.target.checked)} />
                      Mostrar solo sedes
                    </label>
                  </div>
                  <div className="orders-list">
                    {visibleAirports.map((ap) => (
                      <div key={ap.code} className={`order-card airport-card ${getAirportOccupancyClass(ap.ocupacion)}`} onClick={() => onAirportClick(ap)}>
                        <div className="order-card-header">
                          <span className="order-code">{ap.code}</span>
                          <span>{ap.city}</span>
                        </div>
                        <div className="order-card-body">
                          <div className="order-row">
                            <span className="order-label">{ap.esSede ? "Sede principal" : "Intermedio"}</span>
                            <span className="order-value" style={{ fontSize: "11px" }}>
                              Stock: {ap.esSede ? "∞" : `${ap.stockActual} / ${ap.capacidad}`}
                            </span>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {tab === "routes" && (
                <div className="routes-list">
                  {vista.routesNow.length === 0 ? vacio("No hay rutas con vuelos en el aire") : vista.routesNow.map((ruta) => (
                    <div key={ruta.codigo} className="route-card" onClick={() => onRouteClick(ruta)}>
                      <div className="route-card-header">
                        <span className="route-code">{ruta.codigo}</span>
                        <span className="route-type" style={{ fontSize: "10px" }}>{ruta.tipo}</span>
                      </div>
                      <div className="route-card-body">
                        <div className="route-row">
                          <span className="route-label">{ruta.codOrigen} ➝ {ruta.codDestino}</span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </>
          )}
        </div>
      )}
    </aside>
  );
}
