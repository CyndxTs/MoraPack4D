import React, { useState, useEffect } from "react";
import "./pedidos.scss";
import { ButtonAdd, Input, DateTimeInline, Dropdown, Table, SidebarActions, Notification, LoadingOverlay, Pagination, RemoveFileButton  } from "../../components/UI/ui";
import { listarPedidos  } from "../../services/pedidoService";
import { importarPedidos } from "../../services/generalService";
import { formatISOToDDMMYYYY, parseDDMMYYYYToDate } from "../../services/utils/utils";
import plus from '../../assets/icons/plus.svg';
import hideIcon from '../../assets/icons/hide-sidebar.png';

export default function Pedidos() {
  const [collapsed, setCollapsed] = useState(false);

  // Filtros
  const [codigoFiltro, setCodigoFiltro] = useState("");
  const [ordenFecha, setOrdenFecha] = useState("");

  // Modal y datos
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [archivo, setArchivo] = useState(null);
  const [fechaArchivoFechaI, setFechaArchivoFechaI] = useState("");
  const [fechaArchivoHoraI, setFechaArchivoHoraI] = useState("");
  const [fechaArchivoFechaF, setFechaArchivoFechaF] = useState("");
  const [fechaArchivoHoraF, setFechaArchivoHoraF] = useState("");

  const [pedidos, setPedidos] = useState([]);
  const [pedidosOriginales, setPedidosOriginales] = useState([]);

  const [codigo, setCodigo] = useState("");
  const [cliente, setCliente] = useState("");
  const [destino, setDestino] = useState("");

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

        setPedidos(data.dtos || []);
        setPedidosOriginales(data.dtos || []);

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
    { label: "Cliente", key: "codCliente" },
    { label: "Fecha de generación", key: "fechaHoraGeneracion" },
    { label: "Fecha de expiración", key: "fechaHoraExpiracion" },
    { label: "Destino", key: "codDestino" },
    { label: "Cantidad solicitada", key: "cantidadSolicitada" },
    { label: "Acciones", key: "acciones" },
  ];

  const handleFileChange = (e) => {
    if (e.target.files.length > 0) {
      setArchivo(e.target.files[0]);
    } else {
      setArchivo(null);
      setFechaArchivoFechaI("");
      setFechaArchivoHoraI("");
      setFechaArchivoFechaF("");
      setFechaArchivoHoraF("");
    }
  };

  const handleAdd = async () => {
    if (!archivo) {
      showNotification("warning", "Selecciona un archivo antes de continuar.");
      return;
    }

    if (archivo.name !== "Pedidos.txt") {
      showNotification("warning", "El archivo debe llamarse exactamente 'Pedidos.txt'.");
      return;
    }

    // Convertir a UTC los valores seleccionados
    const fechaInicioUTC = convertirLocalAUTCString(
      fechaArchivoFechaI,
      fechaArchivoHoraI
    );

    // Si no quiere rango → mandar strings vacías
    const fechaFinUTC = convertirLocalAUTCString(
      fechaArchivoFechaF,
      fechaArchivoHoraF
    );

    try {
      setProcessing(true);

      await importarPedidos(archivo, fechaInicioUTC, fechaFinUTC);

      showNotification("success", "Pedidos importados correctamente");

      const data = await listarPedidos();

      setPedidos(data.dtos || []);
      setPedidosOriginales(data.dtos || []);

      setIsModalOpen(false);
      setArchivo(null);

    } catch (error) {
      console.error(error);
      showNotification("danger", "Error al importar pedidos");
    } finally {
      setProcessing(false);
    }
  };


  //Filtros
  const handleFilter = () => {
    let lista = [...pedidosOriginales];

    // --- FILTRO POR CÓDIGO ---
    if (codigoFiltro.trim()) {
      lista = lista.filter(p =>
        p.codigo.toLowerCase().includes(codigoFiltro.toLowerCase())
      );
    }

    // --- ORDENAR POR FECHA ---
    if (ordenFecha) {
      lista.sort((a, b) => {
        const f1 = parseFechaHoraDDMMYYYY(a.fechaHoraGeneracion);
        const f2 = parseFechaHoraDDMMYYYY(b.fechaHoraGeneracion);

        return ordenFecha === "ascendente" ? f1 - f2 : f2 - f1;
      });
    }


    setPedidos(lista);
    setCurrentPage(1);
    showNotification("success", "Filtro aplicado correctamente");
  };


  //Limpiar filtros
  const handleCleanFilters = () => {
    setCodigoFiltro("");
    setOrdenFecha("");

    setPedidos(pedidosOriginales);
    setCurrentPage(1);
    showNotification("info", "Filtros limpiados");
  };


  // --- Paginación ---
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 20;
  const indexOfLast = currentPage * itemsPerPage;
  const indexOfFirst = indexOfLast - itemsPerPage;
  const curretPedidos = pedidos.slice(indexOfFirst, indexOfLast);

  function parseFechaHoraDDMMYYYY(fechaHora) {
    if (!fechaHora) return null;

    const [fecha, hora] = fechaHora.split(" ");
    const [dia, mes, anio] = fecha.split("/");
    const [hh, mm] = hora.split(":");

    return new Date(`${anio}-${mes}-${dia}T${hh}:${mm}:00`);
  }

  function convertirLocalAUTCString(fechaDDMMYYYY, horaHHmm) {
    if (!fechaDDMMYYYY || !horaHHmm) return "";

    const [dia, mes, anio] = fechaDDMMYYYY.split("-");
    const [hora, minuto] = horaHHmm.split(":");

    // Construimos una fecha local (sin timezone)
    const fechaLocal = new Date(anio, mes - 1, dia, hora, minuto);

    // Convertimos a UTC ISO → yyyy-MM-ddTHH:mm:ssZ
    return fechaLocal.toISOString();
  }



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

            <div className="file-name">
              {archivo ? archivo.name : "Ningún archivo seleccionado"}
              {archivo && (
                <RemoveFileButton onClick={() => setArchivo(null)} />
              )}
            </div>


            <div className="modal-body">
              <label htmlFor="codigoModal">Código</label>
              <Input id="codigoModal" placeholder="Escribe el código" value={codigo} onChange={e => setCodigo(e.target.value)} disabled={!!archivo}/>

              <label htmlFor="clienteModal">Cliente</label>
              <Input id="clienteModal" placeholder="Escribe el cliente" value={cliente} onChange={e => setCliente(e.target.value)} disabled={!!archivo}/>

              <label htmlFor="destinoModal">Destino</label>
              <Input id="destinoModal" placeholder="Escribe el destino" value={destino} onChange={e => setDestino(e.target.value)} disabled={!!archivo}/>

              {archivo && (
                <>
                  <label>Fecha y hora de inicio</label>
                  <DateTimeInline
                    dateValue={fechaArchivoFechaI}
                    timeValue={fechaArchivoHoraI}
                    onDateChange={(e) => setFechaArchivoFechaI(e.target.value)}
                    onTimeChange={(e) => setFechaArchivoHoraI(e.target.value)}
                  />
                  <label>Fecha y hora de fin</label>
                  <DateTimeInline
                    dateValue={fechaArchivoFechaF}
                    timeValue={fechaArchivoHoraF}
                    onDateChange={(e) => setFechaArchivoFechaF(e.target.value)}
                    onTimeChange={(e) => setFechaArchivoHoraF(e.target.value)}
                  />
                </>
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
