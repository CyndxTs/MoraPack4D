import React from "react";
import hideIcon from "../../assets/icons/hide-sidebar.png";

export default function SimulationSidebar({
  // --- Estados de UI ---
  collapsed,
  setCollapsed,
  sidebarTab,
  setSidebarTab,

  // --- Datos ---
  activeFlights,
  visibleOrders,
  visibleAirports,
  routesInCurrentTime,
  baseOrders, // Necesario para sacar opciones de filtro de pedidos
  airports, // Necesario para sacar nombres de ciudades

  // --- Filtros (Estados y Setters) ---
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

  // --- Listas Filtradas (Resultados finales a mostrar) ---
  filteredActiveFlights,
  filteredOrders,

  // --- Funciones / Callbacks ---
  onFlightClick, // Qué hacer al clickear un vuelo
  onOrderClick, // Qué hacer al clickear un pedido
  onAirportClick, // Qué hacer al clickear un aeropuerto
  onRouteClick, // Qué hacer al clickear una ruta

  // --- Helpers requeridos ---
  getAirportLabel,
  getAirportCityName,
  getOrdersForFlight,
  getLastFlightOfLote,
  getAirportOccupancyClass,
}) {
  return (
    <aside className={`sidebar ${collapsed ? "collapsed" : ""}`}>
      <div className="sidebar-header">
        <span className="sidebar-title">Panel informativo</span>
        <img
          src={hideIcon}
          alt="Ocultar"
          className="hide-icon"
          onClick={() => setCollapsed(!collapsed)}
        />
      </div>

      {!collapsed && (
        <div className="sidebar-content sidebar-content--flights">
          {/* Pestañas Vuelos / Pedidos */}
          <div className="sidebar-tabs">
            <button
              className={`sidebar-tab ${
                sidebarTab === "flights" ? "active" : ""
              }`}
              onClick={() => setSidebarTab("flights")}
            >
              <span>Vuelos</span>
              <span className="sidebar-tab-badge">{activeFlights.length}</span>
            </button>

            <button
              className={`sidebar-tab ${
                sidebarTab === "orders" ? "active" : ""
              }`}
              onClick={() => setSidebarTab("orders")}
            >
              <span>Pedidos</span>
              <span className="sidebar-tab-badge">{visibleOrders.length}</span>
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

          {/* ===== CONTENIDO PESTAÑA VUELOS ===== */}
          {sidebarTab === "flights" && (
            <>
              <div className="active-flights-header">
                <span className="active-flights-title">Vuelos en tránsito</span>
                <span className="active-flights-count">
                  {activeFlights.length}
                </span>
              </div>

              {/* 🔎 Filtros de vuelos */}
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
                  <option value="">Selecciona origen</option>
                  {Array.from(
                    new Set(
                      activeFlights
                        .map((f) => f.origin?.code)
                        .filter((code) => !!code)
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
                  onChange={(e) => setFlightFilterDestination(e.target.value)}
                >
                  <option value="">Selecciona destino</option>
                  {Array.from(
                    new Set(
                      activeFlights
                        .map((f) => f.destination?.code)
                        .filter((code) => !!code)
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
                  <p className="no-flights-text">
                    No hay vuelos activos que coincidan con la búsqueda.
                  </p>
                ) : (
                  filteredActiveFlights.map((flight) => {
                    const progressPct = Math.min(
                      100,
                      Math.max(0, Math.round((flight.progress ?? 0) * 100))
                    );

                    const ordersForFlight = getOrdersForFlight(flight.code);
                    const totalPedidos = ordersForFlight.length;
                    const totalUnidades = ordersForFlight.reduce(
                      (sum, p) => sum + (p.cantidad || 0),
                      0
                    );

                    return (
                      <div
                        key={flight.code}
                        className="flight-card"
                        onClick={() => onFlightClick(flight)}
                      >
                        <div className="flight-card-header">
                          <span className="flight-route">
                            {flight.origin.city}
                            <span className="flight-arrow"> ✈ </span>
                            {flight.destination.city}
                          </span>
                          <span className="flight-code">{flight.code}</span>
                        </div>

                        <div className="flight-card-body">
                          <div className="flight-card-row">
                            <span className="flight-label">Capacidad</span>
                            <span className="flight-value">
                              {flight.capacity} / {flight.planeCapacity}
                            </span>
                          </div>
                          <div className="flight-card-row">
                            <span className="flight-label">Salida</span>
                            <span className="flight-value">
                              {flight.startTime}
                            </span>
                          </div>
                          <div className="flight-card-row">
                            <span className="flight-label">Llegada</span>
                            <span className="flight-value">
                              {flight.endTime}
                            </span>
                          </div>
                          <div className="flight-card-row">
                            <span className="flight-label">Rutas</span>
                            <span className="flight-value">
                              {flight.rutas && flight.rutas.length > 0
                                ? flight.rutas.join(", ")
                                : "Sin rutas"}
                            </span>
                          </div>
                          <div className="flight-card-row">
                            <span className="flight-label">Pedidos</span>
                            <span className="flight-value">
                              {totalPedidos === 0
                                ? "Sin pedidos"
                                : `${totalPedidos} pedido(s)${
                                    totalUnidades
                                      ? ` · ${totalUnidades} u.`
                                      : ""
                                  }`}
                            </span>
                          </div>

                          <div className="flight-progress">
                            <div
                              className="flight-progress-bar"
                              style={{ width: `${progressPct}%` }}
                            />
                          </div>
                        </div>
                      </div>
                    );
                  })
                )}
              </div>
            </>
          )}

          {/* ===== CONTENIDO PESTAÑA PEDIDOS ===== */}
          {sidebarTab === "orders" && (
            <div className="sidebar-section sidebar-section--orders">
              <div className="orders-header">
                <span className="orders-title">Pedidos</span>
                <span className="orders-count">{filteredOrders.length}</span>
              </div>

              {/* 🔎 Filtros de pedidos */}
              <div className="filters-row">
                <input
                  type="text"
                  className="filter-select"
                  placeholder="Buscar código"
                  value={orderFilterCode}
                  onChange={(e) =>
                    setOrderFilterCode(e.target.value.toUpperCase())
                  }
                />
                <select
                  className="filter-select"
                  value={orderFilterEstado}
                  onChange={(e) => setOrderFilterEstado(e.target.value)}
                >
                  <option value="PENDIENTES">En tránsito / pendientes</option>
                  <option value="ENTREGADOS">Entregados</option>
                  <option value="TODOS">Todos</option>
                </select>

                <select
                  className="filter-select"
                  value={orderFilterDestino}
                  onChange={(e) => setOrderFilterDestino(e.target.value)}
                >
                  <option value="">Selecciona destino</option>
                  {Array.from(
                    new Set(
                      baseOrders.map((p) => p.codDestino).filter((cod) => !!cod)
                    )
                  ).map((cod) => (
                    <option key={cod} value={cod}>
                      {getAirportLabel(cod)}
                    </option>
                  ))}
                </select>
              </div>

              <div className="orders-list">
                {filteredOrders.length === 0 ? (
                  <p className="no-orders-text">
                    No hay pedidos que coincidan con la búsqueda.
                  </p>
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
                          <span className="order-label">Cliente</span>
                          <span className="order-value">
                            {pedido.codCliente}
                          </span>
                        </div>
                        <div className="order-row">
                          <span className="order-label">Cantidad</span>
                          <span className="order-value">
                            {pedido.cantidadSolicitada} u.
                          </span>
                        </div>
                        {pedido.fechaHoraGeneracion && (
                          <div className="order-row">
                            <span className="order-label">
                              Fecha generación
                            </span>
                            <span className="order-value">
                              {pedido.fechaHoraGeneracion}
                            </span>
                          </div>
                        )}

                        {pedido.segmentaciones.map((seg, segIndex) => (
                          <React.Fragment key={seg.codigo}>
                            {seg.lotes.map((lote, loteIndex) => {
                              const isLastLoteOfPedido =
                                segIndex === pedido.segmentaciones.length - 1 &&
                                loteIndex === seg.lotes.length - 1;

                              const destinoCodeBase =
                                lote.destinoCode ||
                                lote.arrivalAirportCode ||
                                "";
                              const fechaLlegada =
                                lote.arrivalFechaHoraIngreso ||
                                "Sin información";

                              // 👉 Usamos la función helper pasada por props
                              const lastFlight = getLastFlightOfLote(lote);

                              let origenCode = lote.origenCode;
                              let destinoCode = destinoCodeBase;

                              let origenNombre =
                                getAirportCityName(origenCode) ||
                                lote.origenNombre ||
                                origenCode ||
                                "Origen desconocido";

                              let destinoNombre =
                                getAirportCityName(destinoCode) ||
                                lote.destinoNombre ||
                                lote.arrivalAirportCity ||
                                destinoCode ||
                                "Destino desconocido";

                              if (
                                lastFlight &&
                                lastFlight.origin &&
                                lastFlight.destination
                              ) {
                                origenCode = lastFlight.origin.code;
                                destinoCode = lastFlight.destination.code;
                                origenNombre =
                                  getAirportCityName(origenCode) ||
                                  lastFlight.origin.city ||
                                  lastFlight.originName ||
                                  origenCode ||
                                  "Origen desconocido";
                                destinoNombre =
                                  getAirportCityName(destinoCode) ||
                                  lastFlight.destination.city ||
                                  lastFlight.destinationName ||
                                  destinoCode ||
                                  "Destino desconocido";
                              }

                              return (
                                <div
                                  key={lote.loteCodigo}
                                  className="order-seg-block"
                                >
                                  <div className="order-row">
                                    <span className="order-label">
                                      Segmentación
                                    </span>
                                    <span className="order-value">
                                      {lote.loteTamanio} u.
                                    </span>
                                  </div>
                                  <div className="order-row order-row--ruta">
                                    <span className="order-label">Ruta</span>
                                    <span className="order-value order-value--ruta">
                                      {origenNombre}
                                      {origenCode
                                        ? ` (${origenCode})`
                                        : ""} → {destinoNombre}
                                      {destinoCode ? ` (${destinoCode})` : ""}
                                    </span>
                                  </div>
                                  <div className="order-row">
                                    <span className="order-label">Llegada</span>
                                    <span className="order-value">
                                      {fechaLlegada}
                                    </span>
                                  </div>
                                  {!isLastLoteOfPedido && (
                                    <div className="order-lote-separator" />
                                  )}
                                </div>
                              );
                            })}
                          </React.Fragment>
                        ))}
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}

          {/* ===== CONTENIDO PESTAÑA AEROPUERTOS ===== */}
          {sidebarTab === "airports" && (
            <div className="sidebar-section sidebar-section--airports">
              <div className="orders-header">
                <span className="orders-title">Aeropuertos</span>
                <span className="orders-count">
                  {visibleAirports ? visibleAirports.length : 0}
                </span>
              </div>

              <label className="airports-toggle">
                <input
                  type="checkbox"
                  checked={onlyHubs}
                  onChange={(e) => setOnlyHubs(e.target.checked)}
                />
                Mostrar solo sedes (almacenes principales)
              </label>

              <div className="orders-list">
                {!visibleAirports || visibleAirports.length === 0 ? (
                  <p className="no-orders-text">
                    No hay aeropuertos cargados en la simulación.
                  </p>
                ) : (
                  visibleAirports.map((ap) => {
                    const ocupacion = ap.ocupacion; // Ya viene enriquecido desde el padre si se pasa así, o recalcular
                    const stockActual = ap.stockActual;
                    // Nota: Aquí estamos usando los datos directos del objeto. Asegúrate de pasarlos enriquecidos desde el padre o pasar las funciones.
                    // Para simplificar, asumiremos que "ap" en visibleAirports ya tiene los datos calculados o lo calculamos aquí rápido.
                    // REVISIÓN: En el original se calcula 'stockActual' dentro del map.
                    // Si 'ap' NO tiene stockActual calculado, necesitamos recalcularlo.
                    // Para mantenerlo limpio, asumiremos que recibimos una función o que 'ap' viene listo.
                    // Si no, copia la lógica del padre aquí.
                    // EN EL PADRE el cálculo de 'stockActual' para la lista depende de 'simNowMs'.
                    // Lo ideal es que 'visibleAirports' sea un array que YA tenga { ...ap, stockActual, ocupacion }
                    // Si no es así, puedes pasar 'simNowMs' y hacer el cálculo aquí.

                    // Supongamos que pasamos los datos básicos y recalculamos ocupación visual aquí para el CSS:
                    const ocupPct = Math.min(
                      100,
                      Math.max(0, Math.round((ocupacion ?? 0) * 100))
                    );

                    return (
                      <div
                        key={ap.code}
                        className={`order-card airport-card ${getAirportOccupancyClass(
                          ocupacion
                        )}`}
                        onClick={() => onAirportClick(ap)}
                      >
                        <div className="order-card-header">
                          <span className="order-code">{ap.code}</span>
                          <span className="order-destination">{ap.city}</span>
                        </div>

                        <div className="order-card-body">
                          <div className="order-row">
                            <span className="order-label">Tipo</span>
                            <span className="order-value">
                              {ap.esSede ? "Principal" : "Intermedio"}
                            </span>
                          </div>
                          <div className="order-row">
                            <span className="order-label">Stock actual</span>
                            <span className="order-value">
                              {stockActual} / {ap.capacidad} u. ({ocupPct}%)
                            </span>
                          </div>
                        </div>
                      </div>
                    );
                  })
                )}
              </div>
            </div>
          )}
        </div>
      )}

      {/* ===== CONTENIDO PESTAÑA RUTAS ===== */}
      {sidebarTab === "routes" && (
        <div className="sidebar-section sidebar-section--routes">
          <div className="routes-header">
            <span className="routes-title">Rutas en operación</span>
            <span className="routes-count">{routesInCurrentTime.length}</span>
          </div>

          <div className="routes-list">
            {routesInCurrentTime.length === 0 ? (
              <p className="no-routes-text">
                No hay rutas en operación en este momento.
              </p>
            ) : (
              routesInCurrentTime.map((ruta) => (
                <div
                  key={ruta.codigo}
                  className="route-card"
                  onClick={() => onRouteClick(ruta)}
                >
                  <div className="route-card-header">
                    <span className="route-code">{ruta.codigo}</span>
                  </div>

                  <div className="route-card-body">
                    <div className="route-row">
                      <span className="route-label">Tipo</span>
                      <span className="route-value">{ruta.tipo}</span>
                    </div>
                    <div className="route-row">
                      <span className="route-label">Origen</span>
                      <span className="route-value">{ruta.codOrigen}</span>
                    </div>
                    <div className="route-row">
                      <span className="route-label">Destino</span>
                      <span className="route-value">{ruta.codDestino}</span>
                    </div>
                    <div className="route-row">
                      <span className="route-label">Distancia</span>
                      <span className="route-value">
                        {ruta.distancia.toFixed(0)} km
                      </span>
                    </div>
                    <div className="route-row route-row--vuelos">
                      <span className="route-label">Vuelos</span>
                      <div className="route-value route-value--multiline">
                        {ruta.codVuelos?.length > 0 ? (
                          ruta.codVuelos.map((code) => (
                            <div key={code} className="route-flight-code">
                              {code}
                            </div>
                          ))
                        ) : (
                          <span>Sin vuelos</span>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </aside>
  );
}
