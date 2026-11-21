import React, { useCallback, useEffect, useState } from "react";
import {
  ButtonAdd, DateTimeColumn, DateTimeInline, Dropdown, Dropdown3, Input, LoadingOverlay, Pagination, Radio,
  RemoveFileButton, SidebarActions, Table,
} from "../../../components/UI/ui";
import StatusBadge from "../../../components/Status/status";
import { cancelarPedido, filtrarPedidos, listarClientes, listarTodosLosAeropuertos } from "../../../api/catalogos";
import { importarArchivo, registrarPedido, TIPO_ARCHIVO } from "../../../api/importacion";
import { useNotify } from "../../../notifications/NotificationContext";
import { formatUTC, fromInputsUTC, toServerDateTime } from "../../../utils/fechas";
import { TAMANIO_CATALOGO, TAMANIO_PAGINA } from "../../../config";
import plus from "../../../assets/icons/plus.svg";
import hideIcon from "../../../assets/icons/hide-sidebar.png";
import "./pedidos.scss";

const COLORES = {
  SIMULACION: "#0B6623", OPERACION: "#B5651D",
  ACTIVO: "#2563eb", CANCELADO: "#6b7280", CANCELADO_POR_ERROR: "#b91c1c",
};

const FILTROS_VACIOS = { codigo: "", tipoEscenario: "", codCliente: "", codDestino: "", fueAtendido: "", fecha: "", hora: "" };

// El server filtra con "=" (código, cliente), "empieza por" (destino) y ">=" (fecha de generación)
const aModelo = (f) => ({
  codigo: f.codigo.trim() || null,
  tipoEscenario: f.tipoEscenario || null,
  codCliente: f.codCliente.trim() || null,
  codDestino: f.codDestino.trim() || null,
  fueAtendido: f.fueAtendido === "" ? null : f.fueAtendido === "SI",
  fechaHoraGeneracion: f.fecha ? toServerDateTime(fromInputsUTC(f.fecha, f.hora || "00:00")) : null,
  fechaHoraExpiracion: null,
});

