import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import ScenarioWorkspace from "../../../scenario/ScenarioWorkspace";
import { ControlDivider, ControlRow, ControlsDropdown } from "../../../scenario/MapOverlays";
import { aeropuertosDesdeCatalogo, aplicarSolucion, crearEscenarioVacio, limpiarEscenario } from "../../../scenario/buildScenario";
import { useClock } from "../../../scenario/useClock";
import { ButtonAdd, SimulationLoadingOverlay } from "../../../components/UI/ui";
import { listarTodosLosAeropuertos, obtenerParametros } from "../../../api/catalogos";
import { detenerSimulacion, eliminarReporte, iniciarSimulacion, obtenerEstadoSimulacion } from "../../../api/escenarios";
import ReporteModal from "../../../components/Reporte/ReporteModal";
import { normalizarEstado, suscribir } from "../../../realtime/stompClient";
import { useNotify } from "../../../notifications/NotificationContext";
import { formatDuration, formatFecha, toServerDateTime } from "../../../utils/fechas";
import { zonaCorta } from "../../../utils/zona";
import ConfigurarSimulacionModal from "./ConfigurarSimulacionModal";
import { borrarSimulacion, guardarMeta, guardarSolucion, leerMeta, leerSolucion, limpiarSimulacionesViejas, simulacionActiva } from "./simulacionStorage";
import runIcon from "../../../assets/icons/run.svg";
import stopIcon from "../../../assets/icons/stop.svg";
import downloadIcon from "../../../assets/icons/download.svg";

const BASE = "/trabajador/simulacion";

const ESTADO_TEXTO = {
  EXITOSO: "Finalizada", COLAPSO: "Colapso logístico", FORZADO: "Detenida", ERRONEO: "Error en el server",
};

/**
 * Simulación de una semana. El id de transacción va en la URL (/trabajador/simulacion/:id), así cada
 * pestaña sigue su propia simulación y un F5 la retoma: el reloj se reconstruye con la meta guardada
 * (inicio simulado + tiempo real transcurrido × multiplicador).
 */
