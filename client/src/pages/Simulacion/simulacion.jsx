import React, { useState, useEffect } from "react";
import "./simulacion.scss";
import { Radio, Checkbox, Dropdown, Legend, Notification, SidebarActions } from "../../components/UI/ui";
import hideIcon from '../../assets/icons/hide-sidebar.png';

import { MapContainer, TileLayer, Marker, Popup, Polyline  } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from "leaflet";

import planeIconImg from "../../assets/icons/planeMora.svg";
//import { getAeropuertosMap  } from "../../services/aeropuertoService";
import { getAeropuertosMapWS, getVuelosWS, disconnectWS } from "../../services/simulationService";

export default function Simulacion() {
  const [collapsed, setCollapsed] = useState(false);
  const [codigoVuelo, setCodigoVuelo] = useState("");
  const [ciudadDestino, setCiudadDestino] = useState("");
  const [continente, setContinente] = useState({ america: false, europa: false, asia: false });
  const [estadoVuelo, setEstadoVuelo] = useState({ enCurso: false, finalizado: false, cancelado: false });
  const [archivo, setArchivo] = useState(null);
  const [tipoSimulacion, setTipoSimulacion] = useState("");
  //Aeropuertos
  const [airports, setAirports] = useState(null);
  const [loadingAirports, setLoadingAirports] = useState(true);
  // Controles de tiempo
  const [date, setDate] = useState(new Date().toISOString().split("T")[0]);
  const [time, setTime] = useState(new Date().toTimeString().slice(0,5));
  const [seconds, setSeconds] = useState(0);
  const [timerRunning, setTimerRunning] = useState(false);
  const [timerActive, setTimerActive] = useState(false); // indica si inició cronómetro (start clickeado)

  // Cargar aeropuertos del back una sola vez

  //Notificaciones
  const [notification, setNotification] = useState(null);
  
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

  // Reloj 
  useEffect(() => {
    const clock = setInterval(() => {
      const now = new Date();
      setTime(now.toTimeString().slice(0,5));
      if (!timerActive) setDate(now.toISOString().split("T")[0]); // solo actualiza fecha si no hay simulación activa
    }, 1000);
    return () => clearInterval(clock);
  }, [timerActive]);

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
    // Detener el cronómetro y el estado de simulación
    setTimerRunning(false);
    setTimerActive(false);
    setSeconds(0);

    // Reiniciar todos los vuelos a su punto de origen
    if (airports && rawFlights.length > 0) {
      const reset = rawFlights.map((f) => {
        const origin = airports[f.origenCodigo];
        const dest = airports[f.destinoCodigo];
        if (!origin || !dest) return null;

        const salida = new Date(f.fechaSalida);
        const llegada = new Date(f.fechaLlegada);
        const durationSec = Math.max((llegada - salida) / 1000, 60);

        // Regeneramos el path para evitar errores si se había eliminado
        const path = generateGeodesicPath(origin.lat, origin.lng, dest.lat, dest.lng, 120);

        return {
          code: f.codigo,
          origin,
          originName: origin.name,
          destination: dest,
          destinationName: dest.name,
          startTime: f.fechaSalida,
          endTime: f.fechaLlegada,
          capacity: f.capacidadOcupada,
          durationSec,
          progress: 0, // volver al inicio
          arrived: false, // aún no llegó
          path, // ruta restaurada
          position: { lat: origin.lat, lng: origin.lng }, // vuelve al aeropuerto de salida
          rotation: 0, // sin rotación
        };
      }).filter(Boolean);

      setFlights(reset);
    } else {
      setFlights([]);
    }

    // Restaurar botones
    setBtnState({
      start: { disabled: false, color: "blue" },
      pause: { disabled: true, color: "grey" },
      stop: { disabled: true, color: "grey" }
    });
  };
  
  // Vuelos
  const [flights, setFlights] = useState([]);
  const [rawFlights, setRawFlights] = useState([]);
  // 1) Cargar aeropuertos desde el back (solo una vez)
  /*
  useEffect(() => {
    const ac = new AbortController();
    getAeropuertosMap(ac.signal)
      .then(map => setAirports(map))
      .catch(err => console.error("Error cargando aeropuertos:", err))
      .finally(() => setLoadingAirports(false));
    return () => ac.abort();
  }, []);*/

  // 1) Cargar aeropuertos por WebSocket (y cerrar al desmontar)
  useEffect(() => {
    let mounted = true;
    getAeropuertosMapWS()
      .then(map => { if (mounted) setAirports(map); })
      .catch(err => console.error("WS aeropuertos:", err))
      .finally(() => setLoadingAirports(false));
    return () => {
      mounted = false;
      disconnectWS(); // cierra la conexión WS cuando sales del componente
    };
  }, []);

  useEffect(() => {
    if (!airports) return;

    getVuelosWS()
      .then(data => {
        setRawFlights(data); // guardamos crudo para poder rearmar en "Detener"

        const mapped = data.map((f) => {
          const origin = airports[f.origenCodigo];
          const dest   = airports[f.destinoCodigo];
          if (!origin || !dest) {
            console.warn(` Vuelo ${f.codigo} omitido: ${f.origenCodigo} → ${f.destinoCodigo} no está en airports`);
            return null;
          }

          const salida   = new Date(f.fechaSalida);
          const llegada  = new Date(f.fechaLlegada);
          const durationSec = Math.max((llegada - salida) / 1000, 60);

          const path = generateGeodesicPath(origin.lat, origin.lng, dest.lat, dest.lng, 120);

          return {
            code: f.codigo,
            origin,
            originName: origin.name,
            destination: dest,
            destinationName: dest.name,
            startTime: f.fechaSalida,
            endTime: f.fechaLlegada,
            capacity: f.capacidadOcupada,
            durationSec,
            progress: 0,
            arrived: false,
            path,
            position: path[0],
            rotation: 0,
          };

        }).filter(Boolean);

        setFlights(mapped);
        console.log("Vuelos WS mapeados:", mapped.length);
      })
      .catch(err => console.error("WS vuelos:", err));
  }, [airports]);

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


  // === ANIMACIÓN DE LOS VUELOS ===
  useEffect(() => {
    if (!timerRunning) return;

    const interval = setInterval(() => {
      setFlights((prev) =>
        prev.map((f) => {
          if (f.arrived) return f;
          const newProgress = Math.min(f.progress + (0.002), 1); // más suave
          const index = Math.floor(f.path.length * newProgress);
          const position = f.path[Math.min(index, f.path.length - 1)];

          const nextIndex = Math.min(index + 1, f.path.length - 1);
          const next = f.path[nextIndex];
          // Conversión a radianes
          const lat1 = position.lat * Math.PI / 180;
          const lon1 = position.lng * Math.PI / 180;
          const lat2 = next.lat * Math.PI / 180;
          const lon2 = next.lng * Math.PI / 180;

          // Rumbo geodésico (bearing)
          let bearing = Math.atan2(
            Math.sin(lon2 - lon1) * Math.cos(lat2),
            Math.cos(lat1) * Math.sin(lat2) -
            Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)
          );
          bearing = (bearing * 180 / Math.PI + 360) % 360; // convertir a grados 0–360

          // Compensamos la orientación del SVG (punta arriba-derecha)
          const rotation = bearing - 45;

          return { ...f, progress: newProgress, position, rotation, arrived: newProgress >= 1 };
        })
      );
    }, 100); // cada 100 ms, animación fluida
    return () => clearInterval(interval);
  }, [timerRunning]);

  // Detener cronómetro cuando todos los vuelos hayan llegado
  useEffect(() => {
    if (flights.length > 0 && flights.every(f => f.arrived)) {
      console.log("✈️ Todos los vuelos han llegado.");
      showNotification("info", "Todos los vuelos han llegado a su destino.");

      // Detenemos la simulación
      setTimerRunning(false);
      setTimerActive(false);

      setBtnState({
        start: { disabled: true, color: "grey" },
        pause: { disabled: true, color: "grey" },
        stop:  { disabled: false, color: "blue" }
      });
    }
  }, [flights]);


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
              <span className="sidebar-subtitle">Tipo de simulación</span>
              <div className="filter-group">
                <Radio
                  name="tipoSimulacion"
                  label="Semanal"
                  value="semanal"
                  checked={tipoSimulacion === "semanal"}
                  onChange={(e) => setTipoSimulacion(e.target.value)}
                />
                <Radio
                  name="tipoSimulacion"
                  label="Colapso logístico"
                  value="colapso"
                  checked={tipoSimulacion === "colapso"}
                  onChange={(e) => setTipoSimulacion(e.target.value)}
                />
              </div>

              <span className="sidebar-subtitle">Datos</span>
              <label htmlFor="fileInput" className="file-label">Agregar archivo</label>
              <input type="file" id="fileInput" className="file-input" onChange={handleFileChange} />
              <div className="file-name">{archivo || "Ningún archivo seleccionado"}</div>

              <span className="sidebar-subtitle">Filtros</span>
              <div className="filter-group">
                <span className="sidebar-subtitle-strong">Código de vuelo</span>
                <Dropdown
                  placeholder="Seleccionar..."
                  options={[
                    { label: "Ejemplo 1", value: "ejemplo1" },
                    { label: "Ejemplo 2", value: "ejemplo2" },          
                  ]}
                  onSelect={(val) => setCodigoVuelo(val)}
                />
              </div>

              <div className="filter-group">
                <span className="sidebar-subtitle-strong">Ciudad destino</span>
                <Dropdown
                  placeholder="Seleccionar..."
                  options={[
                    { label: "Ejemplo 1", value: "ejemplo1" },
                    { label: "Ejemplo 2", value: "ejemplo2" },          
                  ]}
                  onSelect={(val) => setCiudadDestino(val)}
                />


                <div className="filter-group">
                  <span className="sidebar-subtitle-strong">Continente</span>
                  <Checkbox label="América" value="america" checked={continente.america} onChange={(e) => setContinente({ ...continente, america: e.target.checked })} />
                  <Checkbox label="Europa" value="europa" checked={continente.europa} onChange={(e) => setContinente({ ...continente, europa: e.target.checked })} />
                  <Checkbox label="Asia" value="asia" checked={continente.asia} onChange={(e) => setContinente({ ...continente, asia: e.target.checked })} />
                </div>
              </div>

              <span className="sidebar-subtitle">Leyenda</span>
              <Legend
                items={[
                  { label: "En curso", status: "en-curso" },
                  { label: "Finalizado", status: "finalizado" },
                  { label: "Cancelado", status: "cancelado" }
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

          <input
            type="date"
            value={date}
            onChange={e=>setDate(e.target.value)}
            className="custom-input"
            disabled={timerActive} // desactivado hasta stop
          />

          <input
            type="time"
            value={time}
            onChange={e=>setTime(e.target.value)}
            className="custom-input"
            disabled={timerActive} // desactivado hasta stop
          />

          <button className={`btn ${btnState.start.color}`} onClick={handleStart} disabled={btnState.start.disabled}>Iniciar</button>
          <button className={`btn ${btnState.pause.color}`} onClick={handlePause} disabled={btnState.pause.disabled}>Pausar</button>
          <button className={`btn ${btnState.stop.color}`} onClick={handleStop} disabled={btnState.stop.disabled}>Detener</button>

          <span className="info-label">Fecha:</span>
          <span className="value">{date}</span>
          <span className="info-label">Hora:</span>
          <span className="value">{time}</span>
          <span className="info-label">Tiempo:</span>
          <span className="value">{formatTime(seconds)}</span>
        </div>
     
        <MapContainer id="map" center={[-12.0464, -77.0428]} zoom={3}>
          <TileLayer
            url="https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png"
            attribution='&copy; <a href="https://carto.com/">Carto</a>'
          />

          {/* Marcadores de aeropuertos */}
          {airports && Object.values(airports).map((ap, i) => (
            <Marker key={i} position={[ap.lat, ap.lng]} icon={airportIcon}>
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
              <React.Fragment key={i}>
                {/* Línea del vuelo */}
                {timerRunning && (
                  <Polyline
                    positions={flight.path.slice(
                      Math.floor(flight.path.length * flight.progress)
                    )}
                    color="#777"
                    weight={2}
                    opacity={0.3}
                    dashArray="6, 10" // punteado
                  />
                )}

                {/* Mostramos el avión solo si no ha llegado */}
                {!flight.arrived && (
                  <Marker
                    position={flight.position}
                    icon={createColoredIcon(filterCss, flight.rotation || 0)}
                  >
                    <Popup>
                      <b>{flight.code}</b>
                      Rumbo: {Math.round(flight.rotation)}°<br />
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

        </MapContainer>


      </section>
    </div>
  );
}