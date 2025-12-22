import React, { useState, useEffect, useRef, useMemo } from "react";
import L from "leaflet";
import {
  MapContainer,
  TileLayer,
  Marker,
  Polyline,
  useMapEvent,
  Tooltip,
} from "react-leaflet";
import "leaflet/dist/leaflet.css";

// Servicios y Tipos
import { listarParametros } from "../../services/parametrosService";
import { listarAeropuertos } from "../../services/aeropuertoService";
import {
  connectSimulatorWS,
  sendSimulationRequest,
  sendStopSimulation,
  disconnectWS,
  subscribeToSimulation,
  downloadExportationFile,
  getExportationPreview,
  deleteExportationFile,
} from "../../services/planificarService";

// Componentes UI
import {
  Notification,
  ButtonAdd,
  DateTimeInline,
  Dropdown2,
  Input,
  SimulationLoadingOverlay,
} from "../../components/UI/ui";
import SimulationSidebar from "./SimulationSidebar";
import { AirportTooltipContent, PlaneTooltipContent } from "./MapTooltips";

// Assets y Estilos
import "./simulacion.scss";
import run from "../../assets/icons/run.svg";
import stopIcon from "../../assets/icons/stop.svg";
import airportIconImg from "../../assets/icons/airport.svg";
import sedeIconImg from "../../assets/icons/sede.svg";
import planeIconImg from "../../assets/icons/planeMora.svg";

/**
 * @typedef {import("../../types/simulationRequest/SimulationRequest").SimulationRequest} SimulationRequest
 */

// ==========================================================================
// 1. CONSTANTES Y UTILIDADES ESTÁTICAS (Fuera del componente)
// ==========================================================================

const HUB_COLORS = [
  "#3b82f6", // Azul brillante
  "#10b981", // Verde esmeralda
  "#8b5cf6", // Violeta
  "#f59e0b", // Ámbar/Naranja
  "#06b6d4", // Cyan
  "#d946ef", // Fucsia
  "#6366f1", // Índigo
  "#84cc16", // Lima
  "#ec4899", // Rosa
  "#14b8a6", // Turquesa
];

// Componente auxiliar para eventos del mapa
function ClickHandler({ onMapClick }) {
  useMapEvent("click", () => onMapClick());
  return null;
}

// Helpers de Parseo y Formato
const parseNumber = (v) => {
  if (v === "" || v === null || v === undefined) return null;
  return Number(v);
};

const toISODate = (ms) => new Date(ms).toISOString().split("T")[0];
const toISOTime = (ms) => new Date(ms).toISOString().slice(11, 19);

