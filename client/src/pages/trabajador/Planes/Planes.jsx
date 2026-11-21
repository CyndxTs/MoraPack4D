import React, { useEffect, useMemo, useState } from "react";
import {
  BarraFiltros, Campo, Etiqueta, IconoMas, Modal, PaginaGestion, Paginador, Segmentado, Selector, Tabla, ZonaArchivo,
} from "../../../components/Gestion/Gestion";
import { buscarPlanesPagina, listarEventosPagina, listarTodosLosAeropuertos } from "../../../api/catalogos";
import { invalidarPaginado, usePaginado } from "../../../paginacion/usePaginado";
import { useImportaciones } from "../../../importaciones/ImportacionesContext";
import { registrarEvento, registrarPlan, TIPO_ARCHIVO } from "../../../api/importacion";
import { useNotify } from "../../../notifications/NotificationContext";
import { fechaServerALocal, fromInputs, horaUTCALocal, toInputDate, toServerDateTime } from "../../../utils/fechas";
import { zonaCorta, zonaLarga } from "../../../utils/zona";

const TIPOS_EVENTO = { CANCELACION: ["alerta", "Cancelación"], REPROGRAMACION: ["aviso", "Reprogramación"], REAJUSTE_CAPACIDAD: ["info", "Reajuste de capacidad"] };
const ESCENARIOS = [{ value: "OPERACION", label: "Operación" }, { value: "SIMULACION", label: "Simulación" }];
const pad = (n) => String(n).padStart(2, "0");
const DIA = 86400000;

// "HH:mm" local del aeropuerto (huso en horas) -> "HH:mm" UTC
function localAUtc(hhmm, huso) {
  const [h, m] = hhmm.split(":").map(Number);
  let min = h * 60 + m - Math.round(huso * 60);
  min = ((min % 1440) + 1440) % 1440;
  return `${pad(Math.floor(min / 60))}:${pad(min % 60)}`;
}

// Planes de vuelo y eventos sobre sus vuelos (ambos paginados desde el servidor)
export default function Planes() {
  const [tab, setTab] = useState("planes");
  const pestanas = (
    <div className="pestanas" role="tablist">
      <button type="button" role="tab" aria-selected={tab === "planes"} className={tab === "planes" ? "is-activo" : ""} onClick={() => setTab("planes")}>Planes de vuelo</button>
      <button type="button" role="tab" aria-selected={tab === "eventos"} className={tab === "eventos" ? "is-activo" : ""} onClick={() => setTab("eventos")}>Eventos</button>
    </div>
  );
  return tab === "planes" ? <PlanesTab pestanas={pestanas} /> : <EventosTab pestanas={pestanas} />;
}

const VACIOS = { texto: "", origen: "", destino: "", orden: "asc" };

