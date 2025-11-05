import React, { useState, useEffect } from "react";
import "./simulacion.scss";
import { Radio, Checkbox, Dropdown, Legend } from "../../components/UI/ui";
import hideIcon from '../../assets/icons/hide-sidebar.png';

import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
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
    setTimerRunning(false);
    setTimerActive(false);
    setSeconds(0);

    if (airports && rawFlights.length > 0) {
      const reset = rawFlights.map((f) => {
        const origin = airports[f.origenCodigo];
        const dest   = airports[f.destinoCodigo];
        if (!origin || !dest) return null;

        const salida  = new Date(f.fechaSalida);
        const llegada = new Date(f.fechaLlegada);
        const durationSec = Math.max((llegada - salida) / 1000, 60);

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

          return {
            code: f.codigo,
            origin,
            originName: origin.name,
            destination: dest,
            destinationName: dest.name,
            startTime: f.fechaSalida,
            endTime: f.fechaLlegada,
            capacity: f.capacidadOcupada,  // mantiene tu Popup igual
            durationSec,
            progress: 0,
            arrived: false,
            position: { lat: origin.lat, lng: origin.lng },
            rotation: 0,
          };
        }).filter(Boolean);

        setFlights(mapped);
        console.log("✈️ Vuelos WS mapeados:", mapped.length);
      })
      .catch(err => console.error("WS vuelos:", err));
  }, [airports]);

  const createColoredIcon = (filterCss, rotation) =>
  L.divIcon({
    html: `<img src="${planeIconImg}" style="width:35px; transform: rotate(${rotation}deg); filter:${filterCss}; transition: transform 0.2s linear;">`,
    className: "",
    iconSize: [35, 35],
  });


  // === ANIMACIÓN DE LOS VUELOS ===
  useEffect(() => {
    if (!timerRunning) return;
    const interval = setInterval(() => {
      setFlights((prev) =>
        prev.map((f) => {
          if (f.arrived) return f;
          // Aumentar velocidad (x3 más rápido)
          const newProgress = Math.min(f.progress + (1000 / f.durationSec), 1);
          const newLat = f.origin.lat + (f.destination.lat - f.origin.lat) * newProgress;
          const newLng = f.origin.lng + (f.destination.lng - f.origin.lng) * newProgress;

          // Calcular ángulo (rumbo hacia destino)
          const dy = f.destination.lat - f.origin.lat;
          const dx = f.destination.lng - f.origin.lng;
          const rotation = Math.atan2(dy, dx) * (180 / Math.PI); // en grados

          return {
            ...f,
            progress: newProgress,
            position: { lat: newLat, lng: newLng },
            arrived: newProgress >= 1,
            rotation,
          };
        })
      );
    }, 500); // intervalo un poco más fluido y rápido
    return () => clearInterval(interval);
  }, [timerRunning]);
  //

  return (
    <div className="page">
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
        </div>

        <div className="sidebar-content">
          <span className="sidebar-subtitle">Datos</span>
          <label htmlFor="fileInput" className="file-label">Agregar archivo</label>
          <input type="file" id="fileInput" className="file-input" onChange={handleFileChange} />
          <div className="file-name">{archivo || "Ningún archivo seleccionado"}</div>
        </div>

        <div className="sidebar-content">
          <span className="sidebar-subtitle">Filtros</span>

          <div className="filter-group">
            <span className="sidebar-subtitle-strong">Estado de vuelo</span>
            <Checkbox 
              label="En curso" 
              value="enCurso" 
              checked={estadoVuelo.enCurso} 
              onChange={(e) => setEstadoVuelo({ ...estadoVuelo, enCurso: e.target.checked })} 
            />
            <Checkbox 
              label="Finalizado" 
              value="finalizado" 
              checked={estadoVuelo.finalizado} 
              onChange={(e) => setEstadoVuelo({ ...estadoVuelo, finalizado: e.target.checked })} 
            />
            <Checkbox 
              label="Cancelado" 
              value="cancelado" 
              checked={estadoVuelo.cancelado} 
              onChange={(e) => setEstadoVuelo({ ...estadoVuelo, cancelado: e.target.checked })} 
            />
          </div>

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
          </div>

          <div className="filter-group">
            <span className="sidebar-subtitle-strong">Continente</span>
            <Checkbox label="América" value="america" checked={continente.america} onChange={(e) => setContinente({ ...continente, america: e.target.checked })} />
            <Checkbox label="Europa" value="europa" checked={continente.europa} onChange={(e) => setContinente({ ...continente, europa: e.target.checked })} />
            <Checkbox label="Asia" value="asia" checked={continente.asia} onChange={(e) => setContinente({ ...continente, asia: e.target.checked })} />
          </div>
        </div>

        <div className="sidebar-content">
          <span className="sidebar-subtitle">Leyenda</span>
          <Legend
            items={[
              { label: "En curso", status: "en-curso" },
              { label: "Finalizado", status: "finalizado" },
              { label: "Cancelado", status: "cancelado" }
            ]}
          />
        </div>
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
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          />

          {flights.map((flight, i) => {
            let filterCss = "";
            if (flight.arrived) filterCss = "invert(35%) sepia(82%) saturate(1595%) hue-rotate(185deg) brightness(94%) contrast(92%)";
            else filterCss = "invert(62%) sepia(86%) saturate(421%) hue-rotate(356deg) brightness(94%) contrast(92%)";

            return (
              <Marker
                key={i}
                position={flight.position}
                icon={createColoredIcon(filterCss, flight.rotation || 0)}
              >
                <Popup>
                  <b>{flight.code}</b><br />
                  {flight.originName} → {flight.destinationName}<br />
                  Salida: {flight.startTime} | Llegada: {flight.endTime}<br />
                  Capacidad: {flight.capacity} pax<br />
                  Estado: {flight.arrived ? "Finalizado" : "En curso"}
                </Popup>
              </Marker>
            );
          })}
        </MapContainer>

        
        
        
      </section>
    </div>
  );
}