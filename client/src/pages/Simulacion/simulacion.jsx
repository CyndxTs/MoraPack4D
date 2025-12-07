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
import { listarParametros } from "../../services/parametrosService";
import { listarAeropuertos } from "../../services/aeropuertoService";
import {
  connectSimulatorWS,
  sendSimulationRequest,
  sendStopSimulation,
  disconnectWS,
} from "../../services/planificarService";
import {
  MapContainer,
  TileLayer,
  Marker,
  Popup,
  Polyline,
  useMapEvent,
} from "react-leaflet";
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
    return Number(v);
  };
  const [maxDiasEntregaIntercontinental, setMaxDiasEntregaIntercontinental] = useState();
  const [maxDiasEntregaIntracontinental, setMaxDiasEntregaIntracontinental] = useState();
  const [maxHorasRecojo, setMaxHorasRecojo] = useState();
  const [minHorasEstancia, setMinHorasEstancia] = useState();
  const [maxHorasEstancia, setMaxHorasEstancia] = useState();
  const [multiplicadorTemporal, setMultiplicadorTemporal] = useState();
  const [tamanioDeSaltoTemporal, setTamanioDeSaltoTemporal] = useState();
  const [parametrosCompletos, setParametrosCompletos] = useState(null);

  const [estadoEjecucionSim, setEstadoEjecucionSim] = useState("POR_INICIAR");
  const [showLoadingSim, setShowLoadingSim] = useState(false);

  //Vuelos
  const [flights, setFlights] = useState([]);
  const [highlightedFlights, setHighlightedFlights] = useState([]);
  //Rutas
  const [routes, setRoutes] = useState([]);
  const [highlightedRoute, setHighlightedRoute] = useState(null);
  //Pedidos
  const [orders, setOrders] = useState([]);
  //Aeropuertos
  const [airports, setAirports] = useState(null);
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
    setAirports(null);
    setSelectedItem(null);
    setSelectedAirport(null);
    setOrders([]);            
    setSidebarTab("flights"); 
    setStopDisabled(true);
    setRoutes([])
    setHighlightedRoute(null);
    setHighlightedFlights([]);
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

  // === Vuelos saliendo / llegando del aeropuerto seleccionado ===
  const vuelosSaliendo = selectedAirport
    ? flights.filter(
        (f) =>
          f &&
          f.origin &&
          f.origin.code === selectedAirport.code &&
          simNowMs < f.endMs // solo los que aún no han llegado
      )
    : [];

  const vuelosLlegando = selectedAirport
    ? flights.filter(
        (f) =>
          f &&
          f.destination &&
          f.destination.code === selectedAirport.code &&
          simNowMs < f.endMs
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
                       filter:${filterCss};
                       transition: transform 0.3s linear;">`,
      className: "",
      iconSize: [18, 18],
      iconAnchor: [11, 8],
    });

  const airportIcon = L.icon({
    iconUrl: airportIconImg,
    iconSize: [24, 24], // tamaño del svg en el mapa
    iconAnchor: [12, 12], // punto que “toca” el mapa (centro del ícono)
    popupAnchor: [0, -12], // dónde aparece el popup respecto al icono
  });
  //

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
      // color por defecto si falta dato → amarillo
      return "invert(80%) sepia(72%) saturate(657%) hue-rotate(3deg) brightness(101%) contrast(102%)";
    }

    const ratio = capacity / maxCapacity; // 0.0 → 1.0

    // VERDE → menos del 50%
    if (ratio < 0.5) {
      return "invert(54%) sepia(81%) saturate(356%) hue-rotate(85deg) brightness(94%) contrast(90%)";
    }

    // AMARILLO → 50% a 75%
    if (ratio >= 0.5 && ratio < 0.75) {
      return "invert(80%) sepia(72%) saturate(657%) hue-rotate(3deg) brightness(101%) contrast(102%)";
    }

    // ROJO → 90% a 100%
    if (ratio >= 0.9) {
      return "invert(37%) sepia(79%) saturate(844%) hue-rotate(338deg) brightness(94%) contrast(92%)";
    }

    // Rango 75%–90% → también amarillo
    return "invert(80%) sepia(72%) saturate(657%) hue-rotate(3deg) brightness(101%) contrast(102%)";
  }

  function ClickHandler({ onMapClick }) {
    useMapEvent("click", () => onMapClick());
    return null; // no renderiza nada visible
  }

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
        // === SOLO LOS 5 PARAMETROS A MOSTRAR EN EL POPUP ===
        setMaxDiasEntregaIntercontinental(p.maxDiasEntregaIntercontinental);
        setMaxDiasEntregaIntracontinental(p.maxDiasEntregaIntracontinental);
        setMaxHorasRecojo(p.maxHorasRecojo);
        setMinHorasEstancia(p.minHorasEstancia);
        setMaxHorasEstancia(p.maxHorasEstancia);
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

  const buildSimulationFromSolution = (solution) => {
    if (!solution) return;

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
      };
    });
    console.log("airportMap construido:", airportMap);

  // Mapa rápido de rutas por código
  const rutasPorCodigo = {};
  (solution.rutasEnOperacion || []).forEach((r) => {
    rutasPorCodigo[r.codigo] = r;
  });

  // Guardar rutas completas para el sidebar
  const rutasFiltradas = (solution.rutasEnOperacion || []).filter(
    (r) => r.estado === "OPERATIVA" || r.estado === "FINALIZADA"
  );

  setRoutes(rutasFiltradas);

  // Construir pedidos con segmentaciones, lotes y vuelos
  const pedidosAtendidos = (solution.pedidosAtendidos || []).map((p) => {
    const segmentaciones = (p.segmentaciones || []).map((seg) => {
      const lotes = (seg.lotesPorRuta || []).map((lpr) => {
        const ruta = rutasPorCodigo[lpr.codRuta];
        const vuelosRuta = ruta?.codVuelos || [];
        const origen = ruta ? airportMap[ruta.codOrigen] : null;
        const destino = ruta ? airportMap[ruta.codDestino] : null;

        return {
          codRuta: lpr.codRuta,
          loteCodigo: lpr.lote.codigo,
          loteTamanio: lpr.lote.tamanio,
          loteEstado: lpr.lote.estado,
          vuelos: vuelosRuta,
          origenCode: ruta?.codOrigen,
          destinoCode: ruta?.codDestino,
          origenNombre: origen?.name || ruta?.codOrigen,
          destinoNombre: destino?.name || ruta?.codDestino,
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
      segmentaciones,
    };
  });

  setOrders(pedidosAtendidos);
  console.log("Pedidos construidos:", pedidosAtendidos);
    // Actualizar airports solo si cambió
    setAirports((prevAirports) => {
      if (JSON.stringify(prevAirports) !== JSON.stringify(airportMap)) {
        return airportMap;
      }
      return prevAirports;
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

        const prev = prevByCode.get(v.codigo);

        if (prev) {
          // ✅ Vuelo ya existía → PRESERVAR completamente su estado de animación
          // Solo actualizamos datos estáticos que podrían haber cambiado
          nextFlights.push({
            ...prev,
            // Actualizar solo metadata que no afecta la animación
            capacity: v.capacidadOcupada,
            planeCapacity: v.capacidadMaxima,
            // NO tocamos: progress, position, rotation, arrived, startMs, endMs, path
            rutas: (solution.rutasEnOperacion || [])
              .filter((r) => (r.codVuelos || []).includes(v.codigo))
              .map((r) => `${r.codOrigen} → ${r.codDestino}`),
          });
        } else {
          // 🆕 Vuelo nuevo → inicializarlo según simNowMs
          const total = Math.max(endMs - startMs, 60 * 1000);
          let progress = 0;
          let position = path[0];
          let arrived = false;
          let rotation = 0;

          // Buscar rutas asociadas a este vuelo
          const rutasDelVuelo = (solution.rutasEnOperacion || []).filter((r) =>
            (r.codVuelos || []).includes(v.codigo)
          );

          if (simNowMs <= startMs) {
            // Aún no despega
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
            rutas: rutasDelVuelo.map((r) => `${r.codOrigen} → ${r.codDestino}`),
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
          console.log(
            `Manteniendo vuelo en tránsito que no vino en nueva solución: ${prevFlight.code}`
          );
          nextFlights.push(prevFlight);
        }
      });

      console.log(
        `Vuelos actualizados: ${nextFlights.length} (${
          vuelosNuevos.length
        } nuevos, ${nextFlights.length - vuelosNuevos.length} preservados)`
      );
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
          maxDiasEntregaIntercontinental,
          maxDiasEntregaIntracontinental,
          maxHorasRecojo,
          minHorasEstancia,
          maxHorasEstancia,
          codOrigenes,
        },
        multiplicadorTemporal,
        tamanioDeSaltoTemporal,
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
      pedido.segmentaciones.forEach((seg) => {
        seg.lotes.forEach((lote) => {
          if (lote.vuelos && lote.vuelos.includes(flightCode)) {
            pedidosEnVuelo.push({
              pedidoCodigo: pedido.codigo,
              loteCodigo: lote.loteCodigo,
              cantidad: lote.loteTamanio
            });
          }
        });
      });
    });

    return pedidosEnVuelo;
  };

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
                {orders.length}
              </span>
            </button>

            <button
              className={`sidebar-tab ${sidebarTab === "routes" ? "active" : ""}`}
              onClick={() => setSidebarTab("routes")}
            >
              <span>Rutas</span>
              <span className="sidebar-tab-badge">
                {routes.length}
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

              <div className="active-flights-list">
                {activeFlights.length === 0 ? (
                  <p className="no-flights-text">
                    No hay vuelos activos en este momento.
                  </p>
                ) : (
                  activeFlights.map((flight) => {
                    const progressPct = Math.min(
                      100,
                      Math.max(0, Math.round((flight.progress ?? 0) * 100))
                    );

                    return (
                      <div
                        key={flight.code}
                        className="flight-card"
                        onClick={() => {
                          setSelectedAirport(null);
                          setSelectedItem(
                            `Vuelo ${flight.code}: ${flight.origin.city} (${flight.origin.code}) → ${flight.destination.city} (${flight.destination.code}) | Llegada: ${flight.endTime}`
                          );
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
                <span className="orders-title">Pedidos atendidos</span>
                <span className="orders-count">{orders.length}</span>
              </div>

              <div className="orders-list">
                {orders.length === 0 ? (
                  <p className="no-orders-text">
                    No hay pedidos en esta simulación.
                  </p>
                ) : (
                  orders.map((pedido) => (
                    <div
                      key={pedido.codigo}
                      className="order-card"
                      onClick={() => {
                          setSelectedAirport(null);
                          setHighlightedRoute(null); // por si había ruta resaltada antes

                          // Buscar TODOS los vuelos de TODOS los lotes del pedido
                          const vuelosPedido = new Set();

                          pedido.segmentaciones.forEach(seg => {
                              seg.lotes.forEach(lote => {
                                  (lote.vuelos || []).forEach(v => vuelosPedido.add(v));
                              });
                          });

                          // Guardar vuelos a resaltar
                          setHighlightedFlights([...vuelosPedido]);

                          setSelectedItem(
                            `Pedido ${pedido.codigo}
                      Cliente: ${pedido.codCliente}
                      Destino: ${pedido.codDestino}
                      Cantidad solicitada: ${pedido.cantidadSolicitada} unidades`
                          );
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

                        {pedido.segmentaciones.map((seg) => (
                          <div key={seg.codigo} className="order-seg">
                            <div className="order-seg-header">
                              <span className="order-seg-code">
                                {seg.codigo}
                              </span>
                              {seg.fechaHoraAplicacion && (
                                <span className="order-seg-date">
                                  {seg.fechaHoraAplicacion}
                                </span>
                              )}
                            </div>

                            {seg.lotes.map((lote) => (
                              <div
                                key={lote.loteCodigo}
                                className="order-lote"
                              >
                                <div className="order-row">
                                  <span className="order-label">Lote</span>
                                  <span className="order-value">
                                    {lote.loteCodigo} · {lote.loteTamanio} u.
                                  </span>
                                </div>
                                <div className="order-row">
                                  <span className="order-label">Ruta</span>
                                  <span className="order-value">
                                    {lote.origenCode} → {lote.destinoCode}
                                  </span>
                                </div>
                                <div className="order-row">
                                  <span className="order-label">Vuelos</span>
                                  <span className="order-value">
                                    {lote.vuelos && lote.vuelos.length > 0
                                      ? lote.vuelos.join(", ")
                                      : "Sin vuelo asignado"}
                                  </span>
                                </div>
                              </div>
                            ))}
                          </div>
                        ))}
                      </div>
                    </div>
                  ))
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
            <span className="routes-count">{routes.length}</span>
          </div>

          <div className="routes-list">
            {routes.length === 0 ? (
              <p className="no-routes-text">No hay rutas en operación.</p>
            ) : (
              routes.map((ruta) => (
                <div
                  key={ruta.codigo}
                  className="route-card"
                  onClick={() => {
                    setSelectedAirport(null);
                    setSelectedItem(
                      `Ruta ${ruta.codigo}
      Origen: ${ruta.codOrigen}
      Destino: ${ruta.codDestino}
      Duración: ${ruta.duracion} h
      Distancia: ${ruta.distancia.toFixed(2)} km
      Vuelos: ${ruta.codVuelos?.join(", ") || "Sin vuelos"}`
                    );
                  setHighlightedRoute(ruta);
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

                    <div className="route-row">
                      <span className="route-label">Vuelos</span>
                      <span className="route-value">
                        {ruta.codVuelos?.length > 0
                          ? ruta.codVuelos.join(", ")
                          : "Sin vuelos"}
                      </span>
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
            <MapContainer id="map" center={[-12.0464, -77.0428]} zoom={3}>
              <TileLayer
                url="https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png"
                attribution='&copy; <a href="https://carto.com/">Carto</a>'
              />

              {/* Marcadores de aeropuertos */}
              {airports &&
                Object.values(airports).map((ap, i) => (
                  <Marker
                    key={i}
                    position={[ap.lat, ap.lng]}
                    icon={airportIcon}
                    eventHandlers={{
                      click: () => {
                        setSelectedAirport(ap);
                        setSelectedItem(null);
                      },
                    }}
                  >
                    <Popup>
                      <b>{ap.country}</b>
                      <br />
                      Código: {ap.code}
                      <br />
                      Ciudad: {ap.city}
                      <br />
                      Capacidad: {ap.capacidad} unidades
                      <br />
                    </Popup>
                  </Marker>
                ))}

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
                    {/* Trayectoria a partir del progreso actual */}
                    
                    <Polyline
                      key={flight.code + "-" + (isHighlighted ? "on" : "off")}
                      positions={flight.path.slice(
                        Math.floor(flight.path.length * (flight.progress ?? 0))
                      )}
                      color={!shouldDimOthers ? "#eb6774" : isHighlighted ? "#ff0019" : "#eb6774"}
                      weight={isHighlighted ? 4 : 2}
                      opacity={!shouldDimOthers ? 1 : isHighlighted ? 1 : 0.15}
                      dashArray="6, 10"
                      interactive={false}
                    />


                    {/* Avión solo mientras está en vuelo */}
                    {!flight.arrived && (
                      <Marker
                        position={flight.position}
                        opacity={!shouldDimOthers ? 1 : isHighlighted ? 1 : 0.3}               // 🔥 avión atenuado si no pertenece al pedido
                        zIndexOffset={1000}       // 🔥 aviones del pedido encima
                        icon={createColoredIcon(
                          filterCss,
                          flight.rotation || 0
                        )}
                        riseOnHover={true}
                        eventHandlers={{
                          click: (e) => {
                            if (e.target && e.target.openPopup) {
                              e.target.openPopup();
                            }

                            setSelectedAirport(null); // 👈 limpiamos selección de aeropuerto
                            const pedidosVuelo = getOrdersForFlight(flight.code);

                            const pedidosTexto =
                              pedidosVuelo.length > 0
                                ? pedidosVuelo
                                    .map(
                                      (p) =>
                                        ` - Pedido ${p.pedidoCodigo} (${p.cantidad} u.)`
                                    )
                                    .join("\n")
                                : " - No transporta pedidos";

                            setSelectedItem(
                              `Vuelo ${flight.code}:
                            ${flight.origin.city} (${flight.origin.code}) → ${flight.destination.city} (${flight.destination.code})
                            Salida: ${flight.startTime}
                            Llegada: ${flight.endTime}
                            Capacidad: ${flight.capacity} / ${flight.planeCapacity} pax
                            
                            Rutas que pasa:
                            ${
                              flight.rutas && flight.rutas.length > 0
                                ? flight.rutas.map((r) => ` - ${r}`).join("\n")
                                : " - No asignadas"
                            }

                            Pedidos transportados:
                            ${pedidosTexto}
                            `
                            );

                          },
                        }}
                      >
                        <Popup>
                          <b>{flight.code}</b>
                          <br />
                          {flight.origin.country} → {flight.destination.country}
                          <br />
                          Salida: {flight.startTime}
                          <br />
                          Llegada: {flight.endTime}
                          <br />
                          Capacidad: {flight.capacity} / {flight.planeCapacity}{" "}
                          pax
                          <br />
                        </Popup>
                      </Marker>
                    )}
                  </React.Fragment>
                );
              })}

              {/* Ruta resaltada (cuando el usuario selecciona una ruta en el sidebar) */}
              {highlightedRoute && airports && (
                <Polyline
                  positions={(() => {
                    const ap1 = airports[highlightedRoute.codOrigen];
                    const ap2 = airports[highlightedRoute.codDestino];
                    if (!ap1 || !ap2) return [];

                    return generateGeodesicPath(ap1.lat, ap1.lng, ap2.lat, ap2.lng, 120);
                  })()}
                  color="#ff0033"        // rojo brillante
                  weight={2}             // más grueso
                  opacity={0.9}
                  dashArray={null}       // línea sólida
                />
              )}

              <ClickHandler
                onMapClick={() => {
                  setSelectedItem(null);
                  setSelectedAirport(null);
                  setHighlightedRoute(null);
                  setHighlightedFlights([]);
                }}
              />
            </MapContainer>

            {/* LEYENDA DENTRO DEL MAPA, ABAJO IZQUIERDA */}
            <div className="legend-overlay">
              <div className="legend-card">
                <div className="legend-card-header">
                  <span className="legend-card-info-icon">i</span>
                  <span className="legend-card-title">Leyenda</span>
                </div>

                <div className="legend-card-body">
                  <div className="legend-item">
                    <img
                      src={planeIconImg}
                      alt=""
                      className="legend-plane legend-plane--green"
                    />
                    <span>Menos del 50% de capacidad</span>
                  </div>

                  <div className="legend-item">
                    <img
                      src={planeIconImg}
                      alt=""
                      className="legend-plane legend-plane--yellow"
                    />
                    <span>Entre 50% y 75% de capacidad</span>
                  </div>

                  <div className="legend-item">
                    <img
                      src={planeIconImg}
                      alt=""
                      className="legend-plane legend-plane--red"
                    />
                    <span>Entre 90% y 100% de capacidad</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* PANEL INFORMATIVO DEBAJO DEL MAPA */}
          {infoPanelExpanded && ( // 👈 solo se pinta si hay aeropuerto o vuelo seleccionado
            <div className="info-panel expanded">
              <div className="info-content">
                {selectedAirport ? (
                  <>
                    {/* Cabecera: aeropuerto */}
                    <div className="info-panel-header">
                      <div className="left">
                        <div className="info-panel-title">
                          Información seleccionada
                        </div>
                        <div className="info-panel-airport">
                          {selectedAirport.city} ({selectedAirport.code})
                        </div>
                        <div className="info-panel-subtitle">
                          {selectedAirport.country}
                        </div>
                      </div>

                      <div className="right">
                        <div className="info-panel-capacity">
                          Capacidad total: {selectedAirport.capacidad} unidades
                        </div>
                        <div className="info-panel-capacity">
                          Capacidad actual: {
                            vuelosLlegando.reduce((sum, f) => sum + (f.capacity || 0), 0)
                          } / {selectedAirport.capacidad} unidades
                        </div>
                      </div>
                    </div>

                    {/* Cuerpo: vuelos saliendo / llegando */}
                    <div className="info-panel-body">
                      {/* Columna 1: saliendo */}
                      <div className="info-panel-column">
                        <h4>Vuelos saliendo</h4>

                        {vuelosSaliendo.length === 0 && (
                          <p className="info-panel-empty-list">
                            No hay vuelos saliendo en este momento.
                          </p>
                        )}

                        <ul className="info-panel-flights">
                          {vuelosSaliendo.map((flight) => {
                            const porcentaje =
                              flight.planeCapacity > 0
                                ? Math.round(
                                    (flight.capacity * 100) /
                                      flight.planeCapacity
                                  )
                                : 0;

                            return (
                              <li key={flight.code} className="flight-card">
                                <div className="flight-card-header">
                                  <span className="flight-route">
                                    {flight.origin.code}
                                    <span className="flight-arrow"> ✈ </span>
                                    {flight.destination.code}
                                  </span>
                                  <span className="flight-code">
                                    {flight.code}
                                  </span>
                                </div>

                                <div className="flight-card-body">
                                  <div className="flight-card-row">
                                    <span className="flight-label">Salida</span>
                                    <span className="flight-value">
                                      {flight.startTime}
                                    </span>
                                  </div>
                                  <div className="flight-card-row">
                                    <span className="flight-label">
                                      Llegada
                                    </span>
                                    <span className="flight-value">
                                      {flight.endTime}
                                    </span>
                                  </div>
                                  <div className="flight-card-row">
                                    <span className="flight-label">
                                      Capacidad
                                    </span>
                                    <span className="flight-value">
                                      {flight.capacity}/{flight.planeCapacity}{" "}
                                      unidades · {porcentaje}% ocupación
                                    </span>
                                  </div>

                                  {/* Pedidos transportados */}
                                  <div className="flight-card-row">
                                    <span className="flight-label">Pedidos</span>
                                  </div>

                                  {(() => {
                                    const pedidos = getOrdersForFlight(flight.code);
                                    if (pedidos.length === 0) {
                                      return <div className="flight-no-orders">—</div>;
                                    }

                                    return (
                                      <ul className="pedido-list">
                                        {pedidos.map((p, idx) => (
                                          <li key={idx}>
                                            Pedido {p.pedidoCodigo} ({p.cantidad} u.)
                                          </li>
                                        ))}
                                      </ul>
                                    );
                                  })()}

                                </div>
                              </li>
                            );
                          })}
                        </ul>
                      </div>

                      {/* Columna 2: llegando */}
                      <div className="info-panel-column">
                        <h4>Vuelos llegando</h4>

                        {vuelosLlegando.length === 0 && (
                          <p className="info-panel-empty-list">
                            No hay vuelos llegando en este momento.
                          </p>
                        )}

                        <ul className="info-panel-flights">
                          {vuelosLlegando.map((flight) => {
                            const porcentaje =
                              flight.planeCapacity > 0
                                ? Math.round(
                                    (flight.capacity * 100) /
                                      flight.planeCapacity
                                  )
                                : 0;

                            return (
                              <li key={flight.code} className="flight-card">
                                <div className="flight-card-header">
                                  <span className="flight-route">
                                    {flight.origin.code}
                                    <span className="flight-arrow"> ✈ </span>
                                    {flight.destination.code}
                                  </span>
                                  <span className="flight-code">
                                    {flight.code}
                                  </span>
                                </div>

                                <div className="flight-card-body">
                                  <div className="flight-card-row">
                                    <span className="flight-label">Salida</span>
                                    <span className="flight-value">
                                      {flight.startTime}
                                    </span>
                                  </div>
                                  <div className="flight-card-row">
                                    <span className="flight-label">
                                      Llegada
                                    </span>
                                    <span className="flight-value">
                                      {flight.endTime}
                                    </span>
                                  </div>
                                  <div className="flight-card-row">
                                    <span className="flight-label">
                                      Capacidad
                                    </span>
                                    <span className="flight-value">
                                      {flight.capacity}/{flight.planeCapacity}{" "}
                                      unidades · {porcentaje}% ocupación
                                    </span>
                                  </div>

                                  {/* Pedidos transportados */}
                                  <div className="flight-card-row">
                                    <span className="flight-label">Pedidos</span>
                                  </div>

                                  {(() => {
                                    const pedidos = getOrdersForFlight(flight.code);
                                    if (pedidos.length === 0) {
                                      return <div className="flight-no-orders">—</div>;
                                    }

                                    return (
                                      <ul className="pedido-list">
                                        {pedidos.map((p, idx) => (
                                          <li key={idx}>
                                            Pedido {p.pedidoCodigo} ({p.cantidad} u.)
                                          </li>
                                        ))}
                                      </ul>
                                    );
                                  })()}

                                </div>
                              </li>
                            );
                          })}
                        </ul>
                      </div>
                    </div>
                  </>
                ) : (
                  // Caso: solo hay selectedItem (avión), usamos layout simple
                  <>
                    <h3>Información seleccionada</h3>
                    <p>{selectedItem}</p>
                  </>
                )}
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

              <label>Tamaño de salto temporal (horas)</label>
              <Input
                label="Tamaño de salto temporal (horas)"
                type="number"
                value={tamanioDeSaltoTemporal}
                onChange={(e) =>
                  setTamanioDeSaltoTemporal(parseNumber(e.target.value))
                }
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