function PlanesTab({ pestanas }) {
  const notify = useNotify();
  const { iniciar } = useImportaciones();
  const [aeropuertos, setAeropuertos] = useState([]);
  const [filtros, setFiltros] = useState(VACIOS);
  const [aplicados, setAplicados] = useState(VACIOS);
  const [texto, setTexto] = useState("");
  const [panel, setPanel] = useState(false);
  const [modal, setModal] = useState(false);

  useEffect(() => { listarTodosLosAeropuertos().then(setAeropuertos).catch(() => {}); }, []);
  // La búsqueda se manda al server cuando se deja de escribir
  useEffect(() => {
    const t = setTimeout(() => setTexto(filtros.texto.trim().toUpperCase()), 350);
    return () => clearTimeout(t);
  }, [filtros.texto]);

  const consulta = { texto: texto || null, codOrigen: aplicados.origen || null, codDestino: aplicados.destino || null, orden: aplicados.orden };
  const { filas: visibles, pagina, setPagina, hayMas, totalPaginas, total, cargando } = usePaginado({
    recurso: "planes",
    filtros: consulta,
    cargarBloque: (pag, tam) => buscarPlanesPagina(consulta, pag, tam),
    onError: (e) => notify("danger", e.message),
  });

  const codigos = useMemo(() => [...aeropuertos.map((a) => a.codigo)].sort(), [aeropuertos]);
  const codigosOrigen = codigos, codigosDestino = codigos;
  const aplicar = (f = filtros) => { setAplicados(f); setFiltros(f); setPanel(false); };

  const activos = [];
  if (aplicados.origen) activos.push({ clave: "origen", texto: `Origen ${aplicados.origen}` });
  if (aplicados.destino) activos.push({ clave: "destino", texto: `Destino ${aplicados.destino}` });
  if (aplicados.orden === "desc") activos.push({ clave: "orden", texto: "Salida descendente" });

  const columnas = [
    { label: "Código", key: "codigo", render: (p) => <span className="codigo">{p.codigo}</span> },
    { label: "Ruta", key: "ruta", render: (p) => `${p.codOrigen} → ${p.codDestino}` },
    { label: `Salida (${zonaCorta()})`, key: "horaSalida", render: (p) => horaUTCALocal(p.horaSalida) },
    { label: `Llegada (${zonaCorta()})`, key: "horaLlegada", render: (p) => horaUTCALocal(p.horaLlegada) },
    { label: "Duración", key: "duracion", alinear: "derecha", render: (p) => (typeof p.duracion === "number" ? `${p.duracion.toFixed(2)} h` : "—") },
    { label: "Capacidad", key: "capacidad", alinear: "derecha" },
    { label: "Eventos", key: "eventos", alinear: "derecha", render: (p) => (p.eventos?.length ? <Etiqueta tono="aviso">{p.eventos.length}</Etiqueta> : <span className="tenue">0</span>) },
  ];

  return (
    <PaginaGestion
      pestanas={pestanas}
      titulo="Planes de vuelo"
      descripcion="Vuelos diarios de Aerolínea PACK entre aeropuertos de la red."
      acciones={<button type="button" className="boton boton--primario" onClick={() => setModal(true)}><IconoMas /> Registrar planes</button>}
    >
      <BarraFiltros
        busqueda={filtros.texto}
        onBusqueda={(v) => setFiltros((f) => ({ ...f, texto: v }))}
        placeholder="Buscar código o ruta (SPIM-SKBO)"
        abierto={panel}
        onAlternar={() => setPanel((v) => !v)}
        activos={activos}
        onQuitar={(k) => aplicar({ ...aplicados, texto: filtros.texto, [k]: VACIOS[k] })}
        onLimpiar={() => aplicar({ ...VACIOS, texto: filtros.texto })}
        onAplicar={() => aplicar()}
        resumen={total != null ? `${total.toLocaleString("es-PE")} planes` : null}
      >
        <Campo label="Origen">
          <select value={filtros.origen} onChange={(e) => setFiltros({ ...filtros, origen: e.target.value })}>
            <option value="">Todos</option>{codigosOrigen.map((c) => <option key={c}>{c}</option>)}
          </select>
        </Campo>
        <Campo label="Destino">
          <select value={filtros.destino} onChange={(e) => setFiltros({ ...filtros, destino: e.target.value })}>
            <option value="">Todos</option>{codigosDestino.map((c) => <option key={c}>{c}</option>)}
          </select>
        </Campo>
        <Campo label="Orden por salida">
          <Segmentado nombre="Orden" opciones={[{ value: "asc", label: "Ascendente" }, { value: "desc", label: "Descendente" }]} value={filtros.orden} onChange={(v) => setFiltros({ ...filtros, orden: v })} />
        </Campo>
      </BarraFiltros>

      <Tabla columnas={columnas} filas={visibles} cargando={cargando} vacio="No hay planes con estos filtros." />
      <Paginador pagina={pagina} total={totalPaginas} hayMas={hayMas} onCambiar={setPagina} />

      {modal && (
        <RegistrarPlanesModal
          onCerrar={() => setModal(false)}
          onListo={() => { setModal(false); invalidarPaginado("planes"); }}
          onImportar={(archivo) => {
            setModal(false);
            iniciar({ archivo, request: { tipoArchivo: TIPO_ARCHIVO.PLANES }, tipo: "PLANES" }).catch((e) => notify("danger", e.message));
          }}
        />
      )}
    </PaginaGestion>
  );
}

