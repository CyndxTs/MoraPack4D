import React, { useState, useEffect } from "react";
import "./simulacion.scss";
import { Radio, Checkbox, Dropdown, Legend } from "../../components/UI/ui";
import hideIcon from '../../assets/icons/hide-sidebar.png';

import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
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
  const [tipoSimulacion, setTipoSimulacion] = useState("");

  // Controles de tiempo
  const [date, setDate] = useState(new Date().toISOString().split("T")[0]);
  const [time, setTime] = useState(new Date().toTimeString().slice(0,5));
  const [seconds, setSeconds] = useState(0);
  const [timerRunning, setTimerRunning] = useState(false);
  const [timerActive, setTimerActive] = useState(false); // indica si inició cronómetro (start clickeado)

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
    setBtnState({
      start: { disabled: false, color: "blue" },
      pause: { disabled: true, color: "grey" },
      stop:  { disabled: true, color: "grey" }
    });
  };

  //EJEMPLO
  // Coordenadas de vuelos
  const lima = { lat: -12.0464, lng: -77.0428 };
  const destinos = [
    { name: "Bogotá", coords: [4.711, -74.0721] },
    { name: "São Paulo", coords: [-23.55, -46.6333] },
    { name: "Buenos Aires", coords: [-34.6037, -58.3816] },
    { name: "Miami", coords: [25.7617, -80.1918] },
    { name: "Madrid", coords: [40.4168, -3.7038] },
    { name: "Atenas", coords: [37.9838, 23.7275] },
    { name: "Bakú", coords: [40.4093, 49.8671] },
    { name: "Pekín", coords: [39.9042, 116.4074] },
    { name: "Berlín", coords: [52.52, 13.405] },
    { name: "Ciudad de México", coords: [19.4326, -99.1332] },
  ];

  const [flights, setFlights] = useState(
    destinos.map((d, i) => ({
      code: `MORA${i + 1}`,
      origin: lima,
      originName: "Lima",
      destination: { lat: d.coords[0], lng: d.coords[1] },
      destinationName: d.name,
      progress: 0, // 0 a 1
      arrived: false,
      position: { lat: lima.lat, lng: lima.lng },
    }))
  );

  // animación del vuelo
  useEffect(() => {
    if (!timerRunning) return;
    const duration = 300; // 5 minutos (300s)
    const interval = setInterval(() => {
      setFlights((prev) =>
        prev.map((f) => {
          if (f.arrived) return f;
          const newProgress = Math.min(f.progress + 1 / duration, 1);
          const newPos = {
            lat: f.origin.lat + (f.destination.lat - f.origin.lat) * newProgress,
            lng: f.origin.lng + (f.destination.lng - f.origin.lng) * newProgress,
          };
          return {
            ...f,
            progress: newProgress,
            position: newPos,
            arrived: newProgress >= 1,
          };
        })
      );
    }, 1000);
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
     
        <MapContainer
          id="map"
          center={[-12.0464, -77.0428]} // Lima
          zoom={2}
        >
          <TileLayer
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          />

          {flights.map((flight, i) => (
            <Marker
              key={i}
              position={flight.position}
              icon={L.icon({
                iconUrl: planeIconImg,
                iconSize: [35, 35],
                className: flight.arrived ? "plane-arrived" : "plane-flying",
              })}
            >
              <Popup>
                <b>{flight.code}</b><br />
                {flight.originName} → {flight.destinationName}<br />
                Estado: {flight.arrived ? "Finalizado" : "En curso"}
              </Popup>
            </Marker>
          ))}
        </MapContainer>

        
        
        
      </section>
    </div>
  );
}