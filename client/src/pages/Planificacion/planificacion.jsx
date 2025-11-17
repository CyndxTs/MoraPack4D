import React, { useState, useEffect } from "react";
import "./planificacion.scss";
import { ButtonAdd, Input, Table, SidebarActions, LoadingOverlay, Notification, Radio, DateTimeInline, Dropdown, Dropdown2} from "../../components/UI/ui";
import plus from "../../assets/icons/plus.svg";
import hideIcon from "../../assets/icons/hide-sidebar.png";
import { listarRutas } from "../../services/rutaService";
import { listarParametros } from "../../services/parametrosService";
import { listarAeropuertos } from "../../services/aeropuertoService";
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
  const [replanificar, setReplanificar] = useState(false);
  const [guardarPlanificacion, setGuardarPlanificacion] = useState(false);
  const [reparametrizar, setReparametrizar] = useState(false);   
  const [guardarParametrizacion, setGuardarParametrizacion] = useState(true);


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

    if (tipoSimulacion === "colapso") {
      return;
    }
  }, [tipoSimulacion, fechaI, horaI]);

  const handlePlanear = async () => {
    try {
      setLoading(true);

      if (!fechaI || !horaI || !fechaF || !horaF) {
        showNotification("danger", "Completa las fechas antes de continuar");
        return;
      }

      const body = {
        replanificar,
        guardarPlanificacion,
        reparametrizar,
        guardarParametrizacion,

        parameters: {
          fechaHoraInicio: `${fechaI}T${horaI}:00`,
          fechaHoraFin: `${fechaF}T${horaF}:00`,

          maxDiasEntregaIntercontinental: (maxDiasEntregaIntercontinental),
          maxDiasEntregaIntracontinental: (maxDiasEntregaIntracontinental),
          maxHorasRecojo: (maxHorasRecojo),
          minHorasEstancia: (minHorasEstancia),
          maxHorasEstancia: (maxHorasEstancia),
          considerarDesfaseTemporal,
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

            <div className="modal-body">

              {/* TIPO DE SIMULACIÓN */}
              <span className="sidebar-subtitle">Tipo de simulación</span>

              <Radio name="tipoSim" label="Seleccionar fechas" value="seleccionar"
                checked={tipoSimulacion === "seleccionar"} onChange={(e) => setTipoSimulacion(e.target.value)} />

              <Radio name="tipoSim" label="Semanal"
                value="semanal"
                checked={tipoSimulacion === "semanal"}
                onChange={(e) => setTipoSimulacion(e.target.value)} />

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
                checked={replanificar === true}
                onChange={(e) => setReplanificar(toBoolean(e.target.value))}
              />
              <Radio
                name="replanificar"
                label="No"
                value="false"
                checked={replanificar === false}
                onChange={(e) => setReplanificar(toBoolean(e.target.value))}
              />

              <label>¿Guardar planificación?</label>
              <Radio
                name="guardarPlanificacion"
                label="Sí"
                value="true"
                checked={guardarPlanificacion === true}
                onChange={(e) => setGuardarPlanificacion(toBoolean(e.target.value))}
              />
              <Radio
                name="guardarPlanificacion"
                label="No"
                value="false"
                checked={guardarPlanificacion === false}
                onChange={(e) => setGuardarPlanificacion(toBoolean(e.target.value))}
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
                checked={guardarParametrizacion === true}
                onChange={(e) => setGuardarParametrizacion(toBoolean(e.target.value))}
              />
              <Radio
                name="guardarParametrizacion"
                label="No"
                value="false"
                checked={guardarParametrizacion === false}
                onChange={(e) => setGuardarParametrizacion(toBoolean(e.target.value))}
              />

              <hr />

              <span className="sidebar-subtitle">Parámetros</span>

              {/* FECHAS SIEMPRE VISIBLES, SOLO SE DESHABILITAN */}
              <label>Fecha y hora de inicio (UTC)</label>
              <DateTimeInline
                dateValue={fechaI}
                timeValue={horaI}
                onDateChange={(e) => setFechaI(e.target.value)}
                onTimeChange={(e) => setHoraI(e.target.value)}
                disabled={tipoSimulacion === "colapso"}
              />

              <label>Fecha y hora de fin (UTC)</label>
              <DateTimeInline
                dateValue={fechaF}
                timeValue={horaF}
                onDateChange={(e) => setFechaF(e.target.value)}
                onTimeChange={(e) => setHoraF(e.target.value)}
                disabled={tipoSimulacion === "colapso" || tipoSimulacion === "semanal"}
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
                checked={considerarDesfaseTemporal === true}
                onChange={(e) => setConsiderarDesfaseTemporal(toBoolean(e.target.value))}
              />
              <Radio
                name="desfase"
                label="No"
                value="false"
                checked={considerarDesfaseTemporal === false}
                onChange={(e) => setConsiderarDesfaseTemporal(toBoolean(e.target.value))}
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