function RegistrarPlanesModal({ onCerrar, onListo, onImportar }) {
  const notify = useNotify();
  const [modo, setModo] = useState("uno");
  const [aeropuertos, setAeropuertos] = useState([]);
  const [origen, setOrigen] = useState(null);
  const [destino, setDestino] = useState(null);
  const [salida, setSalida] = useState("");
  const [llegada, setLlegada] = useState("");
  const [capacidad, setCapacidad] = useState("");
  const [archivo, setArchivo] = useState(null);
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => { listarTodosLosAeropuertos().then(setAeropuertos).catch((e) => notify("danger", e.message)); }, [notify]);
  const opciones = useMemo(() => aeropuertos.map((a) => ({ value: a.codigo, label: `${a.codigo} · ${a.ciudad}`, detalle: a.pais })), [aeropuertos]);
  const ao = aeropuertos.find((a) => a.codigo === origen);
  const ad = aeropuertos.find((a) => a.codigo === destino);
  const huso = (a) => (a?.husoHorario != null ? ` (UTC${a.husoHorario >= 0 ? "+" : "−"}${Math.abs(a.husoHorario)})` : "");

  const registrar = async () => {
    const cap = Number(capacidad);
    if (!origen || !destino) return setError("Elige el origen y el destino.");
    if (origen === destino) return setError("El origen y el destino deben ser distintos.");
    if (!salida || !llegada) return setError("Indica la hora de salida y la de llegada.");
    if (!Number.isInteger(cap) || cap < 1) return setError("La capacidad debe ser un entero mayor que 0.");
    setEnviando(true);
    setError(null);
    try {
      await registrarPlan({ codOrigen: origen, codDestino: destino, horaSalida: localAUtc(salida, ao.husoHorario), horaLlegada: localAUtc(llegada, ad.husoHorario), capacidad: cap });
      notify("success", "Plan de vuelo registrado.");
      onListo();
    } catch (e) {
      setError(e.message);
      setEnviando(false);
    }
  };

  return (
    <Modal titulo="Registrar planes de vuelo" descripcion="Un plan a la vez o un archivo completo." onCerrar={onCerrar} bloqueado={enviando} ancho="ancho"
      pie={<>
        <button type="button" className="boton boton--fantasma" onClick={onCerrar} disabled={enviando}>Cancelar</button>
        <button type="button" className="boton boton--primario" disabled={enviando} onClick={() => (modo === "uno" ? registrar() : (archivo ? onImportar(archivo) : setError("Elige un archivo.")))}>
          {modo === "uno" ? (enviando ? "Registrando..." : "Registrar plan") : "Importar archivo"}
        </button>
      </>}>
      <Segmentado nombre="Modo" opciones={[{ value: "uno", label: "Un plan" }, { value: "archivo", label: "Archivo" }]} value={modo} onChange={(v) => { setModo(v); setError(null); }} />
      {modo === "uno" ? (
        <div className="campos">
          <Campo req label="Origen"><Selector opciones={opciones} value={origen} onChange={setOrigen} placeholder="Busca el aeropuerto de salida" /></Campo>
          <Campo req label="Destino"><Selector opciones={opciones} value={destino} onChange={setDestino} placeholder="Busca el aeropuerto de llegada" /></Campo>
          <Campo req label={`Salida, hora local del origen${huso(ao)}`}><input type="time" value={salida} onChange={(e) => setSalida(e.target.value)} /></Campo>
          <Campo req label={`Llegada, hora local del destino${huso(ad)}`}><input type="time" value={llegada} onChange={(e) => setLlegada(e.target.value)} /></Campo>
          <Campo req label="Capacidad (paquetes)"><input inputMode="numeric" value={capacidad} onChange={(e) => /^\d*$/.test(e.target.value) && setCapacidad(e.target.value)} /></Campo>
        </div>
      ) : (
        <ZonaArchivo archivo={archivo} onArchivo={setArchivo} ayuda="Una línea por plan: ORIG-DEST-HH:MM-HH:MM-CAPACIDAD (horas locales de cada aeropuerto). Los planes repetidos se omiten." />
      )}
      {error && <p className="modal2__error" role="alert">{error}</p>}
    </Modal>
  );
}

