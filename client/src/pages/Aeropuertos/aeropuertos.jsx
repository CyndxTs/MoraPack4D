import React, { useState, useEffect } from "react";
import "./aeropuertos.scss";
import { ButtonAdd, Input, Checkbox, Dropdown, Table, SidebarActions, Notification, LoadingOverlay, Pagination, RemoveFileButton, Radio  } from "../../components/UI/ui";
import { listarAeropuertos, importarAeropuertos  } from "../../services/aeropuertoService";
import plus from '../../assets/icons/plus.svg';
import hideIcon from '../../assets/icons/hide-sidebar.png';

export default function Aeropuertos() {
  const [collapsed, setCollapsed] = useState(false);
  const [codigoFiltro, setCodigoFiltro] = useState("");
  const [ciudadFiltro, setCiudadFiltro] = useState("");
  const [orden, setOrden] = useState("");
  const [continenteFiltro, setContinenteFiltro] = useState("");

  const [aeropuertos, setAeropuertos] = useState([]);
  const [aeropuertosOriginales, setAeropuertosOriginales] = useState([]);

  const [codigo, setCodigo] = useState("");
  const [ciudad, setCiudad] = useState("");
  const [pais, setPais] = useState("");
  const [capacidad, setCapacidad] = useState("");
  const [continente, setContinente] = useState("");
  const [husoHorario,setHusoHorario]=useState("");

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [archivo, setArchivo] = useState(null);

  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);
  const [notification, setNotification] = useState(null);

  const showNotification = (type, message) => {
    setNotification({ type, message });
    setTimeout(() => setNotification(null), 5000);
  };

  const headers = [
    { label: "Código", key: "codigo" },
    { label: "Ciudad", key: "ciudad" },
    { label: "País", key: "pais" },
    { label: "Continente", key: "continente" },
    { label: "Capacidad", key: "capacidad" },
    { label: "Huso horario", key: "husoHorario" },
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
    if (!archivo && (!codigo.trim() || !ciudad.trim())) {
      showNotification(
        "warning",
        "Por favor selecciona un archivo o completa los campos antes de continuar."
      );
      return;
    }

    // Validación del nombre exacto
    if (
      archivo &&
      archivo.name !== "c.1inf54.25.2.Aeropuerto.husos.v1.20250818__estudiantes.txt"
    ) {
      showNotification(
        "warning",
        "El archivo debe llamarse exactamente 'c.1inf54.25.2.Aeropuerto.husos.v1.20250818__estudiantes.txt'."
      );
      return;
    }

    try {
      setProcessing(true);

      if (archivo) {
        // Importación mediante AlgorithmController
        await importarAeropuertos(archivo);
        showNotification("success", "Aeropuertos importados correctamente");
      } else {
        // Aquí podrías agregar lógica para añadir manualmente un aeropuerto si se desea
        showNotification("success", "Aeropuerto agregado correctamente");
      }

      fetchAeropuertos(1);
      setIsModalOpen(false);
      setArchivo(null);
      setCodigo("");
      setCiudad("");
      setPais("");
      setCapacidad("");
      setContinente("");
      setHusoHorario("");
    } catch (error) {
      console.error(error);
      showNotification("danger", "Error al agregar aeropuerto");
    } finally {
      setProcessing(false);
    }
  };

  //Filtros
  const handleFilter = () => {
    let lista = [...aeropuertosOriginales];

    if (codigoFiltro.trim()) {
      lista = lista.filter(a =>
        a.codigo.toLowerCase().includes(codigoFiltro.toLowerCase())
      );
    }

    if (ciudadFiltro.trim()) {
      lista = lista.filter(a =>
        a.ciudad.toLowerCase().includes(ciudadFiltro.toLowerCase())
      );
    }

    if (continenteFiltro) {
      let normalize = (t) =>
        t
          .normalize("NFD")
          .replace(/[\u0300-\u036f]/g, "")
          .replace(/\./g, "")
          .toLowerCase();

      let continenteTexto = "";

      if (continenteFiltro === "AMERICA") continenteTexto = "america del sur";
      if (continenteFiltro === "EUROPA") continenteTexto = "europa";
      if (continenteFiltro === "ASIA") continenteTexto = "asia";

      lista = lista.filter(a =>
        normalize(a.continente).includes(continenteTexto)
      );
    }


    if (orden) {
      if (orden === "ASC") {
        lista.sort((a, b) => a.capacidad - b.capacidad);
      } else if (orden === "DESC") {
        lista.sort((a, b) => b.capacidad - a.capacidad);
      }
    }

    setAeropuertos(lista);
    setCurrentPage(1);
    showNotification("success", "Filtros aplicados");
  };



  //Limpiar filtros
  const handleCleanFilters = () => {
    setCodigoFiltro("");
    setCiudadFiltro("");
    setContinenteFiltro("");
    setOrden("");

    setAeropuertos(aeropuertosOriginales);
    setCurrentPage(1);

    showNotification("info", "Filtros limpiados");
  };



  // --- Paginación ---
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;
  const [hasMorePages, setHasMorePages] = useState(false);

  useEffect(() => {
    fetchAeropuertos(1); // página 1 visual → backend página 0
  }, []);

  const fetchAeropuertos = async (paginaVisual) => {
    try {
      setLoading(true);

      const backendPage = paginaVisual - 1; // visual 1 → backend 0

      const data = await listarAeropuertos(backendPage, itemsPerPage);

      const lista = data.dtos || [];

      setAeropuertos(lista);
      setCurrentPage(paginaVisual);

      // Si devuelve menos de 10, ya no hay más páginas
      setHasMorePages(lista.length === itemsPerPage);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">

      {(loading || processing) && (
        <LoadingOverlay
          text={processing ? "Procesando aeropuertos..." : "Cargando aeropuertos..."}
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
                <Input placeholder="Escribir..." value={ciudadFiltro} onChange={(e) => setCiudadFiltro(e.target.value)} />
              </div>

              <div className="filter-group">
                <span className="sidebar-subtitle-strong">Continente</span>

                <Radio 
                  name="continenteFiltro" 
                  label="América del Sur" 
                  value="AMERICA" 
                  checked={continenteFiltro === "AMERICA"} 
                  onChange={(e) => setContinenteFiltro(e.target.value)} 
                />

                <Radio 
                  name="continenteFiltro" 
                  label="Europa" 
                  value="EUROPA" 
                  checked={continenteFiltro === "EUROPA"} 
                  onChange={(e) => setContinenteFiltro(e.target.value)} 
                />

                <Radio 
                  name="continenteFiltro" 
                  label="Asia" 
                  value="ASIA" 
                  checked={continenteFiltro === "ASIA"} 
                  onChange={(e) => setContinenteFiltro(e.target.value)} 
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
              data={aeropuertos}
            />
            <Pagination
              currentPage={currentPage}
              onPageChange={fetchAeropuertos}
              hasMorePages={hasMorePages}
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

            <div className="file-name">
              {archivo ? archivo.name : "Ningún archivo seleccionado"}
              {archivo && (
                <RemoveFileButton onClick={() => setArchivo(null)} />
              )}
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
