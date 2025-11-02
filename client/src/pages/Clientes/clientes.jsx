import React, { useState, useEffect } from "react";
import "./clientes.scss";
import { ButtonAdd, Input, Radio, Table, LoadingOverlay, Pagination, Notification, SidebarActions } from "../../components/UI/ui";
import plus from "../../assets/icons/plus.svg";
import hideIcon from "../../assets/icons/hide-sidebar.png";
import { listarClientes, filtrarClientes  } from "../../services/clienteService";
import { importarClientes } from "../../services/generalService";

export default function Clientes() {
  const [collapsed, setCollapsed] = useState(false);
  const [nombreFiltro, setNombreFiltro] = useState("");
  const [correoFiltro, setCorreoFiltro] = useState("");
  const [estadoFiltro, setEstadoFiltro] = useState("ONLINE");

  const [clientes, setClientes] = useState([]);
  const [nombre, setNombre] = useState("");
  const [correo, setCorreo] = useState("");
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
    setEstadoFiltro("ONLINE");
    const fetchClientes = async () => {
      try {
        const data = await filtrarClientes(null, null, "ONLINE");
        setClientes(data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchClientes();
  }, []);

  const headers = [
    { label: "Código", key: "codigo" },
    { label: "Nombre completo", key: "nombre" },
    { label: "Correo", key: "correo" },
    { label: "Estado", key: "estado" },
    { label: "Acciones", key: "acciones" },
  ];

  // --- Paginación ---
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 20;

  // Calcular los clientes visibles en esta página
  const indexOfLast = currentPage * itemsPerPage;
  const indexOfFirst = indexOfLast - itemsPerPage;
  const currentClientes = clientes.slice(indexOfFirst, indexOfLast);


  // Manejo de archivo
  const handleFileChange = (e) => {
    if (e.target.files.length > 0) {
      setArchivo(e.target.files[0]);
    } else {
      setArchivo(null);
    }
  };

  //Filtrar
  const handleFilter = async () => {
    const noFilters =
      !nombreFiltro.trim() &&
      !correoFiltro.trim() &&
      !estadoFiltro;

    if (noFilters) {
      showNotification("info", "No hay filtros para aplicar");
      return;
    }

    try {
      setLoading(true);
      const data = await filtrarClientes(
        nombreFiltro.trim() || null,
        correoFiltro.trim() || null,
        estadoFiltro || null
      );
      setClientes(data);
      setCurrentPage(1);
      showNotification("success", "Filtro aplicado correctamente");
    } catch (error) {
      console.error(error);
      showNotification("danger", "Error al filtrar clientes");
    } finally {
      setLoading(false);
    }
  };



  //Limpiar filtros
  const handleCleanFilters = async () => {
    const isClean =
      !nombreFiltro.trim() &&
      !correoFiltro.trim() &&
      estadoFiltro === "ONLINE";

    if (isClean) {
      showNotification("info", "No hay filtros que limpiar");
      return;
    }

    setNombreFiltro("");
    setCorreoFiltro("");
    setEstadoFiltro("ONLINE");

    try {
      setLoading(true);
      const data = await filtrarClientes(null, null, "ONLINE");
      setClientes(data);
      setCurrentPage(1);
      showNotification("info", "Filtros limpiados");
    } catch (error) {
      console.error(error);
      showNotification("danger", "Error al limpiar filtros");
    } finally {
      setLoading(false);
    }
  };

  // Manejo del modal
  const handleAdd = async () => {
    if (!archivo && (!nombre.trim() || !correo.trim())) {
      showNotification(
        "warning",
        "Por favor selecciona un archivo o completa los campos antes de continuar."
      );
      return;
    }

    if (archivo && archivo.name !== "Clientes.txt") {
      showNotification("warning", "El archivo debe llamarse exactamente 'Clientes.txt'.");
      return;
    }

    try {
      setProcessing(true);

      if (archivo) {
        // 👇 Usamos AlgorithmController
        await importarClientes(archivo, "CLIENTES");
        showNotification("success", "Clientes importados correctamente");
      } else {
        showNotification("success", "Cliente agregado correctamente");
      }

      const data = await listarClientes();
      setClientes(data);
      setIsModalOpen(false);
      setArchivo(null);
      setNombre("");
      setCorreo("");
    } catch (error) {
      console.error(error);
      showNotification("danger", "Error al agregar el cliente");
    } finally {
      setProcessing(false);
    }
  };


  return (
    <div className="page">
      {(loading || processing) && (
        <LoadingOverlay
          text={processing ? "Cargando clientes..." : "Cargando clientes..."}
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

        <div className="sidebar-content">
          <span className="sidebar-subtitle">Filtros</span>

          <div className="filter-group">
            <span className="sidebar-subtitle-strong">Nombre</span>
            <Input
              placeholder="Escribir..."
              value={nombreFiltro}
              onChange={(e) => setNombreFiltro(e.target.value)}
            />
          </div>

          <div className="filter-group">
            <span className="sidebar-subtitle-strong">Correo</span>
            <Input
              placeholder="Escribir..."
              value={correoFiltro}
              onChange={(e) => setCorreoFiltro(e.target.value)}
            />
          </div>

          <div className="filter-group">
            <span className="sidebar-subtitle-strong">Estado</span>
            <Radio
              name="estadoCliente"
              label="Online"
              value="ONLINE"
              checked={estadoFiltro === "ONLINE"}
              onChange={(e) => setEstadoFiltro(e.target.value)}
            />
            <Radio
              name="estadoCliente"
              label="Offline"
              value="OFFLINE"
              checked={estadoFiltro === "OFFLINE"}
              onChange={(e) => setEstadoFiltro(e.target.value)}
            />
            <Radio
              name="estadoCliente"
              label="Disabled"
              value="DISABLED"
              checked={estadoFiltro === "DISABLED"}
              onChange={(e) => setEstadoFiltro(e.target.value)}
            />
          </div>

          <SidebarActions 
            onFilter={handleFilter}
            onClean={handleCleanFilters}
          />
        </div>
      </aside>

      {/* Contenido principal */}
      <section className="contenido">
        <div className="content-header">
          <h4>Gestión de clientes</h4>
          <ButtonAdd
            icon={plus}
            label="Agregar cliente"
            onClick={() => setIsModalOpen(true)}
          />
        </div>

        {/* Tabla de clientes */}
        {loading ? (
          <LoadingOverlay text="Cargando clientes..." />
        ) : (
          <>
            <Table headers={headers} data={currentClientes} />
            <Pagination
              totalItems={clientes.length}
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
              <h3 className="modal-title">Agregar cliente</h3>
              <label htmlFor="fileInput" className="file-label">
                Agregar archivo
              </label>
              <input
                type="file"
                id="fileInput"
                className="file-input"
                onChange={handleFileChange}
              />
            </div>

            <div className="file-name">
              {archivo ? archivo.name : "Ningún archivo seleccionado"}
            </div>

            <div className="modal-body">
              <label htmlFor="nombreModal">Nombre</label>
              <Input
                id="nombreModal"
                placeholder="Escribe el nombre"
                value={nombre}
                onChange={(e) => setNombre(e.target.value)}
                disabled={!!archivo}
              />

              <label htmlFor="correoModal">Correo</label>
              <Input
                id="correoModal"
                placeholder="Escribe el correo electrónico"
                value={correo}
                onChange={(e) => setCorreo(e.target.value)}
                disabled={!!archivo}
              />
            </div>

            <div className="modal-footer">
              <button
                className="btn red"
                onClick={() => setIsModalOpen(false)}
              >
                Cancelar
              </button>
              <button className="btn green" onClick={handleAdd}>
                Agregar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
