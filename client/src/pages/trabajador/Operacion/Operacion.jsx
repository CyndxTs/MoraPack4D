import React, { useCallback, useEffect, useState } from "react";
import ScenarioWorkspace from "../../../scenario/ScenarioWorkspace";
import { ControlDivider, ControlRow, ControlsDropdown } from "../../../scenario/MapOverlays";
import { aeropuertosDesdeCatalogo, aplicarSolucion, crearEscenarioVacio } from "../../../scenario/buildScenario";
import { useClock } from "../../../scenario/useClock";
import { ButtonAdd, LoadingOverlay } from "../../../components/UI/ui";
import { listarTodosLosAeropuertos } from "../../../api/catalogos";
import {
  CARPETA_EXPORTACION, descargarReporte, eliminarReporte, esReplanificacionEnCurso, iniciarExportacion, nombreDeReporte,
  obtenerOperacionActual, replanificarOperacion,
} from "../../../api/escenarios";
import { suscribir, normalizarEstado } from "../../../realtime/stompClient";
import { esperarExportacion, prepararSeguimientoDeExportaciones } from "../../../realtime/exportaciones";
import { useProgramacion } from "../../../operacion/useProgramacion";
import { useNotify } from "../../../notifications/NotificationContext";
import { formatCountdown, formatUTC } from "../../../utils/fechas";
import DashboardModal from "./DashboardModal";
import runIcon from "../../../assets/icons/run.svg";
import downloadIcon from "../../../assets/icons/download.svg";
import configIcon from "../../../assets/icons/config.svg";

const KEY_DASHBOARD_VISTO = "morapack.dashboardVisto";

// Vista principal del trabajador: la operación en tiempo real (reloj real, UTC)
export default function Operacion() {
  const notify = useNotify();
  const [escenario, setEscenario] = useState(() => crearEscenarioVacio());
  const [estado, setEstado] = useState({ estadoEjecucion: null, estadoFinalizacion: null });
  const [enviando, setEnviando] = useState(false);
  const [descargando, setDescargando] = useState(false);
  const [dashboardAbierto, setDashboardAbierto] = useState(() => sessionStorage.getItem(KEY_DASHBOARD_VISTO) !== "1");
  const programada = useProgramacion();
  const ahoraReal = useCallback(() => Date.now(), []);
  const nowMs = useClock(ahoraReal, true, 1000);

  const recibirSolucion = useCallback((solucion) => {
    setEscenario((prev) => aplicarSolucion(prev, solucion, Date.now()));
  }, []);

  // 1. Catálogo de aeropuertos + última solución que tenga el server
  useEffect(() => {
    let vivo = true;
    (async () => {
      try {
        const aeropuertos = await listarTodosLosAeropuertos();
        if (!vivo) return;
        setEscenario((prev) => ({ ...prev, airports: { ...aeropuertosDesdeCatalogo(aeropuertos), ...prev.airports } }));
        const actual = await obtenerOperacionActual();
        if (vivo && actual) recibirSolucion(actual);
      } catch (e) {
        notify("danger", e.message);
      }
    })();
    return () => { vivo = false; };
  }, [notify, recibirSolucion]);

  // 2. Soluciones y estados en vivo (cualquier trabajador puede haber lanzado la replanificación)
  useEffect(() => {
    const offSol = suscribir("/topic/operation", (payload) => recibirSolucion(payload?.solucion ?? payload));
    const offEst = suscribir("/topic/operation-status", (status) => {
      const e = normalizarEstado(status);
      setEstado(e);
      if (e.estadoEjecucion === "INICIADO") notify("info", "Replanificando la operación...");
      if (e.estadoEjecucion === "DETENIDO") {
        if (e.estadoFinalizacion === "EXITOSO") notify("success", "Operación replanificada.");
        else if (e.estadoFinalizacion === "COLAPSO") notify("warning", "Replanificación con pedidos no atendidos (ver panel de pedidos).");
        else if (e.estadoFinalizacion === "ERRONEO") notify("danger", "Error al replanificar la operación.");
      }
    });
    return () => { offSol(); offEst(); };
  }, [notify, recibirSolucion]);

  const replanificando = estado.estadoEjecucion === "INICIADO" || estado.estadoEjecucion === "POR_INICIAR";

  const onReplanificar = async () => {
    setEnviando(true);
    try {
      await replanificarOperacion();
      setEstado({ estadoEjecucion: "INICIADO", estadoFinalizacion: null });
    } catch (e) {
      notify(esReplanificacionEnCurso(e) ? "info" : "danger", e.message);
    } finally {
      setTimeout(() => setEnviando(false), 3000);
    }
  };

  const onDescargarReporte = async () => {
    setDescargando(true);
    try {
      const idTransaccion = `OP${Date.now()}`;
      await prepararSeguimientoDeExportaciones();
      const idExportacion = await iniciarExportacion(idTransaccion, "OPERACION");
      const fin = await esperarExportacion(idExportacion);
      if (fin.estadoFinalizacion === "ERRONEO") throw new Error("El server no pudo generar el reporte.");
      const archivo = { nombre: nombreDeReporte("OPERACION", idTransaccion), ruta: CARPETA_EXPORTACION };
      await descargarReporte(archivo);
      await eliminarReporte(archivo);
    } catch (e) {
      notify("danger", e.message);
    } finally {
      setDescargando(false);
    }
  };

  const cerrarDashboard = () => {
    sessionStorage.setItem(KEY_DASHBOARD_VISTO, "1");
    setDashboardAbierto(false);
  };

  const controles = (
    <ControlsDropdown>
      <div className="control-row control-row-main">
        <span className="control-label">Controles:</span>
        <div className="control-label-buttons">
          <ButtonAdd icon={runIcon} label={replanificando ? "Replanificando..." : enviando ? "Espere..." : "Replanificar"} onClick={onReplanificar} disabled={enviando || replanificando} />
          <ButtonAdd icon={configIcon} label="Dashboard" onClick={() => setDashboardAbierto(true)} />
          <ButtonAdd icon={downloadIcon} label={descargando ? "Generando..." : "Reporte"} onClick={onDescargarReporte} disabled={descargando || !escenario.recibidoEn} />
        </div>
      </div>
      <ControlDivider />
      <ControlRow label="Tiempo real (UTC):" value={formatUTC(nowMs, true)} color="#666" />
      <ControlRow label="Última planificación:" value={escenario.recibidoEn ? formatUTC(escenario.recibidoEn, true) : "—"} />
      <ControlRow label="Próxima replanificación:" value={programada ? `en ${formatCountdown(programada - nowMs)}` : "no programada"} />
    </ControlsDropdown>
  );

  const overlays = (
    <>
      {replanificando && <div className="scenario-banner">Replanificando la operación...</div>}
      {!replanificando && escenario.noAtendidos.length > 0 && (
        <div className="scenario-banner scenario-banner--warn">⚠ {escenario.noAtendidos.length} {escenario.noAtendidos.length === 1 ? "pedido" : "pedidos"} sin atender</div>
      )}
      {!dashboardAbierto && <button className="dashboard-fab" onClick={() => setDashboardAbierto(true)}>📊 Dashboard</button>}
    </>
  );

  return (
    <ScenarioWorkspace escenario={escenario} nowMs={nowMs} controles={controles} overlays={overlays}>
      {descargando && <LoadingOverlay text="Generando reporte de la operación..." />}
      {dashboardAbierto && <DashboardModal escenario={escenario} nowMs={nowMs} onClose={cerrarDashboard} />}
    </ScenarioWorkspace>
  );
}
