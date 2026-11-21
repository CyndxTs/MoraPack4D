import React, { useCallback, useEffect, useMemo, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import ScenarioWorkspace from "../../scenario/ScenarioWorkspace";
import { ControlRow, ControlsDropdown } from "../../scenario/MapOverlays";
import { aeropuertosDesdeCatalogo, aplicarSolucion, crearEscenarioVacio } from "../../scenario/buildScenario";
import { useClock } from "../../scenario/useClock";
import { Confirmacion, Etiqueta, Segmentado } from "../../components/Gestion/Gestion";
import { useAuth } from "../../auth/AuthContext";
import { useNotify } from "../../notifications/NotificationContext";
import { suscribir } from "../../realtime/stompClient";
import { cancelarMiPedido, listarAeropuertosCliente, listarMisPedidos, obtenerMiOperacion } from "../../api/cliente";
import { formatFecha, parseServerDateTime } from "../../utils/fechas";
import { zonaCorta } from "../../utils/zona";
import { ES_ACTIVO, estadoDePedido } from "./estadoPedido";
import "./cliente.scss";

const VISTAS = [{ value: "activos", label: "En curso" }, { value: "todos", label: "Todos" }];

// Mis envíos: la operación en el mapa recortada a los pedidos del cliente + su lista con estado y cancelación
export default function MisEnvios() {
  const { usuario } = useAuth();
  const notify = useNotify();
  const { state } = useLocation();
  const [escenario, setEscenario] = useState(() => crearEscenarioVacio());
  const [pedidos, setPedidos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [vista, setVista] = useState("activos");
  const [porCancelar, setPorCancelar] = useState(null);
  const [cancelando, setCancelando] = useState(false);
  const [nuevo, setNuevo] = useState(state?.nuevo ?? null);
  const ahoraReal = useCallback(() => Date.now(), []);
  const nowMs = useClock(ahoraReal, true, 1000);

  const cargarPedidos = useCallback(async () => {
    try {
      const { pedidos: lista } = await listarMisPedidos();
      setPedidos(lista);
    } catch (e) {
      notify("danger", e.message);
    } finally {
      setCargando(false);
    }
  }, [notify]);

  const recibirSolucion = useCallback((solucion) => {
    if (solucion) setEscenario((prev) => aplicarSolucion(prev, solucion, Date.now()));
  }, []);

  // Catálogo + operación actual + pedidos
  useEffect(() => {
    let vivo = true;
    (async () => {
      try {
        const aeropuertos = await listarAeropuertosCliente();
        if (!vivo) return;
        setEscenario((prev) => ({ ...prev, airports: { ...aeropuertosDesdeCatalogo(aeropuertos), ...prev.airports } }));
        const actual = await obtenerMiOperacion();
        if (vivo) recibirSolucion(actual);
      } catch (e) {
        notify("danger", e.message);
      }
    })();
    cargarPedidos();
    return () => { vivo = false; };
  }, [notify, recibirSolucion, cargarPedidos]);

  // En vivo: cada replanificación trae solo la parte del cliente
  useEffect(() => {
    if (!usuario?.codigo) return undefined;
    const off = suscribir(`/topic/cliente/${usuario.codigo}/operacion`, (payload) => {
      recibirSolucion(payload?.solucion ?? payload);
      cargarPedidos();
    });
    const t = setInterval(cargarPedidos, 60000);
    return () => { off(); clearInterval(t); };
  }, [usuario?.codigo, recibirSolucion, cargarPedidos]);

  useEffect(() => {
    if (!nuevo) return undefined;
    const t = setTimeout(() => setNuevo(null), 8000);
    return () => clearTimeout(t);
  }, [nuevo]);

  const conEstado = useMemo(() => pedidos.map((p) => ({ ...p, est: estadoDePedido(p, nowMs) })), [pedidos, nowMs]);
  const visibles = vista === "activos" ? conEstado.filter((p) => ES_ACTIVO.has(p.est.clave)) : conEstado;
  const cuenta = (clave) => conEstado.filter((p) => p.est.clave === clave).length;

  const confirmarCancelacion = async () => {
    setCancelando(true);
    try {
      await cancelarMiPedido(porCancelar.codigo);
      notify("success", `Pedido ${porCancelar.codigo} cancelado.`);
      setPorCancelar(null);
      cargarPedidos();
    } catch (e) {
      notify("danger", e.message);
    } finally {
      setCancelando(false);
    }
  };

  return (
    <div className="mis-envios">
      <aside className="mis-envios__panel" aria-label="Mis pedidos">
        <header className="mis-envios__cabecera">
          <div>
            <h1>Mis envíos</h1>
            <p>Horas en {zonaCorta()}. Se actualiza solo cuando cambia la planificación.</p>
          </div>
          <Link to="/cliente/pedidos/nuevo" className="boton boton--primario">Nuevo pedido</Link>
        </header>

        <div className="mis-envios__resumen">
          <div><strong>{cuenta("camino")}</strong><span>En camino</span></div>
          <div><strong>{cuenta("planificado") + cuenta("espera")}</strong><span>Por salir</span></div>
          <div><strong>{cuenta("llego") + cuenta("entregado")}</strong><span>Entregados</span></div>
        </div>

        <Segmentado nombre="Mostrar" opciones={VISTAS} value={vista} onChange={setVista} />

        <ul className="mis-envios__lista">
          {cargando && [0, 1, 2].map((i) => <li key={i} className="envio envio--esqueleto" aria-hidden="true" />)}
          {!cargando && visibles.length === 0 && (
            <li className="mis-envios__vacio">
              {pedidos.length === 0 ? (
                <>
                  <strong>Aún no tienes envíos</strong>
                  <span>Crea tu primer pedido: eliges el destino y la cantidad, MoraPack planifica la ruta.</span>
                  <Link to="/cliente/pedidos/nuevo" className="boton boton--fantasma">Hacer un pedido</Link>
                </>
              ) : (
                <>
                  <strong>Nada en curso</strong>
                  <span>Tus pedidos anteriores están en "Todos".</span>
                </>
              )}
            </li>
          )}
          {!cargando && visibles.map((p) => (
            <li key={p.codigo} className={`envio${nuevo === p.codigo ? " envio--nuevo" : ""}`}>
              <div className="envio__fila">
                <span className="envio__codigo">{p.codigo}</span>
                <Etiqueta tono={p.est.tono}>{p.est.texto}</Etiqueta>
              </div>
              <div className="envio__destino">
                <span>{p.ciudadDestino ?? p.codDestino}</span>
                <span className="envio__cantidad">{p.cantidadSolicitada} {p.cantidadSolicitada === 1 ? "paquete" : "paquetes"}</span>
              </div>
              {p.est.clave === "camino" && (
                <div className="envio__barra" role="progressbar" aria-valuemin={0} aria-valuemax={100} aria-valuenow={Math.round(p.est.avance * 100)}>
                  <span style={{ transform: `scaleX(${p.est.avance})` }} />
                </div>
              )}
              <dl className="envio__tiempos">
                <div><dt>Pedido</dt><dd>{formatFecha(parseServerDateTime(p.fechaHoraGeneracion))}</dd></div>
                {p.est.salida != null && p.est.clave !== "entregado" && <div><dt>Despega</dt><dd>{formatFecha(p.est.salida)}</dd></div>}
                {p.est.llegada != null && <div><dt>{p.est.clave === "entregado" ? "Entregado" : "Llega"}</dt><dd>{formatFecha(p.est.llegada)}</dd></div>}
              </dl>
              {p.cancelable && (
                <button type="button" className="envio__cancelar" onClick={() => setPorCancelar(p)}>Cancelar pedido</button>
              )}
            </li>
          ))}
        </ul>
      </aside>

      <section className="mis-envios__mapa" aria-label="Mapa de mis envíos">
        <ScenarioWorkspace escenario={escenario} nowMs={nowMs} mostrarFlota={false}
          controles={(
            <ControlsDropdown>
              <ControlRow label={`Hora actual (${zonaCorta()}):`} value={formatFecha(nowMs, true)} color="var(--tinta-3)" />
              <ControlRow label="Última planificación:" value={escenario.recibidoEn ? formatFecha(escenario.recibidoEn, true) : "—"} />
            </ControlsDropdown>
          )}
          overlays={escenario.flights.length === 0 && !cargando ? <div className="scenario-banner">Ningún paquete tuyo vuela en este momento</div> : null} />
      </section>

      {porCancelar && (
        <Confirmacion titulo={`Cancelar ${porCancelar.codigo}`} accion="Sí, cancelar" peligro enviando={cancelando}
          mensaje={`Se cancelará el envío de ${porCancelar.cantidadSolicitada} paquetes a ${porCancelar.ciudadDestino ?? porCancelar.codDestino}. Solo es posible mientras ningún vuelo haya despegado.`}
          onConfirmar={confirmarCancelacion} onCerrar={() => !cancelando && setPorCancelar(null)} />
      )}
    </div>
  );
}
