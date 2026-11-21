import React, { useCallback, useEffect, useMemo, useState } from "react";
import { ButtonAdd, Dropdown, Input, LoadingOverlay, Pagination, Radio, RemoveFileButton, SidebarActions, Table } from "../../../components/UI/ui";
import StatusBadge from "../../../components/Status/status";
import { listarEventos, listarTodosLosPlanes } from "../../../api/catalogos";
import { importarArchivo, TIPO_ARCHIVO } from "../../../api/importacion";
import { useNotify } from "../../../notifications/NotificationContext";
import { TAMANIO_PAGINA } from "../../../config";
import plus from "../../../assets/icons/plus.svg";
import hideIcon from "../../../assets/icons/hide-sidebar.png";
import "../Pedidos/pedidos.scss";
import "./planes.scss";

const COLORES = {
  CANCELACION: "#b91c1c", REPROGRAMACION: "#B5651D", REAJUSTE_CAPACIDAD: "#1d4ed8",
  SIMULACION: "#0B6623", OPERACION: "#B5651D",
};

const FILTROS_VACIOS = { texto: "", origen: "", destino: "", orden: "asc" };

// Planes de vuelo (catálogo completo, filtrado en el navegador) y eventos sobre sus vuelos
export default function Planes() {
  const [tab, setTab] = useState("planes");
  const pestanas = (
    <div className="planes-tabs">
      <button className={tab === "planes" ? "active" : ""} onClick={() => setTab("planes")}>Planes de vuelo</button>
      <button className={tab === "eventos" ? "active" : ""} onClick={() => setTab("eventos")}>Eventos</button>
    </div>
  );
  return (
    <div className="page planes-page">
      {tab === "planes" ? <PlanesTab pestanas={pestanas} /> : <EventosTab pestanas={pestanas} />}
    </div>
  );
}

