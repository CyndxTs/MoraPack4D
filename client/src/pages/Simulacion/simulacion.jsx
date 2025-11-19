import React, { useState, useEffect, useRef } from "react";
import "./simulacion.scss";
import { Radio, Checkbox, Dropdown, Legend, Notification, SidebarActions, ButtonAdd, DateTimeInline, Dropdown2, Input,LoadingOverlay } from "../../components/UI/ui";
import hideIcon from '../../assets/icons/hide-sidebar.png';
import plus from "../../assets/icons/plus.svg";
import { listarParametros } from "../../services/parametrosService";
import { listarAeropuertos } from "../../services/aeropuertoService";
import { planificar } from "../../services/planificarService";

import { MapContainer, TileLayer, Marker, Popup, Polyline, useMapEvent  } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from "leaflet";

import planeIconImg from "../../assets/icons/planeMora.svg";

export default function Simulacion() {
  const [collapsed, setCollapsed] = useState(false);
  const [codigoVuelo, setCodigoVuelo] = useState("");
  const [ciudadDestino, setCiudadDestino] = useState("");
  const [continente, setContinente] = useState({ america: false, europa: false, asia: false });
  const [estadoVuelo, setEstadoVuelo] = useState({ enCurso: false, finalizado: false, cancelado: false });
  const [archivo, setArchivo] = useState(null);
  const [selectedItem, setSelectedItem] = useState(null);

  // -------- MODAL --------
  const [isModalOpen, setIsModalOpen] = useState(false);

  const [tipoSimulacion] = useState("semanal");

  const [fechaI, setFechaI] = useState("");
  const [horaI, setHoraI] = useState("");
  const [fechaF, setFechaF] = useState("");
  const [horaF, setHoraF] = useState("");

  const [loadedOnOpen, setLoadedOnOpen] = useState(false);
  const [parametros, setParametros] = useState(null);
  const [aeropuertos, setAeropuertos] = useState([]);
  const [codOrigenes, setCodOrigenes] = useState([]);

  // estados de todos los parámetros
  const toBoolean = (v) => v === "true";
  const parseNumber = (v) => {
    if (v === "" || v === null || v === undefined) return null;
    return Number(v);
  };
  const [replanificar] = useState(false);
  const [guardarPlanificacion] = useState(false);
  const [reparametrizar, setReparametrizar] = useState(false);   
  const [guardarParametrizacion] = useState(false);


  const [maxDiasEntregaIntercontinental, setMaxDiasEntregaIntercontinental] = useState();
  const [maxDiasEntregaIntracontinental, setMaxDiasEntregaIntracontinental] = useState();
  const [maxHorasRecojo, setMaxHorasRecojo] = useState();
  const [minHorasEstancia, setMinHorasEstancia] = useState();
  const [maxHorasEstancia, setMaxHorasEstancia] = useState();
  const [considerarDesfaseTemporal] = useState(false);

  const [dMin, setDMin] = useState();
  const [iMax, setIMax] = useState();
  const [eleMin, setEleMin] = useState();
  const [eleMax, setEleMax] = useState();
  const [kMin, setKMin] = useState();
  const [kMax, setKMax] = useState();
  const [tMax, setTMax] = useState();
  const [maxIntentos, setMaxIntentos] = useState();

  const [factorDeUmbralDeAberracion, setFactorDeUmbralDeAberracion] = useState();
  const [factorDeUtilizacionTemporal, setFactorDeUtilizacionTemporal] = useState();
  const [factorDeDesviacionEspacial, setFactorDeDesviacionEspacial] = useState();
  const [factorDeDisposicionOperacional, setFactorDeDisposicionOperacional] = useState();

  //Aeropuertos
  const [airports, setAirports] = useState(null);
  const [loadingAirports, setLoadingAirports] = useState(true);
  
  // Inputs de inicio de simulación (no se auto-actualizan)
  const [inputDate, setInputDate] = useState(new Date().toISOString().split("T")[0]);
  const [inputTime, setInputTime] = useState(new Date().toTimeString().slice(0,5));

  // Reloj de simulación (ms) y velocidad: 3600 = 1s real -> 1 hora simulada
  const [simNowMs, setSimNowMs] = useState(() => Date.now());
  const [simSpeed, setSimSpeed] = useState(3600);

  // Refs internas para el avance suave
  const baseSimMsRef = useRef(null);
  const lastRealMsRef = useRef(null);

  // Helpers de tiempo (trabajamos en UTC porque tu JSON está en UTC)
  const toISODate = (ms) => new Date(ms).toISOString().split("T")[0];
  const toISOTime = (ms) => new Date(ms).toISOString().slice(11,16);

  // El backend de planificación manda fechas tipo "03/11/2025 10:20"
  const parseFechaHoraToMs = (fechaHora) => {
    if (!fechaHora) return Date.now();
    const [fecha, hora] = fechaHora.split(" ");      // "03/11/2025 10:20"
    const [dia, mes, anio] = fecha.split("/").map(Number);
    const [hh, mm] = hora.split(":").map(Number);

    // Lo tratamos como UTC para ser consistentes con el resto de la simulación
    return Date.UTC(anio, mes - 1, dia, hh, mm, 0);
  };


  const parseUtcToMs = (iso) => {
    // Si viene sin zona ("2025-11-26T20:06:00") lo forzamos a UTC
    const s = /Z|[+-]\d{2}:\d{2}$/.test(iso) ? iso : iso + "Z";
    return new Date(s).getTime();
  };

  const fromInputsToMsUTC = (d, t) => new Date(`${d}T${t}:00Z`).getTime();




  const [seconds, setSeconds] = useState(0);
  const [timerRunning, setTimerRunning] = useState(false);
  const [timerActive, setTimerActive] = useState(false); // indica si inició cronómetro (start clickeado)

  // Cargar aeropuertos del back una sola vez

  //Notificaciones
  const [notification, setNotification] = useState(null);

  const [loading, setLoading] = useState(false);

  const showNotification = (type, message) => {
    setNotification({ type, message });
    setTimeout(() => setNotification(null), 5000);
  };

  //Filtros
  const handleFilter = async () => {

  };

  //Limpiar filtros
  const handleCleanFilters = async () => {

  };

  // Botones
  const [btnState, setBtnState] = useState({
    start: { disabled: false, color: "blue" },
    pause: { disabled: true, color: "grey" },
    stop:  { disabled: true, color: "grey" }
  });

  // Cronómetro
  useEffect(() => {
    let timer;
    if (timerRunning) {
      timer = setInterval(() => setSeconds(s => s + 1), 1000);
    } else if (!timerRunning && seconds !== 0) {
      clearInterval(timer);
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

    // Avanzar reloj simulado: simSpeed = ms_sim / ms_real (3600 => 1s real = 1h simulada)
    setSimNowMs(prev => prev + elapsedRealMs * simSpeed);

    rafId = requestAnimationFrame(tick);
  };

  lastRealMsRef.current = performance.now();
  rafId = requestAnimationFrame(tick);
  return () => cancelAnimationFrame(rafId);
}, [timerRunning, simSpeed]);

useEffect(() => {
  if (!timerActive) return;

  setFlights(prev => prev.map(f => {
    if (!f || !f.path || f.path.length === 0) return f;

    const total = Math.max(f.endMs - f.startMs, 60 * 1000);

    // Aún no despega
    if (simNowMs <= f.startMs) {
      return { ...f, progress: 0, position: f.path[0], arrived: false };
    }

    const frac = Math.min((simNowMs - f.startMs) / total, 1);
    const idx  = Math.floor(frac * (f.path.length - 1));
    const pos  = f.path[idx];
    const next = f.path[Math.min(idx + 1, f.path.length - 1)];

    // bearing → rotation
    const toRad = d => d * Math.PI / 180, toDeg = r => r * 180 / Math.PI;
    const lat1 = toRad(pos.lat),  lon1 = toRad(pos.lng);
    const lat2 = toRad(next.lat), lon2 = toRad(next.lng);
    let bearing = Math.atan2(
      Math.sin(lon2 - lon1) * Math.cos(lat2),
      Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)
    );
    bearing = (toDeg(bearing) + 360) % 360;
    const rotation = bearing - 45;

    return { ...f, progress: frac, position: pos, rotation, arrived: frac >= 1 };
  }));
}, [simNowMs, timerActive]);

  
  const handleFileChange = (e) => {
    if (e.target.files.length > 0) setArchivo(e.target.files[0].name);
    else setArchivo(null);
  };

  const formatTime = (sec) => {
    const m = Math.floor(sec / 60).toString().padStart(2,'0');
    const s = (sec % 60).toString().padStart(2,'0');
    return `${m}:${s}`;
  };

  // Botones
  const handleStart = () => {
    // Si ya hay simulación activa pero está en pausa, SOLO reanuda
    if (timerActive && !timerRunning) {
      lastRealMsRef.current = performance.now(); // referencia para el RAF
      setTimerRunning(true);
      setBtnState({
        start: { disabled: true, color: "grey" },
        pause: { disabled: false, color: "red" },
        stop:  { disabled: false, color: "blue" }
      });
      return;
    }

    // Primer inicio: fija el tiempo de simulación al valor de los inputs
    const base = fromInputsToMsUTC(inputDate, inputTime);
    setSimNowMs(base);
    lastRealMsRef.current = performance.now();

    setTimerRunning(true);
    setTimerActive(true);
    setBtnState({
      start: { disabled: true, color: "grey" },
      pause: { disabled: false, color: "red" },
      stop:  { disabled: false, color: "blue" }
    });
  };

  const handlePause = () => {
    setTimerRunning(false);
    setBtnState({
      start: { disabled: false, color: "blue" },
      pause: { disabled: true, color: "grey" },
      stop:  { disabled: false, color: "blue" }
    });
    // NOTA: inputs siguen bloqueados hasta stop
  };

  const handleStop = () => {
    setTimerRunning(false);
    setTimerActive(false);
    setSeconds(0);

    // Volver el reloj simulado al valor de los inputs
    const base = fromInputsToMsUTC(inputDate, inputTime);
    setSimNowMs(base);

    // Resetear vuelos al origen usando la última planificación
    if (airports && rawFlights.length > 0) {
      const reset = rawFlights.map((v) => {
        const origin = airports[v.plan.codOrigen];
        const dest = airports[v.plan.codDestino];
        if (!origin || !dest) return null;

        const startMs = parseUtcToMs(f.fechaSalida);
        const endMs   = parseUtcToMs(f.fechaLlegada);
        const path = generateGeodesicPath(origin.lat, origin.lng, dest.lat, dest.lng, 120);

        return {
          code: f.codigo,
          origin, 
          originName: origin.name,
          destination: dest, 
          destinationName: dest.name,
          startTime: v.fechaHoraSalida, 
          endTime: v.fechaHoraLlegada,
          startMs, endMs,
          capacity: v.capacidadOcupada,
          planeCapacity: v.plan.capacidad,
          durationSec: Math.max((endMs - startMs)/1000, 60),
          progress: 0,
          arrived: false,
          path,
          position: { lat: origin.lat, lng: origin.lng },
          rotation: 0,
        };
      }).filter(Boolean);

      setFlights(reset);
    } else {
      setFlights([]);
    }

    setBtnState({
      start: { disabled: false, color: "blue" },
      pause: { disabled: true, color: "grey" },
      stop:  { disabled: true, color: "grey" }
    });
  };
  
  // Vuelos
  const [flights, setFlights] = useState([]);
  const [rawFlights, setRawFlights] = useState([]);




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

  const airportIcon = L.divIcon({
    html: `<div style="
        width: 10px;
        height: 10px;
        background-color: #2a93d5;
        border: 2px solid white;
        border-radius: 50%;
        box-shadow: 0 0 2px rgba(0,0,0,0.5);
      "></div>`,
    className: "",
    iconSize: [10, 10],
    iconAnchor: [5, 5],
  });
  //


  // Detener cronómetro cuando todos los vuelos hayan llegado
  useEffect(() => {
    if (!timerActive || flights.length === 0) return;
    const allArrivedByTime = flights.every(f => simNowMs >= f.endMs);
    if (allArrivedByTime) {
      showNotification("info", "Todos los vuelos han llegado a su destino.");
      setTimerRunning(false);
      setTimerActive(false);
      setBtnState({
        start: { disabled: true, color: "grey" },
        pause: { disabled: true, color: "grey" },
        stop:  { disabled: false, color: "blue" }
      });
    }
  }, [simNowMs, flights, timerActive]);


  // Calcula puntos de una ruta geodésica (gran círculo)
  function generateGeodesicPath(lat1, lon1, lat2, lon2, numPoints = 100) {
    const toRad = deg => (deg * Math.PI) / 180;
    const toDeg = rad => (rad * 180) / Math.PI;

    lat1 = toRad(lat1);
    lon1 = toRad(lon1);
    lat2 = toRad(lat2);
    lon2 = toRad(lon2);

    const d = 2 * Math.asin(
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
      const x = A * Math.cos(lat1) * Math.cos(lon1) + B * Math.cos(lat2) * Math.cos(lon2);
      const y = A * Math.cos(lat1) * Math.sin(lon1) + B * Math.cos(lat2) * Math.sin(lon2);
      const z = A * Math.sin(lat1) + B * Math.sin(lat2);
      const lat = Math.atan2(z, Math.sqrt(x ** 2 + y ** 2));
      const lon = Math.atan2(y, x);
      points.push({ lat: toDeg(lat), lng: toDeg(lon) });
    }
    return points;
  }
  //

  function ClickHandler({ onMapClick }) {
    useMapEvent("click", () => onMapClick());
    return null; // no renderiza nada visible
  }

  //MODAL
  useEffect(() => {
    const fetchParametrosYAeropuertos = async () => {
      try {
        const p = (await listarParametros()).dtos[0];
        const a = await listarAeropuertos();

        setParametros(p);
        setAeropuertos(a.dtos ?? []);

        // setear parámetros desde BD
        setMaxDiasEntregaIntercontinental(p.maxDiasEntregaIntercontinental);
        setMaxDiasEntregaIntracontinental(p.maxDiasEntregaIntracontinental);
        setMaxHorasRecojo(p.maxHorasRecojo);
        setMinHorasEstancia(p.minHorasEstancia);
        setMaxHorasEstancia(p.maxHorasEstancia);

        setDMin(p.dMin);
        setIMax(p.iMax);
        setEleMin(p.eleMin);
        setEleMax(p.eleMax);
        setKMin(p.kMin);
        setKMax(p.kMax);
        setTMax(p.tMax);
        setMaxIntentos(p.maxIntentos);

        setFactorDeUmbralDeAberracion(p.factorDeUmbralDeAberracion);
        setFactorDeUtilizacionTemporal(p.factorDeUtilizacionTemporal);
        setFactorDeDesviacionEspacial(p.factorDeDesviacionEspacial);
        setFactorDeDisposicionOperacional(p.factorDeDisposicionOperacional);

        setCodOrigenes(prev =>
          prev.length === 0 ? (p.codOrigenes || []) : prev
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
    (solution.aeropuertosTransitados || []).forEach(a => {
      airportMap[a.codigo] = {
        lat: a.latitudDEC,
        lng: a.longitudDEC,
        name: a.alias || a.ciudad,  // cómo se mostrará en el mapa
        code: a.codigo,
        city: a.ciudad,
        country: a.pais,
      };
    });
    setAirports(airportMap);
    setLoadingAirports(false);

    // 2) Guardar vuelos crudos para poder reconstruirlos al hacer "Detener"
    const vuelos = solution.vuelosEnTransito || [];
    setRawFlights(vuelos);

    // 3) Construir flights con el mismo formato que usabas con WS
    const mappedFlights = vuelos.map((v) => {
      const origin = airportMap[v.plan.codOrigen];
      const dest   = airportMap[v.plan.codDestino];

      if (!origin || !dest) {
        console.warn(
          `Vuelo ${v.codigo} omitido: no se encontró aeropuerto ${v.plan.codOrigen} o ${v.plan.codDestino}`
        );
        return null;
      }

      const startMs = parseFechaHoraToMs(v.fechaHoraSalida);
      const endMs   = parseFechaHoraToMs(v.fechaHoraLlegada);
      const durationSec = Math.max((endMs - startMs) / 1000, 60);
      const path = generateGeodesicPath(origin.lat, origin.lng, dest.lat, dest.lng, 120);

      return {
        code: v.codigo,
        origin,
        originName: origin.name,
        destination: dest,
        destinationName: dest.name,
        startTime: v.fechaHoraSalida,
        endTime: v.fechaHoraLlegada,
        startMs,
        endMs,
        capacity: v.capacidadOcupada,    // lo que realmente transporta
        planeCapacity: v.plan.capacidad, // capacidad total del avión (extra, por si la quieres mostrar)
        durationSec,
        progress: 0,
        arrived: false,
        path,
        position: path[0],
        rotation: 0,
      };
    }).filter(Boolean);

    setFlights(mappedFlights);
  };

  const handlePlanear = async () => {
    try {
      setLoading(true);

      if (!fechaI || !horaI || !fechaF || !horaF) {
        showNotification("danger", "Completa las fechas antes de continuar");
        setLoading(false);
        return;
      }

      const body = {
        replanificar,
        guardarPlanificacion,
        reparametrizar,
        guardarParametrizacion,
        considerarDesfaseTemporal: !!considerarDesfaseTemporal,
        parameters: {
          fechaHoraInicio: `${fechaI}T${horaI}:00`,
          fechaHoraFin: `${fechaF}T${horaF}:00`,

          maxDiasEntregaIntercontinental: (maxDiasEntregaIntercontinental),
          maxDiasEntregaIntracontinental: (maxDiasEntregaIntracontinental),
          maxHorasRecojo: (maxHorasRecojo),
          minHorasEstancia: (minHorasEstancia),
          maxHorasEstancia: (maxHorasEstancia),
          considerarDesfaseTemporal: !!considerarDesfaseTemporal,
          codOrigenes,

          dMin: (dMin),
          iMax: (iMax),
          eleMin: (eleMin),
          eleMax: (eleMax),
          kMin: (kMin),
          kMax: (kMax),
          tMax: (tMax),
          maxIntentos: (maxIntentos),

          factorDeUmbralDeAberracion: (factorDeUmbralDeAberracion),
          factorDeUtilizacionTemporal: (factorDeUtilizacionTemporal),
          factorDeDesviacionEspacial: (factorDeDesviacionEspacial),
          factorDeDisposicionOperacional: (factorDeDisposicionOperacional)
        }
      };

      console.log(body)
      const result = await planificar(body);

      if (result.success) {
        showNotification("success", "Plan generado correctamente");
        buildSimulationFromSolution(result);
        //seteamos en base a la fecha y hora inicio del modal al planificar
        setInputDate(fechaI);
        setInputTime(horaI);
      } else {
        showNotification("danger", result.message || "Error generando el plan");
      }

      closeModal();

    } catch (err) {
      showNotification("danger", err.message);
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
    if (tipoSimulacion === "semanal") {
      if (fechaI && horaI) {
        const start = new Date(`${fechaI}T${horaI}:00Z`);
        const end = new Date(start.getTime() + 7 * 24 * 60 * 60 * 1000);

        setFechaF(end.toISOString().slice(0, 10));
        setHoraF(end.toISOString().slice(11, 16));
      }
    }

  }, [tipoSimulacion, fechaI, horaI]);


  return (
    <div className="page">
      {/* Overlay de carga de simulación */}
      {loading && <LoadingOverlay text="Cargando simulación..." />}
      
      {notification && (
        <Notification
          type={notification.type}
          message={notification.message}
          onClose={() => setNotification(null)}
        />
      )}    

      <aside className={`sidebar ${collapsed ? "collapsed" : ""}`}>
        <div className="sidebar-header">
          <span className="sidebar-title">Herramientas</span>
          <img
            src={hideIcon}
            alt="Ocultar"
            className="hide-icon"
            onClick={() => setCollapsed(!collapsed)}
          />
        </div>

        {!collapsed && (
          <>
            <div className="sidebar-content">
              <span className="sidebar-subtitle">Planeación</span>
              <div className="filter-group">
                <ButtonAdd icon={plus} label="Generar plan" onClick={openModal} />
              </div>

              <span className="sidebar-subtitle">Filtros</span>
                <div className="filter-group">
                  <span className="sidebar-subtitle-strong">Proximamente...</span>
                  <Dropdown
                    placeholder="Seleccionar..."
                    options={[
                      { label: "Ejemplo 1", value: "ejemplo1" },
                      { label: "Ejemplo 2", value: "ejemplo2" },          
                    ]}
                    onSelect={(val) => setCodigoVuelo(val)}
                  />
                </div>

              <span className="sidebar-subtitle">Leyenda</span>
              <Legend
                items={[
                  { label: "50% Capacidad", status: "en-curso" },
                  { label: "75% Capacidad", status: "finalizado" },
                  { label: "100% Capacidad", status: "cancelado" }
                ]}
              />

              <SidebarActions 
                  onFilter={handleFilter}
                  onClean={handleCleanFilters}
              />
            </div>
          </>
        )}
      </aside>

      <section className="contenido">
        <div className="control-bar">
          <span className="control-label">Controles:</span>

          <button className={`btn ${btnState.start.color}`} onClick={handleStart} disabled={btnState.start.disabled} title={timerActive && !timerRunning ? "Reanudar simulación" : "Iniciar simulación"} > {timerActive && !timerRunning ? "Reanudar" : "Iniciar"} </button>
          <button className={`btn ${btnState.pause.color}`} onClick={handlePause} disabled={btnState.pause.disabled}>Pausar</button>
          <button className={`btn ${btnState.stop.color}`} onClick={handleStop} disabled={btnState.stop.disabled}>Detener</button>

          <span className="info-label">Fecha:</span>
          <span className="value">{toISODate(simNowMs)}</span>
          <span className="info-label">Hora:</span>
          <span className="value">{toISOTime(simNowMs)}</span>
          <span className="info-label">Tiempo:</span>
          <span className="value">{formatTime(seconds)}</span>
        </div>
     
        <div className="map-and-info">
          <MapContainer id="map" center={[-12.0464, -77.0428]} zoom={3}>
            <TileLayer
              url="https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png"
              attribution='&copy; <a href="https://carto.com/">Carto</a>'
            />

            {/* Marcadores de aeropuertos */}
            {airports && Object.values(airports).map((ap, i) => (
              <Marker
                key={i}
                position={[ap.lat, ap.lng]}
                icon={airportIcon}
                eventHandlers={{
                  click: () =>
                    setSelectedItem(`Aeropuerto ${ap.name} (${ap.code}) - ${ap.city}, ${ap.country}`)
                }}
              >
                <Popup>
                  <b>{ap.name}</b><br />
                  Código: {ap.code}<br />
                  Ciudad: {ap.city}<br />
                  País: {ap.country}
                </Popup>
              </Marker>
            ))}


            {flights.map((flight, i) => {
              // Si el vuelo o su path aún no existen, no renderizamos nada
              if (!flight || !flight.path || !Array.isArray(flight.path) || flight.path.length === 0) {
                return null;
              }

              const filterCss = flight.arrived
                ? "invert(35%) sepia(82%) saturate(1595%) hue-rotate(185deg) brightness(94%) contrast(92%)"
                : "invert(62%) sepia(86%) saturate(421%) hue-rotate(356deg) brightness(94%) contrast(92%)";

              return (
                <React.Fragment key={flight.code}>
                  {/* Línea del vuelo: solo mostrar cuando el vuelo ya ha despegado */}
                  {timerActive && simNowMs >= flight.startMs && simNowMs < flight.endMs && (
                    <Polyline
                      positions={flight.path.slice(
                        Math.floor(flight.path.length * flight.progress)
                      )}
                      color="#DC3545"
                      weight={3}
                      opacity={0.5}
                      dashArray="6, 10" // punteado
                    />
                  )}


                  {/* Mostramos el avión solo si no ha llegado */}
                  {!flight.arrived && (
                    <Marker
                      position={flight.position}
                      icon={createColoredIcon(filterCss, flight.rotation || 0)}
                      eventHandlers={{
                        click: () =>
                          setSelectedItem(
                            `Vuelo ${flight.code}: ${flight.originName} → ${flight.destinationName}
                            | Salida: ${flight.startTime} | Llegada: ${flight.endTime}`
                          )
                      }}
                    >
                      <Popup>
                        <b>{flight.code}</b>
                        <br />
                        {flight.originName} → {flight.destinationName}
                        <br />
                        Salida: {flight.startTime} | Llegada: {flight.endTime}
                        <br />
                        Capacidad: {flight.capacity} pax
                        <br />
                        Estado: {flight.arrived ? "Finalizado" : "En curso"}
                      </Popup>
                    </Marker>
                  )}

                </React.Fragment>
              );
            })}

            <ClickHandler onMapClick={() => setSelectedItem(null)} />
          </MapContainer>

          {/* PANEL INFORMATIVO DEBAJO DEL MAPA */}
          <div className={`info-panel ${selectedItem ? "expanded" : ""}`}>
            <div className="info-content">
              {selectedItem ? (
                <>
                  <h3>Información seleccionada</h3>
                  <body>{selectedItem}</body>
                </>
              ) : (
                <body className="placeholder">Haz clic en un avión o aeropuerto para ver detalles.</body>
              )}
            </div>
            <div className="info-triangle"></div>
          </div>
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

              {/* TIPO DE SIMULACIÓN */}
              <span className="sidebar-subtitle">Tipo de simulación</span>

              <Radio name="tipoSim" label="Semanal"
                value="semanal"
                checked={true}
                disabled={true}
                onChange={() => {}} />

              <Radio name="tipoSim" label="Colapso logístico"
                value="colapso"
                checked={tipoSimulacion === "colapso"}
                onChange={(e) => setTipoSimulacion(e.target.value)} />

              <hr />

              <span className="sidebar-subtitle">Planificación</span>
              {/* === PLANIFICACIÓN === */}

              <label>¿Replanificar?</label>
              <Radio
                name="replanificar"
                label="Sí"
                value="true"
                checked={false}
                disabled={true}
                onChange={() => {}}
              />
              <Radio
                name="replanificar"
                label="No"
                value="false"
                checked={true}
                disabled={true}
                onChange={() => {}}
              />

              <label>¿Guardar planificación?</label>
              <Radio
                name="guardarPlanificacion"
                label="Sí"
                value="true"
                checked={false}
                disabled={true}
                onChange={() => {}}
              />
              <Radio
                name="guardarPlanificacion"
                label="No"
                value="false"
                checked={true}
                disabled={true}
                onChange={() => {}}
              />

              <label>¿Reparametrizar?</label>
              <Radio
                name="reparametrizar"
                label="Sí"
                value="true"
                checked={reparametrizar === true}
                onChange={(e) => setReparametrizar(toBoolean(e.target.value))}
              />
              <Radio
                name="reparametrizar"
                label="No"
                value="false"
                checked={reparametrizar === false}
                onChange={(e) => setReparametrizar(toBoolean(e.target.value))}
              />

              <label>¿Guardar parametrización?</label>
              <Radio
                name="guardarParametrizacion"
                label="Sí"
                value="true"
                checked={false}
                disabled={true}
                onChange={() => {}}
              />
              <Radio
                name="guardarParametrizacion"
                label="No"
                value="false"
                checked={true}
                disabled={true}
                onChange={() => {}}
              />

              <hr />

              {reparametrizar && (
                <div className={`parametros-container ${reparametrizar ? "open" : "closed"}`}>
                  <span className="sidebar-subtitle">Parámetros</span>

                  {/* FECHAS SIEMPRE VISIBLES, SOLO SE DESHABILITAN */}
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

                  <label>Ciudades sede</label>              
                  {/* LISTA DE SELECCIONADOS TIPO CHIP */}
                  <div className="selected-codes">
                    {codOrigenes.map((cod) => (
                      <div key={cod} className="chip">
                        <span>{cod}</span>
                        <button
                          className="chip-remove"
                          onClick={() => {
                            setCodOrigenes(codOrigenes.filter(c => c !== cod));
                          }}
                        >
                          ×
                        </button>
                      </div>
                    ))}
                  </div>
                  {/* ORÍGENES */}
                  <Dropdown2
                    label="Códigos Origen"
                    multiple={true}
                    value={codOrigenes}
                    onChange={setCodOrigenes}
                    options={aeropuertos.map(a => ({
                      label: `${a.codigo} - ${a.ciudad} - ${a.pais}`,
                      value: a.codigo
                    }))}
                  />

                  {/* BOOLEANO */}
                  <label>¿Considerar desfase temporal?</label>
                  <Radio
                    name="desfase"
                    label="Sí"
                    value="true"
                    checked={false}
                    disabled={true}
                    onChange={() => {}}
                  />
                  <Radio
                    name="desfase"
                    label="No"
                    value="false"
                    checked={true}
                    disabled={true}
                    onChange={() => {}}
                  />

                  {/* NUMÉRICOS */}
                  <label>Max días entrega intercontinental</label>
                  <Input label="Max días entrega intercontinental" type="number"
                    value={maxDiasEntregaIntercontinental} onChange={(e) => setMaxDiasEntregaIntercontinental(parseNumber(e.target.value))}/>

                  <label>Max días entrega intracontinental</label>
                  <Input label="Max días entrega intracontinental" type="number"
                    value={maxDiasEntregaIntracontinental} onChange={(e) => setMaxDiasEntregaIntracontinental(parseNumber(e.target.value))}/>

                  <label>Max horas recojo</label>
                  <Input label="Max horas recojo" type="number"
                    value={maxHorasRecojo} onChange={(e) => setMaxHorasRecojo(parseNumber(e.target.value))}/>

                  <label>Min horas estancia</label>
                  <Input label="Min horas estancia" type="number"
                    value={minHorasEstancia} onChange={(e) => setMinHorasEstancia(parseNumber(e.target.value))}/>

                  <label>Max horas estancia</label>
                  <Input label="Max horas estancia" type="number"
                    value={maxHorasEstancia} onChange={(e) => setMaxHorasEstancia(parseNumber(e.target.value))}/>

                  {/* OPTIMIZACIÓN */}
                  <label>dMin</label>
                  <Input label="dMin" type="number" value={dMin} onChange={(e) => setDMin(parseNumber(e.target.value))}/>

                  <label>iMax</label>
                  <Input label="iMax" type="number" value={iMax} onChange={(e) => setIMax(parseNumber(e.target.value))}/>

                  <label>eleMin</label>
                  <Input label="eleMin" type="number" value={eleMin} onChange={(e) => setEleMin(parseNumber(e.target.value))}/>

                  <label>eleMax</label>
                  <Input label="eleMax" type="number" value={eleMax} onChange={(e) => setEleMax(parseNumber(e.target.value))}/>

                  <label>kMin</label>
                  <Input label="kMin" type="number" value={kMin} onChange={(e) => setKMin(parseNumber(e.target.value))}/>

                  <label>kMax</label>
                  <Input label="kMax" type="number" value={kMax} onChange={(e) => setKMax(parseNumber(e.target.value))}/>

                  <label>tMax</label>
                  <Input label="tMax" type="number" value={tMax} onChange={(e) => setTMax(parseNumber(e.target.value))}/>

                  <label>Max intentos</label>
                  <Input label="Max intentos" type="number" value={maxIntentos} onChange={(e) => setMaxIntentos(parseNumber(e.target.value))}/>

                  {/* FACTORES */}
                  <label>Factor de Umbral de Aberración</label>
                  <Input label="Factor de Umbral de Aberración" type="number"
                    value={factorDeUmbralDeAberracion} onChange={(e) => setFactorDeUmbralDeAberracion(parseNumber(e.target.value))}/>

                  <label>Factor de Utilización Temporal</label>
                  <Input label="Factor de Utilización Temporal" type="number"
                    value={factorDeUtilizacionTemporal} onChange={(e) => setFactorDeUtilizacionTemporal(parseNumber(e.target.value))}/>

                  <label>Factor de Desviación Espacial</label>
                  <Input label="Factor de Desviación Espacial" type="number"
                    value={factorDeDesviacionEspacial} onChange={(e) => setFactorDeDesviacionEspacial(parseNumber(e.target.value))}/>

                  <label>Factor de Disposición Operacional</label>
                  <Input label="Factor de Disposición Operacional" type="number"
                    value={factorDeDisposicionOperacional} onChange={(e) => setFactorDeDisposicionOperacional(parseNumber(e.target.value))}/>
                </div>
              )}
            
            </div>

            <div className="modal-footer">
              <button className="btn red" onClick={closeModal}>Cancelar</button>
              <button className="btn green" onClick={handlePlanear}>Planear</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}