import React, { useCallback, useEffect, useMemo, useState } from "react";
import { LoadingOverlay } from "../../../components/UI/ui";
import {
  BarraFiltros, Campo, Confirmacion, Etiqueta, IconoMas, Modal, PaginaGestion, Paginador, Segmentado, Selector, Tabla, ZonaArchivo,
} from "../../../components/Gestion/Gestion";
import { cancelarPedido, filtrarPedidos, listarClientes, listarTodosLosAeropuertos } from "../../../api/catalogos";
import { importarArchivo, registrarPedido, TIPO_ARCHIVO } from "../../../api/importacion";
import { useNotify } from "../../../notifications/NotificationContext";
import { fechaServerALocal, formatFecha, fromInputs, toInputDate, toInputTime, toServerDateTime } from "../../../utils/fechas";
import { zonaCorta, zonaLarga } from "../../../utils/zona";
import { TAMANIO_CATALOGO, TAMANIO_PAGINA } from "../../../config";

const VACIOS = { codigo: "", tipoEscenario: "", codCliente: "", codDestino: "", fueAtendido: "", fecha: "", hora: "" };
const ESCENARIOS = [{ value: "", label: "Todos" }, { value: "OPERACION", label: "Operación" }, { value: "SIMULACION", label: "Simulación" }];
const ETIQUETA_ESTADO = { ACTIVO: ["info", "Activo"], CANCELADO: ["neutro", "Cancelado"], CANCELADO_POR_ERROR: ["alerta", "Cancelado por error"] };

// El server filtra con "=" (código, cliente), "empieza por" (destino) y ">=" (fecha de generación)
const aModelo = (f) => ({
  codigo: f.codigo.trim() || null,
  tipoEscenario: f.tipoEscenario || null,
  codCliente: f.codCliente.trim() || null,
  codDestino: f.codDestino.trim().toUpperCase() || null,
  fueAtendido: f.fueAtendido === "" ? null : f.fueAtendido === "SI",
  fechaHoraGeneracion: f.fecha ? toServerDateTime(fromInputs(f.fecha, f.hora || "00:00")) : null,
  fechaHoraExpiracion: null,
});

function chips(f) {
  const c = [];
  if (f.codigo.trim()) c.push({ clave: "codigo", texto: `Código ${f.codigo.trim()}` });
  if (f.tipoEscenario) c.push({ clave: "tipoEscenario", texto: f.tipoEscenario === "OPERACION" ? "Operación" : "Simulación" });
  if (f.codCliente.trim()) c.push({ clave: "codCliente", texto: `Cliente ${f.codCliente.trim()}` });
  if (f.codDestino.trim()) c.push({ clave: "codDestino", texto: `Destino ${f.codDestino.trim().toUpperCase()}` });
  if (f.fueAtendido) c.push({ clave: "fueAtendido", texto: f.fueAtendido === "SI" ? "Planificados" : "Sin planificar" });
  if (f.fecha) c.push({ clave: "fecha", texto: `Desde ${f.fecha.split("-").reverse().join("/")} ${f.hora || "00:00"}` });
  return c;
}

