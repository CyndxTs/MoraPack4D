import React from "react";
import hideIcon from "../../assets/icons/hide-sidebar.png";
import {
  AirportInfoPanel,
  FlightInfoPanel,
  OrderInfoPanel,
  RouteInfoPanel,
} from "./InfoPanels";

export default function SimulationSidebar({
  // --- Estados de UI ---
  collapsed,
  setCollapsed,
  sidebarTab,
  setSidebarTab,
  airportFilterText,
  setAirportFilterText,

  // --- Datos ---
  activeFlights,
  visibleOrders,
  visibleAirports,
  routesInCurrentTime,
  baseOrders,
  airports,
  flights,

  // --- Selección ---
  selectedItem,
  selectedAirport,
  onClearSelection,

  // --- Detalles ---
  vuelosSaliendo,
  vuelosLlegando,

  // --- Filtros ---
  flightFilterCode,
  setFlightFilterCode,
  flightFilterOrigin,
  setFlightFilterOrigin,
  flightFilterDestination,
  setFlightFilterDestination,
  orderFilterCode,
  setOrderFilterCode,
  orderFilterEstado,
  setOrderFilterEstado,
  orderFilterDestino,
  setOrderFilterDestino,
  onlyHubs,
  setOnlyHubs,
  filteredActiveFlights,
  filteredOrders,

  isSidebarPinned,
  setIsSidebarPinned,
  simNowMs,
  // --- Callbacks ---
  onFlightClick,
  onOrderClick,
  onAirportClick,
  onRouteClick,

  // --- Helpers ---
  getAirportLabel,
  getAirportCityName,
  getOrdersForFlight,
  getAirportOccupancyClass,
}) {
  const isDetailView = !!(selectedItem || selectedAirport);

  // Renderizador del contenido de detalle
  const renderDetailContent = () => {
    if (selectedAirport) {
      return (
        <AirportInfoPanel
          airport={selectedAirport}
          vuelosSaliendo={vuelosSaliendo}
          vuelosLlegando={vuelosLlegando}
          getOrdersForFlight={getOrdersForFlight}
        />
      );
    }
    if (selectedItem?.type === "flight") {
      const flightObj = flights.find((f) => f.code === selectedItem.codigo);
      return (
        <FlightInfoPanel
          flight={flightObj}
          getOrdersForFlight={getOrdersForFlight}
        />
      );
    }
    if (selectedItem?.type === "order") {
      const orderObj = baseOrders.find((o) => o.codigo === selectedItem.codigo);
      return (
        <OrderInfoPanel
          order={orderObj}
          flights={flights}
          simNowMs={simNowMs}
        />
      );
    }
    if (selectedItem?.type === "route") {
      const routeObj = routesInCurrentTime.find(
        (r) => r.codigo === selectedItem.codigo
      );
      return <RouteInfoPanel route={routeObj} />;
    }
    return null;
  };

  return (
    <aside className={`sidebar ${collapsed ? "collapsed" : ""}`}>
      {/* HEADER DEL SIDEBAR (CORREGIDO: Sin duplicar el div) */}
      <div className="sidebar-header">
        <span className="sidebar-title">
          {isDetailView ? "Detalle" : "MoraPack"}
        </span>

        <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
          {/* BOTÓN PIN (Se ocultará solo con CSS cuando esté colapsado) */}
          <button
            className="pin-button"
            onClick={() => setIsSidebarPinned(!isSidebarPinned)}
            title={isSidebarPinned ? "Desanclar" : "Fijar panel"}
            style={{
              background: isSidebarPinned ? "#e0f2fe" : "transparent",
              border: "none",
              cursor: "pointer",
              fontSize: "14px",
              padding: "6px",
              borderRadius: "6px",
              color: isSidebarPinned ? "#0284c7" : "#64748b",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              transition: "all 0.2s",
            }}
          >
            {isSidebarPinned ? "📌" : "📍"}
          </button>

          <img
            src={hideIcon}
            alt="Ocultar/Mostrar"
            className="hide-icon"
            onClick={() => setCollapsed(!collapsed)}
            style={{
              // La rotación ya la maneja la clase CSS .sidebar.collapsed .hide-icon
              transition: "transform 0.3s",
            }}
          />
        </div>
      </div>

      {/* CONTENIDO (Solo visible si no está colapsado) */}
      {!collapsed && (
        <div className="sidebar-content">
          {isDetailView ? (
            <div className="sidebar-detail-view">
              <button className="detail-back-btn" onClick={onClearSelection}>
                ← Volver a listas
              </button>
              {renderDetailContent()}
            </div>
          ) : (
            <>
              {/* TABS */}
              <div className="sidebar-tabs">
                <button
                  className={`sidebar-tab ${
                    sidebarTab === "flights" ? "active" : ""
                  }`}
                  onClick={() => setSidebarTab("flights")}
                >
                  <span>Vuelos</span>
                  <span className="sidebar-tab-badge">
                    {activeFlights.length}
                  </span>
                </button>
                <button
                  className={`sidebar-tab ${
                    sidebarTab === "orders" ? "active" : ""
                  }`}
                  onClick={() => setSidebarTab("orders")}
                >
                  <span>Pedidos</span>
                  <span className="sidebar-tab-badge">
                    {visibleOrders.length}
                  </span>
                </button>
                <button
                  className={`sidebar-tab ${
                    sidebarTab === "airports" ? "active" : ""
                  }`}
                  onClick={() => setSidebarTab("airports")}
                >
                  <span>Aeropuertos</span>
                  <span className="sidebar-tab-badge">
                    {visibleAirports ? visibleAirports.length : 0}
                  </span>
                </button>
                <button
                  className={`sidebar-tab ${
                    sidebarTab === "routes" ? "active" : ""
                  }`}
                  onClick={() => setSidebarTab("routes")}
                >
                  <span>Rutas</span>
                  <span className="sidebar-tab-badge">
                    {routesInCurrentTime.length}
                  </span>
                </button>
              </div>

              {/* === VUELOS === */}
              {sidebarTab === "flights" && (
                <>
                  <div className="filters-row">
                    <input
                      type="text"
                      className="filter-select"
                      placeholder="Buscar código..."
                      value={flightFilterCode}
                      onChange={(e) => setFlightFilterCode(e.target.value)}
                    />
                    <select
                      className="filter-select"
                      value={flightFilterOrigin}
                      onChange={(e) => setFlightFilterOrigin(e.target.value)}
                    >
                      <option value="">Origen</option>
                      {Array.from(
                        new Set(
                          activeFlights
                            .map((f) => f.origin?.code)
                            .filter(Boolean)
                        )
                      ).map((code) => (
                        <option key={code} value={code}>
                          {getAirportLabel(code)}
                        </option>
                      ))}
                    </select>
                    <select
                      className="filter-select"
                      value={flightFilterDestination}
                      onChange={(e) =>
                        setFlightFilterDestination(e.target.value)
                      }
                    >
                      <option value="">Destino:</option>
                      {Array.from(
                        new Set(
                          activeFlights
                            .map((f) => f.destination?.code)
                            .filter(Boolean)
                        )
                      ).map((code) => (
                        <option key={code} value={code}>
                          {getAirportLabel(code)}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div className="active-flights-list">
                    {filteredActiveFlights.length === 0 ? (
                      <div
                        style={{
                          padding: "20px",
                          textAlign: "center",
                          color: "#999",
                        }}
                      >
                        No hay vuelos activos
                      </div>
                    ) : (
                      filteredActiveFlights.map((flight) => (
                        <div
                          key={flight.code}
                          className="flight-card"
                          onClick={() => onFlightClick(flight)}
                        >
                          <div className="flight-card-header">
                            <span className="flight-route">
                              {flight.origin.city} ➝ {flight.destination.city}
                            </span>
                            <span className="flight-code">{flight.code}</span>
                          </div>
                          <div className="flight-card-body">
                            <div className="flight-card-row">
                              <span className="flight-label">Progreso</span>
                              <span className="flight-value">
                                {Math.round((flight.progress || 0) * 100)}%
                              </span>
                            </div>
                            <div className="flight-card-row">
                              <span className="flight-label">Carga</span>
                              <span className="flight-value">
                                {flight.capacity} / {flight.planeCapacity}
                              </span>
                            </div>
                          </div>
                        </div>
                      ))
                    )}
                  </div>
                </>
              )}

              {/* === PEDIDOS === */}
              {sidebarTab === "orders" && (
                <>
                  <div className="filters-row">
                    <input
                      type="text"
                      className="filter-select"
                      placeholder="Buscar pedido..."
                      value={orderFilterCode}
                      onChange={(e) => setOrderFilterCode(e.target.value)}
                    />
                    <select
                      className="filter-select"
                      value={orderFilterDestino}
                      onChange={(e) => setOrderFilterDestino(e.target.value)}
                    >
                      <option value="">Destino</option>
                      {Array.from(new Set(baseOrders.map((o) => o.codDestino)))
                        .sort()
                        .map((code) => (
                          <option key={code} value={code}>
                            {getAirportLabel(code)}
                          </option>
                        ))}
                    </select>
                    <select
                      className="filter-select"
                      value={orderFilterEstado}
                      onChange={(e) => setOrderFilterEstado(e.target.value)}
                    >
                      <option value="PENDIENTES">En proceso</option>
                      <option value="ENTREGADOS">Entregados</option>
                      <option value="TODOS">Todos</option>
                    </select>
                  </div>
                  <div className="orders-list">
                    {filteredOrders.length === 0 ? (
                      <div
                        style={{
                          padding: "20px",
                          textAlign: "center",
                          color: "#999",
                        }}
                      >
                        No hay pedidos
                      </div>
                    ) : (
                      filteredOrders.map((pedido) => (
                        <div
                          key={pedido.codigo}
                          className="order-card"
                          onClick={() => onOrderClick(pedido)}
                        >
                          <div className="order-card-header">
                            <span className="order-code">{pedido.codigo}</span>
                            <span className="order-destination">
                              {pedido.codDestino}
                            </span>
                          </div>
                          <div className="order-card-body">
                            <div className="order-row">
                              <span className="order-label">
                                Cliente: {pedido.codCliente}
                              </span>
                              <span className="order-value">
                                {pedido.cantidadSolicitada} u.
                              </span>
                            </div>
                          </div>
                        </div>
                      ))
                    )}
                  </div>
                </>
              )}

              {/* === AEROPUERTOS === */}
              {sidebarTab === "airports" && (
                <div className="sidebar-section--airports">
                  <div className="filters-row">
                    <input
                      type="text"
                      className="filter-select"
                      placeholder="Buscar código o ciudad..."
                      value={airportFilterText}
                      onChange={(e) => setAirportFilterText(e.target.value)}
                    />
                  </div>
                  <div className="filters-row">
                    <label className="airports-toggle">
                      <input
                        type="checkbox"
                        checked={onlyHubs}
                        onChange={(e) => setOnlyHubs(e.target.checked)}
                      />
                      Mostrar solo sedes
                    </label>
                  </div>
                  <div className="orders-list">
                    {visibleAirports &&
                      visibleAirports.map((ap) => (
                        <div
                          key={ap.code}
                          className={`order-card airport-card ${getAirportOccupancyClass(
                            ap.ocupacion
                          )}`}
                          onClick={() => onAirportClick(ap)}
                        >
                          <div className="order-card-header">
                            <span className="order-code">{ap.code}</span>
                            <span>{ap.city}</span>
                          </div>
                          <div className="order-card-body">
                            <div className="order-row">
                              <span className="order-label">
                                {ap.esSede ? "Sede Principal" : "Intermedio"}
                              </span>
                              <span
                                className="order-value"
                                style={{ fontSize: "11px" }}
                              >
                                Stock: {ap.stockActual} / {ap.capacidad}
                              </span>
                            </div>
                          </div>
                        </div>
                      ))}
                  </div>
                </div>
              )}

              {/* === RUTAS === */}
              {sidebarTab === "routes" && (
                <div className="routes-list">
                  {routesInCurrentTime.map((ruta) => (
                    <div
                      key={ruta.codigo}
                      className="route-card"
                      onClick={() => onRouteClick(ruta)}
                    >
                      <div className="route-card-header">
                        <span className="route-code">{ruta.codigo}</span>
                        <span
                          className="route-type"
                          style={{ fontSize: "10px" }}
                        >
                          {ruta.tipo}
                        </span>
                      </div>
                      <div className="route-card-body">
                        <div className="route-row">
                          <span className="route-label">
                            {ruta.codOrigen} ➝ {ruta.codDestino}
                          </span>
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