export default function Pedidos() {
  const notify = useNotify();
  const [collapsed, setCollapsed] = useState(false);
  const [filtros, setFiltros] = useState(FILTROS_VACIOS);
  const [filtrosAplicados, setFiltrosAplicados] = useState(FILTROS_VACIOS);
  const [pagina, setPagina] = useState(1);
  const [pedidos, setPedidos] = useState([]);
  const [hayMas, setHayMas] = useState(false);
  const [cargando, setCargando] = useState(true);
  const [modal, setModal] = useState(null); // "registrar" | "importar" | { cancelar: pedido }
  const [progreso, setProgreso] = useState(null); // overlay de importación

  const setFiltro = (k, v) => setFiltros((f) => ({ ...f, [k]: v }));

  const cargar = useCallback(async (pag, f) => {
    setCargando(true);
    try {
      const lista = await filtrarPedidos(aModelo(f), pag - 1, TAMANIO_PAGINA);
      setPedidos(lista);
      setPagina(pag);
      setHayMas(lista.length === TAMANIO_PAGINA);
    } catch (e) {
      notify("danger", e.message);
    } finally {
      setCargando(false);
    }
  }, [notify]);

  useEffect(() => { cargar(1, FILTROS_VACIOS); }, [cargar]);

  const aplicar = () => { setFiltrosAplicados(filtros); cargar(1, filtros); };
  const limpiar = () => { setFiltros(FILTROS_VACIOS); setFiltrosAplicados(FILTROS_VACIOS); cargar(1, FILTROS_VACIOS); };

  // El server programa la replanificación automática (si está activa en los parámetros)
  const avisarReplanificacion = () => {
    notify("info", "La operación se replanificará automáticamente (o antes desde \"Replanificar\").");
  };

  const headers = [
    { label: "Código", key: "codigo" },
    { label: "Escenario", key: "tipoEscenario", useStatusColors: true },
    { label: "Cliente", key: "codCliente" },
    { label: "Destino", key: "codDestino" },
    { label: "Cantidad", key: "cantidadSolicitada" },
    { label: "Generación (UTC)", key: "fechaHoraGeneracion" },
    { label: "Expiración (UTC)", key: "fechaHoraExpiracion" },
    { label: "¿Planificado?", key: "fueAtendido" },
    { label: "Estado", key: "estado", render: (p) => <StatusBadge value={p.estado} colorMap={COLORES} /> },
    {
      label: "Acciones", key: "acciones",
      render: (p) => (p.tipoEscenario === "OPERACION" && p.estado === "ACTIVO"
        ? <button className="table-action table-action--danger" onClick={() => setModal({ cancelar: p })}>Cancelar</button>
        : <span className="table-action-none">—</span>),
    },
  ];

  return (
    <div className="page pedidos-page">
      {progreso && <LoadingOverlay text="Importando pedidos..." progreso={progreso.proceso ? progreso : null} />}

      <aside className={`sidebar ${collapsed ? "collapsed" : ""}`}>
        <div className="sidebar-header">
          <span className="sidebar-title">Filtros</span>
          <img src={hideIcon} alt="Ocultar" className="hide-icon" onClick={() => setCollapsed(!collapsed)} />
        </div>
        {!collapsed && (
          <div className="sidebar-content">
            <div className="filter-group">
              <span className="sidebar-subtitle-strong">Código del pedido</span>
              <Input placeholder="Código exacto..." value={filtros.codigo} onChange={(e) => setFiltro("codigo", e.target.value)} />
            </div>
            <div className="filter-group">
              <span className="sidebar-subtitle-strong">Escenario</span>
              <Radio name="esc" value="" checked={filtros.tipoEscenario === ""} onChange={() => setFiltro("tipoEscenario", "")} label="Todos" />
              <Radio name="esc" value="OPERACION" checked={filtros.tipoEscenario === "OPERACION"} onChange={() => setFiltro("tipoEscenario", "OPERACION")} label="Operación" />
              <Radio name="esc" value="SIMULACION" checked={filtros.tipoEscenario === "SIMULACION"} onChange={() => setFiltro("tipoEscenario", "SIMULACION")} label="Simulación" />
            </div>
            <div className="filter-group">
              <span className="sidebar-subtitle-strong">Cliente</span>
              <Input placeholder="Código exacto..." value={filtros.codCliente} onChange={(e) => setFiltro("codCliente", e.target.value)} />
            </div>
            <div className="filter-group">
              <span className="sidebar-subtitle-strong">Destino</span>
              <Input placeholder="Empieza por..." value={filtros.codDestino} onChange={(e) => setFiltro("codDestino", e.target.value)} />
            </div>
            <div className="filter-group">
              <span className="sidebar-subtitle-strong">¿Planificado?</span>
              <Dropdown
                placeholder="Todos"
                value={filtros.fueAtendido}
                onSelect={(v) => setFiltro("fueAtendido", v)}
                options={[{ label: "Sí", value: "SI" }, { label: "No", value: "NO" }]}
              />
            </div>
            <div className="filter-group">
              <span className="sidebar-subtitle-strong">Generado desde (UTC)</span>
              <DateTimeColumn dateValue={filtros.fecha} timeValue={filtros.hora} onDateChange={(e) => setFiltro("fecha", e.target.value)} onTimeChange={(e) => setFiltro("hora", e.target.value)} />
            </div>
            <SidebarActions onFilter={aplicar} onClean={limpiar} />
          </div>
        )}
      </aside>

      <section className="contenido">
        <div className="content-header">
          <h4>Gestión de pedidos</h4>
          <div className="content-header-actions">
            <ButtonAdd icon={plus} label="Registrar pedido" onClick={() => setModal("registrar")} />
            <ButtonAdd icon={plus} label="Importar archivo" onClick={() => setModal("importar")} />
          </div>
        </div>

        {cargando ? <p className="table-loading">Cargando pedidos...</p> : <Table headers={headers} data={pedidos} statusColors={COLORES} />}
        <Pagination currentPage={pagina} hasMorePages={hayMas} onPageChange={(p) => cargar(p, filtrosAplicados)} />
      </section>

      {modal === "registrar" && (
        <RegistrarPedidoModal
          onCerrar={() => setModal(null)}
          onRegistrado={() => { setModal(null); avisarReplanificacion(); cargar(1, filtrosAplicados); }}
        />
      )}

      {modal === "importar" && (
        <ImportarPedidosModal
          onCerrar={() => setModal(null)}
          onImportar={async (archivo, request) => {
            setModal(null);
            setProgreso({});
            try {
              const fin = await importarArchivo(archivo, request, (p) => setProgreso(p));
              if (fin.estadoFinalizacion === "DESCONOCIDO") notify("warning", "No se pudo confirmar el final de la importación; revisa la tabla.");
              else notify("success", fin.mensaje || "Pedidos importados.");
              if (request.tipoEscenario === "OPERACION") avisarReplanificacion();
            } catch (e) {
              notify("danger", e.message);
            } finally {
              setProgreso(null);
              cargar(1, filtrosAplicados);
            }
          }}
        />
      )}

      {modal?.cancelar && (
        <CancelarPedidoModal
          pedido={modal.cancelar}
          onCerrar={() => setModal(null)}
          onConfirmar={async () => {
            try {
              await cancelarPedido(modal.cancelar.codigo);
              notify("success", `Pedido ${modal.cancelar.codigo} cancelado.`);
              setModal(null);
              cargar(pagina, filtrosAplicados);
            } catch (e) {
              notify("danger", e.message);
            }
          }}
        />
      )}
    </div>
  );
}

