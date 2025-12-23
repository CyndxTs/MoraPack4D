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
import { onEvent, initOperationManager, disconnectOperationWS, connectOperatorWS, forceReplanification, getLastReplanificationToken } from "../../services/operationManager";
import { listarParametros, importarParametros } from "../../services/parametrosService";
import { listarAeropuertos } from "../../services/aeropuertoService";
import { descargarExportacion, iniciarExportacion, connectOperatorExportWS, disconnectExportWS } from "../../services/exportOpService";

// Componentes UI
import {
  Notification,
  ButtonAdd,
  DateTimeInline,
  Dropdown2,
  Input,
  LoadingOverlay,
  RangeSelector,
  TriPieSelector,
} from "../../components/UI/ui";
import OperationSidebar from "../Simulacion/OperationSidebar";
import { AirportTooltipContent, PlaneTooltipContent } from "../Simulacion/MapTooltips";

// Assets y Estilos
import "../Simulacion/simulacion.scss";
import "./planificacion.scss";
import run from "../../assets/icons/run.svg";
import config from "../../assets/icons/config.svg";
import download from "../../assets/icons/download.svg";
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

export default function Planificacion() {
  // ------------------------------------------------------------------------
  // A. ESTADOS (States)
  // ------------------------------------------------------------------------

  // -- Tiempo --
  const [realNow, setRealNow] = useState(new Date());

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
  const [isCooldown, setIsCooldown] = useState(false);

  // -- Modal y Formulario de Planificación --
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [fechaI, setFechaI] = useState("");
  const [horaI, setHoraI] = useState("");
  const [fechaF, setFechaF] = useState("");
  const [horaF, setHoraF] = useState("");
  const [loadedOnOpen, setLoadedOnOpen] = useState(false);
  const [codOrigenes, setCodOrigenes] = useState([]);
  const [parametrosCompletos, setParametrosCompletos] = useState(null);

  // -- Operacion diaria --
  const [reparametrizar, setReparametrizar] = useState(false);

  const [considerarDesfaseTemporal, setConsiderarDesfaseTemporal] = useState();

  const [dMin, setDMin] = useState();
  const [iMax, setIMax] = useState();
  const [eleMin, setEleMin] = useState();
  const [eleMax, setEleMax] = useState();
  const [kMin, setKMin] = useState();
  const [kMax, setKMax] = useState();
  const [tMax, setTMax] = useState();
  const [nMax, setNMax] = useState();

  const [factorDeUmbralDeAberracion, setFactorDeUmbralDeAberracion] = useState();
  const [factorDeUtilizacionTemporal, setFactorDeUtilizacionTemporal] = useState();
  const [factorDeDesviacionEspacial, setFactorDeDesviacionEspacial] = useState();
  const [factorDeDisposicionOperacional, setFactorDeDisposicionOperacional] = useState();

  // Inputs de simulación
  const [inputDate, setInputDate] = useState(
    new Date().toISOString().split("T")[0]
  );
  const [inputTime, setInputTime] = useState(
    new Date().toTimeString().slice(0, 5)
  );

  // Reporte
  const [reportPanelCollapsed, setReportPanelCollapsed] = useState(true);
  const [reporteDisponible, setReporteDisponible] = useState(null);

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
  // NUEVO EFFECT: RECUPERAR DATOS TRAS F5
  // ------------------------------------------------------------------------
  useEffect(() => {
    const backup = localStorage.getItem("SIMULATION_BACKUP");
    
    if (backup) {
      try {
        console.log("Recuperando sesión anterior tras F5...");
        const solucionRecuperada = JSON.parse(backup);
        
        // Reconstruimos toda la simulación con los datos guardados
        buildSimulationFromSolution(solucionRecuperada);
        
        // Opcional: Marcar estado visualmente
        setEstadoEjecucionSim("RECUPERADO"); 
        showNotification("info", "Datos restaurados de la sesión anterior.");
        
      } catch (error) {
        console.error("Error al leer backup:", error);
        // Si está corrupto, mejor borrarlo
        localStorage.removeItem("SIMULATION_BACKUP");
      }
    }
  }, []); // Array vacío para que solo corra al inicio (F5)
  // ------------------------------------------------------------------------
  // NUEVO EFFECT: RECUPERAR REPORTE TRAS F5
  // ------------------------------------------------------------------------
  useEffect(() => {
    const backupReporte = localStorage.getItem("REPORTE_DISPONIBLE_BACKUP");

    if (backupReporte) {
      try {
        console.log("Recuperando reporte tras F5...");
        const reporte = JSON.parse(backupReporte);
        setReporteDisponible(reporte);
      } catch (e) {
        console.error("Backup de reporte corrupto", e);
        localStorage.removeItem("REPORTE_DISPONIBLE_BACKUP");
      }
    }
  }, []);


  const limpiarSimulacion = () => {
      localStorage.removeItem("SIMULATION_BACKUP");
      localStorage.removeItem("SIMULATION_STATUS");
      // ✅ BORRAR REPORTE SOLO AQUÍ
      localStorage.removeItem("REPORTE_DISPONIBLE_BACKUP");
      window.location.reload(); // Recarga limpia
  };

  // ------------------------------------------------------------------------
  // B. REFS
  // ------------------------------------------------------------------------
  const stopRequestedRef = useRef(false);
  const mapRef = useRef(null);

  // ------------------------------------------------------------------------
  // C. VALORES DERIVADOS (Calculados)
  // ------------------------------------------------------------------------
  const nowMs = realNow.getTime();

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
      nowMs >= f.startMs &&
      nowMs < f.endMs
  );

  // Vuelos NO activos
  const nonActiveFlights = flights.filter(
    (f) =>
      !f ||
      typeof f.startMs !== "number" ||
      typeof f.endMs !== "number" ||
      nowMs < f.startMs ||
      nowMs >= f.endMs
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
    return nowMs >= maxEndMs;
  };

  const getOrderGenerationMs = (pedido) => {
    if (!pedido.fechaHoraGeneracion) return null;
    return parseFechaHoraToMs(pedido.fechaHoraGeneracion);
  };

  const isOrderGenerated = (pedido) => {
    const genMs = getOrderGenerationMs(pedido);
    if (!genMs) return true;
    return nowMs >= genMs;
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
        if (nowMs >= ingresoMs && (!egresoMs || nowMs < egresoMs)) {
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
          nowMs < f.startMs
      )
    : [];

  const vuelosLlegando = selectedAirport
    ? flights.filter(
        (f) =>
          f &&
          f.destination &&
          f.destination.code === selectedAirport.code &&
          typeof f.endMs === "number" &&
          nowMs < f.endMs
      )
    : [];

  // Rutas activas
  const routesInCurrentTime = routes.filter((ruta) => {
    if (!ruta.codVuelos || ruta.codVuelos.length === 0) return false;
    return ruta.codVuelos.some((codigoVuelo) => {
      const f = flights.find((fl) => fl.code === codigoVuelo);
      if (!f || typeof f.startMs !== "number" || typeof f.endMs !== "number")
        return false;
      return nowMs >= f.startMs && nowMs <= f.endMs;
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
    console.log("VUELOS EN TRANSITO (DTO):", vuelosNuevos);
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

          if (nowMs <= startMs) {
            progress = 0;
            position = path[0];
            arrived = false;
          } else if (nowMs >= endMs) {
            progress = 1;
            position = path[path.length - 1];
            arrived = true;
          } else {
            const frac = (nowMs - startMs) / total;
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
          nowMs < prevFlight.endMs
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
  useEffect(() => {
    initOperationManager()
      .then(() => console.log("[OM] Operation Manager iniciado"))
      .catch(err => console.error("[OM] Error init:", err));
  }, []);

  // 8. WebSocket Connection
    useEffect(() => {
      connectOperatorWS(
        (payload) => {
          console.log("SolutionPayload recibido por WS:", payload);
          const solucion = payload.solucion || payload;
          if (!solucion) {
            console.warn("Payload de operacion sin 'solucion'");
            return;
          }
          // 1. GUARDAR EN LOCALSTORAGE (Persistencia ante F5)
          // -----------------------------------------------------
          try {
            localStorage.setItem("SIMULATION_BACKUP", JSON.stringify(solucion)); 
            // También guarda el estado si viene separado, o asume que está corriendo
            localStorage.setItem("SIMULATION_STATUS", "RECUPERADO"); 
          } catch (e) {
            console.error("No se pudo guardar backup local (quizás es muy grande)", e);
          }
          buildSimulationFromSolution(solucion);
        },
        (status) => {
          console.log("Status operacion:", status);
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
              showNotification("success", "Operación finalizada exitosamente");
            } else if (estadoFinalizacion === "FORZADO") {
              showNotification("info", "Operación detenida por el usuario");
            } else if (estadoFinalizacion === "COLAPSO") {
              showNotification("danger", "COLAPSO logístico en operación");
            } else if (estadoFinalizacion === "ERRONEO") {
              showNotification("danger", "Error en la operación");
            } else {
              showNotification("info", "Operación detenida");
            }
          }
        },
      );
  
      const handleBeforeUnload = () => {
        stopRequestedRef.current = true;
      };
  
      window.addEventListener("beforeunload", handleBeforeUnload);
  
      return () => {
        window.removeEventListener("beforeunload", handleBeforeUnload);
        stopRequestedRef.current = true;
        disconnectOperationWS();
      };
    }, []);

  // En planificacion.jsx

  useEffect(() => {
    // Usamos una referencia para saber si el WS respondió a tiempo
    // (Esto evita que el timeout se ejecute si el WS sí funcionó)
    let wsRespondio = false; 

    const unsubscribe = onEvent(async (evt) => {
      console.log("EVENTO:", evt);

      if (evt.type === "replanificacion-iniciada") {
        const token = evt.token;

        // 1. Iniciamos la exportación por HTTP
        const tokenOperacion = evt.token; // <--- ESTE es el que da nombre al archivo (guardalo bien)
        console.log("TOKEN OPERACION: ",tokenOperacion);
        const data = await iniciarExportacion(tokenOperacion, "OPERACION");
        console.log("TOKEN OPERACION: ",data);
        const tokenExportacion = data.token; // <--- Este solo sirve para el WebSocket
        console.log("TOKEN OPERACION: ",tokenExportacion);

        // Reiniciamos la bandera para este nuevo intento
        wsRespondio = false;

        // 2. Intentamos conectar por WebSocket (Plan A)
        connectOperatorExportWS(
          tokenExportacion,
          (solution) => {
            wsRespondio = true; // ¡El WS ganó la carrera!
            console.log("✅ SolutionPayload recibido por WS (OPERACION):", solution);
            setReporteDisponible(solution);
            localStorage.setItem("REPORTE_DISPONIBLE_BACKUP", JSON.stringify(solution));
            showNotification("success", "Reporte generado (vía WS)");
          },
          (status) => {
            // Si llega status, también consideramos que el WS está vivo
            console.log("Status exportación:", status);
          }
        );

        // 3. FALLBACK / PLAN B: "Timeout de Seguridad"
        // Si en 1.5 segundos no ha llegado nada por WS, asumimos que ya terminó
        setTimeout(() => {
          if (!wsRespondio) {
            console.warn("⚠️ Usando Fallback manual.");
            
            // CORRECCIÓN: Usamos 'tokenOperacion' en vez de 'tokenExportacion'
            const idLimpio = tokenOperacion.startsWith("TOK-") 
                ? tokenOperacion.substring(4) 
                : tokenOperacion;

            const reporteManual = {
              nombre: `OPERACION__${idLimpio}.txt`, // Ahora sí coincidirá con el archivo en disco
              ruta: "exports" // Quita la barra final por si acaso
            };

            setReporteDisponible(reporteManual);
            localStorage.setItem("REPORTE_DISPONIBLE_BACKUP", JSON.stringify(reporteManual));
            showNotification("success", "Reporte listo (Manual)");
          }
        }, 1500);
      }
    });

    return () => {
      if (unsubscribe) unsubscribe();
      disconnectExportWS();
    };
  }, []);


  // ------------------------------------------------------------------------
  // ... REPORTE
  // ------------------------------------------------------------------------
  const descargandoRef = useRef(false);

  const descargarReporte = async () => {
    if (!reporteDisponible) {
      showNotification("info", "El reporte aún no está listo");
      return;
    }
    console.log("Reporte disponible: ",reporteDisponible);
    if (descargandoRef.current) return;
    descargandoRef.current = true;
    console.log("DescargandoRef: ",descargandoRef);
    try {
      const fileRequest = {
        nombre: reporteDisponible.nombre,
        ruta: reporteDisponible.ruta
      };
      const blob = await descargarExportacion(fileRequest);

      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");

      a.href = url;
      a.download = reporteDisponible.nombre;
      document.body.appendChild(a);
      a.click();

      a.remove();
      window.URL.revokeObjectURL(url);

    } catch (e) {
      console.error(e);
      showNotification(
        "danger",
        e.message || "Error al descargar el reporte"
      );
    } finally {
      descargandoRef.current = false;
    }
  };


  // ------------------------------------------------------------------------
  // ... USE EFFECT
  // ------------------------------------------------------------------------
  // 1. Reloj Real
  useEffect(() => {
    let animationFrameId;

    const animate = () => {
      setRealNow(new Date());
      animationFrameId = requestAnimationFrame(animate);
    };

    animate(); // Iniciar el loop

    return () => {
      if (animationFrameId) cancelAnimationFrame(animationFrameId);
    };
  }, []);

  // 2. Cronómetro y Loop de Animación


  // 3. Actualización de Posición de Vuelos (Con Interpolación Suave)
  useEffect(() => {
    setFlights((prev) =>
      prev.map((f) => {
        // Si no tiene ruta válida, devolver tal cual
        if (!f || !f.path || f.path.length === 0) return f;

        const total = Math.max(f.endMs - f.startMs, 60 * 1000); // Mínimo 1 min para evitar div/0

        // Caso: Aún no sale
        if (nowMs <= f.startMs) {
          return { ...f, progress: 0, position: f.path[0], arrived: false };
        }

        // Caso: Ya llegó
        if (nowMs >= f.endMs) {
          return {
            ...f,
            progress: 1,
            position: f.path[f.path.length - 1],
            arrived: true,
          };
        }

        // Caso: En vuelo (Cálculo Interpolado)
        const frac = Math.min((nowMs - f.startMs) / total, 1);
        
        // Calcular índices exactos
        const maxIndex = f.path.length - 1;
        const rawIndex = frac * maxIndex;       // Ej: 10.45
        const idx = Math.floor(rawIndex);       // Ej: 10
        const nextIdx = Math.min(idx + 1, maxIndex); // Ej: 11
        const segmentFrac = rawIndex - idx;     // Ej: 0.45 (Progreso entre punto 10 y 11)

        const pos = f.path[idx];
        const next = f.path[nextIdx];

        // INTERPOLACIÓN LINEAL: Calcula la lat/lng exacta entre los dos puntos
        const lat = pos.lat + (next.lat - pos.lat) * segmentFrac;
        const lng = pos.lng + (next.lng - pos.lng) * segmentFrac;

        // Cálculo de rotación (Bearing)
        const toRad = (d) => (d * Math.PI) / 180;
        const toDeg = (r) => (r * 180) / Math.PI;
        const lat1 = toRad(pos.lat), lon1 = toRad(pos.lng);
        const lat2 = toRad(next.lat), lon2 = toRad(next.lng);
        
        let bearing = Math.atan2(
          Math.sin(lon2 - lon1) * Math.cos(lat2),
          Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)
        );
        bearing = (toDeg(bearing) + 360) % 360;
        const rotation = bearing - 45; // Ajuste por tu icono

        return {
          ...f,
          progress: frac,
          position: { lat, lng }, // Usamos la posición interpolada
          rotation,
          arrived: frac >= 1,
        };
      })
    );
  }, [nowMs]);

  // 4. Limpieza de Highlights
  useEffect(() => {
    if (highlightedFlights.length === 0) return;
    const activos = highlightedFlights.filter((code) =>
      flights.some(
        (f) =>
          f.code === code &&
          typeof f.startMs === "number" &&
          typeof f.endMs === "number" &&
          nowMs >= f.startMs &&
          nowMs < f.endMs
      )
    );
    if (activos.length !== highlightedFlights.length) {
      setHighlightedFlights(activos);
    }
  }, [flights, highlightedFlights, nowMs]);

  // 5. Finalización Automática

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

        setDMin(p.dMin);
        setIMax(p.iMax);
        setEleMin(p.eleMin);
        setEleMax(p.eleMax);
        setKMin(p.kMin);
        setKMax(p.kMax);
        setTMax(p.tMax);
        setNMax(p.nMax);

        setFactorDeUmbralDeAberracion(p.factorDeUmbralDeAberracion);
        setFactorDeUtilizacionTemporal(p.factorDeUtilizacionTemporal);
        setFactorDeDesviacionEspacial(p.factorDeDesviacionEspacial);
        setFactorDeDisposicionOperacional(p.factorDeDisposicionOperacional);

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
  

  // 9. Auto-Start

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

  //Guardar parametros
  const handleGuardarParametros = async () => {
    const dto = {
      maxDiasEntregaIntracontinental,
      maxDiasEntregaIntercontinental,
      maxHorasRecojo,
      minHorasEstancia,
      maxHorasEstancia,
      codOrigenes,
      dMin,
      iMax,
      eleMin,
      eleMax,
      kMin,
      kMax,
      tMax,
      nMax,
      factorDeUmbralDeAberracion,
      factorDeUtilizacionTemporal,
      factorDeDesviacionEspacial,
      factorDeDisposicionOperacional,
      probabilidadReplanificacion
    };

    try {
      console.log("ENVIANDO > : ");
      console.log(dto);
      const res = await importarParametros(dto);
      showNotification("success", "Parámetros guardados correctamente");

      // Puedes cerrar modal aquí si quieres
      closeModal();

    } catch (error) {
      showNotification("danger", "Error al guardar los parámetros");
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

  const handleReplanClick = () => {
    // Ejecutamos la lógica de negocio
    forceReplanification();

    // Activamos el bloqueo
    setIsCooldown(true);

    // Desbloqueamos después de 3 segundos (3000ms)
    setTimeout(() => {
      setIsCooldown(false);
    }, 3000);
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
      <OperationSidebar
        collapsed={collapsed}
        setCollapsed={setCollapsed}
        sidebarTab={sidebarTab}
        setSidebarTab={setSidebarTab}
        isSidebarPinned={isSidebarPinned}
        setIsSidebarPinned={setIsSidebarPinned}
        simNowMs={nowMs}
        flights={flights}
        activeFlights={activeFlights}
        nonActiveFlights={nonActiveFlights}
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
                  <div className="control-label-buttons">
                    <ButtonAdd
                      icon={run}
                      // Opcional: Cambiar el texto si está bloqueado
                      label={isCooldown ? "Espere..." : "Replanificar"} 
                      
                      // Usamos nuestra nueva función wrapper
                      onClick={handleReplanClick} 
                      
                      // Pasamos la prop disabled que tu componente ya soporta
                      disabled={isCooldown} 
                      
                      // Opcional: Si quieres forzar que se vea diferente con estilo inline
                      // (aunque lo ideal es hacerlo por CSS)
                      style={{ opacity: isCooldown ? 0.6 : 1, cursor: isCooldown ? 'not-allowed' : 'pointer' }}
                    />
                    <ButtonAdd
                      icon={config}
                      label="Configurar parámetros"
                      onClick={openModal}
                    />
                    {/*<ButtonAdd
                      icon={stopIcon}
                      className="btn-stop"
                      label="Limpiar mapa"
                      onClick={limpiarSimulacion}
                    />*/}
                  </div>
                </div>
                {/*<hr
                  style={{
                    width: "100%",
                    borderColor: "#eee",
                    margin: "4px 0",
                  }}
                />*/}

                {/* 1. TIEMPO SIMULADO */}
                {/*<div className="control-row">
                  <span className="info-label" style={{ color: "#1a73e8" }}>
                    Simulación (Reloj):
                  </span>
                  <span className="value">
                    {toISODate(simNowMs)} {toISOTime(simNowMs)}
                  </span>
                </div>*/}

                {/* 3. TIEMPO TRANSCURRIDO (Simulado) */}
                {/*<div className="control-row">
                  <span className="info-label">Transcurrido (Simulado):</span>
                  <span className="value">
                    {simStartMs
                      ? formatDuration((simNowMs - simStartMs) / 1000)
                      : "00h : 00m : 00s"}
                  </span>
                </div>*/}

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
                {/*<div className="control-row">
                  <span className="info-label">Cronómetro (Sesión):</span>
                  <span className="value">{formatDuration(seconds)}</span>
                </div>*/}
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
                      nowMs >= ingresoMs &&
                      (!egresoMs || nowMs < egresoMs)
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
                        nowMs < f.startMs
                    )
                    .slice()
                    .sort((a, b) => a.startMs - b.startMs);
                  const vuelosQueLlegan = flights
                    .filter(
                      (f) =>
                        f.destination &&
                        f.destination.code === ap.code &&
                        typeof f.endMs === "number" &&
                        nowMs < f.endMs
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
                  typeof flight.startMs === "number" &&
                  typeof flight.endMs === "number" &&
                  nowMs >= flight.startMs &&
                  nowMs < flight.endMs;

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
                      positions={[
                        flight.position,
                        ...flight.path.slice(
                          Math.floor((flight.path.length - 1) * (flight.progress ?? 0)) + 1
                        ),
                      ]}
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
            {/* REPORTES */}
            <div className="report-overlay">
              <div
                className={`legend-card ${
                  reportPanelCollapsed ? "legend-card--collapsed" : ""
                }`}
              >
                <button
                  type="button"
                  className="legend-card-header"
                  onClick={() => setReportPanelCollapsed(!reportPanelCollapsed)}
                >
                  <span className="legend-card-info-icon">📄</span>
                  <span className="legend-card-title">Reporte</span>
                  <span className="legend-card-toggle">
                    {reportPanelCollapsed ? "▲" : "▼"}
                  </span>
                </button>

                {!reportPanelCollapsed && (
                  <div
                    className="legend-card-body"
                    style={{
                      display: "flex",
                      flexDirection: "column",
                      gap: "8px",
                    }}
                  >
                    <span style={{ fontSize: "12px", color: "#6b7280" }}>
                      {reporteDisponible
                        ? "Reporte listo para descarga"
                        : "Generando reporte..."}
                    </span>

                    <ButtonAdd
                      label="Descarga reporte"
                      icon={download} // o un icono de descarga si tienes
                      onClick={descargarReporte}
                    />
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
              <h3 className="modal-title">Configuración de parámetros</h3>
            </div>

            <div className="modal-body">
              
              <span className="sidebar-subtitle">Párametros</span>

                <div className={`parametros-container ${reparametrizar ? "open" : "closed"}`}>

                  <label>Ciudades sede</label>
                  <div className="selected-codes">
                    {codOrigenes.map((cod) => (
                      <div key={cod} className="chip">
                        <span>{cod}</span>
                        <button className="chip-remove" onClick={() => setCodOrigenes(codOrigenes.filter((c) => c !== cod))}>
                          ×
                        </button>
                      </div>
                    ))}
                  </div>

                  <Dropdown2
                    label="Códigos Origen"
                    multiple={true}
                    value={codOrigenes}
                    onChange={setCodOrigenes}
                    options={aeropuertos.map((a) => ({ label: `${a.codigo} - ${a.ciudad} - ${a.pais}`, value: a.codigo }))}
                  />

                  <label>Max días entrega intercontinental</label>
                  <RangeSelector 
                    min={1}
                    max={3}
                    step={1}
                    value={maxDiasEntregaIntercontinental}
                    onChange={(num) => setMaxDiasEntregaIntercontinental(parseNumber(num))}
                  />

                  <label>Max días entrega intracontinental</label>
                  <RangeSelector 
                    min={1}
                    max={3}
                    step={1}
                    value={maxDiasEntregaIntracontinental}
                    onChange={(num) => setMaxDiasEntregaIntracontinental(parseNumber(num))}
                  />

                  <label>Max horas recojo</label>
                  <RangeSelector 
                    min={1}
                    max={3}
                    step={1}
                    value={maxHorasRecojo}
                    onChange={(num) => setMaxHorasRecojo(parseNumber(num))}
                  />

                  <label>Min horas estancia</label>
                  <RangeSelector 
                    min={1}
                    max={12}
                    step={1}
                    value={minHorasEstancia}
                    onChange={(num) => {
                      const n = parseNumber(num);

                      setMinHorasEstancia(n);

                      // Asegurar que min < max
                      if (n >= maxHorasEstancia) {
                        setMaxHorasEstancia(n + 1 <= 12 ? n + 1 : 12);
                      }
                    }}
                  />

                  <label>Max horas estancia</label>
                  <RangeSelector 
                    min={1}
                    max={12}
                    step={1}
                    value={maxHorasEstancia}
                    onChange={(num) => {
                      const n = parseNumber(num);

                      setMaxHorasEstancia(n);

                      // Asegurar que max > min
                      if (n <= minHorasEstancia) {
                        setMinHorasEstancia(n - 1 >= 1 ? n - 1 : 1);
                      }
                    }}
                  />


                  <label>dMin</label>
                  <RangeSelector 
                    min={0.001}
                    max={0.010}
                    step={0.001}
                    value={dMin}
                    onChange={(num) => setDMin(parseNumber(num))}
                  />

                  <label>iMax</label>
                  <RangeSelector 
                    min={1}
                    max={3}
                    step={1}
                    value={iMax}
                    onChange={(num) => setIMax(parseNumber(num))}
                  />

                  <label>eleMin</label>
                  <RangeSelector 
                    min={1}
                    max={2}
                    step={1}
                    value={eleMin}
                    onChange={(num) => {
                      const n = parseNumber(num);

                      // Si eleMin = 1 → eleMax puede ser 2 o 3 (NO tocar)
                      // Si eleMin = 2 → eleMax DEBE ser 3
                      if (n === 2 && eleMax !== 3) {
                        setEleMax(3);
                      }

                      setEleMin(n);
                    }}
                  />

                  <label>eleMax</label>
                  <RangeSelector 
                    min={2}
                    max={3}
                    step={1}
                    value={eleMax}
                    onChange={(num) => {
                      const n = parseNumber(num);

                      // Si eleMax = 2 → eleMin DEBE ser 1
                      if (n === 2 && eleMin !== 1) {
                        setEleMin(1);
                      }

                      // Si eleMax = 3 → eleMin puede ser 1 o 2 (NO tocar)
                      setEleMax(n);
                    }}
                  />

                  <label>kMin</label>
                  <RangeSelector 
                    min={1}
                    max={10}
                    step={1}
                    value={kMin}
                    onChange={(num) => {
                      const n = parseNumber(num);

                      setKMin(n);

                      // Garantizar kMin < kMax
                      if (n >= kMax) {
                        const newMax = Math.min(n + 1, 11);
                        setKMax(newMax);
                      }
                    }}
                  />

                  <label>kMax</label>
                  <RangeSelector 
                    min={2}
                    max={11}
                    step={1}
                    value={kMax}
                    onChange={(num) => {
                      const n = parseNumber(num);

                      setKMax(n);

                      // Garantizar kMin < kMax
                      if (n <= kMin) {
                        const newMin = Math.max(n - 1, 1);
                        setKMin(newMin);
                      }
                    }}
                  />

                  <label>tMax</label>
                  <RangeSelector 
                    min={15}
                    max={60}
                    step={15}
                    value={tMax}
                    onChange={(num) => setTMax(parseNumber(num))}
                  />

                  <label>Max intentos</label>
                  <RangeSelector 
                    min={1}
                    max={10}
                    step={1}
                    value={nMax}
                    onChange={(num) => setNMax(parseNumber(num))}
                  />

                  <label>Probabilidad de replanificación</label>
                  <RangeSelector 
                    min={0}
                    max={0.55}
                    step={0.05}
                    value={probabilidadReplanificacion}
                    onChange={(num) => setProbabilidadReplanificacion(parseNumber(num))}
                  />

                  <label>Factor de Umbral de Aberración</label>
                  <RangeSelector 
                    min={1.015}
                    max={1.075}
                    step={0.015}
                    value={factorDeUmbralDeAberracion}
                    onChange={(num) => setFactorDeUmbralDeAberracion(parseNumber(num))}
                  />

                  <label>Factores</label>
                  <TriPieSelector
                    labels={[
                      "Factor de Utilización Temporal",
                      "Factor de Desviación Espacial",
                      "Factor de Disposición Operacional"
                    ]}
                    valores={[
                      factorDeUtilizacionTemporal,
                      factorDeDesviacionEspacial,
                      factorDeDisposicionOperacional
                    ]}
                    setters={[
                      setFactorDeUtilizacionTemporal,
                      setFactorDeDesviacionEspacial,
                      setFactorDeDisposicionOperacional
                    ]}
                  />

                </div>

            </div>

            <div className="modal-footer">
              <button className="btn red" onClick={closeModal}>
                Cancelar
              </button>
              <button className="btn green" onClick={handleGuardarParametros}>
                Guardar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
