import React, { useState, useEffect, useRef } from "react";
import "./simulacion.scss";
import {
  Dropdown,
  Legend,
  Notification,
  SidebarActions,
  ButtonAdd,
  DateTimeInline,
  Dropdown2,
  Input,
  LoadingOverlay,
} from "../../components/UI/ui";
import hideIcon from "../../assets/icons/hide-sidebar.png";
import run from "../../assets/icons/run.svg";
import stopIcon from "../../assets/icons/stop.svg";
import airportIconImg from "../../assets/icons/airport.svg";
import sedeIconImg from "../../assets/icons/sede.svg";
import { listarParametros } from "../../services/parametrosService";
import { listarAeropuertos } from "../../services/aeropuertoService";
import {
  connectSimulatorWS,
  sendSimulationRequest,
  sendStopSimulation,
  disconnectWS,
} from "../../services/planificarService";
import {
  AirportInfoPanel,
  FlightInfoPanel,
  OrderInfoPanel,
  RouteInfoPanel,
} from "./InfoPanels";
import {
  MapContainer,
  TileLayer,
  Marker,
  Popup,
  Polyline,
  useMapEvent,
  Tooltip,
} from "react-leaflet";
import { AirportTooltipContent, PlaneTooltipContent } from "./MapTooltips";
import "leaflet/dist/leaflet.css";
import L from "leaflet";
import planeIconImg from "../../assets/icons/planeMora.svg";
/**
 * @typedef {import("../../types/simulationRequest/SimulationRequest").SimulationRequest} SimulationRequest
 */