// ---------- Registrar un pedido de OPERACION (la simulación solo se carga por archivo) ----------
function RegistrarPedidoModal({ onCerrar, onRegistrado }) {
  const notify = useNotify();
  const [aeropuertos, setAeropuertos] = useState([]);
  const [clientes, setClientes] = useState([]);
  const [codDestino, setCodDestino] = useState(null);
  const [codCliente, setCodCliente] = useState(null);
  const [cantidad, setCantidad] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    Promise.all([listarTodosLosAeropuertos(), listarClientes(0, TAMANIO_CATALOGO)])
      .then(([a, c]) => { setAeropuertos(a); setClientes(c); })
      .catch((e) => notify("danger", e.message));
  }, [notify]);

  const destino = aeropuertos.find((a) => a.codigo === codDestino);

  const registrar = async () => {
    const n = Number(cantidad);
    if (!codCliente || !codDestino || !Number.isInteger(n) || n < 1 || n > 999) {
      return setError("Completa cliente, destino y una cantidad entre 1 y 999.");
    }
    setEnviando(true);
    setError(null);
    try {
      await registrarPedido({
        codCliente,
        codDestino,
        cantidadSolicitada: n,
        fechaHoraGeneracion: toServerDateTime(Date.now()),
        tipoEscenario: "OPERACION",
      });
      notify("success", destino?.esSede ? "Pedido registrado y entregado en sede." : "Pedido registrado.");
      onRegistrado();
    } catch (e) {
      setError(e.message);
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="modal" onClick={onCerrar}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header"><h3 className="modal-title">Registrar pedido (operación)</h3></div>
        <div className="modal-body">
          <label>Cliente</label>
          <Dropdown3
            placeholder="Buscar cliente..."
            value={codCliente}
            onSelect={setCodCliente}
            options={clientes.map((c) => ({ label: `${c.codigo} - ${c.nombres} ${c.apellidos ?? ""}`.trim(), value: c.codigo }))}
          />
          <label>Destino</label>
          <Dropdown3
            placeholder="Buscar aeropuerto..."
            value={codDestino}
            onSelect={setCodDestino}
            options={aeropuertos.map((a) => ({ label: `${a.codigo} - ${a.ciudad} - ${a.pais}${a.esSede ? " (sede)" : ""}`, value: a.codigo }))}
          />
          <label>Cantidad (1 - 999)</label>
          <Input value={cantidad} maxLength={3} onChange={(e) => /^\d*$/.test(e.target.value) && setCantidad(e.target.value)} />
          <label>Fecha de generación</label>
          <p className="modal-hint">Ahora ({formatUTC(Date.now())} UTC)</p>
          {destino?.esSede && <p className="modal-hint">El destino es una sede: el pedido se entrega al instante.</p>}
          {error && <p className="form-error">{error}</p>}
        </div>
        <div className="modal-footer">
          <button className="btn red" onClick={onCerrar}>Cancelar</button>
          <button className="btn green" onClick={registrar} disabled={enviando}>{enviando ? "Registrando..." : "Registrar"}</button>
        </div>
      </div>
    </div>
  );
}

