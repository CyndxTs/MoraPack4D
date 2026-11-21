import React, { useCallback, useMemo, useRef, useState } from "react";
import ScenarioSidebar from "./ScenarioSidebar";
import ScenarioMap from "./ScenarioMap";
import { FleetOverlay, LegendOverlay } from "./MapOverlays";
import { calcularVista, vuelosDeAeropuerto } from "./vista";
import { mapaDeColoresDeSedes } from "./mapIcons";
import "./scenario.scss";

const FILTROS_INICIALES = {
  vueloCodigo: "", vueloOrigen: "", vueloDestino: "",
  pedidoCodigo: "", pedidoDestino: "", pedidoEstado: "PENDIENTES",
  aeropuertoTexto: "", soloSedes: false,
};

/**
 * Mapa + panel lateral + overlays, común a Operación y Simulación.
 * La página solo aporta el escenario, el instante actual y sus controles.
 *
 *  - controles : contenido del panel "Mostrar controles"
 *  - overlays  : elementos extra sobre el mapa (alertas, reporte, botones)
 *  - mostrarVuelos : false antes de que empiece la simulación
 *  - children : modales de la página (quedan dentro de .scenario-root para tomar sus estilos)
 */
export default function ScenarioWorkspace({ escenario, nowMs, controles, overlays, mostrarVuelos = true, mostrarFlota = true, children }) {
  const mapRef = useRef(null);
  const [collapsed, setCollapsed] = useState(true);
  const [pinned, setPinned] = useState(false);
  const [tab, setTab] = useState("flights");
  const [selected, setSelected] = useState(null); // { type, code }
  const [highlightedFlights, setHighlightedFlights] = useState([]);
  const [highlightedAirportCode, setHighlightedAirportCode] = useState(null);
  const [openTooltip, setOpenTooltip] = useState(null);
  const [filtros, setFiltros] = useState(FILTROS_INICIALES);

  const vista = useMemo(() => calcularVista(escenario, nowMs), [escenario, nowMs]);
  const hubColorMap = useMemo(() => mapaDeColoresDeSedes(escenario.airports), [escenario.airports]);

  // Los resaltados de vuelos que ya aterrizaron desaparecen solos
  const activos = useMemo(() => new Set(vista.activeFlights.map((f) => f.code)), [vista.activeFlights]);
  const resaltadosVigentes = useMemo(() => highlightedFlights.filter((c) => activos.has(c)), [highlightedFlights, activos]);

  const setFiltro = useCallback((k, v) => setFiltros((f) => ({ ...f, [k]: v })), []);

  const getAirportLabel = useCallback((code) => {
    const ap = escenario.airports[code];
    return ap ? `${code} - ${ap.city}` : code || "";
  }, [escenario.airports]);

  const getOrdersForFlight = useCallback((code) => escenario.lotesPorVuelo.get(code) ?? [], [escenario.lotesPorVuelo]);
  const vuelosDe = useCallback((code) => vuelosDeAeropuerto(escenario, code, nowMs), [escenario, nowMs]);

  const centrar = (lat, lng, zoom) => mapRef.current?.setView([lat, lng], zoom, { animate: true });

  const limpiar = () => {
    setSelected(null);
    setHighlightedFlights([]);
    setHighlightedAirportCode(null);
    setFiltro("vueloCodigo", "");
  };

  const onMapClick = () => {
    limpiar();
    setOpenTooltip(null);
  };

  // Desde el mapa: siempre abre el detalle
  const seleccionarEnMapa = (type, code) => {
    setCollapsed(false);
    setSelected({ type, code });
  };

  // Desde el panel: resalta en el mapa; abre el detalle solo si el panel está fijado
  const seleccionarEnPanel = (type, code) => {
    if (pinned) {
      setCollapsed(false);
      setSelected({ type, code });
    } else {
      setCollapsed(true);
      setSelected(null);
    }
  };

  const vuelosDePedido = (pedido) => [...new Set(pedido.segmentaciones.flatMap((s) => s.lotes.flatMap((l) => l.vuelos || [])))];

  const handlers = {
    flightFromMap: (f) => {
      seleccionarEnMapa("flight", f.code);
      setFiltro("vueloCodigo", f.code);
      setHighlightedFlights([f.code]);
      setHighlightedAirportCode(null);
      const p = f.position || f.origin;
      if (p) centrar(p.lat, p.lng, 5);
    },
    airportFromMap: (ap) => {
      seleccionarEnMapa("airport", ap.code);
      setHighlightedAirportCode(ap.code);
      setHighlightedFlights([]);
      centrar(ap.lat, ap.lng, 6);
    },
    flightFromPanel: (f) => {
      seleccionarEnPanel("flight", f.code);
      setFiltro("vueloCodigo", f.code);
      setHighlightedFlights([f.code]);
      setHighlightedAirportCode(null);
      const p = f.position || f.origin;
      if (p) centrar(p.lat, p.lng, 5);
    },
    airportFromPanel: (ap) => {
      seleccionarEnPanel("airport", ap.code);
      setHighlightedAirportCode(ap.code);
      setHighlightedFlights([]);
      centrar(ap.lat, ap.lng, 6);
    },
    orderFromPanel: (pedido) => {
      seleccionarEnPanel("order", pedido.codigo);
      setHighlightedAirportCode(null);
      setHighlightedFlights(vuelosDePedido(pedido));
    },
    routeFromPanel: (ruta) => {
      seleccionarEnPanel("route", ruta.codigo);
      setHighlightedAirportCode(null);
      setHighlightedFlights(ruta.codVuelos || []);
      const o = escenario.airports[ruta.codOrigen];
      if (o) centrar(o.lat, o.lng, 4);
    },
  };

  // Objeto a mostrar en el detalle (se recalcula con cada tick para que el progreso/stock esté al día)
  const seleccionDetalle = useMemo(() => {
    if (!selected) return null;
    switch (selected.type) {
      case "airport": {
        const airport = vista.airports.find((a) => a.code === selected.code);
        return airport ? { airport, ...vuelosDe(selected.code) } : null;
      }
      case "flight":
        return vista.activeFlights.find((f) => f.code === selected.code) ?? escenario.flights.find((f) => f.code === selected.code) ?? null;
      case "order":
        return escenario.orders.find((o) => o.codigo === selected.code) ?? null;
      case "route":
        return escenario.routes.find((r) => r.codigo === selected.code) ?? null;
      default:
        return null;
    }
  }, [selected, vista, escenario, vuelosDe]);

  return (
    <div className="scenario-root">
    <div className="page">
      <ScenarioSidebar
        collapsed={collapsed} setCollapsed={setCollapsed}
        pinned={pinned} setPinned={setPinned}
        tab={tab} setTab={setTab}
        nowMs={nowMs} escenario={escenario} vista={vista}
        selected={selected} onClearSelection={limpiar} seleccionDetalle={seleccionDetalle}
        filtros={filtros} setFiltro={setFiltro}
        onFlightClick={handlers.flightFromPanel}
        onOrderClick={handlers.orderFromPanel}
        onAirportClick={handlers.airportFromPanel}
        onRouteClick={handlers.routeFromPanel}
        getAirportLabel={getAirportLabel}
        getOrdersForFlight={getOrdersForFlight}
      />

      <section className="contenido">
        <div className="map-and-info">
          {controles}
          <div className="map-wrapper">
            <ScenarioMap
              mapRef={mapRef}
              airports={vista.airports}
              activeFlights={vista.activeFlights}
              mostrarVuelos={mostrarVuelos}
              hubColorMap={hubColorMap}
              highlightedFlights={resaltadosVigentes}
              highlightedAirportCode={highlightedAirportCode}
              openTooltip={openTooltip}
              setOpenTooltip={setOpenTooltip}
              vuelosDeAeropuerto={vuelosDe}
              getOrdersForFlight={getOrdersForFlight}
              onAirportClick={handlers.airportFromMap}
              onFlightClick={handlers.flightFromMap}
              onMapClick={onMapClick}
            />
            <LegendOverlay />
            {mostrarFlota && <FleetOverlay activos={vista.activeFlights.length} fleet={vista.fleet} />}
            {overlays}
          </div>
        </div>
      </section>
    </div>
    {children}
    </div>
  );
}
