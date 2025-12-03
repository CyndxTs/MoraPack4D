import React, { useState, useEffect, useRef } from "react";
import "./planificacion.scss";
import { RemoveFileButton, ButtonAdd, Input, Table, SidebarActions, LoadingOverlay, Legend, Notification, Radio, DateTimeInline, Dropdown, Dropdown2, Dropdown3, RangeSelector, TriPieSelector } from "../../components/UI/ui";
import plus from "../../assets/icons/plus.svg";
import run from "../../assets/icons/run.svg";
import config from "../../assets/icons/config.svg";
import hideIcon from "../../assets/icons/hide-sidebar.png";
import { useAppData } from "../../dataProvider";
import { listarPedidos, importarPedido, importarPedidos } from "../../services/pedidoService";
import { listarParametros, importarParametros  } from "../../services/parametrosService";
import { MapContainer, TileLayer, Marker, Popup, Polyline, useMapEvent  } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from "leaflet";

import planeIconImg from "../../assets/icons/planeMora.svg";

export default function Planificacion() {
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
  const [probabilidadReplanificacion, setProbabilidadReplanificacion] = useState();

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

  // ----------------------------------------
  // Aeropuertos / Vuelos (simulación)
  // ----------------------------------------
  const { clientes, aeropuertos } = useAppData();
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

  function unirFechaHoraUTC(f, h) {
    if (!f || !h) return null;

    // Construir fecha base: "yyyy-MM-dd HH:mm:00"
    const base = new Date(`${f}T${h}:00`);

    // Sumar 5 horas
    base.setHours(base.getHours() + 5);

    // Formatear nuevamente a yyyy-MM-dd HH:mm:ss
    const yyyy = base.getFullYear();
    const MM = String(base.getMonth() + 1).padStart(2, "0");
    const dd = String(base.getDate()).padStart(2, "0");

    const HH = String(base.getHours()).padStart(2, "0");
    const mm = String(base.getMinutes()).padStart(2, "0");
    const ss = String(base.getSeconds()).padStart(2, "0");

    return `${yyyy}-${MM}-${dd} ${HH}:${mm}:${ss}`;
  }

  function obtenerFechaHoraActual() {
    const ahora = new Date();

    const yyyy = ahora.getFullYear();
    const MM = String(ahora.getMonth() + 1).padStart(2, "0");
    const dd = String(ahora.getDate()).padStart(2, "0");

    const HH = String(ahora.getHours()).padStart(2, "0");
    const mm = String(ahora.getMinutes()).padStart(2, "0");

    const fecha = `${yyyy}-${MM}-${dd}`; // yyyy-MM-dd
    const hora = `${HH}:${mm}`;          // HH:mm

    return { fecha, hora };
  }

  const { fecha, hora } = obtenerFechaHoraActual();

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
  const [cantidad, setCantidad] = useState("");
  const [selectedCliente, setSelectedCliente] = useState(null);
  const [selectedDestino, setSelectedDestino] = useState(null);

  const [archivo, setArchivo] = useState(null);
  const [fechaArchivoFechaI, setFechaArchivoFechaI] = useState("");
  const [fechaArchivoHoraI, setFechaArchivoHoraI] = useState("");
  const [fechaArchivoFechaF, setFechaArchivoFechaF] = useState("");
  const [fechaArchivoHoraF, setFechaArchivoHoraF] = useState("");

  const isSimulacion = tipoEscenario === "SIMULACION";
  const isOperacion = tipoEscenario === "OPERACION";
  const hayArchivo = !!archivo;

  const handleAdd = async () => {
    try {
      setProcessing(true);

      if (isSimulacion && !hayArchivo) {
        showNotification("warning", "Para escenarios de simulación debes subir un archivo.");
        return;
      }

      // --- CASO 1: ARCHIVO ---
      if (hayArchivo) {
        /*if (archivo.name !== "Pedidos.txt") {
          showNotification("warning", "El archivo debe llamarse 'Pedidos.txt'.");
          return;
        }*/

        const fechaInicio = unirFechaHoraUTC(fechaArchivoFechaI, fechaArchivoHoraI);
        const fechaFin = unirFechaHoraUTC(fechaArchivoFechaF, fechaArchivoHoraF);
        
        console.log(archivo);
        console.log(fechaInicio);
        console.log(fechaFin);

        const req = {
          tipoEscenario: tipoEscenario,
          fechaHoraInicio: unirFechaHoraUTC(fechaArchivoFechaI, fechaArchivoHoraI),
          fechaHoraFin: unirFechaHoraUTC(fechaArchivoFechaF, fechaArchivoHoraF)
        };

        console.log(req);

        const respuesta = await importarPedidos(archivo, req);
        console.log(respuesta);
        if (respuesta.exito) {
          showNotification("success", respuesta.mensaje || "Pedidos importados correctamente");
        } else {
          showNotification("danger", respuesta.mensaje || "Ocurrió un error al importar los pedidos");
        }
      }

      // --- CASO 2: MANUAL ---
      else if (isOperacion && !hayArchivo) {
        if (!selectedCliente || !selectedDestino || !fecha || !hora || !cantidad) {
          showNotification("warning", "Completa todos los campos del pedido manual.");
          return;
        }

        const fechaGeneracion = unirFechaHoraUTC(fecha, hora);

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

  const handleCantidadChange = (e) => {
    const value = e.target.value;

    // Solo números
    if (!/^\d*$/.test(value)) return;

    // Evitar más de 3 dígitos
    if (value.length > 3) return;

    // Convertir a número para validar rango
    const num = Number(value);

    // Si está vacío → permitir porque el usuario está editando
    if (value === "") {
      setCantidad("");
      return;
    }

    // Rango 1–999
    if (num >= 1 && num <= 999) {
      setCantidad(value);
    }
  };

  // ----------------------------------------
  // PARAMETROS
  // ----------------------------------------
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
      console.log("ENVIANDO: ");
      console.log(dto);
      const res = await importarParametros(dto);
      showNotification("success", "Parámetros guardados correctamente");

      // Puedes cerrar modal aquí si quieres
      closeModal();

    } catch (error) {
      showNotification("danger", "Error al guardar los parámetros");
    }
  };


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
    const fetchParametros = async () => {
      try {
        const p = (await listarParametros()).dtos[0];
        console.log(p);
        setParametros(p);

        // setear parámetros desde BD
        setMaxDiasEntregaIntercontinental(p.maxDiasEntregaIntercontinental);
        setMaxDiasEntregaIntracontinental(p.maxDiasEntregaIntracontinental);
        setMaxHorasRecojo(p.maxHorasRecojo);
        setMinHorasEstancia(p.minHorasEstancia);
        setMaxHorasEstancia(p.maxHorasEstancia);
        //setConsiderarDesfaseTemporal(p.considerarDesfaseTemporal);
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

        setCodOrigenes((prev) => (prev.length === 0 ? p.codOrigenes || [] : prev));
      } catch (err) {
        showNotification("danger", "Error cargando parámetros");
      }
    };

    if (isModalOpen && !loadedOnOpen) {
      fetchParametros();
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
                  setIsModalPedidoOpen(true);
                }}
              />
              <ButtonAdd icon={config} label="Config. parám." onClick={() => openModal(true)} />
              <ButtonAdd
                icon={run}
                label={"Detener replanificación"}
              />
            </div>

            <span className="sidebar-subtitle">Leyenda</span>
            <Legend
              items={[
                { label: "0% Capacidad", status: "zero" },
                { label: "01 - 49% Capacidad", status: "level1" },
                { label: "50 - 74% Capacidad", status: "level2" },
                { label: "75 - 99% Capacidad", status: "level3" },
                { label: "100% Capacidad", status: "complete" }
              ]}
            />

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

            {/* Mostrar solo aeropuertos */}
            {aeropuertos &&
              Object.values(aeropuertos).map((ap, i) => (
                <Marker
                  key={i}
                  position={[(ap.latitud), (ap.longitud)]}
                  icon={airportIcon}
                >
                  <Popup>
                    <b>{ap.alias}</b>
                    <br />
                    Código: {ap.codigo}
                    <br />
                    Ciudad: {ap.ciudad}
                    <br />
                    País: {ap.pais}
                    <br />
                    Capacidad: {ap.capacidad} unidades
                    <br />
                  </Popup>
                </Marker>
              ))}
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
                    min={0.30}
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



      {/* Modal pedido */}
      {isModalPedidoOpen && (
        <div className="modal" onClick={() => setIsModalOpen(false)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">Agregar pedido</h3>
              <label htmlFor="fileInput" className="file-label">Agregar archivo</label>
              <input type="file" id="fileInput" className="file-input" onChange={handleFileChange}/>
            </div>

            <div className="file-name">
              {archivo ? archivo.name : "Ningún archivo seleccionado"}
              {archivo && (
                <RemoveFileButton onClick={() => setArchivo(null)} />
              )}
            </div>
            
            <div className="modal-body">
              <span className="sidebar-subtitle">Tipo de escenario</span>
                <Radio
                  name="tipoEscenario"
                  label="OPERACION"
                  value="OPERACION"
                  checked={tipoEscenario === "OPERACION"}
                  onChange={(e) => setTipoEscenario(e.target.value)}
                />

              {hayArchivo && (
                <>
                  <span className="sidebar-subtitle">Parámetros de lectura</span>
                  <label>Fecha y hora inicio (UTC)</label>
                  <DateTimeInline
                    dateValue={fechaArchivoFechaI}
                    timeValue={fechaArchivoHoraI}
                    onDateChange={(e) => setFechaArchivoFechaI(e.target.value)}
                    onTimeChange={(e) => setFechaArchivoHoraI(e.target.value)}
                  />

                  <label>Fecha y hora fin (UTC)</label>
                  <DateTimeInline
                    dateValue={fechaArchivoFechaF}
                    timeValue={fechaArchivoHoraF}
                    onDateChange={(e) => setFechaArchivoFechaF(e.target.value)}
                    onTimeChange={(e) => setFechaArchivoHoraF(e.target.value)}
                  />
                </>
              )}

              {isOperacion && !hayArchivo && (
                <>
                  <span className="sidebar-subtitle">Datos del pedido</span>
                  <label>Cantidad</label>
                  <Input
                    value={cantidad}
                    onChange={handleCantidadChange}
                    maxLength={3}
                  />

                  <label>Destino</label>
                  <Dropdown3
                    placeholder="Seleccionar aeropuerto..."
                    options={aeropuertos.map(a => ({
                      label: `${a.codigo} - ${a.ciudad} - ${a.pais}`,
                      value: a
                    }))}
                    value={selectedDestino}
                    onSelect={(a) => setSelectedDestino(a)}
                  />

                  <label>Cliente</label>
                  <Dropdown3
                    placeholder="Seleccionar cliente..."
                    options={clientes.map(c => ({
                      label: `${c.codigo} - ${c.nombre}`,
                      value: c
                    }))}
                    value={selectedCliente}
                    onSelect={(c) => setSelectedCliente(c)}
                  />
                </>
              )}

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