export default function Simulacion() {
  const { idTransaccion } = useParams();
  const navigate = useNavigate();
  const { state: estadoRuta } = useLocation();
  const notify = useNotify();

  // Entrar a "Simulación" lleva a la simulación abierta (sigue corriendo en el server aunque se cambie de pestaña)
  useEffect(() => {
    if (idTransaccion || estadoRuta?.nueva) return;
    const activa = simulacionActiva();
    if (activa) navigate(`${BASE}/${activa}`, { replace: true });
  }, [idTransaccion, estadoRuta, navigate]);

  const [catalogo, setCatalogo] = useState([]);
  const [parametrosBase, setParametrosBase] = useState(null);
  const [escenario, setEscenario] = useState(() => crearEscenarioVacio());
  const [meta, setMeta] = useState(() => leerMeta(idTransaccion));
  const [configAbierta, setConfigAbierta] = useState(false);
  const [iniciando, setIniciando] = useState(false);
  const [reporteAbierto, setReporteAbierto] = useState(false);
  const [colapsoAbierto, setColapsoAbierto] = useState(false);
  const metaRef = useRef(meta);
  metaRef.current = meta;

  const actualizarMeta = useCallback((cambios) => {
    const id = idTransaccion;
    setMeta((prev) => (prev ? guardarMeta(id, { ...prev, ...(typeof cambios === "function" ? cambios(prev) : cambios) }) : prev));
  }, [idTransaccion]);

  // ---------------- Reloj simulado ----------------
  const calcularAhora = useCallback(() => {
    if (!meta) return Date.now();
    if (meta.congeladoEn) return meta.congeladoEn;
    if (!meta.realInicioMs) return meta.inicioMs;
    return meta.inicioMs + (Date.now() - meta.realInicioMs) * meta.multiplicador;
  }, [meta]);
  const corriendo = !!meta?.realInicioMs && !meta?.congeladoEn;
  const nowMs = useClock(calcularAhora, corriendo);

  // ---------------- Catálogos ----------------
  useEffect(() => {
    limpiarSimulacionesViejas();
    (async () => {
      try {
        const [aeropuertos, parametros] = await Promise.all([listarTodosLosAeropuertos(), obtenerParametros()]);
        setCatalogo(aeropuertos);
        setParametrosBase(parametros);
        setEscenario((prev) => ({ ...prev, airports: { ...aeropuertosDesdeCatalogo(aeropuertos), ...prev.airports } }));
      } catch (e) {
        notify("danger", e.message);
      }
    })();
  }, [notify]);

  // ---------------- Suscripción a la simulación de la URL ----------------
  useEffect(() => {
    const m = leerMeta(idTransaccion);
    setMeta(m);
    setEscenario((prev) => limpiarEscenario(prev));
    setReporteAbierto(false);
    setColapsoAbierto(false);
    if (!idTransaccion) return undefined;
    if (!m) {
      notify("warning", "No se encontraron los datos de esa simulación en este navegador.");
      navigate(BASE, { replace: true });
      return undefined;
    }
    const ahora = () => {
      const mm = metaRef.current;
      if (!mm?.realInicioMs) return mm?.inicioMs ?? Date.now();
      return mm.inicioMs + (Date.now() - mm.realInicioMs) * mm.multiplicador;
    };
    const respaldo = leerSolucion(idTransaccion);
    if (respaldo) setEscenario((prev) => aplicarSolucion(prev, respaldo, ahora(), { acumular: true }));

    const offSol = suscribir(`/topic/simulation-${idTransaccion}`, (payload) => {
      const solucion = payload?.solucion ?? payload;
      if (!solucion) return;
      // El reloj arranca con el primer bloque planificado
      if (!metaRef.current?.realInicioMs) actualizarMeta({ realInicioMs: Date.now() });
      setEscenario((prev) => aplicarSolucion(prev, solucion, ahora(), { acumular: true }));
      guardarSolucion(idTransaccion, solucion);
    });
    const offEst = suscribir(`/topic/simulation-status-${idTransaccion}`, (status) => {
      const { estadoEjecucion, estadoFinalizacion, mensaje } = normalizarEstado(status);
      if (estadoEjecucion === "INICIADO") {
        if (!metaRef.current?.realInicioMs) actualizarMeta({ realInicioMs: Date.now() });
        notify("success", "¡Simulación en curso!");
      } else if (estadoEjecucion === "DETENIDO") {
        actualizarMeta({ terminada: estadoFinalizacion ?? "EXITOSO", ...(mensaje ? { mensajeColapso: mensaje } : {}) });
        if (estadoFinalizacion === "EXITOSO") notify("success", "El server terminó de planificar la semana.");
        else if (estadoFinalizacion === "COLAPSO") { notify("danger", "¡ALERTA DE COLAPSO LOGÍSTICO!"); setColapsoAbierto(true); }
        else if (estadoFinalizacion === "ERRONEO") notify("danger", "La simulación terminó con un error en el server.");
      }
    });
    const offRep = suscribir(`/topic/exportation-${idTransaccion}`, (archivo) => {
      if (archivo?.nombre) {
        actualizarMeta({ reporte: { nombre: archivo.nombre, ruta: archivo.ruta } });
        notify("info", "Reporte de la simulación disponible.");
      }
    });
    // Al volver a la pestaña (o tras un F5) se pregunta al server cómo va: pudo avanzar o terminar mientras no se veía
    let vivo = true;
    obtenerEstadoSimulacion(idTransaccion).then((estado) => {
      if (!vivo || !estado) return;
      if (estado.solucion) {
        if (!metaRef.current?.realInicioMs) actualizarMeta({ realInicioMs: Date.now() });
        setEscenario((prev) => aplicarSolucion(prev, estado.solucion, ahora(), { acumular: true }));
      }
      if (estado.enCurso === false && estado.existe !== false && !metaRef.current?.terminada) {
        actualizarMeta({
          terminada: estado.estadoFinalizacion ?? "EXITOSO",
          ...(estado.mensaje ? { mensajeColapso: estado.mensaje } : {}),
          ...(estado.reporte?.nombre ? { reporte: { nombre: estado.reporte.nombre, ruta: estado.reporte.ruta } } : {}),
        });
        if (estado.estadoFinalizacion === "COLAPSO") setColapsoAbierto(true);
      } else if (estado.reporte?.nombre && !metaRef.current?.reporte) {
        actualizarMeta({ reporte: { nombre: estado.reporte.nombre, ruta: estado.reporte.ruta } });
      }
    }).catch(() => { /* sin conexión: se sigue con lo guardado */ });

    return () => { vivo = false; offSol(); offEst(); offRep(); };
  }, [idTransaccion, navigate, notify, actualizarMeta]);

  // ---------------- Fin visual: pasó la fecha fin y ya no hay vuelos -> se congela el reloj ----------------
  const hayVuelosPendientes = useMemo(() => escenario.flights.some((f) => nowMs < f.endMs), [escenario.flights, nowMs]);
  useEffect(() => {
    if (!meta || meta.congeladoEn || !meta.terminada || !meta.realInicioMs) return;
    if (meta.terminada === "COLAPSO" || meta.terminada === "FORZADO") return;
    if (nowMs >= meta.finMs && !hayVuelosPendientes) {
      actualizarMeta({ congeladoEn: nowMs });
      notify("success", "Visualización completada.");
    }
  }, [meta, nowMs, hayVuelosPendientes, actualizarMeta, notify]);

  // Reporte final: se abre solo cuando la visualización terminó
  useEffect(() => {
    if (meta?.congeladoEn && meta?.reporte && !meta?.reporteVisto) abrirReporte();
  }, [meta?.congeladoEn, meta?.reporte]); // eslint-disable-line react-hooks/exhaustive-deps

  // ---------------- Acciones ----------------
  const onIniciar = async ({ inicioMs, finMs, multiplicador, salto, parametros }) => {
    setIniciando(true);
    try {
      const id = await iniciarSimulacion({
        fechaHoraInicio: toServerDateTime(inicioMs),
        fechaHoraFin: toServerDateTime(finMs),
        parametros,
        multiplicadorTemporal: multiplicador,
        saltoDeAlgoritmo: salto,
      });
      guardarMeta(id, { inicioMs, finMs, multiplicador, salto, realInicioMs: null, creadaEn: Date.now() });
      setConfigAbierta(false);
      navigate(`${BASE}/${id}`);
    } catch (e) {
      notify("danger", e.message);
    } finally {
      setIniciando(false);
    }
  };

  // Cancelar mientras el primer bloque no llega (p. ej. el server se quedó planificando): se pide detenerla
  // y se olvida en este navegador, así la pantalla de carga no bloquea nada
  const [cancelando, setCancelando] = useState(false);
  const cancelarInicio = async () => {
    const id = idTransaccion;
    setCancelando(true);
    try { if (id) await detenerSimulacion(id); } catch { /* el server ya no la tenía */ }
    if (id) borrarSimulacion(id);
    setCancelando(false);
    setMeta(null);
    notify("info", "Simulación cancelada.");
    navigate(BASE, { replace: true, state: { nueva: true } });
  };

  const cerrarSimulacion = async () => {
    const id = idTransaccion;
    if (id && meta && !meta.terminada) {
      try { await detenerSimulacion(id); } catch { /* ya había terminado en el server */ }
    }
    if (meta?.reporte) await eliminarReporte(meta.reporte);
    if (id) borrarSimulacion(id);
    navigate(BASE);
  };

  const abrirReporte = () => {
    if (!metaRef.current?.reporte) return;
    setColapsoAbierto(false);
    setReporteAbierto(true);
    actualizarMeta({ reporteVisto: true });
  };

  // El txt y el pdf se borran del server cuando la simulación ya terminó (fin de la visualización o colapso)
  // y se cierra el modal; si se enviaron por correo, el server ya los borró al responder Brevo.
  const cerrarReporte = async ({ enviado }) => {
    setReporteAbierto(false);
    const m = metaRef.current;
    const termino = !!m?.terminada && (!!m.congeladoEn || m.terminada === "COLAPSO" || m.terminada === "ERRONEO");
    if (enviado || termino) {
      if (!enviado && m?.reporte) await eliminarReporte(m.reporte);
      actualizarMeta({ reporte: null });
    }
  };

  // ---------------- UI ----------------
  const esperandoPrimerBloque = !!meta && !meta.realInicioMs && !meta.terminada;
  const colapso = meta?.terminada === "COLAPSO";

  const controles = (
    <ControlsDropdown defaultOpen>
      <div className="control-row control-row-main">
        <span className="control-label">Controles:</span>
        <div className="control-label-buttons">
          {!meta && <ButtonAdd icon={runIcon} label="Generar plan" onClick={() => setConfigAbierta(true)} />}
          {meta && (
            <ButtonAdd icon={stopIcon} className="btn-stop" label={meta.terminada ? "Cerrar simulación" : "Detener"} onClick={cerrarSimulacion} />
          )}
          {meta?.reporte && <ButtonAdd icon={downloadIcon} label="Ver reporte" onClick={abrirReporte} />}
          {meta && <ButtonAdd icon={runIcon} label="Nueva simulación" onClick={() => navigate(BASE, { state: { nueva: true } })} />}
        </div>
      </div>
      <ControlDivider />
      <ControlRow label={`Simulación (${zonaCorta()}):`} value={meta ? formatFecha(nowMs, true) : "—"} color="var(--cielo)" />
      <ControlRow label="Transcurrido (simulado):" value={meta ? formatDuration((nowMs - meta.inicioMs) / 1000) : "00h : 00m : 00s"} />
      <ControlRow label="Estado:" value={!meta ? "Sin simulación" : meta.terminada ? ESTADO_TEXTO[meta.terminada] ?? meta.terminada : meta.realInicioMs ? "En curso" : "Iniciando..."} />
      <ControlDivider />
      <ControlRow label={`Hora actual (${zonaCorta()}):`} value={formatFecha(Date.now(), true)} color="var(--tinta-3)" />
      <ControlRow label="Cronómetro (real):" value={meta?.realInicioMs ? formatDuration((nowMs - meta.inicioMs) / meta.multiplicador / 1000) : "00h : 00m : 00s"} />
    </ControlsDropdown>
  );

  const overlays = colapso && !colapsoAbierto ? (
    <div className="system-collapse-alert"><span>⚠️ SISTEMA EN ESTADO DE COLAPSO</span></div>
  ) : null;

  return (
    <ScenarioWorkspace escenario={escenario} nowMs={nowMs} controles={controles} overlays={overlays} mostrarVuelos={!!meta?.realInicioMs}>
      {esperandoPrimerBloque && <SimulationLoadingOverlay text="Iniciando entorno de simulación..." onCancelar={cancelarInicio} cancelando={cancelando} />}

      {configAbierta && (
        <ConfigurarSimulacionModal
          parametrosBase={parametrosBase}
          aeropuertos={catalogo}
          iniciando={iniciando}
          onCancelar={() => setConfigAbierta(false)}
          onIniciar={onIniciar}
        />
      )}

      {reporteAbierto && meta?.reporte && (
        <ReporteModal
          titulo={colapso ? "Reporte del colapso" : "Reporte de la simulación"}
          descripcion={colapso ? "La simulación se detuvo por colapso logístico." : meta.congeladoEn ? "La simulación terminó." : "El server terminó de planificar; la visualización sigue en curso."}
          archivo={meta.reporte}
          onCerrar={cerrarReporte}
        />
      )}

      {colapsoAbierto && (
        <div className="report-modal-overlay report-modal-overlay--danger">
          <div className="report-modal-content report-modal-content--danger">
            <div className="report-modal-header report-modal-header--danger">
              <h3 className="report-modal-title"><span>🚨</span> ¡COLAPSO LOGÍSTICO DETECTADO!</h3>
            </div>
            <div className="report-modal-body">
              <div className="report-info-text" style={{ fontSize: "16px", color: "var(--tinta)" }}>
                <p>{meta?.mensajeColapso || "Hay pedidos que ya no pueden entregarse dentro de su plazo."} ({escenario.noAtendidos.length} sin atender en el último bloque)</p>
                {escenario.noAtendidos.length > 0 && (
                  <ul className="colapso-lista">
                    {escenario.noAtendidos.slice(0, 6).map((p) => (
                      <li key={p.codigo}><strong>{p.codigo}</strong> → {p.codDestino} · {p.cantidadSolicitada} u. — {p.motivo}</li>
                    ))}
                    {escenario.noAtendidos.length > 6 && <li>… y {escenario.noAtendidos.length - 6} más (ver panel de pedidos)</li>}
                  </ul>
                )}
                <p><strong>¿Qué desea hacer?</strong></p>
                {meta?.reporte
                  ? <div className="collapse-report-ready">📄 Reporte listo: <strong>{meta.reporte.nombre}</strong></div>
                  : <div className="collapse-report-wait">⏳ Generando reporte de colapso...</div>}
              </div>
            </div>
            <div className="report-modal-footer" style={{ gap: "10px" }}>
              <button className="btn-report-action btn-report-close" onClick={() => setColapsoAbierto(false)}>👀 Continuar visualización</button>
              <button className="btn-report-action btn-report-danger" onClick={abrirReporte} disabled={!meta?.reporte}>
                {meta?.reporte ? "📄 Ver reporte" : "Generando reporte..."}
              </button>
            </div>
          </div>
        </div>
      )}
    </ScenarioWorkspace>
  );
}
