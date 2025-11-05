import React, { useState, useEffect } from "react";
import "./aeropuertos.scss";
import { ButtonAdd, Input, Checkbox, Dropdown, Table, SidebarActions, Notification, LoadingOverlay, Pagination } from "../../components/UI/ui";
import { listarAeropuertos  } from "../../services/aeropuertoService";
import plus from '../../assets/icons/plus.svg';
import hideIcon from '../../assets/icons/hide-sidebar.png';

export default function Aeropuertos() {
  const [collapsed, setCollapsed] = useState(false);
  const [codigoFiltro, setCodigoFiltro] = useState("");
  const [orden, setOrden] = useState("");
  const [continenteFiltro, setContinenteFiltro] = useState({ america: false, europa: false, asia: false });

  const [aeropuertos, setAeropuertos] = useState([]);
  const [codigo, setCodigo] = useState("");
  const [ciudad, setCiudad] = useState("");
  const [pais, setPais] = useState("");
  const [capacidad, setCapacidad] = useState("");
  const [continente, setContinente] = useState("")

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
    const fetchAeropuertos = async () => {
      try {
        const data = await listarAeropuertos();
        setAeropuertos(data);
      } catch (err) {
        console.error(err);
        showNotification("danger", "Error al cargar aeropuertos");
      } finally {
        setLoading(false);
      }
    };
    fetchAeropuertos();
  }, []);

  const headers = [
    { label: "Código", key: "codigo" },
    { label: "Ciudad", key: "ciudad" },
    { label: "País", key: "pais" },
    { label: "Capacidad", key: "capacidad" },
    { label: "Acciones", key: "acciones" },
  ];

  const handleFileChange = (e) => {
    if (e.target.files.length > 0) {
      setArchivo(e.target.files[0].name);
    } else {
      setArchivo(null);
    }
  };

  const handleAdd = () => {
    // Aquí podrías agregar la lógica para guardar el aeropuerto
    console.log({ codigo, ciudad, orden, continente, archivo });
    setIsModalOpen(false); // cerramos modal después de agregar
  };

  //Filtros
  const handleFilter = async () => {

  };

  //Limpiar filtros
  const handleCleanFilters = async () => {

  };

  // --- Paginación ---
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 20;
  const indexOfLast = currentPage * itemsPerPage;
  const indexOfFirst = indexOfLast - itemsPerPage;
  const currentAeropuertos = aeropuertos.slice(indexOfFirst, indexOfLast);

  return (
    <div className="page">

      {(loading || processing) && (
        <LoadingOverlay
          text={processing ? "Cargando aeropuertos..." : "Cargando aeropuertos..."}
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
          <>
            <div className="sidebar-content">
              <span className="sidebar-subtitle">Filtros</span>

              <div className="filter-group">
                <span className="sidebar-subtitle-strong">Código</span>
                <Input placeholder="Escribir..." value={codigoFiltro} onChange={(e) => setCodigoFiltro(e.target.value)} />
              </div>

              <div className="filter-group">
                <span className="sidebar-subtitle-strong">Ciudad</span>
                <Dropdown
                  placeholder="Seleccionar..."
                  options={[
                    { label: "Ejemplo 1", value: "ejemplo1" },
                    { label: "Ejemplo 2", value: "ejemplo2" }, 
                    { label: "Ejemplo 3", value: "ejemplo3" },
                  ]}
                  onSelect={(val) => setCiudad(val)}
                />
              </div>

              <div className="filter-group">
                <span className="sidebar-subtitle-strong">Continente</span>
                <Checkbox label="América" value="america" checked={continenteFiltro.america} onChange={(e) => setContinenteFiltro({ ...continenteFiltro, america: e.target.checked })} />
                <Checkbox label="Europa" value="europa" checked={continenteFiltro.europa} onChange={(e) => setContinenteFiltro({ ...continenteFiltro, europa: e.target.checked })} />
                <Checkbox label="Asia" value="asia" checked={continenteFiltro.asia} onChange={(e) => setContinenteFiltro({ ...continenteFiltro, asia: e.target.checked })} />
              </div>

              <div className="filter-group">
                <span className="sidebar-subtitle-strong">Orden de capacidad</span>
                <Dropdown
                  placeholder="Seleccionar..."
                  options={[
                    { label: "Ascendente", value: "ascendente" },
                    { label: "Descendente", value: "descendente" },          
                  ]}
                  onSelect={(val) => setOrden(val)}
                />
              </div>

              <SidebarActions 
                onFilter={handleFilter}
                onClean={handleCleanFilters}
              />
            </div>
          </>
        )}
      </aside>

      {/* Contenido principal */}
      <section className="contenido">
        <div className="content-header">
          <h4>Gestión de aeropuertos</h4>
          <ButtonAdd icon={plus} label="Agregar aeropuerto" onClick={() => setIsModalOpen(true)} />
        </div>

        {loading ? (
          <LoadingOverlay text="Cargando aeropuertos..." />
        ) : (
          <>
            <Table
              headers={headers}
              data={currentAeropuertos}
            />
            <Pagination
              totalItems={aeropuertos.length}
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
              <h3 className="modal-title">Agregar aeropuerto</h3>
              <label htmlFor="fileInput" className="file-label">Agregar archivo</label>
              <input type="file" id="fileInput" className="file-input" onChange={handleFileChange} />
            </div>

            <div className="file-name">{archivo || "Ningún archivo seleccionado"}</div>

            <div className="modal-body">
              <label htmlFor="codigoModal">Código</label>
              <Input id="codigoModal" placeholder="Escribe el código" value={codigo} onChange={(e) => setCodigo(e.target.value)} />

              <label htmlFor="ciudadModal">Ciudad</label>
              <Dropdown
                placeholder="Seleccionar..."
                options={[
                  { label: "Ejemplo 1", value: "ejemplo1" },
                  { label: "Ejemplo 2", value: "ejemplo2" },
                ]}
                onSelect={(val) => setCiudad(val)}
              />

              <label htmlFor="capacidadModal">Capacidad</label>
              <Input id="capacidadModal" placeholder="Escribe la capacidad" />
            </div>

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