function PlanesTab({ pestanas }) {
  const notify = useNotify();
  const [collapsed, setCollapsed] = useState(false);
  const [planes, setPlanes] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [filtros, setFiltros] = useState(FILTROS_VACIOS);
  const [aplicados, setAplicados] = useState(FILTROS_VACIOS);
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
    const t = aplicados.texto.trim().toUpperCase();
    const lista = planes.filter((p) =>
      (!t || p.codigo.toUpperCase().includes(t))
      && (!aplicados.origen || p.codOrigen === aplicados.origen)
      && (!aplicados.destino || p.codDestino === aplicados.destino));
    const clave = (p) => p.horaSalida ?? "";
    lista.sort((a, b) => (aplicados.orden === "asc" ? clave(a).localeCompare(clave(b)) : clave(b).localeCompare(clave(a))));
    return lista.map((p) => ({ ...p, cantEventos: p.eventos?.length ?? 0 }));
  }, [planes, aplicados]);

  const visibles = filtrados.slice((pagina - 1) * TAMANIO_PAGINA, pagina * TAMANIO_PAGINA);
  const aplicar = () => { setAplicados(filtros); setPagina(1); };
  const limpiar = () => { setFiltros(FILTROS_VACIOS); setAplicados(FILTROS_VACIOS); setPagina(1); };

  const headers = [
    { label: "Código", key: "codigo" },
    { label: "Origen", key: "codOrigen" },
    { label: "Salida (UTC)", key: "horaSalida" },
    { label: "Destino", key: "codDestino" },
    { label: "Llegada (UTC)", key: "horaLlegada" },
    { label: "Capacidad", key: "capacidad" },
    { label: "Duración (h)", key: "duracion", render: (p) => (typeof p.duracion === "number" ? p.duracion.toFixed(2) : "-") },
    { label: "Eventos", key: "cantEventos" },
  ];

  return (
    <>
      {progreso && <LoadingOverlay text="Importando planes..." progreso={progreso.proceso ? progreso : null} />}
      <aside className={`sidebar ${collapsed ? "collapsed" : ""}`}>
        <div className="sidebar-header">
          <span className="sidebar-title">Filtros</span>
          <img src={hideIcon} alt="Ocultar" className="hide-icon" onClick={() => setCollapsed(!collapsed)} />
        </div>
        {!collapsed && (
          <div className="sidebar-content">
            <div className="filter-group">
              <span className="sidebar-subtitle-strong">Código</span>
              <Input placeholder="Contiene..." value={filtros.texto} onChange={(e) => setFiltros({ ...filtros, texto: e.target.value })} />
            </div>
            <div className="filter-group">
              <span className="sidebar-subtitle-strong">Origen</span>
              <Dropdown placeholder="Todos" value={filtros.origen} onSelect={(v) => setFiltros({ ...filtros, origen: v })} options={codigos.origenes.map((c) => ({ label: c, value: c }))} />
            </div>
            <div className="filter-group">
              <span className="sidebar-subtitle-strong">Destino</span>
              <Dropdown placeholder="Todos" value={filtros.destino} onSelect={(v) => setFiltros({ ...filtros, destino: v })} options={codigos.destinos.map((c) => ({ label: c, value: c }))} />
            </div>
            <div className="filter-group">
              <span className="sidebar-subtitle-strong">Ordenar por salida</span>
              <Radio name="orden" value="asc" checked={filtros.orden === "asc"} onChange={() => setFiltros({ ...filtros, orden: "asc" })} label="Ascendente" />
              <Radio name="orden" value="desc" checked={filtros.orden === "desc"} onChange={() => setFiltros({ ...filtros, orden: "desc" })} label="Descendente" />
            </div>
            <SidebarActions onFilter={aplicar} onClean={limpiar} />
          </div>
        )}
      </aside>

      <section className="contenido">
        <div className="content-header">
          <div className="content-header-title">{pestanas}<h4>Planes de vuelo <span className="content-count">({filtrados.length})</span></h4></div>
          <ButtonAdd icon={plus} label="Importar planes" onClick={() => setModal(true)} />
        </div>
        {cargando ? <p className="table-loading">Cargando planes...</p> : <Table headers={headers} data={visibles} />}
        <Pagination currentPage={pagina} hasMorePages={pagina * TAMANIO_PAGINA < filtrados.length} onPageChange={setPagina} />
      </section>

      {modal && (
        <ImportarModal
          titulo="Importar planes de vuelo"
          ayuda="Formato: ORIG-DEST-HH:MM-HH:MM-CAPACIDAD (horas locales). Los planes repetidos se omiten."
          onCerrar={() => setModal(false)}
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
    </>
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

  const headers = [
    { label: "Código", key: "codigo" },
    { label: "Plan", key: "codPlan" },
    { label: "Tipo", key: "tipo", render: (e) => <StatusBadge value={e.tipo} colorMap={COLORES} /> },
    { label: "Escenario", key: "tipoEscenario", useStatusColors: true },
    { label: "Salida original (UTC)", key: "fechaHoraInicio" },
    { label: "Llegada original (UTC)", key: "fechaHoraFin" },
    { label: "Nueva salida", key: "fechaHoraNuevaSalida" },
    { label: "Nueva llegada", key: "fechaHoraNuevaLlegada" },
    { label: "Nueva capacidad", key: "nuevaCapacidad" },
  ];

  return (
    <>
      {progreso && <LoadingOverlay text="Importando eventos..." progreso={progreso.proceso ? progreso : null} />}
      <section className="contenido">
        <div className="content-header">
          <div className="content-header-title">{pestanas}<h4>Eventos sobre vuelos</h4></div>
          <ButtonAdd icon={plus} label="Importar eventos" onClick={() => setModal(true)} />
        </div>
        {cargando ? <p className="table-loading">Cargando eventos...</p> : <Table headers={headers} data={eventos} statusColors={COLORES} />}
        <Pagination currentPage={pagina} hasMorePages={hayMas} onPageChange={cargar} />
      </section>

      {modal && (
        <ImportarModal
          titulo="Importar eventos"
          ayuda="Formato: DD-ORIG-DEST-HH:MM-HH:MM-TIPO[-HH:MM-HH:MM | -CAPACIDAD] (horas locales). TIPO: CANCELACION, REPROGRAMACION o REAJUSTE_CAPACIDAD. Si una línea es inválida se cancela toda la importación."
          conMesYEscenario
          onCerrar={() => setModal(false)}
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
    </>
  );
}

function ImportarModal({ titulo, ayuda, conMesYEscenario = false, onCerrar, onImportar }) {
  const hoy = new Date();
  const [archivo, setArchivo] = useState(null);
  const [mes, setMes] = useState(hoy.getUTCMonth() + 1);
  const [anio, setAnio] = useState(hoy.getUTCFullYear());
  const [tipoEscenario, setTipoEscenario] = useState("SIMULACION");
  const [error, setError] = useState(null);

  const importar = () => {
    if (!archivo) return setError("Selecciona un archivo.");
    if (conMesYEscenario && (!(mes >= 1 && mes <= 12) || !(anio >= 2000 && anio <= 2100))) return setError("Mes o año inválido.");
    onImportar(archivo, conMesYEscenario ? { mes: Number(mes), anio: Number(anio), tipoEscenario } : {});
  };

  return (
    <div className="modal" onClick={onCerrar}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3 className="modal-title">{titulo}</h3>
          <label htmlFor="archivoImportar" className="file-label">Elegir archivo</label>
          <input type="file" id="archivoImportar" className="file-input" accept=".txt" onChange={(e) => setArchivo(e.target.files[0] ?? null)} />
        </div>
        <div className="file-name">
          {archivo ? archivo.name : "Ningún archivo seleccionado"}
          {archivo && <RemoveFileButton onClick={() => setArchivo(null)} />}
        </div>
        <div className="modal-body">
          <p className="modal-hint">{ayuda}</p>
          {conMesYEscenario && (
            <>
              <span className="sidebar-subtitle">Mes y año de los días del archivo</span>
              <div className="inline-fields">
                <Input type="number" min={1} max={12} value={mes} onChange={(e) => setMes(e.target.value)} placeholder="Mes" />
                <Input type="number" min={2000} max={2100} value={anio} onChange={(e) => setAnio(e.target.value)} placeholder="Año" />
              </div>
              <span className="sidebar-subtitle">Escenario</span>
              <Radio name="escEv" label="Simulación" value="SIMULACION" checked={tipoEscenario === "SIMULACION"} onChange={() => setTipoEscenario("SIMULACION")} />
              <Radio name="escEv" label="Operación" value="OPERACION" checked={tipoEscenario === "OPERACION"} onChange={() => setTipoEscenario("OPERACION")} />
            </>
          )}
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