// ---------- Importar un archivo de pedidos ----------
function ImportarPedidosModal({ onCerrar, onImportar }) {
  const [archivo, setArchivo] = useState(null);
  const [tipoEscenario, setTipoEscenario] = useState("SIMULACION");
  const [fi, setFi] = useState({ fecha: "", hora: "" });
  const [ff, setFf] = useState({ fecha: "", hora: "" });
  const [error, setError] = useState(null);

  const importar = () => {
    if (!archivo) return setError("Selecciona un archivo.");
    const inicio = fi.fecha ? fromInputsUTC(fi.fecha, fi.hora || "00:00") : null;
    const fin = ff.fecha ? fromInputsUTC(ff.fecha, ff.hora || "23:59") : null;
    if (inicio && fin && fin < inicio) return setError("El fin del rango es anterior al inicio.");
    onImportar(archivo, {
      tipoArchivo: TIPO_ARCHIVO.PEDIDOS,
      tipoEscenario,
      fechaHoraInicio: inicio ? toServerDateTime(inicio) : null,
      fechaHoraFin: fin ? toServerDateTime(fin) : null,
    });
  };

  return (
    <div className="modal" onClick={onCerrar}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3 className="modal-title">Importar pedidos</h3>
          <label htmlFor="archivoPedidos" className="file-label">Elegir archivo</label>
          <input type="file" id="archivoPedidos" className="file-input" accept=".txt" onChange={(e) => setArchivo(e.target.files[0] ?? null)} />
        </div>
        <div className="file-name">
          {archivo ? archivo.name : "Ningún archivo seleccionado"}
          {archivo && <RemoveFileButton onClick={() => setArchivo(null)} />}
        </div>
        <div className="modal-body">
          <span className="sidebar-subtitle">Escenario</span>
          <Radio name="tipoEsc" label="Simulación" value="SIMULACION" checked={tipoEscenario === "SIMULACION"} onChange={() => setTipoEscenario("SIMULACION")} />
          <Radio name="tipoEsc" label="Operación" value="OPERACION" checked={tipoEscenario === "OPERACION"} onChange={() => setTipoEscenario("OPERACION")} />
          <p className="modal-hint">
            {tipoEscenario === "SIMULACION"
              ? "Formato: NNNNNNNNN-AAAAMMDD-HH-MM-DEST-CANT-CLIENTE (hora local del destino)."
              : "Formato: AAAAMMDD-HH-MM-DEST-CANT-CLIENTE. Se omiten los pedidos de clientes que no existen."}
          </p>
          <span className="sidebar-subtitle">Solo pedidos generados en este rango (opcional, UTC)</span>
          <label>Desde</label>
          <DateTimeInline dateValue={fi.fecha} timeValue={fi.hora} onDateChange={(e) => setFi({ ...fi, fecha: e.target.value })} onTimeChange={(e) => setFi({ ...fi, hora: e.target.value })} />
          <label>Hasta</label>
          <DateTimeInline dateValue={ff.fecha} timeValue={ff.hora} onDateChange={(e) => setFf({ ...ff, fecha: e.target.value })} onTimeChange={(e) => setFf({ ...ff, hora: e.target.value })} />
          {error && <p className="form-error">{error}</p>}
        </div>
        <div className="modal-footer">
          <button className="btn red" onClick={onCerrar}>Cancelar</button>
          <button className="btn green" onClick={importar}>Importar</button>
        </div>
      </div>
    </div>
  );
}

function CancelarPedidoModal({ pedido, onCerrar, onConfirmar }) {
  const [enviando, setEnviando] = useState(false);
  return (
    <div className="modal" onClick={onCerrar}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header"><h3 className="modal-title">Cancelar pedido {pedido.codigo}</h3></div>
        <div className="modal-body">
          <p className="modal-hint">
            Solo se puede cancelar si ningún vuelo de su plan ha despegado. Se liberará la capacidad reservada en vuelos y aeropuertos.
          </p>
        </div>
        <div className="modal-footer">
          <button className="btn red" onClick={onCerrar}>Volver</button>
          <button className="btn green" disabled={enviando} onClick={async () => { setEnviando(true); await onConfirmar(); setEnviando(false); }}>
            {enviando ? "Cancelando..." : "Cancelar pedido"}
          </button>
        </div>
      </div>
    </div>
  );
}