const formatDuration = (totalSeconds) => {
  const days = Math.floor(totalSeconds / (3600 * 24));
  const hours = Math.floor((totalSeconds % (3600 * 24)) / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const secs = Math.floor(totalSeconds % 60);

  const parts = [];
  if (days > 0) parts.push(`${days}d`);
  parts.push(hours.toString().padStart(2, "0") + "h");
  parts.push(minutes.toString().padStart(2, "0") + "m");
  parts.push(secs.toString().padStart(2, "0") + "s");

  return parts.join(" : ");
};

const formatCompactNumber = (num) => {
  if (num >= 1000000) {
    return (num / 1000000).toFixed(1) + "M"; // 1.5M
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + "K"; // 1.5K
  }
  return num.toLocaleString(); // 500
};

// Conversión de Fechas
const parseFechaHoraToMs = (fechaHora) => {
  if (!fechaHora) return Date.now();
  const [fecha, hora] = fechaHora.split(" "); // "03/11/2025 10:20"
  const [dia, mes, anio] = fecha.split("/").map(Number);
  const [hh, mm] = hora.split(":").map(Number);
  return Date.UTC(anio, mes - 1, dia, hh, mm, 0);
};

const fromInputsToMsUTC = (d, t) => new Date(`${d}T${t}:00Z`).getTime();

// Cálculos Geográficos y de Color
function generateGeodesicPath(lat1, lon1, lat2, lon2, numPoints = 100) {
  const toRad = (deg) => (deg * Math.PI) / 180;
  const toDeg = (rad) => (rad * 180) / Math.PI;
  lat1 = toRad(lat1);
  lon1 = toRad(lon1);
  lat2 = toRad(lat2);
  lon2 = toRad(lon2);
  const d =
    2 *
    Math.asin(
      Math.sqrt(
        Math.sin((lat2 - lat1) / 2) ** 2 +
          Math.cos(lat1) * Math.cos(lat2) * Math.sin((lon2 - lon1) / 2) ** 2
      )
    );

  if (d === 0) return [{ lat: toDeg(lat1), lng: toDeg(lon1) }];

  const points = [];
  for (let i = 0; i <= numPoints; i++) {
    const f = i / numPoints;
    const A = Math.sin((1 - f) * d) / Math.sin(d);
    const B = Math.sin(f * d) / Math.sin(d);
    const x =
      A * Math.cos(lat1) * Math.cos(lon1) + B * Math.cos(lat2) * Math.cos(lon2);
    const y =
      A * Math.cos(lat1) * Math.sin(lon1) + B * Math.cos(lat2) * Math.sin(lon2);
    const z = A * Math.sin(lat1) + B * Math.sin(lat2);
    const lat = Math.atan2(z, Math.sqrt(x ** 2 + y ** 2));
    const lon = Math.atan2(y, x);
    points.push({ lat: toDeg(lat), lng: toDeg(lon) });
  }
  return points;
}

function getPlaneColorFilter(capacity, maxCapacity) {
  if (!maxCapacity || maxCapacity <= 0) {
    return "invert(80%) sepia(72%) saturate(657%) hue-rotate(3deg) brightness(88%) contrast(115%)";
  }
  const ratio = capacity / maxCapacity;
  if (ratio < 0.5) {
    return "invert(54%) sepia(81%) saturate(356%) hue-rotate(85deg) brightness(78%) contrast(115%)";
  }
  if (ratio >= 0.5 && ratio < 0.75) {
    return "invert(80%) sepia(72%) saturate(657%) hue-rotate(3deg) brightness(86%) contrast(118%)";
  }
  if (ratio >= 0.9) {
    return "invert(37%) sepia(79%) saturate(844%) hue-rotate(338deg) brightness(78%) contrast(120%)";
  }
  return "invert(80%) sepia(72%) saturate(657%) hue-rotate(3deg) brightness(86%) contrast(118%)";
}

function getAirportFilter(ocupacion) {
  if (ocupacion == null) return "none";
  if (ocupacion < 0.5) {
    return "invert(38%) sepia(77%) saturate(510%) hue-rotate(85deg) brightness(55%) contrast(120%)";
  }
  if (ocupacion < 0.8) {
    return "invert(74%) sepia(94%) saturate(750%) hue-rotate(2deg) brightness(60%) contrast(125%)";
  }
  return "invert(26%) sepia(88%) saturate(900%) hue-rotate(350deg) brightness(55%) contrast(130%)";
}

const getAirportOccupancyClass = (ocupacion) => {
  if (ocupacion == null) return "airport-card--unknown";
  if (ocupacion < 0.5) return "airport-card--low";
  if (ocupacion < 0.8) return "airport-card--medium";
  return "airport-card--high";
};

// ==========================================================================
// 2. COMPONENTE PRINCIPAL
// ==========================================================================

export default function Simulacion() {
  // ------------------------------------------------------------------------
  // A. ESTADOS (States)
  // ------------------------------------------------------------------------
  // ... otros estados
  const [isCollapseModalOpen, setIsCollapseModalOpen] = useState(false); // El modal de decisión
  const [isSystemCollapsed, setIsSystemCollapsed] = useState(false); // Para mantener la alerta roja visible
  //reportes
  const [reporteListo, setReporteListo] = useState(null);
  const [isReportModalOpen, setIsReportModalOpen] = useState(false);
  const [reportPreviewContent, setReportPreviewContent] = useState("");
  // -- Tiempo --
  const [realNow, setRealNow] = useState(new Date());
  const [simStartMs, setSimStartMs] = useState(null);
  const [simNowMs, setSimNowMs] = useState(() => Date.now());
  const [simEndMs, setSimEndMs] = useState(null);
  const [seconds, setSeconds] = useState(0);
  const [timerRunning, setTimerRunning] = useState(false);
  const [timerActive, setTimerActive] = useState(false);

  // -- UI General --
  const [controlsOpen, setControlsOpen] = useState(false);
  const [collapsed, setCollapsed] = useState(true);
  const [isSidebarPinned, setIsSidebarPinned] = useState(false);
  const [sidebarTab, setSidebarTab] = useState("flights");
  const [showLoadingSim, setShowLoadingSim] = useState(false);
  const [notification, setNotification] = useState(null);
  const [loading, setLoading] = useState(false);
  const [legendCollapsed, setLegendCollapsed] = useState(false);
  const [fleetPanelCollapsed, setFleetPanelCollapsed] = useState(false);
  const [stopDisabled, setStopDisabled] = useState(true);

  // -- Datos del Dominio --
  const [flights, setFlights] = useState([]);
  const [routes, setRoutes] = useState([]);
  const [orders, setOrders] = useState([]);
  const [airports, setAirports] = useState(null);
  // Estado para la lista del dropdown (Corregido: agregado aquí)
  const [aeropuertos, setAeropuertos] = useState([]);
  const [estadoEjecucionSim, setEstadoEjecucionSim] = useState("POR_INICIAR");

  // -- Selección y Resaltado --
  const [selectedItem, setSelectedItem] = useState(null);
  const [selectedAirport, setSelectedAirport] = useState(null);
  const [highlightedFlights, setHighlightedFlights] = useState([]);
  const [highlightedAirportCode, setHighlightedAirportCode] = useState(null);
  const [highlightedRoute, setHighlightedRoute] = useState(null);
  const [openAirportTooltipCode, setOpenAirportTooltipCode] = useState(null);
  const [openFlightTooltipCode, setOpenFlightTooltipCode] = useState(null);

  // -- Filtros UI --
  const [flightFilterCode, setFlightFilterCode] = useState("");
  const [flightFilterOrigin, setFlightFilterOrigin] = useState("");
  const [flightFilterDestination, setFlightFilterDestination] = useState("");
  const [orderFilterCode, setOrderFilterCode] = useState("");
  const [orderFilterDestino, setOrderFilterDestino] = useState("");
  const [orderFilterEstado, setOrderFilterEstado] = useState("PENDIENTES");
  const [onlyHubs, setOnlyHubs] = useState(false);
  const [airportFilterText, setAirportFilterText] = useState("");

  // -- Modal y Formulario de Planificación --
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [fechaI, setFechaI] = useState("");
  const [horaI, setHoraI] = useState("");
  const [fechaF, setFechaF] = useState("");
  const [horaF, setHoraF] = useState("");
  const [loadedOnOpen, setLoadedOnOpen] = useState(false);
  const [codOrigenes, setCodOrigenes] = useState([]);
  const [parametrosCompletos, setParametrosCompletos] = useState(null);

  // Inputs de simulación
  const [inputDate, setInputDate] = useState(
    new Date().toISOString().split("T")[0]
  );
  const [inputTime, setInputTime] = useState(
    new Date().toTimeString().slice(0, 5)
  );

  // Parámetros numéricos
  const [maxDiasEntregaIntercontinental, setMaxDiasEntregaIntercontinental] =
    useState();
  const [maxDiasEntregaIntracontinental, setMaxDiasEntregaIntracontinental] =
    useState();
  const [maxHorasRecojo, setMaxHorasRecojo] = useState();
  const [minHorasEstancia, setMinHorasEstancia] = useState();
  const [maxHorasEstancia, setMaxHorasEstancia] = useState();
  const [multiplicadorTemporal, setMultiplicadorTemporal] = useState();
  const [saltoDeAlgoritmo, setTamanioDeSaltoTemporal] = useState();
  const [probabilidadReplanificacion, setProbabilidadReplanificacion] =
    useState();
  const [simulationId, setSimulationId] = useState(null); //  Guardar ID actual
  const subscriptionsRef = useRef(null); //  Para limpiar suscripción

  // ------------------------------------------------------------------------
  // B. REFS
  // ------------------------------------------------------------------------
  const lastRealMsRef = useRef(null);
  const stopRequestedRef = useRef(false);
  const mapRef = useRef(null);

  // ------------------------------------------------------------------------
  // C. VALORES DERIVADOS (Calculados)
  // --------------------------------------------------F----------------------

  const simSpeed =
    typeof multiplicadorTemporal === "number" && multiplicadorTemporal > 0
      ? multiplicadorTemporal
      : 500;

  // Mapa de colores para Hubs
  const hubColorMap = useMemo(() => {
    if (!airports) return {};
    const sedes = Object.values(airports)
      .filter((ap) => ap.esSede)
      .sort((a, b) => a.code.localeCompare(b.code));
    const map = {};
    sedes.forEach((sede, index) => {
      map[sede.code] = HUB_COLORS[index % HUB_COLORS.length];
    });
    return map;
  }, [airports]);

  // Vuelos Activos
  const activeFlights = flights.filter(
    (f) =>
      f &&
      typeof f.startMs === "number" &&
      typeof f.endMs === "number" &&
      simNowMs >= f.startMs &&
      simNowMs < f.endMs
  );

  // Métricas de Flota
  const totalOccupied = activeFlights.reduce(
    (acc, f) => acc + (f.capacity || 0),
    0
  );
  const totalMax = activeFlights.reduce(
    (acc, f) => acc + (f.planeCapacity || 0),
    0
  );
  const fleetPercentage = totalMax > 0 ? (totalOccupied / totalMax) * 100 : 0;
  let fleetStatusColor = "#22c55e";
  let fleetStatusLabel = "Disponibilidad Alta";
  if (fleetPercentage >= 90) {
    fleetStatusColor = "#ef4444";
    fleetStatusLabel = "Saturación Crítica";
  } else if (fleetPercentage >= 50) {
    fleetStatusColor = "#eab308";
    fleetStatusLabel = "Ocupación Media";
  }

  // Lógica de Pedidos
  const isOrderDelivered = (pedido) => {
    let maxEndMs = null;
    (pedido.segmentaciones || []).forEach((seg) => {
      (seg.lotes || []).forEach((lote) => {
        (lote.vuelos || []).forEach((codVuelo) => {
          const f = flights.find((fl) => fl.code === codVuelo);
          if (f && typeof f.endMs === "number") {
            if (maxEndMs === null || f.endMs > maxEndMs) {
              maxEndMs = f.endMs;
            }
          }
        });
      });
    });
    if (maxEndMs === null) return false;
    return simNowMs >= maxEndMs;
  };

  const getOrderGenerationMs = (pedido) => {
    if (!pedido.fechaHoraGeneracion) return null;
    return parseFechaHoraToMs(pedido.fechaHoraGeneracion);
  };

  const isOrderGenerated = (pedido) => {
    const genMs = getOrderGenerationMs(pedido);
    if (!genMs) return true;
    return simNowMs >= genMs;
  };

  const visibleOrders = orders.filter(
    (pedido) => isOrderGenerated(pedido) && !isOrderDelivered(pedido)
  );
  const deliveredOrders = orders.filter(
    (pedido) => isOrderGenerated(pedido) && isOrderDelivered(pedido)
  );

  // Filtros de Listas
  const filteredActiveFlights = activeFlights.filter((f) => {
    if (
      flightFilterCode &&
      !f.code.toUpperCase().includes(flightFilterCode.toUpperCase())
    )
      return false;
    if (flightFilterOrigin && f.origin?.code !== flightFilterOrigin)
      return false;
    if (
      flightFilterDestination &&
      f.destination?.code !== flightFilterDestination
    )
      return false;
    return true;
  });

  const baseOrders =
    orderFilterEstado === "PENDIENTES"
      ? visibleOrders
      : orderFilterEstado === "ENTREGADOS"
      ? deliveredOrders
      : orders.filter(isOrderGenerated);

  const filteredOrders = baseOrders.filter((p) => {
    if (
      orderFilterCode &&
      !p.codigo.toUpperCase().includes(orderFilterCode.toUpperCase())
    )
      return false;
    if (orderFilterDestino && p.codDestino !== orderFilterDestino) return false;
    return true;
  });

  // Aeropuertos enriquecidos (Cálculo de stock)
  const visibleAirportsEnriched = (airports ? Object.values(airports) : [])
    .filter((ap) => {
      if (onlyHubs && !ap.esSede) return false;
      if (highlightedAirportCode && ap.code !== highlightedAirportCode)
        return false;
      if (airportFilterText) {
        const searchText = airportFilterText.toLowerCase();
        const matchCode = (ap.code || "").toLowerCase().includes(searchText);
        const matchCity = (ap.city || "").toLowerCase().includes(searchText);
        if (!matchCode && !matchCity) return false;
      }
      return true;
    })
    .map((ap) => {
      const registros = ap.registros || [];
      let stockActual = 0;
      registros.forEach((reg) => {
        if (!reg.sigueVigente) return;
        const ingresoMs = parseFechaHoraToMs(reg.fechaHoraIngreso);
        const egresoMs = reg.fechaHoraEgreso
          ? parseFechaHoraToMs(reg.fechaHoraEgreso)
          : null;
        if (simNowMs >= ingresoMs && (!egresoMs || simNowMs < egresoMs)) {
          stockActual += reg.tamLote || 0;
        }
      });
      const ocupacion = ap.capacidad > 0 ? stockActual / ap.capacidad : 0;
      return { ...ap, stockActual, ocupacion };
    });

  // Vuelos para detalle de aeropuerto
  const vuelosSaliendo = selectedAirport
    ? flights.filter(
        (f) =>
          f &&
          f.origin &&
          f.origin.code === selectedAirport.code &&
          typeof f.startMs === "number" &&
          simNowMs < f.startMs
      )
    : [];

  const vuelosLlegando = selectedAirport
    ? flights.filter(
        (f) =>
          f &&
          f.destination &&
          f.destination.code === selectedAirport.code &&
          typeof f.endMs === "number" &&
          simNowMs < f.endMs
      )
    : [];

  // Rutas activas
  const routesInCurrentTime = routes.filter((ruta) => {
    if (!ruta.codVuelos || ruta.codVuelos.length === 0) return false;
    return ruta.codVuelos.some((codigoVuelo) => {
      const f = flights.find((fl) => fl.code === codigoVuelo);
      if (!f || typeof f.startMs !== "number" || typeof f.endMs !== "number")
        return false;
      return simNowMs >= f.startMs && simNowMs <= f.endMs;
    });
  });

  // Helpers internos dependientes del estado
  const getAirportLabel = (code) => {
    if (!code) return "";
    if (!airports) return code;
    const ap = airports[code];
    if (!ap) return code;
    return `${code} - ${ap.city}`;
  };

  const getAirportCityName = (code) => {
    if (!code || !airports) return code || "";
    const ap = airports[code];
    return ap ? ap.city : code;
  };

  const getOrdersForFlight = (flightCode) => {
    const pedidosEnVuelo = [];
    orders.forEach((pedido) => {
      (pedido.segmentaciones || []).forEach((seg) => {
        (seg.lotes || []).forEach((lote) => {
          if (lote.vuelos && lote.vuelos.includes(flightCode)) {
            pedidosEnVuelo.push({
              pedidoCodigo: pedido.codigo,
              pedidoDestino: pedido.codDestino,
              loteCodigo: lote.loteCodigo,
              cantidad: lote.loteTamanio,
              origenCode: lote.origenCode,
              destinoCode: lote.destinoCode || lote.arrivalAirportCode,
            });
          }
        });
      });
    });
    return pedidosEnVuelo;
  };

  const getLastFlightOfLote = (lote) => {
    if (!lote || !Array.isArray(lote.vuelos) || lote.vuelos.length === 0)
      return null;
    for (let i = lote.vuelos.length - 1; i >= 0; i--) {
      const code = lote.vuelos[i];
      const f = flights.find((fl) => fl.code === code);
      if (f) return f;
    }
    return null;
  };

  const showNotification = (type, message) => {
    setNotification({ type, message });
    setTimeout(() => setNotification(null), 5000);
  };

  // Creación de iconos (dentro del componente porque usan imports de imágenes)
  const createColoredIcon = (filterCss, rotation) =>
    L.divIcon({
      html: `<img src="${planeIconImg}" 
            style="width:18px;
                   transform: rotate(${rotation}deg);
                   transform-origin: center center;
                   filter:${filterCss} drop-shadow(0 0 2px black) drop-shadow(0 0 1px black);
                   transition: transform 0.3s linear;">`,
      className: "",
      iconSize: [18, 18],
      iconAnchor: [11, 8],
    });

  const createAirportIcon = (ap) => {
    const size = ap.esSede ? 30 : 24;
    const baseIcon = ap.esSede ? sedeIconImg : airportIconImg;
    return L.divIcon({
      html: `
      <img
        src="${baseIcon}"
        class="airport-icon ${ap.esSede ? "airport-icon--hub" : ""}"
        style="
          width: ${size}px;
          height: ${size}px;
          filter: ${getAirportFilter(ap.ocupacion)};
        "
      />
    `,
      className: "",
      iconSize: [size, size],
      iconAnchor: [size / 2, size / 2],
      popupAnchor: [0, -(size / 2)],
    });
  };

  // ------------------------------------------------------------------------
  // D. LÓGICA DE PROCESAMIENTO (WebSocket & Simulation Data)
  // ------------------------------------------------------------------------

  const buildSimulationFromSolution = (solution) => {
    if (!solution) return;

    // Helper para obtener etiquetas de rutas de un vuelo
    const getRutasDeVuelo = (flightCode) => {
      const labels = (solution.rutasEnOperacion || [])
        .filter((r) => (r.codVuelos || []).includes(flightCode))
        .map((r) => `${r.codOrigen} → ${r.codDestino}`);
      return Array.from(new Set(labels));
    };

    // 1. Mapear Aeropuertos
    const airportMap = {};
    (solution.aeropuertosTransitados || []).forEach((a) => {
      airportMap[a.codigo] = {
        lat: a.latitud,
        lng: a.longitud,
        name: a.alias || a.ciudad,
        code: a.codigo,
        city: a.ciudad,
        country: a.pais,
        capacidad: a.capacidad,
        esSede: a.esSede,
        // Filtramos solo registros vigentes
        registros: (a.registros || []).filter(
          (reg) => reg.sigueVigente === true
        ),
      };
    });

    // 2. Mapa de Llegadas de Lotes (para saber dónde está cada lote)
    const loteArrivalMap = {};
    Object.values(airportMap).forEach((ap) => {
      (ap.registros || []).forEach((reg) => {
        if (!reg.sigueVigente) return;
        if (!reg.codLote || !reg.fechaHoraIngreso) return;

        const loteCode = reg.codLote;
        const ingresoMs = parseFechaHoraToMs(reg.fechaHoraIngreso);
        const prev = loteArrivalMap[loteCode];

        // Nos quedamos con el registro más reciente
        if (!prev || ingresoMs > prev.ingresoMs) {
          loteArrivalMap[loteCode] = {
            ingreso: reg.fechaHoraIngreso,
            ingresoMs,
            airportCode: ap.code,
            airportCity: ap.city,
            airportName: ap.name,
          };
        }
      });
    });

    // 3. Mapear Rutas
    const rutasPorCodigo = {};
    (solution.rutasEnOperacion || []).forEach((r) => {
      rutasPorCodigo[r.codigo] = r;
    });

    const rutasFiltradas = (solution.rutasEnOperacion || [])
      .filter((r) => r.estado === "OPERATIVA" || r.estado === "FINALIZADA")
      .map((r) => {
        const origin = airportMap[r.codOrigen];
        const destino = airportMap[r.codDestino];
        return {
          ...r,
          originCity: origin?.city || origin?.name || r.codOrigen,
          destinationCity: destino?.city || destino?.name || r.codDestino,
        };
      });
    setRoutes(rutasFiltradas);

    // 4. Mapear Pedidos (AQUÍ ESTÁ EL CAMBIO PRINCIPAL)
    const pedidosAtendidos = (solution.pedidosAtendidos || []).map((p) => {
      // Adaptador: El backend nuevo manda 'segmentacionVigente' (objeto),
      // pero el frontend espera un array de segmentaciones.
      let sourceSegmentaciones = [];
      if (p.segmentaciones && p.segmentaciones.length > 0) {
        sourceSegmentaciones = p.segmentaciones;
      } else if (p.segmentacionVigente) {
        sourceSegmentaciones = [p.segmentacionVigente];
      }

      const segmentaciones = sourceSegmentaciones.map((seg) => {
        const lotes = (seg.lotesPorRuta || []).map((lpr) => {
          const ruta = rutasPorCodigo[lpr.codRuta];
          const origen = ruta ? airportMap[ruta.codOrigen] : null;
          const destino = ruta ? airportMap[ruta.codDestino] : null;

          // En el nuevo JSON, 'codVuelos' es un array de strings directo
          const vuelosLote =
            Array.isArray(lpr.codVuelos) && lpr.codVuelos.length > 0
              ? lpr.codVuelos
              : ruta?.codVuelos || []; // Fallback a la ruta

          // Datos del lote anidado (lpr.lote)
          const loteData = lpr.lote || {};
          const loteCodigo = loteData.codigo;
          const arrivalInfo = loteCodigo ? loteArrivalMap[loteCodigo] : null;

          return {
            codRuta: lpr.codRuta,
            loteCodigo: loteCodigo,
            loteTamanio: loteData.tamanio,
            loteEstado: loteData.estado,
            vuelos: vuelosLote,

            // Info geográfica derivada de la ruta
            origenCode: ruta?.codOrigen,
            destinoCode: ruta?.codDestino,
            origenNombre: origen?.city || ruta?.codOrigen,
            destinoNombre: destino?.city || ruta?.codDestino,

            // Info de llegada real (si existe registro en aeropuerto)
            arrivalAirportCode: arrivalInfo?.airportCode || null,
            arrivalAirportCity: arrivalInfo?.airportCity || null,
            arrivalFechaHoraIngreso: arrivalInfo?.ingreso || null,
          };
        });

        return {
          codigo: seg.codigo,
          fechaHoraAplicacion: seg.fechaHoraAplicacion,
          fechaHoraSustitucion: seg.fechaHoraSustitucion || null,
          lotes,
        };
      });

      return {
        codigo: p.codigo,
        codCliente: p.codCliente,
        codDestino: p.codDestino,
        cantidadSolicitada: p.cantidadSolicitada,
        fueAtendido: p.fueAtendido,
        fechaHoraGeneracion: p.fechaHoraGeneracion,
        segmentaciones,
      };
    });
    setOrders(pedidosAtendidos);

    // 5. Actualizar Stock de Aeropuertos
    setAirports((prevAirports) => {
      const merged = { ...(prevAirports || {}) };
      Object.entries(airportMap).forEach(([code, data]) => {
        const prev = merged[code] || {};
        // Preservar flag esSede si ya existía
        const esSedeFinal =
          data.esSede !== undefined && data.esSede !== null
            ? data.esSede
            : prev.esSede ?? false;

        merged[code] = {
          ...prev,
          ...data,
          esSede: esSedeFinal,
        };
      });
      return merged;
    });

    // 6. Procesar Vuelos (Interpolación de movimiento)
    const vuelosNuevos = solution.vuelosEnTransito || [];
    const codigosNuevos = new Set(vuelosNuevos.map((v) => v.codigo));

    setFlights((prevFlights) => {
      const prevByCode = new Map(prevFlights.map((f) => [f.code, f]));
      const nextFlights = [];

      vuelosNuevos.forEach((v) => {
        const origin = airportMap[v.codOrigen];
        const dest = airportMap[v.codDestino];

        if (!origin || !dest) {
          // Si faltan datos de aeropuertos, saltamos para evitar crash
          return;
        }

        const startMs = parseFechaHoraToMs(v.fechaHoraSalida);
        const endMs = parseFechaHoraToMs(v.fechaHoraLlegada);
        const durationSec = Math.max((endMs - startMs) / 1000, 60);

        // Generar curva geodésica
        const path = generateGeodesicPath(
          origin.lat,
          origin.lng,
          dest.lat,
          dest.lng,
          120
        );
        const rutasVuelo = getRutasDeVuelo(v.codigo);
        const prev = prevByCode.get(v.codigo);

        if (prev) {
          // Si el vuelo ya existía, actualizamos datos dinámicos (carga)
          nextFlights.push({
            ...prev,
            capacity: v.capacidadOcupada,
            planeCapacity: v.capacidadMaxima,
            rutas: rutasVuelo,
          });
        } else {
          // Si es vuelo nuevo, inicializamos posición
          let progress = 0;
          let position = path[0];
          let arrived = false;
          let rotation = 0;

          if (simNowMs <= startMs) {
            progress = 0;
            position = path[0];
            arrived = false;
          } else if (simNowMs >= endMs) {
            progress = 1;
            position = path[path.length - 1];
            arrived = true;
          } else {
            // Calcular posición interpolada inicial
            const total = Math.max(endMs - startMs, 60 * 1000);
            const frac = (simNowMs - startMs) / total;
            progress = Math.min(Math.max(frac, 0), 1);

            const idx = Math.floor(progress * (path.length - 1));
            const pos = path[idx];
            const next = path[Math.min(idx + 1, path.length - 1)];

            // Calcular rotación del avión
            const toRad = (d) => (d * Math.PI) / 180;
            const toDeg = (r) => (r * 180) / Math.PI;
            const lat1 = toRad(pos.lat),
              lon1 = toRad(pos.lng);
            const lat2 = toRad(next.lat),
              lon2 = toRad(next.lng);

            let bearing = Math.atan2(
              Math.sin(lon2 - lon1) * Math.cos(lat2),
              Math.cos(lat1) * Math.sin(lat2) -
                Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)
            );
            bearing = (toDeg(bearing) + 360) % 360;
            rotation = bearing - 45; // Ajuste por icono del avión
            position = pos;
          }

          nextFlights.push({
            code: v.codigo,
            origin,
            originName: origin.name,
            destination: dest,
            destinationName: dest.name,
            startTime: v.fechaHoraSalida,
            endTime: v.fechaHoraLlegada,
            startMs,
            endMs,
            capacity: v.capacidadOcupada,
            planeCapacity: v.capacidadMaxima,
            durationSec,
            path,
            progress,
            position,
            rotation,
            arrived,
            rutas: rutasVuelo,
          });
        }
      });

      // Mantener vuelos antiguos que aún no terminan (visualización suave)
      prevFlights.forEach((prevFlight) => {
        if (
          !codigosNuevos.has(prevFlight.code) &&
          !prevFlight.arrived &&
          simNowMs < prevFlight.endMs
        ) {
          nextFlights.push(prevFlight);
        }
      });

      return nextFlights;
    });
  };

  // ------------------------------------------------------------------------
  // E. EFFECTS (Ciclos de Vida)
  // ------------------------------------------------------------------------

  // 1. Reloj Real
  useEffect(() => {
    const interval = setInterval(() => {
      setRealNow(new Date());
    }, 1000);
    return () => clearInterval(interval);
  }, []);

  // 2. Cronómetro y Loop de Animación
  useEffect(() => {
    let timer;
    if (timerRunning) {
      timer = setInterval(() => setSeconds((s) => s + 1), 1000);
    }
    return () => clearInterval(timer);
  }, [timerRunning]);

  useEffect(() => {
    if (!timerRunning) return;
    let rafId;

    const tick = (now) => {
      if (lastRealMsRef.current == null) lastRealMsRef.current = now;
      const elapsedRealMs = now - lastRealMsRef.current;
      lastRealMsRef.current = now;
      setSimNowMs((prev) => prev + elapsedRealMs * simSpeed);
      rafId = requestAnimationFrame(tick);
    };

    lastRealMsRef.current = performance.now();
    rafId = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(rafId);
  }, [timerRunning, simSpeed]);

  // 3. Actualización de Posición de Vuelos
  useEffect(() => {
    if (!timerActive) return;

    setFlights((prev) =>
      prev.map((f) => {
        if (!f || !f.path || f.path.length === 0) return f;
        const total = Math.max(f.endMs - f.startMs, 60 * 1000);

        if (simNowMs <= f.startMs) {
          return { ...f, progress: 0, position: f.path[0], arrived: false };
        }
        if (simNowMs >= f.endMs) {
          return {
            ...f,
            progress: 1,
            position: f.path[f.path.length - 1],
            arrived: true,
          };
        }

        const frac = Math.min((simNowMs - f.startMs) / total, 1);
        const idx = Math.floor(frac * (f.path.length - 1));
        const pos = f.path[idx];
        const next = f.path[Math.min(idx + 1, f.path.length - 1)];

        const toRad = (d) => (d * Math.PI) / 180,
          toDeg = (r) => (r * 180) / Math.PI;
        const lat1 = toRad(pos.lat),
          lon1 = toRad(pos.lng);
        const lat2 = toRad(next.lat),
          lon2 = toRad(next.lng);

        let bearing = Math.atan2(
          Math.sin(lon2 - lon1) * Math.cos(lat2),
          Math.cos(lat1) * Math.sin(lat2) -
            Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)
        );
        bearing = (toDeg(bearing) + 360) % 360;
        const rotation = bearing - 45;

        return {
          ...f,
          progress: frac,
          position: pos,
          rotation,
          arrived: frac >= 1,
        };
      })
    );
  }, [simNowMs, timerActive]);

  // 4. Limpieza de Highlights
  useEffect(() => {
    if (highlightedFlights.length === 0) return;
    const activos = highlightedFlights.filter((code) =>
      flights.some(
        (f) =>
          f.code === code &&
          typeof f.startMs === "number" &&
          typeof f.endMs === "number" &&
          timerActive &&
          simNowMs >= f.startMs &&
          simNowMs < f.endMs
      )
    );
    if (activos.length !== highlightedFlights.length) {
      setHighlightedFlights(activos);
    }
  }, [flights, highlightedFlights, simNowMs, timerActive]);

  // 5. Finalización Automática
  // 5. Finalización Automática Inteligente (Espera a TODOS los vuelos)
  useEffect(() => {
    // Si el reloj no está corriendo, no hacemos nada
    if (!timerActive) return;
    // 1. ¿Ya llegamos a la fecha fin configurada?
    // (Esto es solo una referencia mínima, no un límite estricto)
    const fechaFinAlcanzada = simEndMs && simNowMs >= simEndMs;
    // 2. ¿Hay vuelos en el aire AHORA MISMO?
    const hayVuelosActivos = activeFlights.length > 0;
    // 3. ¿Hay vuelos programados par EL FUTURO?
    // Buscamos si existe algún vuelo cuyo inicio sea mayor a "ahora"
    const hayVuelosPendientes = flights.some((f) => f.startMs > simNowMs);
    // CASO A: Aún no llegamos a la fecha fin -> Seguimos
    if (!fechaFinAlcanzada) return;

    // CASO B: Ya pasamos la fecha fin, PERO hay actividad -> Seguimos
    if (hayVuelosActivos || hayVuelosPendientes) {
      return;
    }

    // CASO C: Pasó la fecha fin Y no hay nadie volando Y no viene nadie más
    // -> AHORA SÍ PARAMOS
    console.log(
      "✅ Fin Visual: Tiempo cumplido y sin actividad aérea pendiente."
    );
    setTimerRunning(false);
    setTimerActive(false);
    showNotification("success", "Visualización completada.");
  }, [simNowMs, simEndMs, timerActive, activeFlights.length, flights]);

  // 4. NUEVO EFECTO: ABRIR MODAL CUANDO HAYA REPORTE Y LA SIMULACIÓN HAYA TERMINADO
  useEffect(() => {
    if (
      !timerActive &&
      reporteListo &&
      !isReportModalOpen &&
      !isSystemCollapsed
    ) {
      const abrirModalAutomaticamente = async () => {
        try {
          const texto = await getExportationPreview(reporteListo);
          setReportPreviewContent(texto);
          setIsReportModalOpen(true);
        } catch (err) {
          console.warn(
            "No se pudo cargar vista previa (posiblemente eliminado)."
          );
        }
      };
      abrirModalAutomaticamente();
    }
  }, [timerActive, reporteListo, isReportModalOpen, isSystemCollapsed]);

  // 5. FUNCIONES PARA LOS BOTONES DEL MODAL

  // Opción A: Descargar (Baja el archivo y cierra conexión)
  const handleModalDownload = async () => {
    if (reporteListo) {
      try {
        await downloadExportationFile(reporteListo);
        try {
          await deleteExportationFile(reporteListo);
        } catch (delError) {
          console.warn(
            "Advertencia: No se pudo eliminar el archivo remoto.",
            delError
          );
        }

        showNotification(
          "success",
          "Archivo descargado y eliminado. Cerrando sesión."
        );
        cerrarTodoYDesconectar();
      } catch (error) {
        console.error("Error al descargar:", error);
        showNotification(
          "danger",
          "Falló la descarga. No se cerrará la sesión."
        );
      }
    }
  };

  // Opción B: Cerrar (Borra el archivo del server y cierra conexión)
  const handleModalClose = async () => {
    if (reporteListo) {
      await deleteExportationFile(reporteListo); // Borrar del server
      showNotification("info", "Reporte descartado. Cerrando sesión.");
      cerrarTodoYDesconectar();
    }
  };

  const cerrarTodoYDesconectar = () => {
    setIsReportModalOpen(false);
    setReporteListo(null);
    setReportPreviewContent("");

    // Desconectar WebSocket
    if (subscriptionsRef.current) {
      subscriptionsRef.current.unsubscribe();
      subscriptionsRef.current = null;
    }
    setSimulationId(null);
    // handleStop();
  };
  // 6. Carga de Aeropuertos Iniciales
  useEffect(() => {
    const fetchAeropuertosIniciales = async () => {
      try {
        const res = await listarAeropuertos();
        const dtos = res.dtos ?? [];
        const baseMap = {};
        dtos.forEach((a) => {
          baseMap[a.codigo] = {
            lat: a.latitud,
            lng: a.longitud,
            name: a.alias || a.ciudad,
            code: a.codigo,
            city: a.ciudad,
            country: a.pais,
            capacidad: a.capacidad ?? 0,
            esSede: a.esSede ?? false,
            registros: [],
          };
        });
        setAirports(baseMap);
      } catch (err) {
        console.error("Error cargando aeropuertos iniciales", err);
        showNotification("danger", "Error cargando aeropuertos");
      }
    };
    fetchAeropuertosIniciales();
  }, []);

  // 7. Carga de Parámetros en Modal (Corregido: ahora setAeropuertos existe)
  useEffect(() => {
    const fetchParametrosYAeropuertos = async () => {
      try {
        const parametrosResponse = await listarParametros();
        const p = parametrosResponse.dtos[0];
        setParametrosCompletos(p);
        const a = await listarAeropuertos();
        setAeropuertos(a.dtos ?? []); // <-- ¡AQUÍ ESTABA EL ERROR!
        console.log("Aeropuertos cargados:", a.dtos);
        setMaxDiasEntregaIntercontinental(p.maxDiasEntregaIntercontinental);
        setMaxDiasEntregaIntracontinental(p.maxDiasEntregaIntracontinental);
        setMaxHorasRecojo(p.maxHorasRecojo);
        setMinHorasEstancia(p.minHorasEstancia);
        setMaxHorasEstancia(p.maxHorasEstancia);
        setProbabilidadReplanificacion(p.probabilidadReplanificacion);
        setCodOrigenes((prev) =>
          prev.length === 0 ? p.codOrigenes || [] : prev
        );
      } catch (err) {
        showNotification("danger", "Error cargando parámetros");
      }
    };

    if (isModalOpen && !loadedOnOpen) {
      fetchParametrosYAeropuertos();
      setLoadedOnOpen(true);
    }
  }, [isModalOpen, loadedOnOpen]);

  // 8. WebSocket Connection (Solo Conexión Inicial)
  useEffect(() => {
    // Conectamos al socket al entrar a la página
    connectSimulatorWS(() => {
      console.log("Socket listo esperando simulación...");
    });

    // Cleanup al salir
    return () => {
      if (subscriptionsRef.current) {
        subscriptionsRef.current.unsubscribe(); // Limpiar suscripción si hay una activa
      }
      disconnectWS();
    };
  }, []);

  // 9. Auto-Start
  useEffect(() => {
    if (!showLoadingSim && estadoEjecucionSim === "INICIADO") {
      handleStart();
    }
  }, [showLoadingSim, estadoEjecucionSim]);

  // 10. Actualización Fechas Modal
  useEffect(() => {
    if (fechaI && horaI) {
      const start = new Date(`${fechaI}T${horaI}:00Z`);
      const end = new Date(start.getTime() + 7 * 24 * 60 * 60 * 1000);

      setFechaF(end.toISOString().slice(0, 10));
      setHoraF(end.toISOString().slice(11, 16));
    }
  }, [fechaI, horaI]);

  // 11. Tooltip Event Propagation Fix
  useEffect(() => {
    setTimeout(() => {
      const elems = document.querySelectorAll(".airport-tooltip");
      elems.forEach((el) => {
        L.DomEvent.disableClickPropagation(el);
        L.DomEvent.disableScrollPropagation(el);
        el.style.pointerEvents = "auto";
      });
    }, 50);
  }, [openAirportTooltipCode]);

  useEffect(() => {
    setTimeout(() => {
      const elems = document.querySelectorAll(".plane-tooltip");
      elems.forEach((el) => {
        L.DomEvent.disableClickPropagation(el);
        L.DomEvent.disableScrollPropagation(el);
        el.style.pointerEvents = "auto";
      });
    }, 50);
  }, [openFlightTooltipCode]);

  // ------------------------------------------------------------------------
  // F. HANDLERS (Manejadores de Eventos)
  // ------------------------------------------------------------------------

  // Control de Simulación
  const handleStart = (overrideDate, overrideTime) => {
    if (timerActive && !timerRunning) {
      lastRealMsRef.current = performance.now();
      setTimerRunning(true);
      setStopDisabled(false);
      return;
    }

    // TRUCO: Usar la fecha que llega por parámetro (fresca)
    // o caer en el estado (inputDate) si no se pasan argumentos.
    const d = overrideDate || inputDate;
    const t = overrideTime || inputTime;

    // Convertimos esa fecha específica a milisegundos
    const base = fromInputsToMsUTC(d, t);

    setSimNowMs(base);
    setSimStartMs(base);

    lastRealMsRef.current = performance.now();
    setTimerRunning(true);
    setTimerActive(true);
    setStopDisabled(false);
  };

  const handleStop = async () => {
    // 1. Marcar bandera para evitar notificaciones de "error" al cortar la conexión
    stopRequestedRef.current = true;
    showNotification("info", "Deteniendo simulación...");

    try {
      // 2. Enviar petición al backend usando el ID guardado (sin el "TOK-")
      if (simulationId && estadoEjecucionSim !== "DETENIDO") {
        await sendStopSimulation(simulationId);
      } else {
        console.log(
          "ℹ️ La simulación ya había terminado en el servidor, solo limpiamos localmente."
        );
      }
    } catch (err) {
      console.warn("Error al intentar detener la simulación en servidor:", err);
    }

    // 3. Limpieza de Lógica de Simulación
    setTimerRunning(false);
    setTimerActive(false);
    setSeconds(0);

    // Limpiar datos del mapa
    setFlights([]);
    setOrders([]);
    setRoutes([]);
    setAirports((prev) => {
      return prev;
    });

    // 4. Limpieza de Selección de UI
    setSelectedItem(null);
    setSelectedAirport(null);
    setSidebarTab("flights");
    setHighlightedRoute(null);
    setHighlightedFlights([]);
    setOpenAirportTooltipCode(null);
    setOpenFlightTooltipCode(null);
    setStopDisabled(true);

    // 5. Resetear Fechas Visuales a "Ahora"
    const now = new Date();
    setInputDate(now.toISOString().split("T")[0]);
    setInputTime(now.toTimeString().slice(0, 5));
    setSimNowMs(now.getTime());
    setSimStartMs(null);
    setSimEndMs(null);
    setIsCollapseModalOpen(false);
    setIsSystemCollapsed(false);
    // 6. IMPORTANTE: Limpiar Suscripción WebSocket y borrar el ID
    /*
    if (subscriptionsRef.current) {
      console.log("🔌 Desuscribiendo canales WebSocket...");
      subscriptionsRef.current.unsubscribe();
      subscriptionsRef.current = null;
    }
    setSimulationId(null);*/
  };

  const formatDateForBackend = (dateStr, timeStr) => {
    if (!dateStr || !timeStr) return null;
    const [year, month, day] = dateStr.split("-");
    return `${day}/${month}/${year} ${timeStr}`;
  };

  const handlePlanear = async () => {
    try {
      setLoading(true);
      setShowLoadingSim(true);
      if (!fechaI || !horaI || !fechaF || !horaF) {
        showNotification("danger", "Completa las fechas antes de continuar");
        setLoading(false);
        setShowLoadingSim(false);
        return;
      }

      // 1. ACTUALIZAR ESTADO LOCAL PARA EL RELOJ (¡ESTO FALTABA!)
      // Esto asegura que cuando handleStart() se ejecute, use estas fechas
      setInputDate(fechaI);
      setInputTime(horaI);
      if (fechaF && horaF) {
        setSimEndMs(fromInputsToMsUTC(fechaF, horaF));
      }
      /** @type {SimulationRequest} */
      const body = {
        fechaHoraInicio: formatDateForBackend(fechaI, horaI), // Antes: `${fechaI}T${horaI}:00`
        fechaHoraFin: formatDateForBackend(fechaF, horaF),
        parametros: {
          ...parametrosCompletos,
          maxDiasEntregaIntercontinental,
          maxDiasEntregaIntracontinental,
          maxHorasRecojo,
          minHorasEstancia,
          maxHorasEstancia,
          codOrigenes,
          probabilidadReplanificacion,
        },
        multiplicadorTemporal,
        saltoDeAlgoritmo,
      };
      // 1.2. Enviar Petición
      console.log("SimulationRequest enviado por HTTP:", body);
      const res = await sendSimulationRequest(body);

      // 2. Procesar Token y Suscribirse
      if (res && res.token) {
        // Extraer ID (quitar "TOK-")
        const idTransaccion = res.token.replace("TOK-", "");
        setSimulationId(idTransaccion); // Guardar en estado

        console.log("🆔 ID Simulación:", idTransaccion);

        // Limpiar suscripción anterior si existe
        if (subscriptionsRef.current) subscriptionsRef.current.unsubscribe();
        // Resetear reporte anterior al iniciar nueva simulación
        setReporteListo(null);
        // SUSCRIBIRSE DINÁMICAMENTE
        subscriptionsRef.current = subscribeToSimulation(
          idTransaccion,
          (payload) => {
            // Callback de DATOS (Solución)
            //console.log("📦 DATA WebSocket Recibida:", payload);
            console.log(
              `📦 Paquete recibido a las ${new Date().toLocaleTimeString()}`
            );
            const solucion = payload.solucion || payload;
            if (solucion) buildSimulationFromSolution(solucion);
          },
          (status) => {
            // Callback de ESTADO (Status)
            console.log("🚦 STATUS WebSocket Recibido:", status);
            const estado = status.estadoEjecucion || status;
            const fin = status.estadoFinalizacion;

            setEstadoEjecucionSim(estado);
            console.log(
              `🎫 [TOKEN: ${idTransaccion}] Estado: "${estado}" | Fin: "${fin}"`
            );
            // Tu lógica de notificaciones movida aquí:
            if (estado === "POR_INICIAR") {
              setShowLoadingSim(true);
              showNotification("info", "Iniciando motores...");
            } else if (estado === "INICIADO") {
              setShowLoadingSim(false);
              showNotification("success", "¡Simulación en curso!");
              handleStart(fechaI, horaI); // <--- IMPORTANTE: Iniciar reloj visual
            } else if (estado === "DETENIDO") {
              setShowLoadingSim(false);
              if (fin === "EXITOSO") {
                console.log(`✅ [${idTransaccion}] Terminó con ÉXITO`);
                showNotification("success", "Finalizado con éxito");
              } else if (fin === "COLAPSO") {
                console.warn(`💥 [${idTransaccion}] COLAPSÓ.`);
                // Marcamos el sistema como colapsado (para alertas visuales)
                setIsSystemCollapsed(true);
                // ABRIMOS EL MODAL DE DECISIÓN INMEDIATAMENTE
                setIsCollapseModalOpen(true);
                // Notificacion breve
                showNotification("danger", "¡ALERTA DE COLAPSO LOGÍSTICO!");
              } else {
                if (fin === "FORZADO") {
                  console.log(
                    `🛑 [${idTransaccion}] Detenido FORZOSAMENTE (Confirmado por Backend)`
                  );
                }
                if (subscriptionsRef.current) {
                  console.log(
                    `🔌 [${idTransaccion}] Cerrando conexión WebSocket.`
                  );
                  subscriptionsRef.current.unsubscribe();
                  subscriptionsRef.current = null;
                }
                setSimulationId(null);
              }
            }
          },
          (fileData) => {
            console.log("📄 Reporte generado:", fileData);
            setReporteListo(fileData);
            showNotification(
              "success",
              "Reporte disponible para descargar al finalizar 📥"
            );
          }
        );
      }

      closeModal();
    } catch (err) {
      console.error("❌ Error en handlePlanear:", err);
      setShowLoadingSim(false);
      showNotification("danger", err.message || "Error al iniciar");
    } finally {
      setLoading(false);
    }
  };

  // Modal Handlers
  const resetModal = () => {
    setFechaI("");
    setHoraI("");
    setFechaF("");
    setHoraF("");
  };
  const openModal = () => {
    resetModal();
    setIsModalOpen(true);
  };
  const closeModal = () => {
    resetModal();
    setIsModalOpen(false);
    setLoadedOnOpen(false);
  };

  // Mapa & Sidebar Handlers
  const handleMapReset = () => {
    setSelectedItem(null);
    setSelectedAirport(null);
    setHighlightedRoute(null);
    setHighlightedFlights([]);
    setHighlightedAirportCode(null);
    setFlightFilterCode("");
    setOpenAirportTooltipCode(null);
    setOpenFlightTooltipCode(null);
  };

  const handleFlightClick = (flight) => {
    setCollapsed(false);
    setSelectedItem({ type: "flight", codigo: flight.code });
    setSelectedAirport(null);
    setFlightFilterCode(flight.code);
    setHighlightedFlights([flight.code]);
    setHighlightedRoute(null);
    setHighlightedAirportCode(null);
    const pos = flight.position || {
      lat: flight.origin.lat,
      lng: flight.origin.lng,
    };
    if (mapRef.current && pos) {
      mapRef.current.setView([pos.lat, pos.lng], 5, { animate: true });
    }
  };

  const handleOrderClick = (pedido) => {
    setSelectedAirport(null);
    setHighlightedRoute(null);
    const vuelosPedido = new Set();
    pedido.segmentaciones.forEach((seg) => {
      seg.lotes.forEach((lote) => {
        (lote.vuelos || []).forEach((v) => vuelosPedido.add(v));
      });
    });
    setHighlightedFlights([...vuelosPedido]);
    setSelectedItem({ type: "order", codigo: pedido.codigo });
  };

  const handleAirportClick = (ap) => {
    setCollapsed(false);
    setSelectedAirport(ap);
    setSelectedItem(null);
    setHighlightedAirportCode(ap.code);
    setHighlightedFlights([]);
    setHighlightedRoute(null);
    if (mapRef.current) {
      mapRef.current.setView([ap.lat, ap.lng], 6, { animate: true });
    }
  };

  const handleRouteClick = (ruta) => {
    setSelectedAirport(null);
    setSelectedItem({ type: "route", codigo: ruta.codigo });
    setHighlightedRoute(null);
    setHighlightedFlights(ruta.codVuelos || []);
  };

  const handleSidebarClickGeneral = (type, item, mapAction) => {
    mapAction();
    if (isSidebarPinned) {
      setCollapsed(false);
      if (type === "flight")
        setSelectedItem({ type: "flight", codigo: item.code });
      if (type === "order")
        setSelectedItem({ type: "order", codigo: item.codigo });
      if (type === "route")
        setSelectedItem({ type: "route", codigo: item.codigo });
      if (type === "airport") {
        setSelectedAirport(item);
        setSelectedItem(null);
      } else {
        setSelectedAirport(null);
      }
    } else {
      setCollapsed(true);
      setSelectedItem(null);
      setSelectedAirport(null);
    }
  };

  const handleSidebarFlightClick = (flight) => {
    handleSidebarClickGeneral("flight", flight, () => {
      setFlightFilterCode(flight.code);
      setHighlightedFlights([flight.code]);
      setHighlightedRoute(null);
      setHighlightedAirportCode(null);
      const pos = flight.position || {
        lat: flight.origin.lat,
        lng: flight.origin.lng,
      };
      if (mapRef.current && pos) {
        mapRef.current.setView([pos.lat, pos.lng], 5, { animate: true });
      }
    });
  };

  const handleSidebarAirportClick = (ap) => {
    handleSidebarClickGeneral("airport", ap, () => {
      setHighlightedAirportCode(ap.code);
      setHighlightedFlights([]);
      setHighlightedRoute(null);
      if (mapRef.current) {
        mapRef.current.setView([ap.lat, ap.lng], 6, { animate: true });
      }
    });
  };

  const handleSidebarOrderClick = (pedido) => {
    handleSidebarClickGeneral("order", pedido, () => {
      setSelectedAirport(null);
      setHighlightedRoute(null);
      const vuelosPedido = new Set();
      pedido.segmentaciones.forEach((seg) => {
        seg.lotes.forEach((lote) => {
          (lote.vuelos || []).forEach((v) => vuelosPedido.add(v));
        });
      });
      setHighlightedFlights([...vuelosPedido]);
    });
  };

  const handleSidebarRouteClick = (ruta) => {
    handleSidebarClickGeneral("route", ruta, () => {
      setSelectedAirport(null);
      setHighlightedRoute(null);
      setHighlightedFlights(ruta.codVuelos || []);
      const originAp = airports ? airports[ruta.codOrigen] : null;
      if (mapRef.current && originAp) {
        mapRef.current.setView([originAp.lat, originAp.lng], 4, {
          animate: true,
        });
      }
    });
  };

  const handleClearSelection = () => {
    setSelectedItem(null);
    setSelectedAirport(null);
    setHighlightedRoute(null);
    setHighlightedFlights([]);
    setHighlightedAirportCode(null);
    setFlightFilterCode("");
  };

  /*ESTADOS PARA EL MODAL DE COLAPSO*/
  // OPCIÓN A: Continuar simulando (Cierra modal, mantiene alerta, no borra reporte)
  const handleCollapseContinue = () => {
    setIsCollapseModalOpen(false);
    showNotification("info", "Continuando visualización del colapso...");
    // No hacemos handleStop(), dejamos que el timer siga corriendo hasta que acaben los vuelos
  };

  // OPCIÓN B: Descargar y Detener (Descarga, limpia mapa y detiene todo)
  const handleCollapseDownloadAndStop = async () => {
    if (!reporteListo) return;

    try {
      await downloadExportationFile(reporteListo);
      try {
        await deleteExportationFile(reporteListo);
      } catch (delError) {
        console.warn("No se pudo eliminar remoto:", delError);
      }

      showNotification("success", "Reporte guardado y eliminado. Limpiando...");
      setReporteListo(null);
      setReportPreviewContent("");
      if (subscriptionsRef.current) {
        subscriptionsRef.current.unsubscribe();
        subscriptionsRef.current = null;
      }
      setSimulationId(null);
      handleStop();
      setIsCollapseModalOpen(false);
    } catch (error) {
      console.error("Error descarga:", error);
      showNotification("danger", "Error al descargar. Intenta de nuevo.");
    }
  };
  // ------------------------------------------------------------------------
  // G. RENDER
  // ------------------------------------------------------------------------

  return (
    <div className="page">
      {/* Overlay de carga */}
      {showLoadingSim && (
        <SimulationLoadingOverlay text="Iniciando entorno de simulación..." />
      )}

      {/* Notificaciones */}
      {notification && (
        <Notification
          type={notification.type}
          message={notification.message}
          onClose={() => setNotification(null)}
        />
      )}

      {/* Sidebar */}
      <SimulationSidebar
        collapsed={collapsed}
        setCollapsed={setCollapsed}
        sidebarTab={sidebarTab}
        setSidebarTab={setSidebarTab}
        isSidebarPinned={isSidebarPinned}
        setIsSidebarPinned={setIsSidebarPinned}
        simNowMs={simNowMs}
        flights={flights}
        activeFlights={activeFlights}
        visibleOrders={visibleOrders}
        visibleAirports={visibleAirportsEnriched}
        routesInCurrentTime={routesInCurrentTime}
        baseOrders={baseOrders}
        airports={airports}
        selectedItem={selectedItem}
        selectedAirport={selectedAirport}
        onClearSelection={handleClearSelection}
        vuelosSaliendo={vuelosSaliendo}
        vuelosLlegando={vuelosLlegando}
        flightFilterCode={flightFilterCode}
        setFlightFilterCode={setFlightFilterCode}
        flightFilterOrigin={flightFilterOrigin}
        setFlightFilterOrigin={setFlightFilterOrigin}
        flightFilterDestination={flightFilterDestination}
        setFlightFilterDestination={setFlightFilterDestination}
        orderFilterCode={orderFilterCode}
        setOrderFilterCode={setOrderFilterCode}
        orderFilterEstado={orderFilterEstado}
        setOrderFilterEstado={setOrderFilterEstado}
        orderFilterDestino={orderFilterDestino}
        setOrderFilterDestino={setOrderFilterDestino}
        onlyHubs={onlyHubs}
        setOnlyHubs={setOnlyHubs}
        filteredActiveFlights={filteredActiveFlights}
        filteredOrders={filteredOrders}
        airportFilterText={airportFilterText}
        setAirportFilterText={setAirportFilterText}
        onFlightClick={handleSidebarFlightClick}
        onOrderClick={handleSidebarOrderClick}
        onAirportClick={handleSidebarAirportClick}
        onRouteClick={handleSidebarRouteClick}
        getAirportLabel={getAirportLabel}
        getAirportCityName={getAirportCityName}
        getOrdersForFlight={getOrdersForFlight}
        getLastFlightOfLote={getLastFlightOfLote}
        getAirportOccupancyClass={getAirportOccupancyClass}
      />

      <section className="contenido">
        <div className="map-and-info">
          {/* BOTÓN + PANEL DE CONTROLES */}
          <div className={`controls-dropdown ${controlsOpen ? "open" : ""}`}>
            <button
              className="controls-toggle"
              onClick={() => setControlsOpen((open) => !open)}
            >
              {controlsOpen ? "Ocultar controles ▲" : "Mostrar controles ▼"}
            </button>

            {controlsOpen && (
              <div className="control-bar">
                <div className="control-row control-row-main">
                  <span className="control-label">Controles:</span>
                  {!timerActive && (
                    <ButtonAdd
                      icon={run}
                      label="Generar plan"
                      onClick={openModal}
                    />
                  )}
                  {timerActive && (
                    <ButtonAdd
                      icon={stopIcon}
                      label="Detener"
                      type="button"
                      onClick={handleStop}
                      className="btn-stop"
                      disabled={stopDisabled}
                    />
                  )}
                </div>
                <hr
                  style={{
                    width: "100%",
                    borderColor: "#eee",
                    margin: "4px 0",
                  }}
                />

                {/* 1. TIEMPO SIMULADO */}
                <div className="control-row">
                  <span className="info-label" style={{ color: "#1a73e8" }}>
                    Simulación (Reloj):
                  </span>
                  <span className="value">
                    {toISODate(simNowMs)} {toISOTime(simNowMs)}
                  </span>
                </div>

                {/* 3. TIEMPO TRANSCURRIDO (Simulado) */}
                <div className="control-row">
                  <span className="info-label">Transcurrido (Simulado):</span>
                  <span className="value">
                    {simStartMs
                      ? formatDuration((simNowMs - simStartMs) / 1000)
                      : "00h : 00m : 00s"}
                  </span>
                </div>

                <hr
                  style={{
                    width: "100%",
                    borderColor: "#eee",
                    margin: "4px 0",
                  }}
                />

                {/* 2. TIEMPO REAL */}
                <div className="control-row">
                  <span className="info-label" style={{ color: "#666" }}>
                    Tiempo Real (UTC):
                  </span>
                  <span className="value">
                    {realNow.toISOString().split("T")[0]}{" "}
                    {realNow.toISOString().slice(11, 19)}
                  </span>
                </div>

                {/* 4. TIEMPO TRANSCURRIDO (Real/Cronómetro) */}
                <div className="control-row">
                  <span className="info-label">Cronómetro (Sesión):</span>
                  <span className="value">{formatDuration(seconds)}</span>
                </div>
              </div>
            )}
          </div>
          {/* ALERTA DE COLAPSO */}
          {isSystemCollapsed && !isCollapseModalOpen && (
            <div className="system-collapse-alert">
              <span>⚠️ SISTEMA EN ESTADO DE COLAPSO</span>
            </div>
          )}
          {/* MAPA */}
          <div className="map-wrapper">
            <MapContainer
              id="map"
              center={[20, 0]}
              zoom={3}
              minZoom={2}
              maxBounds={[
                [-90, -180],
                [90, 180],
              ]}
              whenCreated={(map) => {
                mapRef.current = map;
              }}
            >
              <TileLayer
                url="https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png"
                attribution='&copy; <a href="https://carto.com/">Carto</a>'
              />

              {/* AEROPUERTOS */}
              {airports &&
                Object.values(airports).map((ap, i) => {
                  const registros = ap.registros || [];
                  let stockActual = 0;
                  registros.forEach((reg) => {
                    if (!reg.sigueVigente) return;
                    const ingresoMs = parseFechaHoraToMs(reg.fechaHoraIngreso);
                    const egresoMs = reg.fechaHoraEgreso
                      ? parseFechaHoraToMs(reg.fechaHoraEgreso)
                      : null;
                    if (
                      simNowMs >= ingresoMs &&
                      (!egresoMs || simNowMs < egresoMs)
                    ) {
                      stockActual += reg.tamLote || 0;
                    }
                  });

                  const ocupacion =
                    ap.capacidad > 0 ? stockActual / ap.capacidad : 0;
                  const enrichedAp = { ...ap, stockActual, ocupacion };

                  const vuelosQueSalen = flights
                    .filter(
                      (f) =>
                        f.origin &&
                        f.origin.code === ap.code &&
                        typeof f.startMs === "number" &&
                        simNowMs < f.startMs
                    )
                    .slice()
                    .sort((a, b) => a.startMs - b.startMs);
                  const vuelosQueLlegan = flights
                    .filter(
                      (f) =>
                        f.destination &&
                        f.destination.code === ap.code &&
                        typeof f.endMs === "number" &&
                        simNowMs < f.endMs
                    )
                    .slice()
                    .sort((a, b) => a.endMs - b.endMs);

                  const isDimmed =
                    (highlightedAirportCode &&
                      highlightedAirportCode !== enrichedAp.code) ||
                    highlightedFlights.length > 0;

                  return (
                    <Marker
                      key={i}
                      position={[enrichedAp.lat, enrichedAp.lng]}
                      icon={createAirportIcon(enrichedAp)}
                      opacity={isDimmed ? 0.3 : 1}
                      eventHandlers={{
                        mouseover: () => {
                          setOpenAirportTooltipCode(enrichedAp.code);
                          setOpenFlightTooltipCode(null);
                        },
                        click: () => {
                          handleAirportClick(enrichedAp);
                        },
                      }}
                    >
                      {openAirportTooltipCode === enrichedAp.code && (
                        <Tooltip
                          direction="top"
                          opacity={0.95}
                          interactive
                          permanent
                          className="airport-tooltip"
                        >
                          <AirportTooltipContent
                            airport={enrichedAp}
                            vuelosQueSalen={vuelosQueSalen}
                            vuelosQueLlegan={vuelosQueLlegan}
                            getOrdersForFlight={getOrdersForFlight}
                            onOpenPanel={(ap) => handleAirportClick(ap)}
                          />
                        </Tooltip>
                      )}
                    </Marker>
                  );
                })}

              {/* VUELOS */}
              {flights.map((flight) => {
                if (
                  !flight ||
                  !flight.path ||
                  !Array.isArray(flight.path) ||
                  flight.path.length === 0
                ) {
                  return null;
                }
                const enVentanaVuelo =
                  timerActive &&
                  typeof flight.startMs === "number" &&
                  typeof flight.endMs === "number" &&
                  simNowMs >= flight.startMs &&
                  simNowMs < flight.endMs;

                if (!enVentanaVuelo) return null;

                const filterCss = getPlaneColorFilter(
                  flight.capacity,
                  flight.planeCapacity
                );
                const shouldDimOthers = highlightedFlights.length > 0;
                const isHighlighted = highlightedFlights.includes(flight.code);
                const isOriginHub = flight.origin && flight.origin.esSede;
                const routeNormalColor = isOriginHub
                  ? hubColorMap[flight.origin.code] || "#eb6774"
                  : "#eb6774";

                return (
                  <React.Fragment key={flight.code}>
                    <Polyline
                      key={flight.code + "-" + (isHighlighted ? "on" : "off")}
                      positions={flight.path.slice(
                        Math.floor(flight.path.length * (flight.progress ?? 0))
                      )}
                      color={
                        !shouldDimOthers
                          ? routeNormalColor
                          : isHighlighted
                          ? "#ff0019"
                          : "#e5e7eb"
                      }
                      weight={isHighlighted ? 4 : 2}
                      opacity={
                        !shouldDimOthers ? 0.8 : isHighlighted ? 1 : 0.03
                      }
                      dashArray="6, 10"
                      interactive={false}
                    />

                    {!flight.arrived && (
                      <Marker
                        position={flight.position}
                        opacity={
                          !shouldDimOthers ? 1 : isHighlighted ? 1 : 0.25
                        }
                        zIndexOffset={1000}
                        icon={createColoredIcon(
                          filterCss,
                          flight.rotation || 0
                        )}
                        riseOnHover={true}
                        eventHandlers={{
                          mouseover: () => {
                            setOpenFlightTooltipCode(flight.code);
                            setOpenAirportTooltipCode(null);
                          },
                          click: () => {
                            setSelectedItem({
                              type: "flight",
                              codigo: flight.code,
                            });
                            setSelectedAirport(null);
                          },
                        }}
                      >
                        {openFlightTooltipCode === flight.code && (
                          <Tooltip
                            direction="top"
                            opacity={0.95}
                            permanent
                            interactive
                            className="plane-tooltip"
                          >
                            <PlaneTooltipContent
                              flight={flight}
                              getOrdersForFlight={getOrdersForFlight}
                              onOpenPanel={() => handleFlightClick(flight)}
                            />
                          </Tooltip>
                        )}
                      </Marker>
                    )}
                  </React.Fragment>
                );
              })}

              <ClickHandler onMapClick={handleMapReset} />
            </MapContainer>

            {/* LEYENDA */}
            <div className="legend-overlay">
              <div
                className={`legend-card ${
                  legendCollapsed ? "legend-card--collapsed" : ""
                }`}
              >
                <button
                  type="button"
                  className="legend-card-header"
                  onClick={() => setLegendCollapsed((prev) => !prev)}
                >
                  <span className="legend-card-info-icon">i</span>
                  <span className="legend-card-title">Leyenda</span>
                  <span className="legend-card-toggle">
                    {legendCollapsed ? "▲" : "▼"}
                  </span>
                </button>
                {!legendCollapsed && (
                  <div className="legend-card-body">
                    <div className="legend-item">
                      <span className="legend-dot legend-dot--green" />
                      <span>Menos del 50% de capacidad</span>
                    </div>
                    <div className="legend-item">
                      <span className="legend-dot legend-dot--yellow" />
                      <span>Entre 50% y 75% de capacidad</span>
                    </div>
                    <div className="legend-item">
                      <span className="legend-dot legend-dot--red" />
                      <span>Entre 90% y 100% de capacidad</span>
                    </div>
                    <p className="legend-footnote">
                      Colores aplican a aviones y aeropuertos.
                    </p>
                  </div>
                )}
              </div>
            </div>

            {/* ESTADO DE FLOTA */}
            <div className="fleet-overlay">
              <div
                className={`legend-card ${
                  fleetPanelCollapsed ? "legend-card--collapsed" : ""
                }`}
              >
                <button
                  type="button"
                  className="legend-card-header"
                  onClick={() => setFleetPanelCollapsed(!fleetPanelCollapsed)}
                >
                  <span
                    className="legend-card-info-icon"
                    style={{ backgroundColor: fleetStatusColor, color: "#fff" }}
                  >
                    %
                  </span>
                  <span className="legend-card-title">Estado de Flota</span>
                  <span className="legend-card-toggle">
                    {fleetPanelCollapsed ? "▲" : "▼"}
                  </span>
                </button>
                {!fleetPanelCollapsed && (
                  <div
                    className="legend-card-body"
                    style={{
                      display: "flex",
                      flexDirection: "column",
                      gap: "8px",
                    }}
                  >
                    <div
                      style={{
                        display: "flex",
                        alignItems: "center",
                        gap: "8px",
                        fontSize: "13px",
                        marginBottom: "4px",
                      }}
                    >
                      <span
                        style={{
                          width: "12px",
                          height: "12px",
                          borderRadius: "50%",
                          backgroundColor: fleetStatusColor,
                          boxShadow: `0 0 6px ${fleetStatusColor}`,
                        }}
                      />
                      <span style={{ fontWeight: 600, color: "#374151" }}>
                        {fleetStatusLabel}
                      </span>
                    </div>
                    <div
                      style={{
                        display: "flex",
                        justifyContent: "space-between",
                        fontSize: "12px",
                        borderBottom: "1px solid #f3f4f6",
                        paddingBottom: "4px",
                      }}
                    >
                      <span style={{ color: "#6b7280" }}>
                        Vuelos en tránsito:
                      </span>
                      <strong style={{ color: "#111827" }}>
                        {activeFlights.length}
                      </strong>
                    </div>
                    <div
                      style={{
                        display: "flex",
                        justifyContent: "space-between",
                        fontSize: "12px",
                        marginTop: "2px",
                      }}
                    >
                      <span style={{ color: "#6b7280" }}>Ocupación Total:</span>
                      <strong style={{ color: "#111827" }}>
                        {fleetPercentage.toFixed(1)}%
                      </strong>
                    </div>
                    <div
                      style={{
                        width: "100%",
                        height: "6px",
                        background: "#e5e7eb",
                        borderRadius: "4px",
                        overflow: "hidden",
                      }}
                    >
                      <div
                        style={{
                          width: `${fleetPercentage}%`,
                          height: "100%",
                          background: fleetStatusColor,
                          transition: "width 0.5s ease",
                        }}
                      />
                    </div>
                    <div
                      style={{
                        fontSize: "11px",
                        color: "#9ca3af",
                        marginTop: "2px",
                        textAlign: "right",
                      }}
                      title={`Exacto: ${totalOccupied.toLocaleString()} / ${totalMax.toLocaleString()}`}
                    >
                      Carga:{" "}
                      <strong>{formatCompactNumber(totalOccupied)}</strong> /{" "}
                      {formatCompactNumber(totalMax)} u.
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* MODAL PLANIFICAR */}
      {isModalOpen && (
        <div className="modal" onClick={closeModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">Planificar</h3>
            </div>
            <div className="modal-body">
              <span className="sidebar-subtitle">Rango de simulación</span>
              <label>Fecha y hora de inicio (UTC)</label>
              <DateTimeInline
                dateValue={fechaI}
                timeValue={horaI}
                onDateChange={(e) => setFechaI(e.target.value)}
                onTimeChange={(e) => setHoraI(e.target.value)}
              />
              <label>Fecha y hora de fin (UTC)</label>
              <DateTimeInline
                dateValue={fechaF}
                timeValue={horaF}
                onDateChange={(e) => setFechaF(e.target.value)}
                onTimeChange={(e) => setHoraF(e.target.value)}
                disabled={true}
              />

              <span className="sidebar-subtitle">Configuración temporal</span>
              <label>Multiplicador temporal</label>
              <Input
                label="Multiplicador temporal"
                type="number"
                value={multiplicadorTemporal}
                onChange={(e) =>
                  setMultiplicadorTemporal(parseNumber(e.target.value))
                }
              />
              <label>Salto de algoritmo (minutos)</label>
              <Input
                label="Salto de algoritmo (minutos)"
                type="number"
                step="0.1"
                value={saltoDeAlgoritmo}
                onChange={(e) =>
                  setTamanioDeSaltoTemporal(parseFloat(e.target.value))
                }
              />

              <span className="sidebar-subtitle">Ciudades sede</span>
              <div className="selected-codes">
                {codOrigenes.map((cod) => (
                  <div key={cod} className="chip">
                    <span>{cod}</span>
                    <button
                      className="chip-remove"
                      onClick={() => {
                        setCodOrigenes(codOrigenes.filter((c) => c !== cod));
                      }}
                    >
                      ×
                    </button>
                  </div>
                ))}
              </div>
              <Dropdown2
                label="Códigos origen"
                multiple={true}
                value={codOrigenes}
                onChange={setCodOrigenes}
                options={aeropuertos.map((a) => ({
                  label: `${a.codigo} - ${a.ciudad} - ${a.pais}`,
                  value: a.codigo,
                }))}
              />

              <span className="sidebar-subtitle">
                Parámetros de planificación
              </span>
              <label>Máx. días entrega intercontinental</label>
              <Input
                label="Máx. días entrega intercontinental"
                type="number"
                value={maxDiasEntregaIntercontinental}
                onChange={(e) =>
                  setMaxDiasEntregaIntercontinental(parseNumber(e.target.value))
                }
              />
              <label>Máx. días entrega intracontinental</label>
              <Input
                label="Máx. días entrega intracontinental"
                type="number"
                value={maxDiasEntregaIntracontinental}
                onChange={(e) =>
                  setMaxDiasEntregaIntracontinental(parseNumber(e.target.value))
                }
              />
              <label>Máx. horas de recojo</label>
              <Input
                label="Máx. horas de recojo"
                type="number"
                value={maxHorasRecojo}
                onChange={(e) => setMaxHorasRecojo(parseNumber(e.target.value))}
              />
              <label>Mín. horas de estancia</label>
              <Input
                label="Mín. horas de estancia"
                type="number"
                value={minHorasEstancia}
                onChange={(e) =>
                  setMinHorasEstancia(parseNumber(e.target.value))
                }
              />
              <label>Máx. horas de estancia</label>
              <Input
                label="Máx. horas de estancia"
                type="number"
                value={maxHorasEstancia}
                onChange={(e) =>
                  setMaxHorasEstancia(parseNumber(e.target.value))
                }
              />
              <label>Probabilidad de replanificación</label>
              <Input
                label="Probabilidad de replanificación"
                type="number"
                value={probabilidadReplanificacion}
                onChange={(e) =>
                  setProbabilidadReplanificacion(parseNumber(e.target.value))
                }
              />
            </div>
            <div className="modal-footer">
              <button className="btn red" onClick={closeModal}>
                Cancelar
              </button>
              <button className="btn green" onClick={handlePlanear}>
                Planear
              </button>
            </div>
          </div>
        </div>
      )}
      {isReportModalOpen && (
        <div className="report-modal-overlay">
          <div className="report-modal-content">
            {/* HEADER */}
            <div className="report-modal-header">
              <h3 className="report-modal-title">
                <span>📋</span> Reporte de Simulación
              </h3>
            </div>

            {/* BODY */}
            <div className="report-modal-body">
              <div className="report-info-text">
                <p style={{ margin: 0 }}>
                  La simulación ha finalizado exitosamente.
                </p>
                <p
                  style={{ margin: "4px 0 0", fontSize: "12px", opacity: 0.8 }}
                >
                  Archivo generado:{" "}
                  <span className="report-filename">
                    {reporteListo?.nombre}
                  </span>
                </p>
              </div>

              {/* PREVIEW BOX */}
              <div className="report-preview-box">
                {reportPreviewContent || "Cargando vista previa..."}
              </div>
            </div>

            {/* FOOTER */}
            <div className="report-modal-footer">
              <button
                className="btn-report-action btn-report-close"
                onClick={handleModalClose}
              >
                🗑️ Descartar y Cerrar
              </button>

              <button
                className="btn-report-action btn-report-download"
                onClick={handleModalDownload}
              >
                📥 Descargar Reporte
              </button>
            </div>
          </div>
        </div>
      )}
      {/* Modal de colapso logístico */}
      {isCollapseModalOpen && (
        <div
          className="report-modal-overlay"
          style={{ backgroundColor: "rgba(69, 10, 10, 0.8)" }}
        >
          <div
            className="report-modal-content"
            style={{ border: "2px solid #ef4444" }}
          >
            <div
              className="report-modal-header"
              style={{ background: "#7f1d1d" }}
            >
              <h3 className="report-modal-title">
                <span>🚨</span> ¡COLAPSO LOGÍSTICO DETECTADO!
              </h3>
            </div>

            <div className="report-modal-body">
              <div
                className="report-info-text"
                style={{ fontSize: "16px", color: "#333" }}
              >
                <p>
                  La simulación ha alcanzado un estado crítico y no puede
                  cumplir con los pedidos solicitados.
                </p>
                <p>
                  Se ha generado un reporte de incidencias.{" "}
                  <strong>¿Qué desea hacer?</strong>
                </p>

                {reporteListo ? (
                  <div
                    style={{
                      marginTop: "12px",
                      padding: "8px",
                      background: "#fef2f2",
                      border: "1px solid #fca5a5",
                      borderRadius: "6px",
                      color: "#b91c1c",
                    }}
                  >
                    📄 Reporte listo: <strong>{reporteListo.nombre}</strong>
                  </div>
                ) : (
                  <div
                    style={{
                      marginTop: "12px",
                      fontStyle: "italic",
                      color: "#666",
                    }}
                  >
                    ⏳ Generando reporte de colapso...
                  </div>
                )}
              </div>
            </div>

            <div className="report-modal-footer" style={{ gap: "10px" }}>
              <button
                className="btn-report-action"
                style={{
                  backgroundColor: "#fff",
                  border: "1px solid #ccc",
                  color: "#333",
                }}
                onClick={handleCollapseContinue}
              >
                👀 Continuar visualización
              </button>

              <button
                className="btn-report-action"
                style={{
                  backgroundColor: "#dc2626",
                  color: "white",
                  border: "none",
                }}
                onClick={handleCollapseDownloadAndStop}
                disabled={!reporteListo}
              >
                {reporteListo
                  ? "📥 Descargar y Detener"
                  : "Esperando archivo..."}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
