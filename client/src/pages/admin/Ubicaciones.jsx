import React, { useCallback, useEffect, useMemo, useState } from "react";
import { BarraFiltros, Campo, Etiqueta, IconoMas, Modal, PaginaGestion, Paginador, Segmentado, Tabla } from "../../components/Gestion/Gestion";
import { listarPaises, listarTodosLosAeropuertos } from "../../api/catalogos";
import { registrarPais } from "../../api/importacion";
import { useNotify } from "../../notifications/NotificationContext";
import { TAMANIO_PAGINA } from "../../config";

const husoTexto = (h) => `UTC${h >= 0 ? "+" : "−"}${Math.abs(h)}`;

// Países y continentes soportados (cada usuario y cada aeropuerto pertenecen a un país)
export default function Ubicaciones() {
  const notify = useNotify();
  const [paises, setPaises] = useState([]);
  const [aeropuertos, setAeropuertos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [texto, setTexto] = useState("");
  const [continente, setContinente] = useState("");
  const [pagina, setPagina] = useState(1);
  const [modal, setModal] = useState(false);

  const cargar = useCallback(async () => {
    setCargando(true);
    try {
      const [p, a] = await Promise.all([listarPaises(), listarTodosLosAeropuertos()]);
      setPaises(p); setAeropuertos(a);
    } catch (e) { notify("danger", e.message); } finally { setCargando(false); }
  }, [notify]);
  useEffect(() => { cargar(); }, [cargar]);

  const continentes = useMemo(() => {
    const m = new Map();
    paises.forEach((p) => m.set(p.continente, (m.get(p.continente) ?? 0) + 1));
    return [...m.entries()].sort((a, b) => a[0].localeCompare(b[0], "es"));
  }, [paises]);
  const porPais = useMemo(() => {
    const m = new Map();
    aeropuertos.forEach((a) => m.set(a.pais, (m.get(a.pais) ?? 0) + 1));
    return m;
  }, [aeropuertos]);

  const filtrados = useMemo(() => {
    const t = texto.trim().toLowerCase();
    return paises
      .filter((p) => (!continente || p.continente === continente) && (!t || p.nombre.toLowerCase().includes(t)))
      .sort((a, b) => a.nombre.localeCompare(b.nombre, "es"));
  }, [paises, texto, continente]);
  useEffect(() => setPagina(1), [texto, continente]);
  const total = Math.max(1, Math.ceil(filtrados.length / TAMANIO_PAGINA));

  const columnas = [
    { label: "País", key: "nombre", render: (p) => <span className="codigo">{p.nombre}</span> },
    { label: "Continente", key: "continente" },
    { label: "Huso horario", key: "husoHorario", render: (p) => husoTexto(p.husoHorario) },
    { label: "Aeropuertos", key: "aeropuertos", alinear: "derecha", render: (p) => (porPais.get(p.nombre) ? <Etiqueta tono="info">{porPais.get(p.nombre)}</Etiqueta> : <span className="tenue">0</span>) },
  ];

  return (
    <PaginaGestion
      titulo="Países y continentes"
      descripcion="Países soportados por el sistema. Cada usuario y cada aeropuerto pertenece a uno; su huso horario define la hora que ve el usuario."
      acciones={<button type="button" className="boton boton--primario" onClick={() => setModal(true)}><IconoMas /> Registrar país</button>}
    >
      <BarraFiltros busqueda={texto} onBusqueda={setTexto} placeholder="Buscar país" resumen={!cargando && `${filtrados.length} de ${paises.length} países`} />
      <div style={{ marginBottom: 14 }}>
        <Segmentado nombre="Continente" value={continente} onChange={setContinente}
          opciones={[{ value: "", label: `Todos (${paises.length})` }, ...continentes.map(([c, n]) => ({ value: c, label: `${c} (${n})` }))]} />
      </div>
      <Tabla columnas={columnas} filas={filtrados.slice((pagina - 1) * TAMANIO_PAGINA, pagina * TAMANIO_PAGINA)} cargando={cargando} clave="nombre" vacio="No hay países con estos filtros." />
      <Paginador pagina={pagina} total={total} hayMas={pagina < total} onCambiar={setPagina} />

      {modal && <RegistrarPais continentes={continentes.map(([c]) => c)} onCerrar={() => setModal(false)} onListo={() => { setModal(false); cargar(); }} />}
    </PaginaGestion>
  );
}

function RegistrarPais({ continentes, onCerrar, onListo }) {
  const notify = useNotify();
  const [nombre, setNombre] = useState("");
  const [huso, setHuso] = useState("-5");
  const [continente, setContinente] = useState(continentes[0] ?? "");
  const [nuevoContinente, setNuevoContinente] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState(null);
  const esNuevo = continente === "__nuevo";

  const registrar = async () => {
    const cont = esNuevo ? nuevoContinente.trim() : continente;
    if (!nombre.trim()) return setError("Escribe el nombre del país.");
    if (!cont) return setError("Elige o escribe el continente.");
    setEnviando(true);
    setError(null);
    try {
      await registrarPais({ nombre: nombre.trim(), husoHorario: Number(huso), continente: cont });
      notify("success", `País ${nombre.trim()} registrado.`);
      onListo();
    } catch (e) {
      setError(e.message);
      setEnviando(false);
    }
  };

  return (
    <Modal titulo="Registrar país" onCerrar={onCerrar} bloqueado={enviando}
      pie={<>
        <button type="button" className="boton boton--fantasma" onClick={onCerrar} disabled={enviando}>Cancelar</button>
        <button type="button" className="boton boton--primario" onClick={registrar} disabled={enviando}>{enviando ? "Registrando..." : "Registrar país"}</button>
      </>}>
      <div className="campos">
        <Campo req label="Nombre" ancho="completo"><input value={nombre} maxLength={30} onChange={(e) => setNombre(e.target.value)} placeholder="p. ej. Ecuador" /></Campo>
        <Campo req label="Huso horario">
          <select value={huso} onChange={(e) => setHuso(e.target.value)}>
            {Array.from({ length: 27 }, (_, i) => i - 12).map((h) => <option key={h} value={h}>{husoTexto(h)}</option>)}
          </select>
        </Campo>
        <Campo req label="Continente">
          <select value={continente} onChange={(e) => setContinente(e.target.value)}>
            {continentes.map((c) => <option key={c} value={c}>{c}</option>)}
            <option value="__nuevo">Otro continente...</option>
          </select>
        </Campo>
        {esNuevo && <Campo req label="Nombre del continente nuevo" ancho="completo"><input value={nuevoContinente} maxLength={30} onChange={(e) => setNuevoContinente(e.target.value)} /></Campo>}
      </div>
      {error && <p className="modal2__error" role="alert">{error}</p>}
    </Modal>
  );
}