export default function Pedidos() {
  const notify = useNotify();
  const [filtros, setFiltros] = useState(VACIOS);
  const [aplicados, setAplicados] = useState(VACIOS);
  const [panel, setPanel] = useState(false);
  const [pagina, setPagina] = useState(1);
  const [pedidos, setPedidos] = useState([]);
  const [hayMas, setHayMas] = useState(false);
  const [cargando, setCargando] = useState(true);
  const [modal, setModal] = useState(null); // "registrar" | { cancelar }
  const [progreso, setProgreso] = useState(null);

  const set = (k, v) => setFiltros((f) => ({ ...f, [k]: v }));

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

  useEffect(() => { cargar(1, VACIOS); }, [cargar]);

  const aplicar = (f = filtros) => { setAplicados(f); setFiltros(f); cargar(1, f); setPanel(false); };
  const limpiar = () => aplicar(VACIOS);
  const quitar = (clave) => aplicar({ ...aplicados, [clave]: VACIOS[clave], ...(clave === "fecha" ? { hora: "" } : {}) });

  const columnas = [
    { label: "Código", key: "codigo", render: (p) => <span className="codigo">{p.codigo}</span> },
    { label: "Escenario", key: "tipoEscenario", render: (p) => <Etiqueta tono={p.tipoEscenario === "OPERACION" ? "ambar" : "neutro"}>{p.tipoEscenario === "OPERACION" ? "Operación" : "Simulación"}</Etiqueta> },
    { label: "Cliente", key: "codCliente" },
    { label: "Destino", key: "codDestino" },
    { label: "Cantidad", key: "cantidadSolicitada", alinear: "derecha" },
    { label: `Generado (${zonaCorta()})`, key: "fechaHoraGeneracion", render: (p) => fechaServerALocal(p.fechaHoraGeneracion) },
    { label: `Plazo (${zonaCorta()})`, key: "fechaHoraExpiracion", render: (p) => (p.fechaHoraExpiracion ? fechaServerALocal(p.fechaHoraExpiracion) : <span className="tenue">—</span>) },
    { label: "Planificado", key: "fueAtendido", render: (p) => (p.fueAtendido ? <Etiqueta tono="ok">Sí</Etiqueta> : <Etiqueta tono="aviso">No</Etiqueta>) },
    { label: "Estado", key: "estado", render: (p) => { const [t, l] = ETIQUETA_ESTADO[p.estado] ?? ["neutro", p.estado]; return <Etiqueta tono={t}>{l}</Etiqueta>; } },
    {
      label: "", key: "acciones", alinear: "derecha",
      render: (p) => (p.tipoEscenario === "OPERACION" && p.estado === "ACTIVO"
        ? <button type="button" className="accion-tabla accion-tabla--peligro" onClick={() => setModal({ cancelar: p })}>Cancelar</button>
        : null),
    },
  ];

  return (
    <PaginaGestion
      titulo="Pedidos"
      descripcion="Pedidos de operación y de simulación. Los de operación activos se pueden cancelar mientras ningún vuelo haya despegado."
      acciones={<button type="button" className="boton boton--primario" onClick={() => setModal("registrar")}><IconoMas /> Registrar pedidos</button>}
    >
      {progreso && <LoadingOverlay text="Importando pedidos..." progreso={progreso.proceso ? progreso : null} />}

      <BarraFiltros
        busqueda={filtros.codigo}
        onBusqueda={(v) => set("codigo", v)}
        placeholder="Buscar por código exacto (Enter)"
        abierto={panel}
        onAlternar={() => setPanel((v) => !v)}
        activos={chips(aplicados)}
        onQuitar={quitar}
        onLimpiar={limpiar}
        onAplicar={() => aplicar()}
        resumen={!cargando && `${pedidos.length} en esta página`}
      >
        <Campo label="Escenario"><Segmentado nombre="Escenario" opciones={ESCENARIOS} value={filtros.tipoEscenario} onChange={(v) => set("tipoEscenario", v)} /></Campo>
        <Campo label="Cliente"><input placeholder="Código exacto" value={filtros.codCliente} onChange={(e) => set("codCliente", e.target.value)} /></Campo>
        <Campo label="Destino"><input placeholder="Empieza por, p. ej. SK" value={filtros.codDestino} onChange={(e) => set("codDestino", e.target.value)} /></Campo>
        <Campo label="Planificado">
          <select value={filtros.fueAtendido} onChange={(e) => set("fueAtendido", e.target.value)}>
            <option value="">Todos</option><option value="SI">Sí</option><option value="NO">No</option>
          </select>
        </Campo>
        <Campo label={`Generado desde (${zonaCorta()})`}><input type="date" value={filtros.fecha} onChange={(e) => set("fecha", e.target.value)} /></Campo>
        <Campo label="Hora"><input type="time" value={filtros.hora} onChange={(e) => set("hora", e.target.value)} disabled={!filtros.fecha} /></Campo>
      </BarraFiltros>

      <Tabla columnas={columnas} filas={pedidos} cargando={cargando} vacio="No hay pedidos con estos filtros." />
      <Paginador pagina={pagina} hayMas={hayMas} onCambiar={(p) => cargar(p, aplicados)} />

      {modal === "registrar" && (
        <RegistrarPedidosModal
          onCerrar={() => setModal(null)}
          onListo={() => { setModal(null); cargar(1, aplicados); }}
          onImportar={async (archivo, request) => {
            setModal(null);
            setProgreso({});
            try {
              const fin = await importarArchivo(archivo, request, (p) => setProgreso(p));
              if (fin.estadoFinalizacion === "DESCONOCIDO") notify("warning", "No se pudo confirmar el final de la importación; revisa la tabla.");
              else notify("success", fin.mensaje || "Pedidos importados.");
            } catch (e) {
              notify("danger", e.message);
            } finally {
              setProgreso(null);
              cargar(1, aplicados);
            }
          }}
        />
      )}

      {modal?.cancelar && (
        <CancelarPedido pedido={modal.cancelar} onCerrar={() => setModal(null)} onListo={() => { setModal(null); cargar(pagina, aplicados); }} />
      )}
    </PaginaGestion>
  );
}

