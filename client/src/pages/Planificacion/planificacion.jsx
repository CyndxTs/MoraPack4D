import React, { useState, useEffect, useRef } from "react";
import "./planificacion.scss";
import { RemoveFileButton, ButtonAdd, Input, Table, SidebarActions, LoadingOverlay, Legend, Notification, Radio, DateTimeInline, Dropdown, Dropdown2, Dropdown3 } from "../../components/UI/ui";
import plus from "../../assets/icons/plus.svg";
import run from "../../assets/icons/run.svg";
import config from "../../assets/icons/config.svg";
import hideIcon from "../../assets/icons/hide-sidebar.png";
import { listarPedidos, importarPedido, importarPedidos } from "../../services/pedidoService";
import { listarParametros } from "../../services/parametrosService";
import { useAppData } from "../../dataProvider";
import { MapContainer, TileLayer, Marker, Popup, Polyline, useMapEvent  } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from "leaflet";

import planeIconImg from "../../assets/icons/planeMora.svg";

export default function Planificacion() {
  const { loadingData, pedidos, pedidosOriginales, clientes, aeropuertos, rutas, vuelos, flights, setPedidos, setPedidosOriginales, setFlights } = useAppData();
  // ----------------------------------------
  // UI / layout state
  // ----------------------------------------
  const [collapsed, setCollapsed] = useState(false);
  const [selectedItem, setSelectedItem] = useState(null);
  const [notification, setNotification] = useState(null);

  // ----------------------------------------
  // Modal / planificación state
  // ----------------------------------------
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isModalPedidoOpen, setIsModalPedidoOpen] = useState(false);
  const [tipoSimulacion, setTipoSimulacion] = useState("seleccionar");
  const [fechaI, setFechaI] = useState("");
  const [horaI, setHoraI] = useState("");
  const [fechaF, setFechaF] = useState("");
  const [horaF, setHoraF] = useState("");
  const [loadedOnOpen, setLoadedOnOpen] = useState(false);
  const [parametros, setParametros] = useState(null);
  const [tipoEscenario, setTipoEscenario] = useState("");

  // control visual / inputs
  const [codigoVuelo, setCodigoVuelo] = useState("");
  
  // ----------------------------------------
  // Parámetros de planificación (form)
  // ----------------------------------------
  // flags
  const [reparametrizar, setReparametrizar] = useState(false);

  // listas / selects
  
  const [codOrigenes, setCodOrigenes] = useState([]);

  // parámetros numéricos
  const [maxDiasEntregaIntercontinental, setMaxDiasEntregaIntercontinental] = useState();
  const [maxDiasEntregaIntracontinental, setMaxDiasEntregaIntracontinental] = useState();
  const [maxHorasRecojo, setMaxHorasRecojo] = useState();
  const [minHorasEstancia, setMinHorasEstancia] = useState();
  const [maxHorasEstancia, setMaxHorasEstancia] = useState();
  const [considerarDesfaseTemporal, setConsiderarDesfaseTemporal] = useState();

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

  // ----------------------------------------
  // Aeropuertos / Vuelos (simulación)
  // ----------------------------------------
  const [airports, setAirports] = useState(null);

  const [rawFlights, setRawFlights] = useState([]);

  // ----------------------------------------
  // Simulación: reloj, velocidad y timers
  // ----------------------------------------

  const [simNowMs, setSimNowMs] = useState(() => Date.now());
  const [simSpeed, setSimSpeed] = useState(); // se espera número ms_sim / ms_real

  const lastRealMsRef = useRef(null);

  const [seconds, setSeconds] = useState(0);
  const [timerRunning, setTimerRunning] = useState(false);
  const [timerActive, setTimerActive] = useState(false); // start clickeado

  // loading local para operaciones (handlePlanear)
  const [loading, setLoading] = useState(false);
  const [processing, setProcessing] = useState(false);
  
  // ----------------------------------------
  // Reloj de CONTROL-BAR: hora real UTC-5 (Perú) con segundos
  // ----------------------------------------
  // controlNowMs se actualiza cada segundo con la hora completed en UTC (0) menos 5 horas.
  const [controlNowMs, setControlNowMs] = useState(() => {
    const now = new Date();
    const utcMs = now.getTime() + now.getTimezoneOffset() * 60000;
    const peruMs = utcMs - 5 * 60 * 60 * 1000; // UTC-5
    return peruMs;
  });

  // actualiza controlNowMs cada segundo (reloj en vivo para control-bar)
  useEffect(() => {
    const tick = () => {
      const now = new Date();
      const utcMs = now.getTime() + now.getTimezoneOffset() * 60000;
      const peruMs = utcMs - 5 * 60 * 60 * 1000; // UTC-5
      setControlNowMs(peruMs);
    };

    tick(); // setear inmediatamente
    const id = setInterval(tick, 1000);
    return () => clearInterval(id);
  }, []);

  // ----------------------------------------
  // Helpers / utilidades (unificadas)
  // ----------------------------------------

  const parseNumber = (v) => {
    if (v === "" || v === null || v === undefined) return null;
    return Number(v);
  };

  const toISODate = (ms) => new Date(ms).toISOString().split("T")[0];

  // ahora incluye segundos HH:MM:SS
  const toISOTimeWithSeconds = (ms) => {
    const d = new Date(ms);
    // obtener componentes en UTC-0 del ms ya ajustado (controlNowMs está en UTC-5 ms)
    const hh = d.getUTCHours().toString().padStart(2, "0");
    const mm = d.getUTCMinutes().toString().padStart(2, "0");
    const ss = d.getUTCSeconds().toString().padStart(2, "0");
    return `${hh}:${mm}:${ss}`;
  };

  function unirFechaHora(fechaDateInput, horaHHmm) {
    if (!fechaDateInput || !horaHHmm) return null;

    return `${fechaDateInput} ${horaHHmm}:00`; 
  }

  const handleFileChange = (e) => {
    if (e.target.files.length > 0) setArchivo(e.target.files[0]);
    else resetArchivo();
  };

  const showNotification = (type, message) => {
    setNotification({ type, message });
    setTimeout(() => setNotification(null), 5000);
  };

  const resetDatos = () => {
    setFecha("");
    setHora("");
    setCantidad("");
    setSelectedCliente(null);
    setSelectedDestino(null);
  };

  function formatearFechaInput(fecha) {
    if (!fecha) return "";
    return fecha.replace(/-/g, ""); 
  }

  function formatearHoraeInput(hora) {
    if (!hora) return "";
    return hora.replace(/:/g, "-"); 
  }

  // ----------------------------------------
  // Pedidos
  // ----------------------------------------
  
  const [fecha, setFecha] = useState("");   
  const [hora, setHora] = useState("");    
  const [cantidad, setCantidad] = useState("");
  const [selectedCliente, setSelectedCliente] = useState(null);
  const [selectedDestino, setSelectedDestino] = useState(null);

  const [archivo, setArchivo] = useState(null);
  const [fechaArchivoFechaI, setFechaArchivoFechaI] = useState("");
  const [fechaArchivoHoraI, setFechaArchivoHoraI] = useState("");
  const [fechaArchivoFechaF, setFechaArchivoFechaF] = useState("");
  const [fechaArchivoHoraF, setFechaArchivoHoraF] = useState("");

  

  const handleAdd = async () => {
    try {
      setProcessing(true);

      // --- CASO 1: ARCHIVO ---
      if (archivo) {
        /*if (archivo.name !== "Pedidos.txt") {
          showNotification("warning", "El archivo debe llamarse 'Pedidos.txt'.");
          return;
        }*/

        const fechaInicio = unirFechaHora(fechaArchivoFechaI, fechaArchivoHoraI);
        const fechaFin = unirFechaHora(fechaArchivoFechaF, fechaArchivoHoraF);
        
        console.log(archivo);
        console.log(fechaInicio);
        console.log(fechaFin);

        const req = {
          tipoEscenario: tipoEscenario,
          fechaHoraInicio: unirFechaHora(fechaArchivoFechaI, fechaArchivoHoraI),
          fechaHoraFin: unirFechaHora(fechaArchivoFechaF, fechaArchivoHoraF)
        };

        console.log(req);
        const respuesta = await importarPedidos(archivo, req);
        if (respuesta.success) {
          showNotification("success", respuesta.message || "Pedidos importados correctamente");
        } else {
          showNotification("danger", respuesta.message || "Ocurrió un error al importar los pedidos");
        }
      }

      // --- CASO 2: MANUAL ---
      else {
        if (!selectedCliente || !selectedDestino || !fecha || !hora || !cantidad) {
          showNotification("warning", "Completa todos los campos del pedido manual.");
          return;
        }

        const fechaGeneracion = unirFechaHora(fecha, hora);

        const dto = {
          codigo: null,
          codCliente: selectedCliente.codigo,
          codDestino: selectedDestino.codigo,
          fechaHoraGeneracion: fechaGeneracion,
          cantidadSolicitada: Number(cantidad),
          lotesPorRuta: [],
          tipoEscenario: tipoEscenario
        };

        console.log(dto);
        console.log("DTO generado:", dto);

        await importarPedido(dto);

        showNotification("success", "Pedido manual registrado correctamente");
        resetDatos();
      }

      // --- Recargar tabla ---
      const data = await listarPedidos();
      setPedidos(data.dtos || []);
      setPedidosOriginales(data.dtos || []);

      setIsModalPedidoOpen(false);
      setArchivo(null);
    } catch {
      showNotification("danger", "Error al agregar pedido");
    } finally {
      setProcessing(false);
    }
  };

  function generarCodigoPedido() {
    if (!selectedCliente || !selectedDestino || !fecha || !hora || !cantidad)
      return "";

    return (
      "XXXXXXXX" +
      "-" + formatearFechaInput(fecha) +
      "-" + formatearHoraeInput(hora) +
      "-" + cantidad.padStart(3, "0") +
      "-" + selectedDestino.codigo +
      "-" + selectedCliente.codigo.padStart(7, "0")
    );
  }

  // ----------------------------------------
  // Icons (leaflet divIcons)
  // ----------------------------------------
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
      iconAnchor: [11, 8]
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
    iconAnchor: [5, 5]
  });

  // ----------------------------------------
  // MAPA
  // ----------------------------------------
  function computeBearingRotation(pos, next) {
    const toRad = d => d * Math.PI / 180;
    const toDeg = r => r * 180 / Math.PI;

    const lat1 = toRad(pos.lat);
    const lon1 = toRad(pos.lng);
    const lat2 = toRad(next.lat);
    const lon2 = toRad(next.lng);

    let bearing = Math.atan2(
      Math.sin(lon2 - lon1) * Math.cos(lat2),
      Math.cos(lat1) * Math.sin(lat2) -
      Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)
    );

    bearing = (toDeg(bearing) + 360) % 360;

    return bearing - 45; // igual que en Simulacion.jsx
  }

  const createPlaneIcon = (filterCss,rotation) =>
    L.divIcon({
      html: `<img src="${planeIconImg}" 
              style="width:18px;
                    transform: rotate(${rotation}deg);
                    transform-origin: center center;
                    filter:${filterCss};
                    transition: transform 0.3s linear;">`,
      className: "",
      iconSize: [18, 18],
      iconAnchor: [11, 8]
    });


  function parseFechaHoraLocalPeru(str) {
    // str viene así: "19/11/2025 21:02"
    const [fecha, hora] = str.split(" ");
    const [dd, mm, yyyy] = fecha.split("/");
    const [HH, MM] = hora.split(":");

    // Construir fecha en UTC-5
    const date = new Date(Date.UTC(yyyy, mm - 1, dd, HH, MM));
    // Restar 5h para Perú
    return date.getTime() - 5 * 3600000;
  }

  function createFlights(vuelos, aeropuertos) {
    return vuelos.map(v => {
      const startMs = parseFechaHoraLocalPeru(v.fechaHoraSalida);
      const endMs   = parseFechaHoraLocalPeru(v.fechaHoraLlegada);

      const origen = aeropuertos.find(a => a.codigo === v.plan.codOrigen);
      const destino = aeropuertos.find(a => a.codigo === v.plan.codDestino);

      // ❗ Si falta alguno o no tiene lat/lng → vuelo sin path (no se dibuja)
      if (!origen || !destino ||
          origen.latitudDEC == null || origen.longitudDEC == null ||
          destino.latitudDEC == null || destino.longitudDEC == null) {

        return {
          code: v.codigo,
          startMs,
          endMs,
          originName: origen?.ciudad ?? "N/A",
          destinationName: destino?.ciudad ?? "N/A",
          startTime: v.fechaHoraSalida,
          endTime: v.fechaHoraLlegada,
          capacity: 0,
          origen,
          destino,
          path: [],        // <-- vacío
          progress: 0,
          visible: false,
          arrived: false,
          position: null   // <-- no hay posición inicial
        };
      }

      // Si hay coordenadas, sí generamos path
      const path = generateGeodesicPath(
        origen.latitudDEC,
        origen.longitudDEC,
        destino.latitudDEC,
        destino.longitudDEC
      );

      return {
        code: v.codigo,
        startMs,
        endMs,
        originName: origen.ciudad,
        destinationName: destino.ciudad,
        startTime: v.fechaHoraSalida,
        endTime: v.fechaHoraLlegada,
        capacity: 0,
        origen,
        destino,
        path,
        progress: 0,
        visible: false,
        arrived: false,
        position: path[0]
      };
    });
  }


  useEffect(() => {
    if (!vuelos || vuelos.length === 0 || !aeropuertos) return;
    const f = createFlights(vuelos, aeropuertos);
    setFlights(f);
  }, [vuelos, aeropuertos]);

  useEffect(() => {
    const now = controlNowMs;

    setFlights(prev =>
      prev.map(f => {

        if (!f || !f.path || f.path.length === 0) return f;
        
        if (now < f.startMs) {
          return { ...f, visible: false, progress: 0, arrived: false, position: f.path[0] };
        }

        if (now >= f.endMs) {
          return {
            ...f,
            visible: false,
            progress: 1,
            arrived: true,
            position: f.path[f.path.length - 1]
          };
        }

        const total = Math.max(f.endMs - f.startMs, 60 * 1000);
        const frac = Math.min((controlNowMs - f.startMs) / total, 1);
        const idx = Math.floor(frac * (f.path.length - 1));
        const pos = f.path[idx];
        const next = f.path[Math.min(idx + 1, f.path.length - 1)];
        const rotation = computeBearingRotation(pos, next);

        return {
          ...f,
          visible: true,
          progress: frac,
          arrived: false,
          position: pos,
          rotation,
        };
      })
    );
  }, [controlNowMs]);


  // ----------------------------------------
  // Generación de path geodésico (utilidad)
  // ----------------------------------------
  function generateGeodesicPath(lat1, lon1, lat2, lon2, numPoints = 100) {
    // Si falta algún valor → regresamos un path vacío
    if (
      lat1 == null || lon1 == null ||
      lat2 == null || lon2 == null ||
      isNaN(lat1) || isNaN(lon1) ||
      isNaN(lat2) || isNaN(lon2)
    ) {
      return [];
    }

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
      const x = A * Math.cos(lat1) * Math.cos(lon1) + B * Math.cos(lat2) * Math.cos(lon2);
      const y = A * Math.cos(lat1) * Math.sin(lon1) + B * Math.cos(lat2) * Math.sin(lon2);
      const z = A * Math.sin(lat1) + B * Math.sin(lat2);
      const lat = Math.atan2(z, Math.sqrt(x ** 2 + y ** 2));
      const lon = Math.atan2(y, x);
      points.push({ lat: toDeg(lat), lng: toDeg(lon) });
    }
    return points;
  }

  // ----------------------------------------
  // Efecto: detectar cuando todos los vuelos han llegado
  // ----------------------------------------

  // ----------------------------------------
  // Modal: cargar parámetros y aeropuertos al abrir
  // ----------------------------------------
  useEffect(() => {
    const fetchParametrosYAeropuertos = async () => {
      try {
        const p = (await listarParametros()).dtos[0];

        setParametros(p);

        // setear parámetros desde BD
        setMaxDiasEntregaIntercontinental(p.maxDiasEntregaIntercontinental);
        setMaxDiasEntregaIntracontinental(p.maxDiasEntregaIntracontinental);
        setMaxHorasRecojo(p.maxHorasRecojo);
        setMinHorasEstancia(p.minHorasEstancia);
        setMaxHorasEstancia(p.maxHorasEstancia);
        setConsiderarDesfaseTemporal(p.considerarDesfaseTemporal);

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

        setCodOrigenes((prev) => (prev.length === 0 ? p.codOrigenes || [] : prev));
      } catch (err) {
        showNotification("danger", "Error cargando parámetros");
      }
    };

    if (isModalOpen && !loadedOnOpen) {
      fetchParametrosYAeropuertos();
      setLoadedOnOpen(true);
    }
  }, [isModalOpen, loadedOnOpen]);

  // ----------------------------------------
  // Manejo fechas según tipoSimulacion
  // ----------------------------------------

  // ----------------------------------------
  // Planear -> llama al servicio planificar
  // ----------------------------------------


  // ----------------------------------------
  // Modal helpers
  // ----------------------------------------
  const resetModal = () => {
    setTipoSimulacion("seleccionar");
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

  // ----------------------------------------
  // Filtros (placeholders, se dejan vacíos)
  // ----------------------------------------
  const handleFilter = async () => {
    // implementar según necesidad
  };

  const handleCleanFilters = async () => {
    // implementar según necesidad
  };

  // ----------------------------------------
  // Small ClickHandler for map (leaflet)
  // ----------------------------------------
  function ClickHandler({ onMapClick }) {
    useMapEvent("click", () => onMapClick());
    return null;
  }

  return (
    <div className="page">
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
          <img src={hideIcon} alt="Ocultar" className="hide-icon" onClick={() => setCollapsed(!collapsed)} />
        </div>

        {!collapsed && (
          <div className="sidebar-content">
            <span className="sidebar-subtitle">Operación diaria</span>
            <div className="filter-group">
              <ButtonAdd
                icon={run}
                label={"Replanificar"}
              />
              <ButtonAdd icon={plus} label="Agreg. pedido" 
                onClick={() => {
                  const now = new Date();
                  const yyyy = now.getUTCFullYear();
                  const mm = String(now.getUTCMonth() + 1).padStart(2, "0");
                  const dd = String(now.getUTCDate()).padStart(2, "0");
                  const HH = String(now.getUTCHours()).padStart(2, "0");
                  const MM = String(now.getUTCMinutes()).padStart(2, "0");

                  setFecha(`${yyyy}-${mm}-${dd}`);   // input type="date" usa YYYY-MM-DD
                  setHora(`${HH}:${MM}`); 
                  setIsModalPedidoOpen(true);
                }}
              />
              <ButtonAdd icon={config} label="Config. parám." onClick={() => openModal(true)} />
            </div>

            <span className="sidebar-subtitle">Filtros</span>
            <div className="filter-group">
              <span className="sidebar-subtitle-strong">Proximamente...</span>
              <Dropdown
                placeholder="Seleccionar..."
                options={[
                  { label: "Ejemplo 1", value: "ejemplo1" },
                  { label: "Ejemplo 2", value: "ejemplo2" }
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

            <SidebarActions onFilter={handleFilter} onClean={handleCleanFilters} />
          </div>
        )}
      </aside>

      <section className="contenido">
        <div className="control-bar">
          <span className="info-label">Fecha:</span>
          <span className="value">{toISODate(controlNowMs)}</span>
          <span className="info-label">Hora:</span>
          <span className="value">{toISOTimeWithSeconds(controlNowMs)}</span>
        </div>

        <div className="map-and-info">
          <MapContainer id="map" center={[-12.0464, -77.0428]} zoom={3}>
            <TileLayer
              url="https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png"
              attribution='&copy; <a href="https://carto.com/">Carto</a>'
            />

            {/* Marcadores de aeropuertos (usando tu lista aeropuertos) */}
            {aeropuertos
              .filter(aero =>
                aero.latitudDEC !== null &&
                aero.longitudDEC !== null &&
                !isNaN(aero.latitudDEC) &&
                !isNaN(aero.longitudDEC)
              )
              .map((aero) => (
                <Marker
                  key={aero.codigo}
                  position={[aero.latitudDEC, aero.longitudDEC]}
                  icon={airportIcon}
                  eventHandlers={{
                    click: () =>
                      setSelectedItem(
                        `Aeropuerto ${aero.ciudad} (${aero.codigo}) - ${aero.pais}`
                      )
                  }}
                >
                <Popup>
                  <b>{aero.ciudad}</b><br />
                  Código: {aero.codigo}<br />
                  Ciudad: {aero.ciudad}<br />
                  País: {aero.pais}<br />
                  Capacidad: {aero.capacidad}/{aero.capacidad}<br />
                </Popup>
              </Marker>
            ))}

            {/* Aviones y rutas */}
            {flights.map((flight) => {
              if (!flight || !flight.path || !Array.isArray(flight.path)) return null;

              // COLOR DEL AVIÓN
              const filterCss = flight.arrived
                ? "invert(35%) sepia(82%) saturate(1595%) hue-rotate(185deg) brightness(94%) contrast(92%)"
                : "invert(62%) sepia(86%) saturate(421%) hue-rotate(356deg) brightness(94%) contrast(92%)";

              return (
              <React.Fragment key={flight.code}>

                {/* Línea del vuelo (solo si está en vuelo) */}
                {controlNowMs >= flight.startMs &&
                  controlNowMs < flight.endMs && (
                    <Polyline
                      positions={flight.path.slice(
                        Math.floor(flight.path.length * flight.progress)
                      )}
                      color="#DC3545"
                      weight={3}
                      opacity={0.5}
                      dashArray="6, 10"
                    />
                )}

                {/* Avión rotado según bearing, solo si está visible y no llegó */}
                {!flight.arrived && flight.visible && (
                  <Marker
                    position={[flight.position.lat, flight.position.lng]}
                    icon={createPlaneIcon(filterCss, flight.rotation || 0)}
                    eventHandlers={{
                      click: () =>
                        setSelectedItem(
                          `Vuelo ${flight.code}: ${flight.originName} → ${flight.destinationName}
                          | Salida: ${flight.startTime} | Llegada: ${flight.endTime}`
                        )
                    }}
                  >
                    <Popup>
                      <b>{flight.code}</b><br />
                      {flight.originName} → {flight.destinationName}<br />
                      Salida: {flight.startTime}<br />
                      Llegada: {flight.endTime}<br />
                      Capacidad: {flight.capacity} pax<br />
                      Progreso: {(flight.progress * 100).toFixed(1)}%<br />
                      Estado: {flight.arrived ? "Finalizado" : "En curso"}
                    </Popup>
                  </Marker>
                )}
              </React.Fragment>
            );
          })}

            <ClickHandler onMapClick={() => setSelectedItem(null)} />
          </MapContainer>


          {/* PANEL INFORMATIVO */}
          <div className={`info-panel ${selectedItem ? "expanded" : ""}`}>
            <div className="info-content">
              {selectedItem ? (
                <>
                  <h3>Información seleccionada</h3>
                  <div>{selectedItem}</div>
                </>
              ) : (
                <div className="placeholder">Haz clic en un avión o aeropuerto para ver detalles.</div>
              )}
            </div>
            <div className="info-triangle" />
          </div>
        </div>
      </section>

      {/* MODAL */}
      {isModalOpen && (
        <div className="modal" onClick={closeModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">Configuración de parámetros</h3>
            </div>

            <div className="modal-body">
              
              <span className="sidebar-subtitle">Párametros</span>

                <div className={`parametros-container ${reparametrizar ? "open" : "closed"}`}>

                  <label>Fecha y hora de inicio (UTC)</label>
                  <DateTimeInline dateValue={fechaI} timeValue={horaI} onDateChange={(e) => setFechaI(e.target.value)} onTimeChange={(e) => setHoraI(e.target.value)} />

                  <label>Fecha y hora de fin (UTC)</label>
                  <DateTimeInline dateValue={fechaF} timeValue={horaF} onDateChange={(e) => setFechaF(e.target.value)} onTimeChange={(e) => setHoraF(e.target.value)} />

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
                  <Input label="Max días entrega intercontinental" type="number" value={maxDiasEntregaIntercontinental} onChange={(e) => setMaxDiasEntregaIntercontinental(parseNumber(e.target.value))} />

                  <label>Max días entrega intracontinental</label>
                  <Input label="Max días entrega intracontinental" type="number" value={maxDiasEntregaIntracontinental} onChange={(e) => setMaxDiasEntregaIntracontinental(parseNumber(e.target.value))} />

                  <label>Max horas recojo</label>
                  <Input label="Max horas recojo" type="number" value={maxHorasRecojo} onChange={(e) => setMaxHorasRecojo(parseNumber(e.target.value))} />

                  <label>Min horas estancia</label>
                  <Input label="Min horas estancia" type="number" value={minHorasEstancia} onChange={(e) => setMinHorasEstancia(parseNumber(e.target.value))} />

                  <label>Max horas estancia</label>
                  <Input label="Max horas estancia" type="number" value={maxHorasEstancia} onChange={(e) => setMaxHorasEstancia(parseNumber(e.target.value))} />

                  <label>dMin</label>
                  <Input label="dMin" type="number" value={dMin} onChange={(e) => setDMin(parseNumber(e.target.value))} />

                  <label>iMax</label>
                  <Input label="iMax" type="number" value={iMax} onChange={(e) => setIMax(parseNumber(e.target.value))} />

                  <label>eleMin</label>
                  <Input label="eleMin" type="number" value={eleMin} onChange={(e) => setEleMin(parseNumber(e.target.value))} />

                  <label>eleMax</label>
                  <Input label="eleMax" type="number" value={eleMax} onChange={(e) => setEleMax(parseNumber(e.target.value))} />

                  <label>kMin</label>
                  <Input label="kMin" type="number" value={kMin} onChange={(e) => setKMin(parseNumber(e.target.value))} />

                  <label>kMax</label>
                  <Input label="kMax" type="number" value={kMax} onChange={(e) => setKMax(parseNumber(e.target.value))} />

                  <label>tMax</label>
                  <Input label="tMax" type="number" value={tMax} onChange={(e) => setTMax(parseNumber(e.target.value))} />

                  <label>Max intentos</label>
                  <Input label="Max intentos" type="number" value={maxIntentos} onChange={(e) => setMaxIntentos(parseNumber(e.target.value))} />

                  <label>Factor de Umbral de Aberración</label>
                  <Input label="Factor de Umbral de Aberración" type="number" value={factorDeUmbralDeAberracion} onChange={(e) => setFactorDeUmbralDeAberracion(parseNumber(e.target.value))} />

                  <label>Factor de Utilización Temporal</label>
                  <Input label="Factor de Utilización Temporal" type="number" value={factorDeUtilizacionTemporal} onChange={(e) => setFactorDeUtilizacionTemporal(parseNumber(e.target.value))} />

                  <label>Factor de Desviación Espacial</label>
                  <Input label="Factor de Desviación Espacial" type="number" value={factorDeDesviacionEspacial} onChange={(e) => setFactorDeDesviacionEspacial(parseNumber(e.target.value))} />

                  <label>Factor de Disposición Operacional</label>
                  <Input label="Factor de Disposición Operacional" type="number" value={factorDeDisposicionOperacional} onChange={(e) => setFactorDeDisposicionOperacional(parseNumber(e.target.value))} />
                </div>

            </div>

            <div className="modal-footer">
              <button className="btn red" onClick={closeModal}>
                Cancelar
              </button>
              <button className="btn green" onClick={openModal}>
                Guardar
              </button>
            </div>
          </div>
        </div>
      )}



      {/* Modal pedido */}
      {isModalPedidoOpen && (
        <div className="modal" onClick={() => setIsModalOpen(false)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">Agregar pedido</h3>
              <label htmlFor="fileInput" className="file-label">Agregar archivo</label>
              <input type="file" id="fileInput" className="file-input" onChange={handleFileChange} disabled={generarCodigoPedido() !== ""}/>
            </div>

            <div className="file-name">
              {archivo ? archivo.name : "Ningún archivo seleccionado"}
              {archivo && (
                <RemoveFileButton onClick={() => setArchivo(null)} />
              )}
            </div>
            
            <div className="modal-body">
              {archivo && (
              <>
                <label>Fecha y hora de inicio (UTC)</label>
                <DateTimeInline
                  dateValue={fechaArchivoFechaI}
                  timeValue={fechaArchivoHoraI}
                  onDateChange={(e) => setFechaArchivoFechaI(e.target.value)}
                  onTimeChange={(e) => setFechaArchivoHoraI(e.target.value)}
                />
                <label>Fecha y hora de fin (UTC)</label>
                <DateTimeInline
                  dateValue={fechaArchivoFechaF}
                  timeValue={fechaArchivoHoraF}
                  onDateChange={(e) => setFechaArchivoFechaF(e.target.value)}
                  onTimeChange={(e) => setFechaArchivoHoraF(e.target.value)}
                />
              </>
              )}

              <label>Tipo de escenario</label>
                <Radio
                  name="tipoEscenario"
                  label="OPERACION"
                  value="OPERACION"
                  checked={tipoEscenario === "OPERACION"}
                  onChange={(e) => setTipoEscenario(e.target.value)}
                />
                <Radio
                  name="tipoEscenario"
                  label="SIMLULACION"
                  value="SIMLULACION"
                  checked={tipoEscenario === "SIMLULACION"}
                  onChange={(e) => setTipoEscenario(e.target.value)}
                />

              <label>Fecha y hora de generación (UTC)</label>
              <DateTimeInline
                dateValue={fecha}
                timeValue={hora}
                onDateChange={(e) => setFecha(e.target.value)}
                onTimeChange={(e) => setHora(e.target.value)}
                disabled={!!archivo}
              />

              {/* CANTIDAD */}
              <label>Cantidad</label>
              <Input
                placeholder="006"
                value={cantidad}
                onChange={(e) => setCantidad(e.target.value)}
                disabled={!!archivo}
              />

              {/* DESTINO */}
              <label>Destino</label>
              <Dropdown3
                placeholder="Seleccionar aeropuerto..."
                options={aeropuertos.map(a => ({
                  label: `${a.codigo} - ${a.ciudad} - ${a.pais}`,
                  value: a
                }))}
                value={selectedDestino}
                onSelect={(item) => setSelectedDestino(item)}
                disabled={!!archivo}
              />

              {/* CLIENTE */}
              <label>Cliente</label>
              <Dropdown3
                placeholder="Seleccionar cliente..."
                options={clientes.map(c => ({
                  label: `${c.codigo} - ${c.nombre}`,
                  value: c
                }))}
                value={selectedCliente}
                onSelect={(item) => setSelectedCliente(item)}
                disabled={!!archivo}
              />

              {/* MOSTRAR RESULTADO */}
              <label>Código generado</label>
              <Input
                value={generarCodigoPedido()}
                disabled
              />
            </div>

            <div className="modal-footer">
              <button className="btn red" onClick={() => setIsModalPedidoOpen(false)}>Cancelar</button>
              <button className="btn green" onClick={handleAdd}>Agregar</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