function EventosTab({ pestanas }) {
  const notify = useNotify();
  const { iniciar } = useImportaciones();
  const [modal, setModal] = useState(false);
  const { filas: eventos, pagina, setPagina, hayMas, totalPaginas, cargando } = usePaginado({
    recurso: "eventos",
    filtros: {},
    cargarBloque: (pag, tam) => listarEventosPagina(pag, tam),
    onError: (e) => notify("danger", e.message),
  });

  const columnas = [
    { label: "Código", key: "codigo", render: (e) => <span className="codigo">{e.codigo}</span> },
    { label: "Plan", key: "codPlan" },
    { label: "Tipo", key: "tipo", render: (e) => { const [t, l] = TIPOS_EVENTO[e.tipo] ?? ["neutro", e.tipo]; return <Etiqueta tono={t}>{l}</Etiqueta>; } },
    { label: "Escenario", key: "tipoEscenario", render: (e) => <Etiqueta tono={e.tipoEscenario === "OPERACION" ? "ambar" : "neutro"}>{e.tipoEscenario === "OPERACION" ? "Operación" : "Simulación"}</Etiqueta> },
    { label: `Salida del vuelo (${zonaCorta()})`, key: "fechaHoraInicio", render: (e) => fechaServerALocal(e.fechaHoraInicio) },
    { label: "Llegada del vuelo", key: "fechaHoraFin", render: (e) => fechaServerALocal(e.fechaHoraFin) },
    { label: "Nueva salida", key: "fechaHoraNuevaSalida", render: (e) => (e.fechaHoraNuevaSalida ? fechaServerALocal(e.fechaHoraNuevaSalida) : <span className="tenue">—</span>) },
    { label: "Nueva llegada", key: "fechaHoraNuevaLlegada", render: (e) => (e.fechaHoraNuevaLlegada ? fechaServerALocal(e.fechaHoraNuevaLlegada) : <span className="tenue">—</span>) },
    { label: "Nueva capacidad", key: "nuevaCapacidad", alinear: "derecha", render: (e) => e.nuevaCapacidad ?? <span className="tenue">—</span> },
  ];

  return (
    <PaginaGestion
      pestanas={pestanas}
      titulo="Eventos sobre vuelos"
      descripcion="Cancelaciones, reprogramaciones y reajustes de capacidad de vuelos concretos."
      acciones={<button type="button" className="boton boton--primario" onClick={() => setModal(true)}><IconoMas /> Registrar eventos</button>}
    >
      <Tabla columnas={columnas} filas={eventos} cargando={cargando} vacio="Aún no hay eventos registrados." />
      <Paginador pagina={pagina} total={totalPaginas} hayMas={hayMas} onCambiar={setPagina} />

      {modal && (
        <RegistrarEventosModal
          onCerrar={() => setModal(false)}
          onListo={() => { setModal(false); invalidarPaginado("eventos"); invalidarPaginado("planes"); }}
          onImportar={(archivo, extra) => {
            setModal(false);
            iniciar({ archivo, request: { tipoArchivo: TIPO_ARCHIVO.EVENTOS, ...extra }, tipo: "EVENTOS" }).catch((e) => notify("danger", e.message));
          }}
        />
      )}
    </PaginaGestion>
  );
}

// Primera salida del plan (hora UTC diaria) dentro del día elegido en la zona de la sesión
function salidaEnElDia(fechaLocal, horaSalidaUTC) {
  const inicioDia = fromInputs(fechaLocal, "00:00");
  if (inicioDia == null) return null;
  const [h, m] = horaSalidaUTC.split(":").map(Number);
  const medianocheUTC = Math.floor(inicioDia / DIA) * DIA;
  for (let k = 0; k < 3; k++) {
    const t = medianocheUTC + k * DIA + (h * 60 + m) * 60000;
    if (t >= inicioDia && t < inicioDia + DIA) return t;
  }
  return null;
}

