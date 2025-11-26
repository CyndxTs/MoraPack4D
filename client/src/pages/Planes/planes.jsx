import React, { useState, useEffect } from "react";
import './planes.scss'
import { ButtonAdd, Input, Table, SidebarActions, Notification, LoadingOverlay, Pagination, RemoveFileButton, Dropdown } from "../../components/UI/ui";
import { listarPlanes, importarPlanes } from "../../services/planesService";
import plus from '../../assets/icons/plus.svg';
import hideIcon from '../../assets/icons/hide-sidebar.png';

export default function Planes() {
  const [collapsed, setCollapsed] = useState(false);
  const [codigoFiltro, setCodigoFiltro] = useState("");
  const [ordenHora, setOrdenHora] = useState("");

  const opcionesOrden = [
    { label: "Ascendente", value: "asc" },
    { label: "Descendente", value: "desc" },
  ];

  const [planes, setPlanes] = useState([]);
  const [planesOriginales, setPlanesOriginales] = useState([]);

  const [codigo, setCodigo] = useState("");
  const [horaSalida, setHoraSalida] = useState("");
  const [horaLlegada, setHoraLlegada] = useState("");
  const [capacidad, setCapacidad] = useState("");
  const [origen, setOrigen] = useState("");
  const [destino, setDestino] = useState("");

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [archivo, setArchivo] = useState(null);

  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);
  const [notification, setNotification] = useState(null);

  const showNotification = (type, message) => {
    setNotification({ type, message });
    setTimeout(() => setNotification(null), 5000);
  };

  useEffect(() => {
    const fetchPlanes = async () => {
      try {
        const data = await listarPlanes(0,3000);
        setPlanes(data.dtos || []);
        setPlanesOriginales(data.dtos || []);
      } catch (err) {
        console.error(err);
        showNotification("danger", "Error al cargar planes de vuelos");
      } finally {
        setLoading(false);
      }
    };
    fetchPlanes();
  }, []);

  
  // Columnas nuevas para vuelos
  const headers = [
    { label: "Código", key: "codigo" },
    { label: "Origen", key: "codOrigen" },
    { label: "Salida (UTC)", key: "horaSalida" },
    { label: "Llegada (UTC)", key: "horaLlegada" },
    { label: "Destino", key: "codDestino" },
    { label: "Capacidad", key: "capacidad" },
    //{ label: "Acciones", key: "acciones" },
  ];

  const handleFileChange = (e) => {
    if (e.target.files.length > 0) {
      setArchivo(e.target.files[0]);
    } else {
      setArchivo(null);
    }
  };

  const handleAdd = async () => {
    if (!archivo && (!codigo.trim() || !horaSalida.trim())) {
      showNotification(
        "warning",
        "Por favor selecciona un archivo o completa los campos antes de continuar."
      );
      return;
    }

    // Validación del nombre exacto del archivo
    if (
      archivo &&
      archivo.name !== "c.1inf54.25.2.planes_vuelo.v4.20250818.txt"
    ) {
      showNotification(
        "warning",
        "El archivo debe llamarse exactamente 'c.1inf54.25.2.planes_vuelo.v4.20250818'."
      );
      return;
    }

    try {
      setProcessing(true);

      if (archivo) {
        // Importar usando AlgorithmController
        await importarPlanes(archivo);
        showNotification("success", "Planes de vuelos importados correctamente");
      } else {
        // Aquí podrías implementar agregar manualmente un vuelo si lo decides
        showNotification("success", "Planes de vuelos agregado correctamente");
      }

      // Recargar lista
      const data = await listarPlanes(0,3000);
      setPlanes(data.dtos || []);

      // Reset form & modal
      setIsModalOpen(false);
      setArchivo(null);

      setCodigo("");
      setHoraSalida("");
      setHoraLlegada("");
      setCapacidad("");
      setOrigen("");
      setDestino("");

    } catch (error) {
      console.error(error);
      showNotification("danger", "Error al agregar vuelo");
    } finally {
      setProcessing(false);
    }
  };


  //Filtro
  const handleFilter = () => {
    let lista = [...planesOriginales];

    // 1) Filtro por código
    if (codigoFiltro.trim()) {
      lista = lista.filter(v =>
        v.codigo.toLowerCase().includes(codigoFiltro.toLowerCase())
      );
    }

    // 2) Orden por hora
    const parseHora = (h) => new Date(`1970-01-01T${h}:00Z`).getTime();

    if (ordenHora) {
      lista.sort((a, b) =>
        ordenHora === "asc"
          ? parseHora(a.horaSalida) - parseHora(b.horaSalida)
          : parseHora(b.horaSalida) - parseHora(a.horaSalida)
      );
    }

    setPlanes(lista);
    setCurrentPage(1);
  };

  //Limpiar filtros
  const handleCleanFilters = () => {
    setCodigoFiltro("");
    setOrdenHora("")
    setPlanes(planesOriginales);
    setCurrentPage(1);
  };


  // --- Paginación ---
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 20;
  const indexOfLast = currentPage * itemsPerPage;
  const indexOfFirst = indexOfLast - itemsPerPage;
  const currentPlanes = planes.slice(indexOfFirst, indexOfLast);

  return (
    <div className="page">

      {(loading || processing) && (
        <LoadingOverlay
          text={processing ? "Cargando planes de vuelos..." : "Cargando planes de vuelos..."}
        />
      )}

      {notification && (
        <Notification
          type={notification.type}
          message={notification.message}
          onClose={() => setNotification(null)}
        />
      )}

      {/* Sidebar */}
      <aside className={`sidebar ${collapsed ? "collapsed" : ""}`}>
        <div className="sidebar-header">
          <span className="sidebar-title">Herramientas</span>
          <img
            src={hideIcon}
            alt="Ocultar"
            className="hide-icon"
            onClick={() => setCollapsed(!collapsed)}
          />
        </div>

        {!collapsed && (
          <div className="sidebar-content">
            <span className="sidebar-subtitle">Filtros</span>

            <div className="filter-group">
              <span className="sidebar-subtitle-strong">Código</span>
              <Input placeholder="Escribir..." value={codigoFiltro} onChange={(e) => setCodigoFiltro(e.target.value)} />
            </div>

            <div className="filter-group">
              <span className="sidebar-subtitle-strong">Ordenar por salida</span>

              <Dropdown
                options={opcionesOrden}
                value={ordenHora}
                onSelect={(v) => setOrdenHora(v)}
                placeholder="Ordenar por hora salida"
              />
            </div>

            <SidebarActions 
              onFilter={handleFilter}
              onClean={handleCleanFilters}
            />
          </div>
        )}
      </aside>

      {/* Contenido principal */}
      <section className="contenido">
        <div className="content-header">
          <h4>Gestión de planes de vuelos</h4>
          <ButtonAdd icon={plus} label="Agregar plan de vuelo" onClick={() => setIsModalOpen(true)} />
        </div>

        {loading ? (
          <LoadingOverlay text="Cargando planes de vuelos..." />
        ) : (
          <>
            <Table
              headers={headers}
              data={currentPlanes}
            />
            <Pagination
              totalItems={planes.length}
              itemsPerPage={itemsPerPage}
              currentPage={currentPage}
              onPageChange={setCurrentPage}
            />
          </>
        )}
      </section>

      {/* Modal */}
      {isModalOpen && (
        <div className="modal" onClick={() => setIsModalOpen(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">Agregar plan de vuelo</h3>

              <label htmlFor="fileInput" className="file-label">Agregar archivo</label>
              <input type="file" id="fileInput" className="file-input" onChange={handleFileChange} />
            </div>

            <div className="file-name">
              {archivo ? archivo.name : "Ningún archivo seleccionado"}
              {archivo && (
                <RemoveFileButton onClick={() => setArchivo(null)} />
              )}
            </div>

            {/*<div className="modal-body">
              <label>Código</label>
              <Input placeholder="Escribe el código" value={codigo} onChange={(e) => setCodigo(e.target.value)} disabled={!!archivo}/>

              <label>Hora salida UTC</label>
              <Input placeholder="Escribe la hora de salida UTC" value={horaSalida} onChange={(e) => setHoraSalida(e.target.value)} disabled={!!archivo}/>

              <label>Hora llegada UTC</label>
              <Input placeholder="Escribe la hora de llegada UTC" value={horaLlegada} onChange={(e) => setHoraLlegada(e.target.value)} disabled={!!archivo}/>

              <label>Capacidad</label>
              <Input placeholder="Escribe la capacidad" value={capacidad} onChange={(e) => setCapacidad(e.target.value)} disabled={!!archivo}/>

              <label>Aeropuerto origen</label>
              <Input placeholder="Escribe el aeropuerto origen" value={origen} onChange={(e) => setOrigen(e.target.value)} disabled={!!archivo}/>

              <label>Aeropuerto destino</label>
              <Input placeholder="Escribe el aeropuerto destino" value={destino} onChange={(e) => setDestino(e.target.value)} disabled={!!archivo}/>
            </div>*/}

            <div className="modal-footer">
              <button className="btn red" onClick={() => setIsModalOpen(false)}>Cancelar</button>
              <button className="btn green" onClick={handleAdd}>Agregar</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
