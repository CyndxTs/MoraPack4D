import React, { useState, useEffect } from "react";
import "./planificacion.scss";
import { ButtonAdd, Input, Table, SidebarActions, LoadingOverlay, Notification, Radio, DateTimeInline} from "../../components/UI/ui";
import plus from "../../assets/icons/plus.svg";
import hideIcon from "../../assets/icons/hide-sidebar.png";
import { listarRutas } from "../../services/rutaService";
import { planificar } from "../../services/planificarService";

export default function Planificacion() {
  const [collapsed, setCollapsed] = useState(false);

  const [codigoVuelo, setCodigoVuelo] = useState("");
  const [ciudadDestino, setCiudadDestino] = useState("");

  const [rutas, setRutas] = useState([]);
  const [rutasOriginales, setRutasOriginales] = useState([]);

  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);
  const [notification, setNotification] = useState(null);

  // -------- MODAL --------
  const [isModalOpen, setIsModalOpen] = useState(false);

  const [tipoSimulacion, setTipoSimulacion] = useState("seleccionar");
  const [fechaI, setFechaI] = useState("");
  const [horaI, setHoraI] = useState("");
  const [fechaF, setFechaF] = useState("");
  const [horaF, setHoraF] = useState("");

  const headers = [
    { label: "Código", key: "codigo" },
    { label: "Duración", key: "duracion" },
    { label: "Hora salida UTC", key: "horaSalidaUTC" },
    { label: "Hora llegada UTC", key: "horaLlegadaUTC" },
  ];

  const showNotification = (type, message) => {
    setNotification({ type, message });
    setTimeout(() => setNotification(null), 5000);
  };

  useEffect(() => {
    const fetchRutas = async () => {
      try {
        const data = await listarRutas();
        setRutas(data);
        setRutasOriginales(data);
      } catch (err) {
        showNotification("danger", "Error al cargar rutas");
      } finally {
        setLoading(false);
      }
    };
    fetchRutas();
  }, []);

  // Filtros
  const handleFilter = () => {
    let lista = [...rutasOriginales];

    if (codigoVuelo.trim()) {
      lista = lista.filter((r) =>
        r.codigo.toLowerCase().includes(codigoVuelo.toLowerCase())
      );
    }

    if (ciudadDestino.trim()) {
      lista = lista.filter((r) =>
        r.destino?.toLowerCase().includes(ciudadDestino.toLowerCase())
      );
    }

    setRutas(lista);
  };

  const handleCleanFilters = () => {
    setCodigoVuelo("");
    setCiudadDestino("");
    setRutas(rutasOriginales);
  };

  // LIMPIAR MODAL SIEMPRE QUE SE CIERRA
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

    if (tipoSimulacion === "colapso") {
      setFechaF("");
      setHoraF("");
      setFechaI("");
      setHoraI("");
    }
  }, [tipoSimulacion, fechaI, horaI]);

  const handlePlanear = async () => {
    try {
      setLoading(true);

      const fechaHoraInicio = `${fechaI}T${horaI}:00`;
      const fechaHoraFin = `${fechaF}T${horaF}:00`;

      const body = {
        replanificar: false,
        guardarPlanificacion: true,
        reparametrizar: false,
        parameters: {
          maxDiasEntregaIntercontinental: 3,
          maxDiasEntregaIntracontinental: 2,
          maxHorasRecojo: 2.0,
          minHorasEstancia: 1.0,
          maxHorasEstancia: 12.0,

          fechaHoraInicio,
          fechaHoraFin,

          desfaseTemporal: 3,
          dMin: 0.001,
          iMax: 3,
          eleMin: 1,
          eleMax: 2,
          kMin: 3,
          kMax: 4,
          tMax: 60,
          maxIntentos: 5,
          factorDeUmbralDeAberracion: 1.015,
          factorDeUtilizacionTemporal: 5000.0,
          factorDeDesviacionEspacial: 2000.0,
          factorDeDisposicionOperacional: 3000.0,
          codOrigenes: []
        },

        guardarParametrizacion: false
      };

      const result = await planificar(body);

      if (result.success) {
        showNotification("success", "Plan generado correctamente");
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

  return (
    <div className="page">
      {(loading || processing) && (
        <LoadingOverlay
          text={processing ? "Procesando planificación..." : "Cargando planificación..."}
        />
      )}

      {notification && (
        <Notification
          type={notification.type}
          message={notification.message}
          onClose={() => setNotification(null)}
        />
      )}

      {/* Sidebar */}
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
          <div className="sidebar-content">
            <span className="sidebar-subtitle">Filtros</span>

            <div className="filter-group">
              <span className="sidebar-subtitle-strong">Código</span>
              <Input
                placeholder="Escribir..."
                value={codigoVuelo}
                onChange={(e) => setCodigoVuelo(e.target.value)}
              />
            </div>

            <div className="filter-group">
              <span className="sidebar-subtitle-strong">Ciudad destino</span>
              <Input
                placeholder="Escribir..."
                value={ciudadDestino}
                onChange={(e) => setCiudadDestino(e.target.value)}
              />
            </div>

            <SidebarActions onFilter={handleFilter} onClean={handleCleanFilters} />
          </div>
        )}
      </aside>

      {/* Contenido principal */}
      <section className="contenido">
        <div className="content-header">
          <h4>Planificación</h4>
          <ButtonAdd icon={plus} label="Generar plan" onClick={openModal} />
        </div>

        <Table headers={headers} data={rutas} />
      </section>

      {/* MODAL */}
      {isModalOpen && (
        <div className="modal" onClick={closeModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">Planificar</h3>
            </div>

            {/* RADIO BUTTONS */}
            <div className="modal-body">
              <span className="sidebar-subtitle">Tipo de simulación</span>

              <Radio
                name="tipoSim"
                label="Seleccionar fechas"
                value="seleccionar"
                checked={tipoSimulacion === "seleccionar"}
                onChange={(e) => setTipoSimulacion(e.target.value)}
              />

              <Radio
                name="tipoSim"
                label="Semanal"
                value="semanal"
                checked={tipoSimulacion === "semanal"}
                onChange={(e) => setTipoSimulacion(e.target.value)}
              />

              <Radio
                name="tipoSim"
                label="Colapso logístico"
                value="colapso"
                checked={tipoSimulacion === "colapso"}
                onChange={(e) => setTipoSimulacion(e.target.value)}
              />

              {/* FECHAS */}
              {(tipoSimulacion === "seleccionar" || tipoSimulacion === "semanal") && (
                <>
                  <label>Fecha y hora de inicio (UTC)</label>
                  <DateTimeInline
                    dateValue={fechaI}
                    timeValue={horaI}
                    disabled={tipoSimulacion === "colapso"}
                    onDateChange={(e) => setFechaI(e.target.value)}
                    onTimeChange={(e) => setHoraI(e.target.value)}
                  />

                  <label>Fecha y hora de fin (UTC)</label>
                  <DateTimeInline
                    dateValue={fechaF}
                    timeValue={horaF}
                    disabled={tipoSimulacion !== "seleccionar"}
                    onDateChange={(e) => setFechaF(e.target.value)}
                    onTimeChange={(e) => setHoraF(e.target.value)}
                  />
                </>
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