function RegistrarEventosModal({ onCerrar, onListo, onImportar }) {
  const notify = useNotify();
  const hoy = new Date();
  const [modo, setModo] = useState("uno");
  const [escenario, setEscenario] = useState("OPERACION");
  const [planes, setPlanes] = useState([]);
  const [codPlan, setCodPlan] = useState(null);
  const [tipo, setTipo] = useState("CANCELACION");
  const [fecha, setFecha] = useState(toInputDate(Date.now()));
  const [nueva, setNueva] = useState({ fs: "", hs: "", fl: "", hl: "" });
  const [capacidad, setCapacidad] = useState("");
  const [archivo, setArchivo] = useState(null);
  const [mes, setMes] = useState(hoy.getMonth() + 1);
  const [anio, setAnio] = useState(hoy.getFullYear());
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState(null);

  // Se elige la ruta y solo se piden al server los planes de esa ruta (no todo el catálogo)
  const [aeropuertos, setAeropuertos] = useState([]);
  const [origen, setOrigen] = useState(null);
  const [destino, setDestino] = useState(null);
  useEffect(() => { listarTodosLosAeropuertos().then(setAeropuertos).catch((e) => notify("danger", e.message)); }, [notify]);
  useEffect(() => {
    setCodPlan(null);
    if (!origen || !destino) { setPlanes([]); return undefined; }
    let vivo = true;
    buscarPlanesPagina({ codOrigen: origen, codDestino: destino, orden: "asc" }, 0, 200)
      .then(({ filas }) => { if (vivo) setPlanes(filas); })
      .catch((e) => notify("danger", e.message));
    return () => { vivo = false; };
  }, [origen, destino, notify]);
  const opcionesAeropuertos = useMemo(() => aeropuertos.map((a) => ({ value: a.codigo, label: `${a.codigo} · ${a.ciudad}`, detalle: a.pais })), [aeropuertos]);
  const opciones = useMemo(() => planes.map((p) => ({ value: p.codigo, label: `${p.codOrigen} → ${p.codDestino} · ${horaUTCALocal(p.horaSalida)}`, detalle: p.codigo })), [planes]);
  const plan = planes.find((p) => p.codigo === codPlan);
  const salida = plan && fecha ? salidaEnElDia(fecha, plan.horaSalida) : null;
  const llegada = salida != null && plan ? salida + Math.round((plan.duracion ?? 0) * 3600000) : null;

  const registrar = async () => {
    if (!plan) return setError("Elige el plan de vuelo.");
    if (salida == null) return setError("Elige el día del vuelo.");
    const dto = { codPlan, tipo, tipoEscenario: escenario, fechaHoraInicio: toServerDateTime(salida), fechaHoraFin: toServerDateTime(llegada) };
    if (tipo === "REPROGRAMACION") {
      const ns = fromInputs(nueva.fs, nueva.hs), nl = fromInputs(nueva.fl, nueva.hl);
      if (ns == null || nl == null) return setError("Indica la nueva salida y la nueva llegada.");
      if (nl <= ns) return setError("La nueva llegada debe ser posterior a la nueva salida.");
      dto.fechaHoraNuevaSalida = toServerDateTime(ns);
      dto.fechaHoraNuevaLlegada = toServerDateTime(nl);
    }
    if (tipo === "REAJUSTE_CAPACIDAD") {
      const c = Number(capacidad);
      if (!Number.isInteger(c) || c < 0) return setError("La nueva capacidad debe ser un entero mayor o igual a 0.");
      dto.nuevaCapacidad = c;
    }
    setEnviando(true);
    setError(null);
    try {
      await registrarEvento(dto);
      notify("success", "Evento registrado.");
      onListo();
    } catch (e) {
      setError(e.message);
      setEnviando(false);
    }
  };

  const importar = () => {
    if (!archivo) return setError("Elige un archivo.");
    if (!(mes >= 1 && mes <= 12) || !(anio >= 2000 && anio <= 2100)) return setError("Mes o año inválido.");
    onImportar(archivo, { mes: Number(mes), anio: Number(anio), tipoEscenario: escenario });
  };

  return (
    <Modal titulo="Registrar eventos" descripcion="Afectan a un vuelo concreto de un plan." onCerrar={onCerrar} bloqueado={enviando} ancho="ancho"
      pie={<>
        <button type="button" className="boton boton--fantasma" onClick={onCerrar} disabled={enviando}>Cancelar</button>
        <button type="button" className="boton boton--primario" disabled={enviando} onClick={() => (modo === "uno" ? registrar() : importar())}>
          {modo === "uno" ? (enviando ? "Registrando..." : "Registrar evento") : "Importar archivo"}
        </button>
      </>}>
      <div className="modal2__fila">
        <Segmentado nombre="Escenario" opciones={ESCENARIOS} value={escenario} onChange={setEscenario} />
        <Segmentado nombre="Modo" opciones={[{ value: "uno", label: "Un evento" }, { value: "archivo", label: "Archivo" }]} value={modo} onChange={(v) => { setModo(v); setError(null); }} />
      </div>
      {modo === "uno" ? (
        <div className="campos">
          <Campo req label="Origen"><Selector opciones={opcionesAeropuertos} value={origen} onChange={setOrigen} placeholder="Aeropuerto de salida" /></Campo>
          <Campo req label="Destino"><Selector opciones={opcionesAeropuertos} value={destino} onChange={setDestino} placeholder="Aeropuerto de llegada" /></Campo>
          <Campo req label="Plan de vuelo" ancho="completo" hint={origen && destino && planes.length === 0 ? "No hay planes en esta ruta." : undefined}>
            <Selector opciones={opciones} value={codPlan} onChange={setCodPlan} disabled={!origen || !destino} placeholder={origen && destino ? "Elige la hora de salida" : "Primero elige origen y destino"} />
          </Campo>
          <Campo req label={`Día del vuelo (${zonaCorta()})`} hint={salida != null ? `Sale ${fechaServerALocal(toServerDateTime(salida))} y llega ${fechaServerALocal(toServerDateTime(llegada))}` : undefined}>
            <input type="date" value={fecha} onChange={(e) => setFecha(e.target.value)} />
          </Campo>
          <Campo req label="Tipo de evento">
            <select value={tipo} onChange={(e) => setTipo(e.target.value)}>
              <option value="CANCELACION">Cancelación</option>
              <option value="REPROGRAMACION">Reprogramación</option>
              <option value="REAJUSTE_CAPACIDAD">Reajuste de capacidad</option>
            </select>
          </Campo>
          {tipo === "REPROGRAMACION" && (
            <>
              <Campo req label={`Nueva salida (${zonaCorta()})`}><input type="date" value={nueva.fs} onChange={(e) => setNueva({ ...nueva, fs: e.target.value })} /></Campo>
              <Campo req label="Hora"><input type="time" value={nueva.hs} onChange={(e) => setNueva({ ...nueva, hs: e.target.value })} /></Campo>
              <Campo req label={`Nueva llegada (${zonaCorta()})`}><input type="date" value={nueva.fl} onChange={(e) => setNueva({ ...nueva, fl: e.target.value })} /></Campo>
              <Campo req label="Hora"><input type="time" value={nueva.hl} onChange={(e) => setNueva({ ...nueva, hl: e.target.value })} /></Campo>
            </>
          )}
          {tipo === "REAJUSTE_CAPACIDAD" && (
            <Campo req label="Nueva capacidad" hint={plan ? `Capacidad actual: ${plan.capacidad}` : undefined}>
              <input inputMode="numeric" value={capacidad} onChange={(e) => /^\d*$/.test(e.target.value) && setCapacidad(e.target.value)} />
            </Campo>
          )}
        </div>
      ) : (
        <>
          <ZonaArchivo archivo={archivo} onArchivo={setArchivo}
            ayuda="Una línea por evento: DD-ORIG-DEST-HH:MM-HH:MM-TIPO[-HH:MM-HH:MM | -CAPACIDAD] (horas locales). Si una línea es inválida se cancela toda la importación." />
          <div className="campos">
            <Campo req label="Mes de los días del archivo"><input type="number" min={1} max={12} value={mes} onChange={(e) => setMes(e.target.value)} /></Campo>
            <Campo req label="Año"><input type="number" min={2000} max={2100} value={anio} onChange={(e) => setAnio(e.target.value)} /></Campo>
          </div>
          <p className="modal2__nota">Las horas del archivo son locales de cada aeropuerto; en la tabla se muestran en {zonaLarga()}.</p>
        </>
      )}
      {error && <p className="modal2__error" role="alert">{error}</p>}
    </Modal>
  );
}