export default function Simulacion() {
  const [controlsOpen, setControlsOpen] = useState(false);
  const [collapsed, setCollapsed] = useState(true);
  // qué pestaña se ve en el sidebar: "flights" o "orders"
  const [sidebarTab, setSidebarTab] = useState("flights");
  const [codigoVuelo, setCodigoVuelo] = useState("");
  const [selectedItem, setSelectedItem] = useState(null);
  const [selectedAirport, setSelectedAirport] = useState(null);
  // -------- MODAL --------
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [fechaI, setFechaI] = useState("");
  const [horaI, setHoraI] = useState("");
  const [fechaF, setFechaF] = useState("");
  const [horaF, setHoraF] = useState("");
  const [loadedOnOpen, setLoadedOnOpen] = useState(false);
  const [aeropuertos, setAeropuertos] = useState([]);
  const [codOrigenes, setCodOrigenes] = useState([]);
  // estados de todos los parámetros
  const parseNumber = (v) => {
    if (v === "" || v === null || v === undefined) return null;

    const normalized = String(v).replace(",", "."); // admite 1,75 e igual 1.75
    const n = Number(normalized);

    return Number.isNaN(n) ? null : n; // así nunca guardas NaN en el estado
  };
  const [maxDiasEntregaIntercontinental, setMaxDiasEntregaIntercontinental] =
    useState();
  const [maxDiasEntregaIntracontinental, setMaxDiasEntregaIntracontinental] =
    useState();
  const [maxHorasRecojo, setMaxHorasRecojo] = useState();
  const [minHorasEstancia, setMinHorasEstancia] = useState();
  const [maxHorasEstancia, setMaxHorasEstancia] = useState();
  const [multiplicadorTemporal, setMultiplicadorTemporal] = useState();
  const [saltoDeAlgoritmo, setTamanioDeSaltoTemporal] = useState();
  const [saltoDeAlgoritmoInput, setSaltoDeAlgoritmoInput] = useState("");
  const [parametrosCompletos, setParametrosCompletos] = useState(null);
  const [probabilidadReplanificacion, setProbabilidadReplanificacion] =
    useState();
  const [estadoEjecucionSim, setEstadoEjecucionSim] = useState("POR_INICIAR");
  const [showLoadingSim, setShowLoadingSim] = useState(false);
  const [legendCollapsed, setLegendCollapsed] = useState(false);
  //Vuelos
  const [flights, setFlights] = useState([]);
  const [highlightedFlights, setHighlightedFlights] = useState([]);
  // NUEVOS estados -> para ver que no se cierre cuando quitamos el mouse
  const [openAirportTooltipCode, setOpenAirportTooltipCode] = useState(null);
  const [openFlightTooltipCode, setOpenFlightTooltipCode] = useState(null);
  //Rutas
  const [routes, setRoutes] = useState([]);
  const [highlightedRoute, setHighlightedRoute] = useState(null);
  //Pedidos
  const [orders, setOrders] = useState([]);
  //Aeropuertos
  const [airports, setAirports] = useState(null);
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
  // 🔎 Búsquedas y filtros SOLO DEL PANEL
  // Vuelos
  const [flightFilterCode, setFlightFilterCode] = useState("");
  const [flightFilterOrigin, setFlightFilterOrigin] = useState("");
  const [flightFilterDestination, setFlightFilterDestination] = useState("");

  // Pedidos
  const [orderFilterCode, setOrderFilterCode] = useState("");
  const [orderFilterDestino, setOrderFilterDestino] = useState("");
  // PENDIENTES = en tránsito / planificados, ENTREGADOS, TODOS
  const [orderFilterEstado, setOrderFilterEstado] = useState("PENDIENTES");

  // Aeropuertos
  const [onlyHubs, setOnlyHubs] = useState(false); // fila 61
  // Inputs de inicio de simulación (no se auto-actualizan)
  const [inputDate, setInputDate] = useState(
    new Date().toISOString().split("T")[0]
  );
  const [inputTime, setInputTime] = useState(
    new Date().toTimeString().slice(0, 5)
  );
  // Reloj de simulación (ms) y velocidad: 600 = 1s real -> 10 minutos simulados (1h en 6s)
  const [simNowMs, setSimNowMs] = useState(() => Date.now());
  const simSpeed =
    typeof multiplicadorTemporal === "number" && multiplicadorTemporal > 0
      ? multiplicadorTemporal
      : 500;
  // Refs internas para el avance suave
  const lastRealMsRef = useRef(null);
  const stopRequestedRef = useRef(false);
  const mapRef = useRef(null);

  // Helpers de tiempo (trabajamos en UTC porque tu JSON está en UTC)
  const toISODate = (ms) => new Date(ms).toISOString().split("T")[0];
  const toISOTime = (ms) => new Date(ms).toISOString().slice(11, 16);
  // El backend de planificación manda fechas tipo "03/11/2025 10:20"
  const parseFechaHoraToMs = (fechaHora) => {
    if (!fechaHora) return Date.now();
    const [fecha, hora] = fechaHora.split(" "); // "03/11/2025 10:20"
    const [dia, mes, anio] = fecha.split("/").map(Number);
    const [hh, mm] = hora.split(":").map(Number);
    return Date.UTC(anio, mes - 1, dia, hh, mm, 0); // Lo tratamos como UTC para ser consistentes con el resto de la simulación
  };

  const fromInputsToMsUTC = (d, t) => new Date(`${d}T${t}:00Z`).getTime();

  const [seconds, setSeconds] = useState(0);
  const [timerRunning, setTimerRunning] = useState(false);
  const [timerActive, setTimerActive] = useState(false); // indica si inició cronómetro (start clickeado)

  //Notificaciones
  const [notification, setNotification] = useState(null);
  const [loading, setLoading] = useState(false);
  const showNotification = (type, message) => {
    setNotification({ type, message });
    setTimeout(() => setNotification(null), 5000);
  };

  // Botones
  const [stopDisabled, setStopDisabled] = useState(true);

  // Cronómetro
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
      const elapsedRealMs = now - lastRealMsRef.current; // ms reales desde el último frame
      lastRealMsRef.current = now;

      // Avanzar reloj simulado según el multiplicador temporal del modal
      setSimNowMs((prev) => prev + elapsedRealMs * simSpeed);

      rafId = requestAnimationFrame(tick);
    };

    lastRealMsRef.current = performance.now();
    rafId = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(rafId);
  }, [timerRunning, simSpeed]);

  useEffect(() => {
    if (!timerActive) return;

    setFlights((prev) =>
      prev.map((f) => {
        if (!f || !f.path || f.path.length === 0) return f;

        const total = Math.max(f.endMs - f.startMs, 60 * 1000);

        // Aún no despega
        if (simNowMs <= f.startMs) {
          return { ...f, progress: 0, position: f.path[0], arrived: false };
        }

        // Ya llegó
        if (simNowMs >= f.endMs) {
          return {
            ...f,
            progress: 1,
            position: f.path[f.path.length - 1],
            arrived: true,
          };
        }

        // En tránsito - actualizar posición
        const frac = Math.min((simNowMs - f.startMs) / total, 1);
        const idx = Math.floor(frac * (f.path.length - 1));
        const pos = f.path[idx];
        const next = f.path[Math.min(idx + 1, f.path.length - 1)];

        // bearing → rotation
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
  // 🔄 Limpiar resaltado cuando los vuelos seleccionados ya no están visibles
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
          simNowMs < f.endMs // solo si el vuelo sigue en su ventana de vuelo
      )
    );

    // Si ya no queda ningún vuelo de los resaltados activo, quitamos el resaltado
    if (activos.length !== highlightedFlights.length) {
      setHighlightedFlights(activos);
    }
  }, [flights, highlightedFlights, simNowMs, timerActive]);

  const formatTime = (sec) => {
    const m = Math.floor(sec / 60)
      .toString()
      .padStart(2, "0");
    const s = (sec % 60).toString().padStart(2, "0");
    return `${m}:${s}`;
  };

  // Botones
  const handleStart = () => {
    // Si ya hay simulación activa pero está en pausa, SOLO reanuda
    if (timerActive && !timerRunning) {
      lastRealMsRef.current = performance.now(); // referencia para el RAF
      setTimerRunning(true);
      setStopDisabled(false);
      return;
    }

    // Primer inicio: fija el tiempo de simulación al valor de los inputs
    const base = fromInputsToMsUTC(inputDate, inputTime);
    setSimNowMs(base);
    lastRealMsRef.current = performance.now();

    setTimerRunning(true);
    setTimerActive(true);
    setStopDisabled(false);
  };

  const handleStop = async () => {
    // Marcamos que el stop lo inició el usuario
    stopRequestedRef.current = true;

    // ÚNICO mensaje que queremos mostrar por el botón
    showNotification("info", "Deteniendo simulación...");

    try {
      await sendStopSimulation();
    } catch (err) {
      // ❌ NO mostrar errores del stop
      console.warn("Error interno al detener simulación:", err);
      // Mantener el flag de stopRequested activo
    }

    // 1. Detener timers
    setTimerRunning(false);
    setTimerActive(false);
    setSeconds(0);

    // 2. Resetear fecha y hora (inputs + reloj simulado)
    const now = new Date();
    const nowDate = now.toISOString().split("T")[0];
    const nowTime = now.toTimeString().slice(0, 5);

    setInputDate(nowDate);
    setInputTime(nowTime);
    setSimNowMs(now.getTime()); // lo que se muestra en "Fecha" y "Hora"

    // 3. Limpiar mapa: vuelos + aeropuertos + panel de info
    setFlights([]);
    //setAirports(null);
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

  // Vuelos

  // 🚚 Pedidos (para el sidebar)
  const activeFlights = flights.filter(
    (f) =>
      f &&
      typeof f.startMs === "number" &&
      typeof f.endMs === "number" &&
      simNowMs >= f.startMs &&
      simNowMs < f.endMs
  );

  // Pedidos que aún no han sido completamente entregados
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

    // si no tiene vuelos asociados, lo consideramos aún no entregado
    if (maxEndMs === null) return false;

    // entregado si el tiempo simulado ya pasó la última llegada
    return simNowMs >= maxEndMs;
  };
  // ⏱️ helpers para saber si el pedido ya "existe" en la simulación
  const getOrderGenerationMs = (pedido) => {
    if (!pedido.fechaHoraGeneracion) return null;
    return parseFechaHoraToMs(pedido.fechaHoraGeneracion);
  };

  const isOrderGenerated = (pedido) => {
    const genMs = getOrderGenerationMs(pedido);
    if (!genMs) return true; // si por alguna razón no viene fecha, lo mostramos siempre
    return simNowMs >= genMs;
  };

  // Pedidos aún no entregados (pendientes / en tránsito) y YA generados
  const visibleOrders = orders.filter(
    (pedido) => isOrderGenerated(pedido) && !isOrderDelivered(pedido)
  );

  // Pedidos ya entregados y YA generados
  const deliveredOrders = orders.filter(
    (pedido) => isOrderGenerated(pedido) && isOrderDelivered(pedido)
  );

  // Vuelos filtrados por código / origen / destino
  const filteredActiveFlights = activeFlights.filter((f) => {
    if (flightFilterCode && f.code !== flightFilterCode) return false;
    if (flightFilterOrigin && f.origin?.code !== flightFilterOrigin)
      return false;
    if (
      flightFilterDestination &&
      f.destination?.code !== flightFilterDestination
    )
      return false;
    return true;
  });

  // Pedidos base según estado seleccionado
  const baseOrders =
    orderFilterEstado === "PENDIENTES"
      ? visibleOrders
      : orderFilterEstado === "ENTREGADOS"
      ? deliveredOrders
      : orders.filter(isOrderGenerated); // "TODOS" pero solo los ya generados

  // Pedidos filtrados por código y destino
  const filteredOrders = baseOrders.filter((p) => {
    if (
      orderFilterCode &&
      !p.codigo.toUpperCase().includes(orderFilterCode.toUpperCase())
    ) {
      return false;
    }

    if (orderFilterDestino && p.codDestino !== orderFilterDestino) return false;
    return true;
  });

  // Aeropuertos visibles según "solo sedes" (fila 61)
  const visibleAirports =
    airports &&
    Object.values(airports).filter((ap) => (onlyHubs ? ap.esSede : true));

  // === Vuelos saliendo / llegando del aeropuerto seleccionado ===
  const vuelosSaliendo = selectedAirport
    ? flights.filter(
        (f) =>
          f &&
          f.origin &&
          f.origin.code === selectedAirport.code &&
          typeof f.startMs === "number" &&
          simNowMs < f.startMs //
      )
    : [];

  const vuelosLlegando = selectedAirport
    ? flights.filter(
        (f) =>
          f &&
          f.destination &&
          f.destination.code === selectedAirport.code &&
          typeof f.endMs === "number" &&
          simNowMs < f.endMs //
      )
    : [];

  const haySeleccionAeropuerto =
    !!selectedAirport &&
    (vuelosSaliendo.length > 0 || vuelosLlegando.length > 0);

  const infoPanelExpanded = !!(selectedAirport || selectedItem);

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

  // Ícono de aeropuerto coloreado según ocupación (semáforo)
  function getAirportFilter(ocupacion) {
    if (ocupacion == null) {
      return "none";
    }

    if (ocupacion < 0.5) {
      // VERDE más oscuro
      return "invert(38%) sepia(77%) saturate(510%) hue-rotate(85deg) brightness(55%) contrast(120%)";
    }

    if (ocupacion < 0.8) {
      // AMARILLO más oscuro
      return "invert(74%) sepia(94%) saturate(750%) hue-rotate(2deg) brightness(60%) contrast(125%)";
    }

    // ROJO más oscuro
    return "invert(26%) sepia(88%) saturate(900%) hue-rotate(350deg) brightness(55%) contrast(130%)";
  }

  const createAirportIcon = (ap) => {
    // si es sede usamos sede.svg, si no el icono normal de aeropuerto
    const baseIcon = ap.esSede ? sedeIconImg : airportIconImg;

    return L.divIcon({
      html: `
      <img
        src="${baseIcon}"
        class="airport-icon ${ap.esSede ? "airport-icon--hub" : ""}"
        style="
          width: 20px;
          height: 20px;
          filter: ${getAirportFilter(ap.ocupacion)};
        "
      />
    `,
      className: "",
      iconSize: [20, 20],
      iconAnchor: [10, 10],
      popupAnchor: [0, -10],
    });
  };

  // Detener cronómetro cuando todos los vuelos hayan llegado
  useEffect(() => {
    if (!timerActive || flights.length === 0) return;
    const allArrivedByTime = flights.every((f) => simNowMs >= f.endMs);
    if (allArrivedByTime) {
      showNotification("info", "Todos los vuelos han llegado a su destino.");
      setTimerRunning(false);
      setTimerActive(false);
    }
  }, [simNowMs, flights, timerActive]);

  // Calcula puntos de una ruta geodésica (gran círculo)
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
        A * Math.cos(lat1) * Math.cos(lon1) +
        B * Math.cos(lat2) * Math.cos(lon2);
      const y =
        A * Math.cos(lat1) * Math.sin(lon1) +
        B * Math.cos(lat2) * Math.sin(lon2);
      const z = A * Math.sin(lat1) + B * Math.sin(lat2);
      const lat = Math.atan2(z, Math.sqrt(x ** 2 + y ** 2));
      const lon = Math.atan2(y, x);
      points.push({ lat: toDeg(lat), lng: toDeg(lon) });
    }
    return points;
  }
  //
  function getPlaneColorFilter(capacity, maxCapacity) {
    if (!maxCapacity || maxCapacity <= 0) {
      // amarillo por defecto, más intenso
      return "invert(80%) sepia(72%) saturate(657%) hue-rotate(3deg) brightness(88%) contrast(115%)";
    }

    const ratio = capacity / maxCapacity;

    // VERDE → menos del 50% (más oscuro y con más contraste)
    if (ratio < 0.5) {
      return "invert(54%) sepia(81%) saturate(356%) hue-rotate(85deg) brightness(78%) contrast(115%)";
    }

    // AMARILLO → 50% a 75%
    if (ratio >= 0.5 && ratio < 0.75) {
      return "invert(80%) sepia(72%) saturate(657%) hue-rotate(3deg) brightness(86%) contrast(118%)";
    }

    // ROJO → 90% a 100%
    if (ratio >= 0.9) {
      return "invert(37%) sepia(79%) saturate(844%) hue-rotate(338deg) brightness(78%) contrast(120%)";
    }

    // 75%–90% → amarillo intenso
    return "invert(80%) sepia(72%) saturate(657%) hue-rotate(3deg) brightness(86%) contrast(118%)";
  }

  function ClickHandler({ onMapClick }) {
    useMapEvent("click", () => onMapClick());
    return null; // no renderiza nada visible
  }
  const getAirportOccupancyClass = (ocupacion) => {
    if (ocupacion == null) return "airport-card--unknown";
    if (ocupacion < 0.5) return "airport-card--low";
    if (ocupacion < 0.8) return "airport-card--medium";
    return "airport-card--high";
  };
  //MODAL
  useEffect(() => {
    const fetchParametrosYAeropuertos = async () => {
      try {
        /** @type {ParametrosResponse} */
        const parametrosResponse = await listarParametros();
        /** @type {ParametrosDTO} */
        const p = parametrosResponse.dtos[0];
        setParametrosCompletos(p);
        const a = await listarAeropuertos();
        setAeropuertos(a.dtos ?? []);
        console.log("Aeropuertos cargados:", a.dtos);
        // === SOLO LOS 5 PARAMETROS A MOSTRAR EN EL POPUP ===
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
  // Cargar aeropuertos base al inicio para que el mapa nunca esté vacío
  useEffect(() => {
    const fetchAeropuertosIniciales = async () => {
      try {
        const res = await listarAeropuertos();
        const dtos = res.dtos ?? [];

        // Los transformamos al mismo formato que usa buildSimulationFromSolution
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
            registros: [], // al inicio sin registros de stock
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
  const buildSimulationFromSolution = (solution) => {
    if (!solution) return;
    const getRutasDeVuelo = (flightCode) => {
      const labels = (solution.rutasEnOperacion || [])
        .filter((r) => (r.codVuelos || []).includes(flightCode))
        .map((r) => `${r.codOrigen} → ${r.codDestino}`);

      // eliminamos duplicados
      return Array.from(new Set(labels));
    };

    // 1) Mapear aeropuertosTransitados → airports (mapa por código)
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
        ), // aquí vienen los REG-... con tamLote
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
    // Mapa rápido de rutas por código
    const rutasPorCodigo = {};
    (solution.rutasEnOperacion || []).forEach((r) => {
      rutasPorCodigo[r.codigo] = r;
    });

    // Guardar rutas completas para el sidebar
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

    // Construir pedidos con segmentaciones, lotes y vuelos
    const pedidosAtendidos = (solution.pedidosAtendidos || []).map((p) => {
      const segmentaciones = (p.segmentaciones || []).map((seg) => {
        const lotes = (seg.lotesPorRuta || []).map((lpr) => {
          const ruta = rutasPorCodigo[lpr.codRuta];
          const origen = ruta ? airportMap[ruta.codOrigen] : null;
          const destino = ruta ? airportMap[ruta.codDestino] : null;

          // 👇 1) vuelos específicos del lote (si vienen en el JSON)
          //    2) si no hay, usamos los de la ruta como antes
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
            vuelos: vuelosLote, // 👈 AHORA sí está bien
            origenCode: ruta?.codOrigen,
            destinoCode: ruta?.codDestino,

            // 👇 Llegada basada en registros de aeropuertos
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
    // Actualizar airports solo si cambió
    setAirports((prevAirports) => {
      const merged = { ...(prevAirports || {}) };

      Object.entries(airportMap).forEach(([code, data]) => {
        const prev = merged[code] || {};

        // 👇 si data.esSede viene undefined/null, conservamos el de antes
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

    // Crear un Set con los códigos de vuelos que vienen en la nueva solución
    const codigosNuevos = new Set(vuelosNuevos.map((v) => v.codigo));

    // 2) MERGE inteligente: mantener progreso de vuelos existentes
    setFlights((prevFlights) => {
      const prevByCode = new Map(prevFlights.map((f) => [f.code, f]));
      const nextFlights = [];

      // PASO 1: Procesar vuelos de la nueva solución
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
        const rutasVuelo = getRutasDeVuelo(v.codigo); // sin duplicados
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
          const rutasDelVuelo = (solution.rutasEnOperacion || []).filter((r) =>
            (r.codVuelos || []).includes(v.codigo)
          );

          if (simNowMs <= startMs) {
            progress = 0;
            position = path[0];
            arrived = false;
          } else if (simNowMs >= endMs) {
            // Ya llegó
            progress = 1;
            position = path[path.length - 1];
            arrived = true;
          } else {
            // En vuelo → ubicarlo en el punto correspondiente
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

      // PASO 2: Mantener vuelos existentes que NO están en la nueva solución
      // pero que aún están en tránsito (no han llegado)
      prevFlights.forEach((prevFlight) => {
        // Si el vuelo NO viene en la nueva solución pero está en tránsito, lo mantenemos
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
          setShowLoadingSim(true); // mostrar overlay
        } else {
          setShowLoadingSim(false); // ocultar overlay
        }

        if (estadoEjecucion === "POR_INICIAR") {
          setShowLoadingSim(true);
          showNotification("info", "Simulación por iniciar...");
        } else if (estadoEjecucion === "INICIADO") {
          showNotification("info", "Simulación iniciada");
        } else if (estadoEjecucion === "POR_DETENER") {
          // Si el stop no vino del botón, dejamos que el back muestre este mensaje
          if (!stopRequestedRef.current) {
            showNotification("info", "Deteniendo simulación...");
          }
        } else if (estadoEjecucion === "DETENIDO") {
          if (stopRequestedRef.current) {
            // limpiamos el flag para futuras simulaciones
            stopRequestedRef.current = false;
            return;
          }

          // Si NO vino del botón (terminó sola / por error), mantenemos la lógica original
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

    return () => {
      disconnectWS();
    };
  }, []);

  useEffect(() => {
    // Solo auto-inicia cuando el estado pasa a INICIADO
    if (!showLoadingSim && estadoEjecucionSim === "INICIADO") {
      handleStart();
    }
  }, [showLoadingSim, estadoEjecucionSim]);

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

      const res = await sendSimulationRequest(body); // 👈 ahora va por /api/simulation-init
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

  // LIMPIAR MODAL SIEMPRE QUE SE CIERRA
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

  // Manejo de fechas según tipo de simulación
  useEffect(() => {
    if (fechaI && horaI) {
      const start = new Date(`${fechaI}T${horaI}:00Z`);
      const end = new Date(start.getTime() + 7 * 24 * 60 * 60 * 1000);

      setFechaF(end.toISOString().slice(0, 10));
      setHoraF(end.toISOString().slice(11, 16));
    }
  }, [fechaI, horaI]);

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
    if (!lote || !Array.isArray(lote.vuelos) || lote.vuelos.length === 0) {
      return null;
    }

    // Vamos desde el último, por si alguno no está en `flights` todavía
    for (let i = lote.vuelos.length - 1; i >= 0; i--) {
      const code = lote.vuelos[i];
      const f = flights.find((fl) => fl.code === code);
      if (f) return f;
    }

    return null;
  };

  const selectedFlight =
    selectedItem?.type === "flight"
      ? flights.find((f) => f.code === selectedItem.codigo)
      : null;

  const selectedOrder =
    selectedItem?.type === "order"
      ? orders.find((o) => o.codigo === selectedItem.codigo)
      : null;

  const selectedRoute =
    selectedItem?.type === "route"
      ? routes.find((r) => r.codigo === selectedItem.codigo)
      : null;
  const routesInCurrentTime = routes.filter((ruta) => {
    if (!ruta.codVuelos || ruta.codVuelos.length === 0) return false;

    return ruta.codVuelos.some((codigoVuelo) => {
      const f = flights.find((fl) => fl.code === codigoVuelo);
      if (!f || typeof f.startMs !== "number" || typeof f.endMs !== "number") {
        return false;
      }
      // Ruta visible solo si al menos uno de sus vuelos está activo
      return simNowMs >= f.startMs && simNowMs <= f.endMs;
    });
  });
  const infoPanelVariant = selectedAirport
    ? "info-panel--wide"
    : "info-panel--compact";
  return (
    <div className="page">
      {/* Overlay de carga de simulación */}
      {showLoadingSim && <LoadingOverlay text="Cargando Simulación..." />}

      {notification && (
        <Notification
          type={notification.type}
          message={notification.message}
          onClose={() => setNotification(null)}
        />
      )}

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

            {/* ===== CONTENIDO PESTAÑA VUELOS ===== */}
            {sidebarTab === "flights" && (
              <>
                <div className="active-flights-header">
                  <span className="active-flights-title">
                    Vuelos en tránsito
                  </span>
                  <span className="active-flights-count">
                    {activeFlights.length}
                  </span>
                </div>

                {/* 🔎 Filtros de vuelos: código / origen / destino */}
                <div className="filters-row">
                  {/* Código de vuelo (solo activos) */}
                  <select
                    className="filter-select"
                    value={flightFilterCode}
                    onChange={(e) => setFlightFilterCode(e.target.value)}
                  >
                    <option value="">Selecciona código</option>
                    {Array.from(new Set(activeFlights.map((f) => f.code))).map(
                      (code) => (
                        <option key={code} value={code}>
                          {code}
                        </option>
                      )
                    )}
                  </select>

                  {/* Origen (solo de vuelos activos) */}
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

                  {/* Destino (solo de vuelos activos) */}
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

                      // 📦 Pedidos asignados a este vuelo (fila 73)
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
                          onClick={() => {
                            setSelectedAirport(null);

                            setSelectedItem({
                              type: "flight",
                              codigo: flight.code,
                            });

                            setHighlightedRoute(null);
                            setHighlightedFlights([flight.code]);
                            setOpenFlightTooltipCode(flight.code);
                            setOpenAirportTooltipCode(null);

                            const pos = flight.position || {
                              lat: flight.origin.lat,
                              lng: flight.origin.lng,
                            };

                            if (mapRef.current && pos) {
                              mapRef.current.setView([pos.lat, pos.lng], 4, {
                                animate: true,
                              });
                            }
                          }}
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

                            {/* 🛣️ Rutas planificadas para este vuelo (fila 72) */}
                            <div className="flight-card-row">
                              <span className="flight-label">Rutas</span>
                              <span className="flight-value">
                                {flight.rutas && flight.rutas.length > 0
                                  ? flight.rutas.join(", ")
                                  : "Sin rutas"}
                              </span>
                            </div>

                            {/* 📦 Resumen de pedidos del vuelo (fila 73) */}
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

                {/* 🔎 Filtros de pedidos: estado / código / destino */}
                <div className="filters-row">
                  {/* Estado: pendientes / entregados / todos */}
                  <select
                    className="filter-select"
                    value={orderFilterEstado}
                    onChange={(e) => setOrderFilterEstado(e.target.value)}
                  >
                    <option value="PENDIENTES">En tránsito / pendientes</option>
                    <option value="ENTREGADOS">Entregados</option>
                    <option value="TODOS">Todos</option>
                  </select>

                  <input
                    type="text"
                    className="filter-select" // reutilizas el mismo estilo
                    placeholder="Buscar código"
                    value={orderFilterCode}
                    onChange={(e) =>
                      setOrderFilterCode(e.target.value.toUpperCase())
                    }
                  />

                  <select
                    className="filter-select"
                    value={orderFilterDestino}
                    onChange={(e) => setOrderFilterDestino(e.target.value)}
                  >
                    <option value="">Selecciona destino</option>
                    {Array.from(
                      new Set(
                        baseOrders
                          .map((p) => p.codDestino)
                          .filter((cod) => !!cod)
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
                        onClick={() => {
                          setSelectedAirport(null);
                          setHighlightedRoute(null);

                          const vuelosPedido = new Set();

                          pedido.segmentaciones.forEach((seg) => {
                            seg.lotes.forEach((lote) => {
                              (lote.vuelos || []).forEach((v) =>
                                vuelosPedido.add(v)
                              );
                            });
                          });

                          setHighlightedFlights([...vuelosPedido]);

                          setSelectedItem({
                            type: "order",
                            codigo: pedido.codigo,
                          });
                        }}
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
                                  segIndex ===
                                    pedido.segmentaciones.length - 1 &&
                                  loteIndex === seg.lotes.length - 1;

                                const destinoCodeBase =
                                  lote.destinoCode ||
                                  lote.arrivalAirportCode ||
                                  "";

                                const fechaLlegada =
                                  lote.arrivalFechaHoraIngreso ||
                                  "Sin información";

                                // 👉 Último vuelo del lote (si tiene varios)
                                const lastFlight = getLastFlightOfLote(lote);

                                // Valores por defecto (como antes)
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

                                // Si encontramos último vuelo, usamos su origen/destino
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
                                    {/* Segmentación */}
                                    <div className="order-row">
                                      <span className="order-label">
                                        Segmentación
                                      </span>
                                      <span className="order-value">
                                        {lote.loteTamanio} u.
                                      </span>
                                    </div>

                                    {/* Ruta: SOLO el último vuelo hacia la ciudad destino */}
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

                                    {/* Llegada */}
                                    <div className="order-row">
                                      <span className="order-label">
                                        Llegada
                                      </span>
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
            {/* ===== CONTENIDO PESTAÑA AEROPUERTOS ===== */}
            {sidebarTab === "airports" && (
              <div className="sidebar-section sidebar-section--airports">
                <div className="orders-header">
                  <span className="orders-title">Aeropuertos</span>
                  <span className="orders-count">
                    {visibleAirports ? visibleAirports.length : 0}
                  </span>
                </div>

                {/* ✅ Filtro "solo sedes" (fila 61) */}
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
                      const registros = ap.registros || [];
                      let stockActual = 0;

                      registros.forEach((reg) => {
                        // 👈 Solo consideramos registros vigentes
                        if (!reg.sigueVigente) return;

                        const ingresoMs = parseFechaHoraToMs(
                          reg.fechaHoraIngreso
                        );
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
                          onClick={() => {
                            const enrichedAp = {
                              ...ap,
                              stockActual,
                              ocupacion,
                            };

                            setSelectedAirport(enrichedAp);
                            setSelectedItem(null);
                            setHighlightedRoute(null);
                            setHighlightedFlights([]);
                            setOpenAirportTooltipCode(ap.code);
                            setOpenFlightTooltipCode(null);

                            if (mapRef.current) {
                              mapRef.current.setView([ap.lat, ap.lng], 4, {
                                animate: true,
                              });
                            }
                          }}
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
                    onClick={() => {
                      setSelectedAirport(null);

                      // Para el panel inferior
                      setSelectedItem({
                        type: "route",
                        codigo: ruta.codigo,
                      });

                      // ❌ Ya no usamos polyline especial para la ruta
                      setHighlightedRoute(null);

                      // ✅ Solo resaltamos los vuelos asociados a esta ruta
                      setHighlightedFlights(ruta.codVuelos || []);
                    }}
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

      <section className="contenido">
        <div className="map-and-info">
          {/* BOTÓN + PANEL DE CONTROLES (arriba izquierda) */}
          <div className={`controls-dropdown ${controlsOpen ? "open" : ""}`}>
            <button
              className="controls-toggle"
              onClick={() => setControlsOpen((open) => !open)}
            >
              {controlsOpen ? "Ocultar controles ▲" : "Mostrar controles ▼"}
            </button>

            {controlsOpen && (
              <div className="control-bar">
                {/* Fila 1: Controles + botones */}
                <div className="control-row control-row-main">
                  <span className="control-label">Controles:</span>

                  <ButtonAdd
                    icon={run}
                    label="Generar plan"
                    onClick={openModal}
                  />

                  <ButtonAdd
                    icon={stopIcon}
                    label="Detener"
                    type="button"
                    onClick={handleStop}
                    className="btn-stop"
                    disabled={stopDisabled}
                  />
                </div>

                {/* Fila 2: Fecha + Hora */}
                <div className="control-row">
                  <span className="info-label">Fecha:</span>
                  <span className="value">{toISODate(simNowMs)}</span>

                  <span className="info-label">Hora:</span>
                  <span className="value">{toISOTime(simNowMs)}</span>
                </div>

                {/* Fila 3: Tiempo */}
                <div className="control-row">
                  <span className="info-label">Tiempo de la simulación:</span>
                  <span className="value">{formatTime(seconds)}</span>
                </div>
              </div>
            )}
          </div>

          {/* WRAPPER DEL MAPA (para anclar la leyenda dentro) */}
          <div className="map-wrapper">
            <MapContainer
              id="map"
              center={[-12.0464, -77.0428]}
              zoom={3}
              whenCreated={(map) => {
                mapRef.current = map; // guardamos referencia al mapa Leaflet
              }}
            >
              <TileLayer
                url="https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png"
                attribution='&copy; <a href="https://carto.com/">Carto</a>'
              />

              {/* Marcadores de aeropuertos */}
              {/* Marcadores de aeropuertos */}
              {airports &&
                Object.values(airports).map((ap, i) => {
                  // 1) STOCK ACTUAL EN BASE A REGISTROS + TIEMPO DE SIMULACIÓN
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

                  const enrichedAp = {
                    ...ap,
                    stockActual,
                    ocupacion,
                  };

                  // 2) VUELOS QUE SALEN Y LLEGAN EN ESTE AEROPUERTO
                  // Solo mostramos los que aún NO han ocurrido y los ordenamos por fecha/hora
                  const vuelosQueSalen = flights
                    .filter(
                      (f) =>
                        f.origin &&
                        f.origin.code === ap.code &&
                        typeof f.startMs === "number" &&
                        simNowMs < f.startMs // 🔥 solo salidas futuras
                    )
                    .slice()
                    .sort((a, b) => a.startMs - b.startMs); // 🔥 orden por hora de salida

                  const vuelosQueLlegan = flights
                    .filter(
                      (f) =>
                        f.destination &&
                        f.destination.code === ap.code &&
                        typeof f.endMs === "number" &&
                        simNowMs < f.endMs // 🔥 solo llegadas futuras
                    )
                    .slice()
                    .sort((a, b) => a.endMs - b.endMs); // 🔥 orden por hora de llegada

                  const ocupPct = Math.min(
                    100,
                    Math.max(0, Math.round((enrichedAp.ocupacion ?? 0) * 100))
                  );

                  return (
                    <Marker
                      key={i}
                      position={[enrichedAp.lat, enrichedAp.lng]}
                      icon={createAirportIcon(enrichedAp)}
                      eventHandlers={{
                        mouseover: () => {
                          // Solo tooltip
                          setOpenAirportTooltipCode(enrichedAp.code);
                          setOpenFlightTooltipCode(null);
                        },
                        click: () => {
                          // Abrir panel informativo
                          setSelectedAirport(enrichedAp);
                          setSelectedItem(null);
                        },
                      }}
                    >
                      {openAirportTooltipCode === enrichedAp.code && (
                        <Tooltip
                          direction="top"
                          opacity={0.95}
                          interactive
                          permanent // 👈 para que NO se cierre al salir el mouse
                        >
                          <AirportTooltipContent
                            airport={enrichedAp}
                            vuelosQueSalen={vuelosQueSalen}
                            vuelosQueLlegan={vuelosQueLlegan}
                            getOrdersForFlight={getOrdersForFlight}
                          />
                        </Tooltip>
                      )}
                    </Marker>
                  );
                })}

              {flights.map((flight) => {
                if (
                  !flight ||
                  !flight.path ||
                  !Array.isArray(flight.path) ||
                  flight.path.length === 0
                ) {
                  return null;
                }

                // ✅ Solo queremos mostrar el vuelo cuando YA está en su ventana de vuelo
                const enVentanaVuelo =
                  timerActive &&
                  typeof flight.startMs === "number" &&
                  typeof flight.endMs === "number" &&
                  simNowMs >= flight.startMs &&
                  simNowMs < flight.endMs;

                // Si aún no es hora de despegar o ya llegó → no se dibuja nada
                if (!enVentanaVuelo) {
                  return null;
                }

                const filterCss = getPlaneColorFilter(
                  flight.capacity,
                  flight.planeCapacity
                );

                const shouldDimOthers = highlightedFlights.length > 0;
                const isHighlighted = highlightedFlights.includes(flight.code);

                return (
                  <React.Fragment key={flight.code}>
                    <Polyline
                      key={flight.code + "-" + (isHighlighted ? "on" : "off")}
                      positions={flight.path.slice(
                        Math.floor(flight.path.length * (flight.progress ?? 0))
                      )}
                      color={
                        !shouldDimOthers
                          ? "#eb6774" // normal
                          : isHighlighted
                          ? "#ff0019" // seleccionado
                          : "#e5e7eb" // otros → gris muy clarito
                      }
                      weight={isHighlighted ? 4 : 1} // otros aún más finos
                      opacity={
                        !shouldDimOthers
                          ? 0.9 // normal
                          : isHighlighted
                          ? 1 // seleccionado bien fuerte
                          : 0.03 // otros casi desaparecen
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
                            // Solo tooltip
                            setOpenFlightTooltipCode(flight.code);
                            setOpenAirportTooltipCode(null);
                          },
                          click: () => {
                            // Abrir panel con info del vuelo
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
                          >
                            <PlaneTooltipContent
                              flight={flight}
                              getOrdersForFlight={getOrdersForFlight}
                            />
                          </Tooltip>
                        )}
                      </Marker>
                    )}
                  </React.Fragment>
                );
              })}

              <ClickHandler
                onMapClick={() => {
                  setSelectedItem(null);
                  setSelectedAirport(null);
                  setHighlightedRoute(null);
                  setHighlightedFlights([]);
                  setOpenAirportTooltipCode(null);
                  setOpenFlightTooltipCode(null);
                }}
              />
            </MapContainer>

            {/* LEYENDA DENTRO DEL MAPA, ABAJO IZQUIERDA */}
            <div className="legend-overlay">
              <div
                className={`legend-card ${
                  legendCollapsed ? "legend-card--collapsed" : ""
                }`}
              >
                {/* Header clickeable para abrir/cerrar */}
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

                {/* Cuerpo solo si NO está colapsada */}
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
          </div>

          {/* PANEL INFORMATIVO DEBAJO DEL MAPA */}
          {infoPanelExpanded && (
            <div className={`info-panel expanded ${infoPanelVariant}`}>
              <div className="info-panel-inner">
                <div className="info-content">
                  {selectedAirport ? (
                    <AirportInfoPanel
                      airport={selectedAirport}
                      vuelosSaliendo={vuelosSaliendo}
                      vuelosLlegando={vuelosLlegando}
                      getOrdersForFlight={getOrdersForFlight}
                    />
                  ) : selectedFlight ? (
                    <FlightInfoPanel
                      flight={selectedFlight}
                      getOrdersForFlight={getOrdersForFlight}
                    />
                  ) : selectedOrder ? (
                    <OrderInfoPanel order={selectedOrder} flights={flights} />
                  ) : selectedRoute ? (
                    <RouteInfoPanel route={selectedRoute} />
                  ) : (
                    <>
                      <h3>Información seleccionada</h3>
                      <p>
                        Selecciona un vuelo, pedido o ruta para ver el detalle.
                      </p>
                    </>
                  )}
                </div>
              </div>
            </div>
          )}
        </div>
      </section>

      {/* MODAL */}
      {isModalOpen && (
        <div className="modal" onClick={closeModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">Planificar</h3>
            </div>

            <div className="modal-body">
              {/* === RANGO DE SIMULACIÓN === */}
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

              {/* === CONFIGURACIÓN TEMPORAL === */}
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
                type="text"
                value={saltoDeAlgoritmoInput}
                onChange={(e) => {
                  const raw = e.target.value;
                  setSaltoDeAlgoritmoInput(raw);
                  setTamanioDeSaltoTemporal(parseNumber(raw));
                }}
              />

              {/* === CIUDADES SEDE (codOrigenes) === */}
              <span className="sidebar-subtitle">Ciudades sede</span>

              {/* Chips con los códigos seleccionados */}
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

              {/* Dropdown para agregar / quitar códigos origen */}
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

              {/* === PARÁMETROS QUE SE ENVIARÁN AL BACK === */}
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
