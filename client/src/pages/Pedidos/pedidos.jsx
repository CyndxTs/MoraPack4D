import React, { useState, useEffect } from "react";
import "./pedidos.scss";
import { ButtonAdd, Input, Checkbox, Dropdown, Table, SidebarActions, Notification, LoadingOverlay, Pagination, RemoveFileButton  } from "../../components/UI/ui";
import { listarPedidos  } from "../../services/pedidoService";
import plus from '../../assets/icons/plus.svg';
import hideIcon from '../../assets/icons/hide-sidebar.png';

export default function Pedidos() {
  const [collapsed, setCollapsed] = useState(false);

  // Filtros
  const [codigoFiltro, setCodigoFiltro] = useState("");
  const [estadoFiltro, setEstadoFiltro] = useState({
    enCurso: false,
    pendiente: false,
    entregado: false,
    cancelado: false
  });
  const [clienteFiltro, setClienteFiltro] = useState("");
  const [destinoFiltro, setDestinoFiltro] = useState("");
  const [ordenFecha, setOrdenFecha] = useState("");
  const [ordenCantidad, setOrdenCantidad] = useState("");

  // Modal y datos
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [archivo, setArchivo] = useState(null);

  const [pedidos, setPedidos] = useState([]);
  const [codigo, setCodigo] = useState("");

  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false); 
  const [notification, setNotification] = useState(null);

  const showNotification = (type, message) => {
    setNotification({ type, message });
    setTimeout(() => setNotification(null), 5000);
  };

  useEffect(() => {
    const fetchPedidos = async () => {
      try {
        const data = await listarPedidos();
        setPedidos(data);
      } catch (err) {
        console.error(err);
        showNotification("danger", "Error al cargar pedidos");
      } finally {
        setLoading(false);
      }
    };
    fetchPedidos();
  }, []);

  // Tabla
  const headers = [
    { label: "Código", key: "codigo" },
    { label: "Cliente", key: "nombreCliente" },
    { label: "Destino", key: "codigoAeropuertoDestino" },
    { label: "Cantidad solicitada", key: "cantidadSolicitada" },
    { label: "Fecha de generación (UTC)", key: "fechaHoraGeneracionUTC" },
    { label: "Acciones", key: "acciones" },
  ];

  const handleFileChange = (e) => {
    if (e.target.files.length > 0) {
      setArchivo(e.target.files[0]);
    } else {
      setArchivo(null);
    }
  };

  const handleAdd = () => {
    console.log({ archivo, codigoFiltro, estadoFiltro, clienteFiltro, vueloFiltro });
    setIsModalOpen(false);
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
  const curretPedidos = pedidos.slice(indexOfFirst, indexOfLast);

  return (
    <div className="page">

      {(loading || processing) && (
        <LoadingOverlay
          text={processing ? "Cargando pedidos..." : "Cargando pedidos..."}
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
                <Input placeholder="Escribir..." value={codigoFiltro} onChange={e => setCodigoFiltro(e.target.value)} />
              </div>

              <div className="filter-group">
                <span className="sidebar-subtitle-strong">Cliente</span>
                <Input placeholder="Escribir..." value={clienteFiltro} onChange={e => setClienteFiltro(e.target.value)} />
              </div>

              <div className="filter-group">
                <span className="sidebar-subtitle-strong">Destino</span>
                <Input placeholder="Escribir..." value={destinoFiltro} onChange={e => setDestinoFiltro(e.target.value)} />
              </div>

              <div className="filter-group">
                <span className="sidebar-subtitle-strong">Orden de cantidad solicitada</span>
                <Dropdown
                  placeholder="Seleccionar..."
                  value={ordenCantidad} // hace que el valor dependa del estado
                  options={[
                    { label: "Ascendente", value: "ascendente" },
                    { label: "Descendente", value: "descendente" },
                  ]}
                  onSelect={(val) => setOrdenCantidad(val)}
                />
              </div>

              <div className="filter-group">
                <span className="sidebar-subtitle-strong">Orden de fecha de generación</span>
                <Dropdown
                  placeholder="Seleccionar..."
                  value={ordenFecha} // hace que el valor dependa del estado
                  options={[
                    { label: "Ascendente", value: "ascendente" },
                    { label: "Descendente", value: "descendente" },
                  ]}
                  onSelect={(val) => setOrdenFecha(val)}
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
          <h4>Gestión de pedidos</h4>
          <ButtonAdd icon={plus} label="Agregar pedido" onClick={() => setIsModalOpen(true)} />
        </div>

        {loading ? (
          <LoadingOverlay text="Cargando pedidos..." />
        ) : (
          <>
            <Table
              headers={headers}
              data={curretPedidos}
            />
            <Pagination
              totalItems={pedidos.length}
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
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">Agregar pedido</h3>
              <label htmlFor="fileInput" className="file-label">Agregar archivo</label>
              <input type="file" id="fileInput" className="file-input" onChange={handleFileChange} />
            </div>

            <div className="file-name">{archivo || "Ningún archivo seleccionado"}</div>

            <div className="modal-body">
              <label htmlFor="codigoModal">Código</label>
              <Input id="codigoModal" placeholder="Escribe el código" value={codigo} onChange={e => setCodigo(e.target.value)} />

              <label htmlFor="clienteModal">Cliente</label>
              <Dropdown
                placeholder="Seleccionar..."
                options={[
                  { label: "Ejemplo 1", value: "ejemplo1" },
                  { label: "Ejemplo 2", value: "ejemplo2" }
                ]}
                onSelect={val => setClienteFiltro(val)}
              />

              <label htmlFor="vueloModal">Vuelo</label>
              <Dropdown
                placeholder="Seleccionar..."
                options={[
                  { label: "Ejemplo 1", value: "ejemplo1" },
                  { label: "Ejemplo 2", value: "ejemplo2" }
                ]}
                onSelect={val => setVueloFiltro(val)}
              />
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
