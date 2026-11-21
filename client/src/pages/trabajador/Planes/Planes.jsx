import React, { useCallback, useEffect, useMemo, useState } from "react";
import { LoadingOverlay } from "../../../components/UI/ui";
import {
  BarraFiltros, Campo, Etiqueta, IconoMas, Modal, PaginaGestion, Paginador, Segmentado, Selector, Tabla, ZonaArchivo,
} from "../../../components/Gestion/Gestion";
import { listarEventos, listarTodosLosAeropuertos, listarTodosLosPlanes } from "../../../api/catalogos";
import { importarArchivo, registrarEvento, registrarPlan, TIPO_ARCHIVO } from "../../../api/importacion";
import { useNotify } from "../../../notifications/NotificationContext";
import { fechaServerALocal, fromInputs, horaUTCALocal, toInputDate, toServerDateTime } from "../../../utils/fechas";
import { zonaCorta, zonaLarga } from "../../../utils/zona";
import { TAMANIO_PAGINA } from "../../../config";

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

// Planes de vuelo (catálogo completo, filtrado en el navegador) y eventos sobre sus vuelos
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
  const [planes, setPlanes] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [filtros, setFiltros] = useState(VACIOS);
  const [aplicados, setAplicados] = useState(VACIOS);
  const [panel, setPanel] = useState(false);
  const [pagina, setPagina] = useState(1);
  const [modal, setModal] = useState(false);
  const [progreso, setProgreso] = useState(null);

  const cargar = useCallback(async () => {
    setCargando(true);
    try { setPlanes(await listarTodosLosPlanes()); } catch (e) { notify("danger", e.message); } finally { setCargando(false); }
  }, [notify]);
  useEffect(() => { cargar(); }, [cargar]);

  const codigos = useMemo(() => ({
    origenes: [...new Set(planes.map((p) => p.codOrigen))].sort(),
    destinos: [...new Set(planes.map((p) => p.codDestino))].sort(),
  }), [planes]);

  const filtrados = useMemo(() => {
    const t = filtros.texto.trim().toUpperCase();
    const lista = planes.filter((p) =>
      (!t || p.codigo.toUpperCase().includes(t) || `${p.codOrigen}-${p.codDestino}`.includes(t))
      && (!aplicados.origen || p.codOrigen === aplicados.origen)
      && (!aplicados.destino || p.codDestino === aplicados.destino));
    const clave = (p) => p.horaSalida ?? "";
    lista.sort((a, b) => (aplicados.orden === "asc" ? clave(a).localeCompare(clave(b)) : clave(b).localeCompare(clave(a))));
    return lista;
  }, [planes, aplicados, filtros.texto]);

  useEffect(() => setPagina(1), [filtros.texto]);
  const visibles = filtrados.slice((pagina - 1) * TAMANIO_PAGINA, pagina * TAMANIO_PAGINA);
  const totalPaginas = Math.max(1, Math.ceil(filtrados.length / TAMANIO_PAGINA));
  const aplicar = (f = filtros) => { setAplicados(f); setFiltros(f); setPagina(1); setPanel(false); };

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
      {progreso && <LoadingOverlay text="Importando planes..." progreso={progreso.proceso ? progreso : null} />}
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
        resumen={!cargando && `${filtrados.length} planes`}
      >
        <Campo label="Origen">
          <select value={filtros.origen} onChange={(e) => setFiltros({ ...filtros, origen: e.target.value })}>
            <option value="">Todos</option>{codigos.origenes.map((c) => <option key={c}>{c}</option>)}
          </select>
        </Campo>
        <Campo label="Destino">
          <select value={filtros.destino} onChange={(e) => setFiltros({ ...filtros, destino: e.target.value })}>
            <option value="">Todos</option>{codigos.destinos.map((c) => <option key={c}>{c}</option>)}
          </select>
        </Campo>
        <Campo label="Orden por salida">
          <Segmentado nombre="Orden" opciones={[{ value: "asc", label: "Ascendente" }, { value: "desc", label: "Descendente" }]} value={filtros.orden} onChange={(v) => setFiltros({ ...filtros, orden: v })} />
        </Campo>
      </BarraFiltros>

      <Tabla columnas={columnas} filas={visibles} cargando={cargando} vacio="No hay planes con estos filtros." />
      <Paginador pagina={pagina} total={totalPaginas} hayMas={pagina < totalPaginas} onCambiar={setPagina} />

      {modal && (
        <RegistrarPlanesModal
          onCerrar={() => setModal(false)}
          onListo={() => { setModal(false); cargar(); }}
          onImportar={async (archivo) => {
            setModal(false);
            setProgreso({});
            try {
              await importarArchivo(archivo, { tipoArchivo: TIPO_ARCHIVO.PLANES }, setProgreso);
              notify("success", "Planes importados.");
            } catch (e) {
              notify("danger", e.message);
            } finally {
              setProgreso(null);
              cargar();
            }
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
          <Campo label="Origen"><Selector opciones={opciones} value={origen} onChange={setOrigen} placeholder="Busca el aeropuerto de salida" /></Campo>
          <Campo label="Destino"><Selector opciones={opciones} value={destino} onChange={setDestino} placeholder="Busca el aeropuerto de llegada" /></Campo>
          <Campo label={`Salida, hora local del origen${huso(ao)}`}><input type="time" value={salida} onChange={(e) => setSalida(e.target.value)} /></Campo>
          <Campo label={`Llegada, hora local del destino${huso(ad)}`}><input type="time" value={llegada} onChange={(e) => setLlegada(e.target.value)} /></Campo>
          <Campo label="Capacidad (paquetes)"><input inputMode="numeric" value={capacidad} onChange={(e) => /^\d*$/.test(e.target.value) && setCapacidad(e.target.value)} /></Campo>
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
  const [eventos, setEventos] = useState([]);
  const [pagina, setPagina] = useState(1);
  const [hayMas, setHayMas] = useState(false);
  const [cargando, setCargando] = useState(true);
  const [modal, setModal] = useState(false);
  const [progreso, setProgreso] = useState(null);

  const cargar = useCallback(async (pag) => {
    setCargando(true);
    try {
      const lista = await listarEventos(pag - 1, TAMANIO_PAGINA);
      setEventos(lista);
      setPagina(pag);
      setHayMas(lista.length === TAMANIO_PAGINA);
    } catch (e) {
      notify("danger", e.message);
    } finally {
      setCargando(false);
    }
  }, [notify]);
  useEffect(() => { cargar(1); }, [cargar]);

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
      {progreso && <LoadingOverlay text="Importando eventos..." progreso={progreso.proceso ? progreso : null} />}
      <Tabla columnas={columnas} filas={eventos} cargando={cargando} vacio="Aún no hay eventos registrados." />
      <Paginador pagina={pagina} hayMas={hayMas} onCambiar={cargar} />

      {modal && (
        <RegistrarEventosModal
          onCerrar={() => setModal(false)}
          onListo={() => { setModal(false); cargar(1); }}
          onImportar={async (archivo, extra) => {
            setModal(false);
            setProgreso({});
            try {
              await importarArchivo(archivo, { tipoArchivo: TIPO_ARCHIVO.EVENTOS, ...extra }, setProgreso);
              notify("success", "Eventos importados.");
            } catch (e) {
              notify("danger", e.message);
            } finally {
              setProgreso(null);
              cargar(1);
            }
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

  useEffect(() => { listarTodosLosPlanes().then(setPlanes).catch((e) => notify("danger", e.message)); }, [notify]);
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
          <Campo label="Plan de vuelo" ancho="completo"><Selector opciones={opciones} value={codPlan} onChange={setCodPlan} placeholder="Busca por ruta (SPIM) o código" /></Campo>
          <Campo label={`Día del vuelo (${zonaCorta()})`} hint={salida != null ? `Sale ${fechaServerALocal(toServerDateTime(salida))} y llega ${fechaServerALocal(toServerDateTime(llegada))}` : undefined}>
            <input type="date" value={fecha} onChange={(e) => setFecha(e.target.value)} />
          </Campo>
          <Campo label="Tipo de evento">
            <select value={tipo} onChange={(e) => setTipo(e.target.value)}>
              <option value="CANCELACION">Cancelación</option>
              <option value="REPROGRAMACION">Reprogramación</option>
              <option value="REAJUSTE_CAPACIDAD">Reajuste de capacidad</option>
            </select>
          </Campo>
          {tipo === "REPROGRAMACION" && (
            <>
              <Campo label={`Nueva salida (${zonaCorta()})`}><input type="date" value={nueva.fs} onChange={(e) => setNueva({ ...nueva, fs: e.target.value })} /></Campo>
              <Campo label="Hora"><input type="time" value={nueva.hs} onChange={(e) => setNueva({ ...nueva, hs: e.target.value })} /></Campo>
              <Campo label={`Nueva llegada (${zonaCorta()})`}><input type="date" value={nueva.fl} onChange={(e) => setNueva({ ...nueva, fl: e.target.value })} /></Campo>
              <Campo label="Hora"><input type="time" value={nueva.hl} onChange={(e) => setNueva({ ...nueva, hl: e.target.value })} /></Campo>
            </>
          )}
          {tipo === "REAJUSTE_CAPACIDAD" && (
            <Campo label="Nueva capacidad" hint={plan ? `Capacidad actual: ${plan.capacidad}` : undefined}>
              <input inputMode="numeric" value={capacidad} onChange={(e) => /^\d*$/.test(e.target.value) && setCapacidad(e.target.value)} />
            </Campo>
          )}
        </div>
      ) : (
        <>
          <ZonaArchivo archivo={archivo} onArchivo={setArchivo}
            ayuda="Una línea por evento: DD-ORIG-DEST-HH:MM-HH:MM-TIPO[-HH:MM-HH:MM | -CAPACIDAD] (horas locales). Si una línea es inválida se cancela toda la importación." />
          <div className="campos">
            <Campo label="Mes de los días del archivo"><input type="number" min={1} max={12} value={mes} onChange={(e) => setMes(e.target.value)} /></Campo>
            <Campo label="Año"><input type="number" min={2000} max={2100} value={anio} onChange={(e) => setAnio(e.target.value)} /></Campo>
          </div>
          <p className="modal2__nota">Las horas del archivo son locales de cada aeropuerto; en la tabla se muestran en {zonaLarga()}.</p>
        </>
      )}
      {error && <p className="modal2__error" role="alert">{error}</p>}
    </Modal>
  );
}
