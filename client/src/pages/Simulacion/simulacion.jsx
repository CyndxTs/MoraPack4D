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
} from "../../services/planificarService";

// Componentes UI
import {
  Notification,
  ButtonAdd,
  DateTimeInline,
  Dropdown2,
  Input,
  LoadingOverlay,
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

  // -- Tiempo --
  const [realNow, setRealNow] = useState(new Date());
  const [simStartMs, setSimStartMs] = useState(null);
  const [simNowMs, setSimNowMs] = useState(() => Date.now());
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

  // ------------------------------------------------------------------------
  // B. REFS
  // ------------------------------------------------------------------------
  const lastRealMsRef = useRef(null);
  const stopRequestedRef = useRef(false);
  const mapRef = useRef(null);

  // ------------------------------------------------------------------------
  // C. VALORES DERIVADOS (Calculados)
  // ------------------------------------------------------------------------

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
    const getRutasDeVuelo = (flightCode) => {
      const labels = (solution.rutasEnOperacion || [])
        .filter((r) => (r.codVuelos || []).includes(flightCode))
        .map((r) => `${r.codOrigen} → ${r.codDestino}`);
      return Array.from(new Set(labels));
    };

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
        registros: (a.registros || []).filter(
          (reg) => reg.sigueVigente === true
        ),
      };
    });
    const loteArrivalMap = {};
    Object.values(airportMap).forEach((ap) => {
      (ap.registros || []).forEach((reg) => {
        if (!reg.sigueVigente) return;
        if (!reg.codLote || !reg.fechaHoraIngreso) return;
        const loteCode = reg.codLote;
        const ingresoMs = parseFechaHoraToMs(reg.fechaHoraIngreso);
        const prev = loteArrivalMap[loteCode];
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

    const pedidosAtendidos = (solution.pedidosAtendidos || []).map((p) => {
      const segmentaciones = (p.segmentaciones || []).map((seg) => {
        const lotes = (seg.lotesPorRuta || []).map((lpr) => {
          const ruta = rutasPorCodigo[lpr.codRuta];
          const origen = ruta ? airportMap[ruta.codOrigen] : null;
          const destino = ruta ? airportMap[ruta.codDestino] : null;
          const vuelosLote =
            Array.isArray(lpr.codVuelos) && lpr.codVuelos.length > 0
              ? lpr.codVuelos
              : ruta?.codVuelos || [];
          const loteCodigo = lpr.lote.codigo;
          const arrivalInfo = loteArrivalMap[loteCodigo];
          return {
            codRuta: lpr.codRuta,
            loteCodigo,
            loteTamanio: lpr.lote.tamanio,
            loteEstado: lpr.lote.estado,
            vuelos: vuelosLote,
            origenCode: ruta?.codOrigen,
            destinoCode: ruta?.codDestino,
            origenNombre: origen?.city || ruta?.codOrigen,
            destinoNombre: destino?.city || ruta?.codDestino,
            arrivalAirportCode: arrivalInfo?.airportCode || null,
            arrivalAirportCity: arrivalInfo?.airportCity || null,
            arrivalFechaHoraIngreso: arrivalInfo?.ingreso || null,
          };
        });
        return {
          codigo: seg.codigo,
          fechaHoraAplicacion: seg.fechaHoraAplicacion,
          fechaHoraSustitucion: seg.fechaHoraSustitucion,
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

    setAirports((prevAirports) => {
      const merged = { ...(prevAirports || {}) };
      Object.entries(airportMap).forEach(([code, data]) => {
        const prev = merged[code] || {};
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

    const vuelosNuevos = solution.vuelosEnTransito || [];
    const codigosNuevos = new Set(vuelosNuevos.map((v) => v.codigo));

    setFlights((prevFlights) => {
      const prevByCode = new Map(prevFlights.map((f) => [f.code, f]));
      const nextFlights = [];

      vuelosNuevos.forEach((v) => {
        const origin = airportMap[v.codOrigen];
        const dest = airportMap[v.codDestino];
        if (!origin || !dest) {
          console.warn(
            `Vuelo ${v.codigo} omitido: no se encontró aeropuerto ${v.codOrigen} o ${v.codDestino}`
          );
          return;
        }
        const startMs = parseFechaHoraToMs(v.fechaHoraSalida);
        const endMs = parseFechaHoraToMs(v.fechaHoraLlegada);
        const durationSec = Math.max((endMs - startMs) / 1000, 60);
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
          nextFlights.push({
            ...prev,
            capacity: v.capacidadOcupada,
            planeCapacity: v.capacidadMaxima,
            rutas: rutasVuelo,
          });
        } else {
          const total = Math.max(endMs - startMs, 60 * 1000);
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
            const frac = (simNowMs - startMs) / total;
            progress = Math.min(Math.max(frac, 0), 1);
            const idx = Math.floor(progress * (path.length - 1));
            const pos = path[idx];
            const next = path[Math.min(idx + 1, path.length - 1)];

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
            rotation = bearing - 45;
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
  useEffect(() => {
    if (!timerActive || flights.length === 0) return;
    const allArrivedByTime = flights.every((f) => simNowMs >= f.endMs);
    if (allArrivedByTime) {
      showNotification("info", "Todos los vuelos han llegado a su destino.");
      setTimerRunning(false);
      setTimerActive(false);
    }
  }, [simNowMs, flights, timerActive]);

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

  // 8. WebSocket Connection
  useEffect(() => {
    connectSimulatorWS(
      (payload) => {
        console.log("SolutionPayload recibido por WS:", payload);
        const solucion = payload.solucion || payload;
        if (!solucion) {
          console.warn("Payload de simulación sin 'solucion'");
          return;
        }
        buildSimulationFromSolution(solucion);
      },
      (status) => {
        console.log("Status simulador:", status);
        const estadoEjecucion =
          typeof status === "string" ? status : status.estadoEjecucion;
        const estadoFinalizacion =
          typeof status === "string" ? null : status.estadoFinalizacion;

        if (!estadoEjecucion) return;

        setEstadoEjecucionSim(estadoEjecucion);
        if (estadoEjecucion === "POR_INICIAR") {
          setShowLoadingSim(true);
        } else {
          setShowLoadingSim(false);
        }

        if (estadoEjecucion === "POR_INICIAR") {
          setShowLoadingSim(true);
          showNotification("info", "Simulación por iniciar...");
        } else if (estadoEjecucion === "INICIADO") {
          showNotification("info", "Simulación iniciada");
        } else if (estadoEjecucion === "POR_DETENER") {
          if (!stopRequestedRef.current) {
            showNotification("info", "Deteniendo simulación...");
          }
        } else if (estadoEjecucion === "DETENIDO") {
          if (stopRequestedRef.current) {
            stopRequestedRef.current = false;
            return;
          }
          if (estadoFinalizacion === "EXITOSO") {
            showNotification("success", "Simulación finalizada exitosamente");
          } else if (estadoFinalizacion === "FORZADO") {
            showNotification("info", "Simulación detenida por el usuario");
          } else if (estadoFinalizacion === "COLAPSO") {
            showNotification("danger", "COLAPSO logístico en simulación");
          } else if (estadoFinalizacion === "ERRONEO") {
            showNotification("danger", "Error en la simulación");
          } else {
            showNotification("info", "Simulación detenida");
          }
        }
      }
    );

    const handleBeforeUnload = () => {
      stopRequestedRef.current = true;
      sendStopSimulation().catch((err) =>
        console.warn("Auto-stop on unload error:", err)
      );
    };

    window.addEventListener("beforeunload", handleBeforeUnload);

    return () => {
      window.removeEventListener("beforeunload", handleBeforeUnload);
      stopRequestedRef.current = true;
      sendStopSimulation().catch((err) =>
        console.warn("Auto-stop on unmount error:", err)
      );
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
  const handleStart = () => {
    if (timerActive && !timerRunning) {
      lastRealMsRef.current = performance.now();
      setTimerRunning(true);
      setStopDisabled(false);
      return;
    }
    const base = fromInputsToMsUTC(inputDate, inputTime);
    setSimNowMs(base);
    setSimStartMs(base);
    lastRealMsRef.current = performance.now();
    setTimerRunning(true);
    setTimerActive(true);
    setStopDisabled(false);
  };

  const handleStop = async () => {
    stopRequestedRef.current = true;
    showNotification("info", "Deteniendo simulación...");
    try {
      await sendStopSimulation();
    } catch (err) {
      console.warn("Error interno al detener simulación:", err);
    }
    setTimerRunning(false);
    setTimerActive(false);
    setSeconds(0);
    const now = new Date();
    const nowDate = now.toISOString().split("T")[0];
    const nowTime = now.toTimeString().slice(0, 5);
    setInputDate(nowDate);
    setInputTime(nowTime);
    setSimNowMs(now.getTime());
    setSimStartMs(null);
    setFlights([]);
    setSelectedItem(null);
    setSelectedAirport(null);
    setOrders([]);
    setSidebarTab("flights");
    setStopDisabled(true);
    setRoutes([]);
    setHighlightedRoute(null);
    setHighlightedFlights([]);
    setOpenAirportTooltipCode(null);
    setOpenFlightTooltipCode(null);
  };

  const handlePlanear = async () => {
    try {
      setLoading(true);
      if (!fechaI || !horaI || !fechaF || !horaF) {
        showNotification("danger", "Completa las fechas antes de continuar");
        setLoading(false);
        return;
      }
      /** @type {SimulationRequest} */
      const body = {
        fechaHoraInicio: `${fechaI}T${horaI}:00`,
        fechaHoraFin: `${fechaF}T${horaF}:00`,
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
      console.log("SimulationRequest enviado por HTTP:", body);
      setInputDate(fechaI);
      setInputTime(horaI);
      const res = await sendSimulationRequest(body);
      if (res && res.message) {
        showNotification("info", res.message);
      } else {
        showNotification("info", "Simulación en iniciación");
      }
      closeModal();
    } catch (err) {
      showNotification("danger", err.message || "Error al iniciar simulación");
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

  // ------------------------------------------------------------------------
  // G. RENDER
  // ------------------------------------------------------------------------

  return (
    <div className="page">
      {/* Overlay de carga */}
      {showLoadingSim && <LoadingOverlay text="Cargando Simulación..." />}

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
                    {fleetPanelCollapsed ? "▼" : "▲"}
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
    </div>
  );
}