function CancelarPedido({ pedido, onCerrar, onListo }) {
  const notify = useNotify();
  const [enviando, setEnviando] = useState(false);
  return (
    <Confirmacion
      titulo={`Cancelar el pedido ${pedido.codigo}`}
      mensaje="Solo se puede cancelar si ningún vuelo de su plan ha despegado. Se libera la capacidad reservada en vuelos y aeropuertos. Esta acción no se puede deshacer."
      accion="Cancelar pedido"
      peligro
      enviando={enviando}
      onCerrar={onCerrar}
      onConfirmar={async () => {
        setEnviando(true);
        try {
          await cancelarPedido(pedido.codigo);
          notify("success", `Pedido ${pedido.codigo} cancelado.`);
          onListo();
        } catch (e) {
          notify("danger", e.message);
          setEnviando(false);
        }
      }}
    />
  );
}

// ---------- Un pedido o un archivo, de operación o de simulación ----------
function RegistrarPedidosModal({ onCerrar, onListo, onImportar }) {
  const notify = useNotify();
  const [modo, setModo] = useState("uno");
  const [escenario, setEscenario] = useState("OPERACION");
  const [aeropuertos, setAeropuertos] = useState([]);
  const [clientes, setClientes] = useState([]);
  const [codCliente, setCodCliente] = useState(null);
  const [codClienteSim, setCodClienteSim] = useState("");
  const [codDestino, setCodDestino] = useState(null);
  const [cantidad, setCantidad] = useState("");
  const ahora = Date.now();
  const [fecha, setFecha] = useState(toInputDate(ahora));
  const [hora, setHora] = useState(toInputTime(ahora));
  const [archivo, setArchivo] = useState(null);
  const [desde, setDesde] = useState({ fecha: "", hora: "" });
  const [hasta, setHasta] = useState({ fecha: "", hora: "" });
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    listarTodosLosAeropuertos().then(setAeropuertos).catch((e) => notify("danger", e.message));
    listarClientes(0, TAMANIO_CATALOGO).then(setClientes).catch(() => {});
  }, [notify]);

  const opcionesAeropuertos = useMemo(() => aeropuertos.map((a) => ({ value: a.codigo, label: `${a.codigo} · ${a.ciudad}`, detalle: a.esSede ? "Sede" : a.pais })), [aeropuertos]);
  const opcionesClientes = useMemo(() => clientes.map((c) => ({ value: c.codigo, label: `${c.codigo} · ${c.nombres} ${c.apellidos ?? ""}`.trim(), detalle: c.correo })), [clientes]);
  const destino = aeropuertos.find((a) => a.codigo === codDestino);
  const esOperacion = escenario === "OPERACION";

  const registrarUno = async () => {
    const n = Number(cantidad);
    const cliente = esOperacion ? codCliente : codClienteSim.trim();
    if (!cliente) return setError(esOperacion ? "Elige el cliente." : "Escribe el código del cliente simulado.");
    if (!esOperacion && !/^\d{7}$/.test(cliente)) return setError("El cliente simulado es un código de 7 dígitos, p. ej. 0000102.");
    if (!codDestino) return setError("Elige el destino.");
    if (!Number.isInteger(n) || n < 1 || n > 999) return setError("La cantidad debe estar entre 1 y 999.");
    const generacion = esOperacion ? Date.now() : fromInputs(fecha, hora);
    if (generacion == null) return setError("Indica la fecha y hora de generación.");
    setEnviando(true);
    setError(null);
    try {
      await registrarPedido({ codCliente: cliente, codDestino, cantidadSolicitada: n, fechaHoraGeneracion: toServerDateTime(generacion), tipoEscenario: escenario });
      notify("success", destino?.esSede && esOperacion ? "Pedido registrado y entregado en sede." : "Pedido registrado.");
      if (esOperacion) notify("info", "La operación se replanificará automáticamente según los parámetros.");
      onListo();
    } catch (e) {
      setError(e.message);
      setEnviando(false);
    }
  };

  const importar = () => {
    if (!archivo) return setError("Elige un archivo.");
    const inicio = desde.fecha ? fromInputs(desde.fecha, desde.hora || "00:00") : null;
    const fin = hasta.fecha ? fromInputs(hasta.fecha, hasta.hora || "23:59") : null;
    if (inicio && fin && fin < inicio) return setError("El fin del rango es anterior al inicio.");
    onImportar(archivo, {
      tipoArchivo: TIPO_ARCHIVO.PEDIDOS,
      tipoEscenario: escenario,
      fechaHoraInicio: inicio ? toServerDateTime(inicio) : null,
      fechaHoraFin: fin ? toServerDateTime(fin) : null,
    });
  };

  return (
    <Modal
      titulo="Registrar pedidos"
      descripcion="Elige el escenario y si cargarás un pedido o un archivo."
      onCerrar={onCerrar}
      bloqueado={enviando}
      ancho="ancho"
      pie={<>
        <button type="button" className="boton boton--fantasma" onClick={onCerrar} disabled={enviando}>Cancelar</button>
        <button type="button" className="boton boton--primario" disabled={enviando} onClick={() => (modo === "uno" ? registrarUno() : importar())}>
          {modo === "uno" ? (enviando ? "Registrando..." : "Registrar pedido") : "Importar archivo"}
        </button>
      </>}
    >
      <div className="modal2__fila">
        <Segmentado nombre="Escenario" opciones={[{ value: "OPERACION", label: "Operación" }, { value: "SIMULACION", label: "Simulación" }]} value={escenario} onChange={(v) => { setEscenario(v); setError(null); }} />
        <Segmentado nombre="Modo" opciones={[{ value: "uno", label: "Un pedido" }, { value: "archivo", label: "Archivo" }]} value={modo} onChange={(v) => { setModo(v); setError(null); }} />
      </div>

      {modo === "uno" ? (
        <div className="campos">
          {esOperacion ? (
            <Campo label="Cliente" ancho="completo">
              <Selector opciones={opcionesClientes} value={codCliente} onChange={setCodCliente} placeholder="Busca por código, nombre o correo" />
            </Campo>
          ) : (
            <Campo label="Cliente simulado" hint="No existe en la base: se crea en memoria durante la simulación.">
              <input inputMode="numeric" maxLength={7} placeholder="7 dígitos, p. ej. 0000102" value={codClienteSim} onChange={(e) => /^\d*$/.test(e.target.value) && setCodClienteSim(e.target.value)} />
            </Campo>
          )}
          <Campo label="Destino" ancho={esOperacion ? undefined : undefined}>
            <Selector opciones={opcionesAeropuertos} value={codDestino} onChange={setCodDestino} placeholder="Busca un aeropuerto" />
          </Campo>
          <Campo label="Cantidad de paquetes" hint="Entre 1 y 999">
            <input inputMode="numeric" maxLength={3} value={cantidad} onChange={(e) => /^\d*$/.test(e.target.value) && setCantidad(e.target.value)} />
          </Campo>
          {esOperacion ? (
            <Campo label="Fecha de generación" hint={`Se registra con la hora actual (${zonaCorta()}).`}>
              <input value={formatFecha(ahora)} disabled />
            </Campo>
          ) : (
            <>
              <Campo label={`Fecha de generación (${zonaCorta()})`}><input type="date" value={fecha} onChange={(e) => setFecha(e.target.value)} /></Campo>
              <Campo label="Hora"><input type="time" value={hora} onChange={(e) => setHora(e.target.value)} /></Campo>
            </>
          )}
          {esOperacion && destino?.esSede && <p className="modal2__nota campo--completo">El destino es una sede: el pedido se entrega al instante.</p>}
        </div>
      ) : (
        <>
          <ZonaArchivo
            archivo={archivo}
            onArchivo={setArchivo}
            ayuda={esOperacion
              ? "Una línea por pedido: AAAAMMDD-HH-MM-DEST-CANT-CLIENTE (hora local del destino). Se omiten los pedidos de clientes que no existen."
              : "Una línea por pedido: NNNNNNNNN-AAAAMMDD-HH-MM-DEST-CANT-CLIENTE (hora local del destino). El cliente es un código de 7 dígitos."}
          />
          <span className="modal2__subtitulo">Solo pedidos generados en este rango (opcional, {zonaLarga()})</span>
          <div className="campos">
            <Campo label="Desde"><input type="date" value={desde.fecha} onChange={(e) => setDesde({ ...desde, fecha: e.target.value })} /></Campo>
            <Campo label="Hora"><input type="time" value={desde.hora} onChange={(e) => setDesde({ ...desde, hora: e.target.value })} disabled={!desde.fecha} /></Campo>
            <Campo label="Hasta"><input type="date" value={hasta.fecha} onChange={(e) => setHasta({ ...hasta, fecha: e.target.value })} /></Campo>
            <Campo label="Hora"><input type="time" value={hasta.hora} onChange={(e) => setHasta({ ...hasta, hora: e.target.value })} disabled={!hasta.fecha} /></Campo>
          </div>
        </>
      )}
      {error && <p className="modal2__error" role="alert">{error}</p>}
    </Modal>
  );
}
