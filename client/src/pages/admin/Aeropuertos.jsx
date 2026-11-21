import React, { useCallback, useEffect, useMemo, useState } from "react";
import { LoadingOverlay } from "../../components/UI/ui";
import {
  BarraFiltros, Campo, Confirmacion, Etiqueta, IconoMas, Modal, PaginaGestion, Paginador, Segmentado, Selector, Tabla, ZonaArchivo,
} from "../../components/Gestion/Gestion";
import { actualizarSedes, listarPaises, listarTodosLosAeropuertos } from "../../api/catalogos";
import { importarArchivo, registrarAeropuerto, TIPO_ARCHIVO } from "../../api/importacion";
import { useNotify } from "../../notifications/NotificationContext";
import { TAMANIO_PAGINA } from "../../config";

const husoTexto = (h) => (h == null ? "—" : `UTC${h >= 0 ? "+" : "−"}${Math.abs(h)}`);

export default function Aeropuertos() {
  const notify = useNotify();
  const [aeropuertos, setAeropuertos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [texto, setTexto] = useState("");
  const [vista, setVista] = useState("todos");
  const [pagina, setPagina] = useState(1);
  const [modal, setModal] = useState(null); // "registrar" | { sede, activar }
  const [progreso, setProgreso] = useState(null);
  const [enviando, setEnviando] = useState(false);

  const cargar = useCallback(async () => {
    setCargando(true);
    try { setAeropuertos(await listarTodosLosAeropuertos()); } catch (e) { notify("danger", e.message); } finally { setCargando(false); }
  }, [notify]);
  useEffect(() => { cargar(); }, [cargar]);

  const sedes = aeropuertos.filter((a) => a.esSede);
  const filtrados = useMemo(() => {
    const t = texto.trim().toLowerCase();
    return aeropuertos
      .filter((a) => (vista === "todos" || (vista === "sedes" ? a.esSede : !a.esSede)))
      .filter((a) => !t || `${a.codigo} ${a.ciudad} ${a.pais} ${a.continente}`.toLowerCase().includes(t))
      .sort((a, b) => Number(b.esSede) - Number(a.esSede) || a.codigo.localeCompare(b.codigo));
  }, [aeropuertos, texto, vista]);
  useEffect(() => setPagina(1), [texto, vista]);
  const total = Math.max(1, Math.ceil(filtrados.length / TAMANIO_PAGINA));

  const cambiarSede = async () => {
    const { sede, activar } = modal;
    const codigos = activar ? [...sedes.map((s) => s.codigo), sede.codigo] : sedes.map((s) => s.codigo).filter((c) => c !== sede.codigo);
    setEnviando(true);
    try {
      const r = await actualizarSedes(codigos);
      notify("success", r.mensaje);
      setModal(null);
      cargar();
    } catch (e) {
      notify("danger", e.message);
    } finally {
      setEnviando(false);
    }
  };

  const columnas = [
    { label: "Código", key: "codigo", render: (a) => <span className="codigo">{a.codigo}</span> },
    { label: "Ciudad", key: "ciudad" },
    { label: "País", key: "pais" },
    { label: "Continente", key: "continente" },
    { label: "Huso", key: "husoHorario", render: (a) => husoTexto(a.husoHorario) },
    { label: "Capacidad", key: "capacidad", alinear: "derecha" },
    { label: "Coordenadas", key: "coords", render: (a) => <span className="tenue">{a.latitud?.toFixed(3)}, {a.longitud?.toFixed(3)}</span> },
    { label: "Tipo", key: "esSede", render: (a) => (a.esSede ? <Etiqueta tono="info">Sede</Etiqueta> : <Etiqueta tono="neutro">Destino</Etiqueta>) },
    {
      label: "", key: "acciones", alinear: "derecha",
      render: (a) => (a.esSede
        ? <button type="button" className="accion-tabla accion-tabla--peligro" disabled={sedes.length <= 1} title={sedes.length <= 1 ? "Debe quedar al menos una sede" : undefined} onClick={() => setModal({ sede: a, activar: false })}>Quitar sede</button>
        : <button type="button" className="accion-tabla" onClick={() => setModal({ sede: a, activar: true })}>Hacer sede</button>),
    },
  ];

  return (
    <PaginaGestion
      titulo="Aeropuertos y sedes"
      descripcion="Las sedes son el origen de todos los envíos. Cambiarlas afecta a la próxima planificación de la operación."
      acciones={<button type="button" className="boton boton--primario" onClick={() => setModal("registrar")}><IconoMas /> Registrar aeropuertos</button>}
    >
      {progreso && <LoadingOverlay text="Importando aeropuertos..." progreso={progreso.proceso ? progreso : null} />}
      <BarraFiltros busqueda={texto} onBusqueda={setTexto} placeholder="Buscar código, ciudad, país o continente" resumen={!cargando && `${filtrados.length} aeropuertos · ${sedes.length} sedes`} />
      <div style={{ marginBottom: 14 }}>
        <Segmentado nombre="Tipo" value={vista} onChange={setVista} opciones={[{ value: "todos", label: "Todos" }, { value: "sedes", label: "Sedes" }, { value: "destinos", label: "Destinos" }]} />
      </div>
      <Tabla columnas={columnas} filas={filtrados.slice((pagina - 1) * TAMANIO_PAGINA, pagina * TAMANIO_PAGINA)} cargando={cargando} vacio="No hay aeropuertos. Importa el archivo de aeropuertos para empezar." />
      <Paginador pagina={pagina} total={total} hayMas={pagina < total} onCambiar={setPagina} />

      {modal === "registrar" && (
        <RegistrarAeropuertos
          onCerrar={() => setModal(null)}
          onListo={() => { setModal(null); cargar(); }}
          onImportar={async (archivo) => {
            setModal(null);
            setProgreso({});
            try {
              await importarArchivo(archivo, { tipoArchivo: TIPO_ARCHIVO.AEROPUERTOS }, setProgreso);
              notify("success", "Aeropuertos importados.");
            } catch (e) {
              notify("danger", e.message);
            } finally {
              setProgreso(null);
              cargar();
            }
          }}
        />
      )}
      {modal?.sede && (
        <Confirmacion
          titulo={modal.activar ? `Hacer sede a ${modal.sede.ciudad}` : `Quitar la sede de ${modal.sede.ciudad}`}
          mensaje={modal.activar
            ? `${modal.sede.codigo} pasará a despachar envíos. La operación lo usará desde su próxima planificación.`
            : `${modal.sede.codigo} dejará de despachar envíos desde la próxima planificación. Los pedidos ya planificados no cambian.`}
          accion={modal.activar ? "Hacer sede" : "Quitar sede"}
          peligro={!modal.activar}
          enviando={enviando}
          onCerrar={() => setModal(null)}
          onConfirmar={cambiarSede}
        />
      )}
    </PaginaGestion>
  );
}

function RegistrarAeropuertos({ onCerrar, onListo, onImportar }) {
  const notify = useNotify();
  const [modo, setModo] = useState("uno");
  const [paises, setPaises] = useState([]);
  const [f, setF] = useState({ codigo: "", ciudad: "", pais: null, alias: "", capacidad: "", latitud: "", longitud: "" });
  const [archivo, setArchivo] = useState(null);
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState(null);
  const set = (k) => (e) => setF({ ...f, [k]: e?.target ? e.target.value : e });

  useEffect(() => { listarPaises().then(setPaises).catch(() => {}); }, []);
  const opcionesPaises = useMemo(() => [...paises].sort((a, b) => a.nombre.localeCompare(b.nombre, "es")).map((p) => ({ value: p.nombre, label: p.nombre, detalle: `${p.continente} · ${husoTexto(p.husoHorario)}` })), [paises]);

  const registrar = async () => {
    const codigo = f.codigo.trim().toUpperCase();
    const cap = Number(f.capacidad), lat = Number(f.latitud), lon = Number(f.longitud);
    if (!/^[A-Z]{4}$/.test(codigo)) return setError("El código OACI tiene 4 letras, p. ej. SPIM.");
    if (!f.ciudad.trim()) return setError("Escribe la ciudad.");
    if (!f.pais) return setError("Elige el país (regístralo antes si no aparece).");
    if (!Number.isInteger(cap) || cap < 1) return setError("La capacidad del almacén debe ser un entero mayor que 0.");
    if (!(lat >= -90 && lat <= 90) || !(lon >= -180 && lon <= 180) || f.latitud === "" || f.longitud === "") return setError("Revisa las coordenadas (latitud -90 a 90, longitud -180 a 180).");
    setEnviando(true);
    setError(null);
    try {
      await registrarAeropuerto({ codigo, ciudad: f.ciudad.trim(), pais: f.pais, alias: f.alias.trim() || f.ciudad.trim().slice(0, 4).toLowerCase(), capacidad: cap, latitud: lat, longitud: lon, esSede: false });
      notify("success", `Aeropuerto ${codigo} registrado.`);
      onListo();
    } catch (e) {
      setError(e.message);
      setEnviando(false);
    }
  };

  return (
    <Modal titulo="Registrar aeropuertos" descripcion="Uno a la vez o el archivo de aeropuertos del curso." onCerrar={onCerrar} bloqueado={enviando} ancho="ancho"
      pie={<>
        <button type="button" className="boton boton--fantasma" onClick={onCerrar} disabled={enviando}>Cancelar</button>
        <button type="button" className="boton boton--primario" disabled={enviando} onClick={() => (modo === "uno" ? registrar() : (archivo ? onImportar(archivo) : setError("Elige un archivo.")))}>
          {modo === "uno" ? (enviando ? "Registrando..." : "Registrar aeropuerto") : "Importar archivo"}
        </button>
      </>}>
      <Segmentado nombre="Modo" opciones={[{ value: "uno", label: "Un aeropuerto" }, { value: "archivo", label: "Archivo" }]} value={modo} onChange={(v) => { setModo(v); setError(null); }} />
      {modo === "uno" ? (
        <div className="campos">
          <Campo req label="Código OACI"><input value={f.codigo} maxLength={4} onChange={set("codigo")} placeholder="SPIM" style={{ textTransform: "uppercase" }} /></Campo>
          <Campo req label="Ciudad"><input value={f.ciudad} onChange={set("ciudad")} placeholder="Lima" /></Campo>
          <Campo req label="País" hint="El huso horario sale del país."><Selector opciones={opcionesPaises} value={f.pais} onChange={set("pais")} placeholder="Busca el país" /></Campo>
          <Campo opc label="Alias"><input value={f.alias} maxLength={10} onChange={set("alias")} placeholder="lima" /></Campo>
          <Campo req label="Capacidad del almacén (paquetes)"><input inputMode="numeric" value={f.capacidad} onChange={(e) => /^\d*$/.test(e.target.value) && set("capacidad")(e)} /></Campo>
          <div />
          <Campo req label="Latitud (decimal)" hint="Sur negativa, p. ej. -12.022"><input inputMode="decimal" value={f.latitud} onChange={set("latitud")} /></Campo>
          <Campo req label="Longitud (decimal)" hint="Oeste negativa, p. ej. -77.114"><input inputMode="decimal" value={f.longitud} onChange={set("longitud")} /></Campo>
        </div>
      ) : (
        <>
          <ZonaArchivo archivo={archivo} onArchivo={setArchivo} ayuda="El archivo de aeropuertos del curso (agrupado por continente, con coordenadas en grados, minutos y segundos). El país de cada aeropuerto debe existir. Solo quedan como sede las líneas que terminen con la palabra SEDE; las demás sedes se marcan aquí con «Hacer sede»." />
        </>
      )}
      {error && <p className="modal2__error" role="alert">{error}</p>}
    </Modal>
  );
}
