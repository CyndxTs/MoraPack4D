import React, { useState, useEffect } from "react";
import "./pedidos.scss";
import { ButtonAdd, Input, DateTimeInline, Dropdown, Table, SidebarActions, Notification, LoadingOverlay, Pagination, RemoveFileButton, Dropdown3  } from "../../components/UI/ui";
import { listarPedidos  } from "../../services/pedidoService";
import { importarPedidos, importarPedidosLista } from "../../services/generalService";
import { listarClientes } from "../../services/clienteService";
import { listarAeropuertos } from "../../services/aeropuertoService";
import plus from '../../assets/icons/plus.svg';
import hideIcon from '../../assets/icons/hide-sidebar.png';


export default function Pedidos() {
  const [collapsed, setCollapsed] = useState(false);

  // --- Filtros ---
  const [codigoFiltro, setCodigoFiltro] = useState("");
  const [ordenFecha, setOrdenFecha] = useState("");

  // --- Modal ---
  const [isModalOpen, setIsModalOpen] = useState(false);

  // Datos base
  const [clientes, setClientes] = useState([]);
  const [aeropuertos, setAeropuertos] = useState([]);

  // Para pedido manual
  const [fecha, setFecha] = useState("");   
  const [hora, setHora] = useState("");    
  const [cantidad, setCantidad] = useState("");
  const [selectedCliente, setSelectedCliente] = useState(null);
  const [selectedDestino, setSelectedDestino] = useState(null);

  // Para archivo
  const [archivo, setArchivo] = useState(null);
  const [fechaArchivoFechaI, setFechaArchivoFechaI] = useState("");
  const [fechaArchivoHoraI, setFechaArchivoHoraI] = useState("");
  const [fechaArchivoFechaF, setFechaArchivoFechaF] = useState("");
  const [fechaArchivoHoraF, setFechaArchivoHoraF] = useState("");

  // Tabla
  const [pedidos, setPedidos] = useState([]);
  const [pedidosOriginales, setPedidosOriginales] = useState([]);

  // Filtros tabla
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);
  const [notification, setNotification] = useState(null);

  const showNotification = (type, message) => {
    setNotification({ type, message });
    setTimeout(() => setNotification(null), 5000);
  };

  // =============================
  // CARGA INICIAL
  // =============================
  useEffect(() => {
    const fetchData = async () => {
      try {
        const pedidosData = await listarPedidos();
        setPedidos(pedidosData.dtos || []);
        setPedidosOriginales(pedidosData.dtos || []);

        const clientesData = await listarClientes();
        setClientes(clientesData.dtos || []);

        const aeropuertosData = await listarAeropuertos();
        setAeropuertos(aeropuertosData.dtos || []);
      } catch {
        showNotification("danger", "Error al cargar datos");
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  // =============================
  // HEADERS TABLA
  // =============================
  const headers = [
    { label: "Código", key: "codigo" },
    { label: "Cliente", key: "codCliente" },
    { label: "Fecha de generación (UTC)", key: "fechaHoraGeneracion" },
    { label: "Fecha de expiración (UTC)", key: "fechaHoraExpiracion" },
    { label: "Destino", key: "codDestino" },
    { label: "Cantidad solicitada", key: "cantidadSolicitada" }
  ];

  // =============================
  // LIMPIAR ARCHIVO
  // =============================
  const resetArchivo = () => {
    setArchivo(null);
    setFechaArchivoFechaI("");
    setFechaArchivoHoraI("");
    setFechaArchivoFechaF("");
    setFechaArchivoHoraF("");
  };

  const resetDatos = () => {
    setFecha("");
    setHora("");
    setCantidad("");
    setSelectedCliente(null);
    setSelectedDestino(null);
  };

  const handleFileChange = (e) => {
    if (e.target.files.length > 0) setArchivo(e.target.files[0]);
    else resetArchivo();
  };

  // =============================
  // FORMATEO FECHAS — FORMATO FINAL
  //   → "YYYYMMDD HH:MM:00"
  // =============================

  function unirFechaHora(fechaDateInput, horaHHmm) {
    if (!fechaDateInput || !horaHHmm) return null;

    return `${fechaDateInput} ${horaHHmm}:00`; 
  }


  // =============================
  // OBTENER ID SIGUIENTE
  // =============================
  function obtenerSiguienteIdPedido() {
    if (pedidosOriginales.length < 1) return "00000001";

    const ids = pedidosOriginales.map(p => {
      const ult8 = p.codigo?.slice(-8) || "0";
      const num = parseInt(ult8);
      return isNaN(num) ? 0 : num;
    });

    const maxId = Math.max(...ids);
    return String(maxId + 1).padStart(8, "0");
  }

  // =============================
  // GENERAR CÓDIGO AUTOMÁTICO
  // =============================
  function generarCodigoPedido() {
    if (!selectedCliente || !selectedDestino || !fecha || !hora || !cantidad)
      return "";

    return (
      obtenerSiguienteIdPedido() +
      "-" + formatearFechaInput(fecha) +
      "-" + formatearHoraeInput(hora) +
      "-" + cantidad.padStart(3, "0") +
      "-" + selectedDestino.codigo +
      "-" + selectedCliente.codigo.padStart(7, "0")
    );
  }

  function formatearFechaInput(fecha) {
    if (!fecha) return "";
    return fecha.replace(/-/g, ""); 
  }

  function formatearHoraeInput(hora) {
    if (!hora) return "";
    return hora.replace(/:/g, "-"); 
  }

  // =============================
  // AGREGAR PEDIDO
  // =============================
  const handleAdd = async () => {
    try {
      setProcessing(true);

      // --- CASO 1: ARCHIVO ---
      if (archivo) {
        if (archivo.name !== "Pedidos.txt") {
          showNotification("warning", "El archivo debe llamarse 'Pedidos.txt'.");
          return;
        }

        const fechaInicio = unirFechaHora(fechaArchivoFechaI, fechaArchivoHoraI);
        const fechaFin = unirFechaHora(fechaArchivoFechaF, fechaArchivoHoraF);
        
        console.log(archivo);
        console.log(fechaInicio);
        console.log(fechaFin);

        await importarPedidos(archivo, fechaInicio, fechaFin);
        showNotification("success", "Pedidos importados correctamente");
      }

      // --- CASO 2: MANUAL ---
      else {
        if (!selectedCliente || !selectedDestino || !fecha || !hora || !cantidad) {
          showNotification("warning", "Completa todos los campos del pedido manual.");
          return;
        }

        const fechaGeneracion = unirFechaHora(fecha, hora);

        const dto = {
          codigo: selectedDestino.codigo + obtenerSiguienteIdPedido().toString().padStart(9, "0"),
          codCliente: selectedCliente.codigo,
          codDestino: selectedDestino.codigo,
          fechaHoraGeneracion: fechaGeneracion,
          cantidadSolicitada: Number(cantidad),
          fechaHoraExpiracion: null,       
          lotesPorRuta: []
        };

        const dtos = [];
        dtos.push(dto);

        console.log("DTO generado:", dto);
        console.log("Lista DTOS:", dtos);

        await importarPedidosLista(dtos);

        showNotification("success", "Pedido manual registrado correctamente");
        resetDatos();
      }

      // --- Recargar tabla ---
      const data = await listarPedidos();
      setPedidos(data.dtos || []);
      setPedidosOriginales(data.dtos || []);

      setIsModalOpen(false);
      setArchivo(null);
    } catch {
      showNotification("danger", "Error al agregar pedido");
    } finally {
      setProcessing(false);
    }
  };

  // =============================
  // FILTROS
  // =============================
  const handleFilter = () => {
    let lista = [...pedidosOriginales];

    if (codigoFiltro.trim())
      lista = lista.filter(p => p.codigo.toLowerCase().includes(codigoFiltro.toLowerCase()));

    if (ordenFecha) {
      lista.sort((a, b) => {
        const fechaA = parseFecha(a.fechaHoraGeneracion);
        const fechaB = parseFecha(b.fechaHoraGeneracion);
        return fechaA - fechaB;
      });
      if (ordenFecha === "descendente") lista.reverse();
    }

    setPedidos(lista);
    setCurrentPage(1);
    showNotification("success", "Filtro aplicado");
  };

  const handleCleanFilters = () => {
    setCodigoFiltro("");
    setOrdenFecha("");
    setPedidos(pedidosOriginales);
    setCurrentPage(1);
  };

  function parseFecha(fechaStr) {
    if (!fechaStr) return new Date(0); // para evitar errores

    const [fecha, hora] = fechaStr.split(' ');
    const [dd, mm, yyyy] = fecha.split('/');
    const [HH, MM] = hora.split(':');

    return new Date(yyyy, mm - 1, dd, HH, MM);
  }


  // =============================
  // PAGINACIÓN
  // =============================
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 20;
  const indexOfLast = currentPage * itemsPerPage;
  const indexOfFirst = indexOfLast - itemsPerPage;
  const curretPedidos = pedidos.slice(indexOfFirst, indexOfLast);

  return (
    <div className="page">

      {(loading || processing) && (
        <LoadingOverlay
          text={processing ? "Procesando pedidos..." : "Cargando pedidos..."}
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
              <input type="file" id="fileInput" className="file-input" onChange={handleFileChange} disabled={generarCodigoPedido() !== ""}/>
            </div>

            <div className="file-name">
              {archivo ? archivo.name : "Ningún archivo seleccionado"}
              {archivo && (
                <RemoveFileButton onClick={() => setArchivo(null)} />
              )}
            </div>
            
            <div className="modal-body">
              {archivo && (
              <>
                <label>Fecha y hora de inicio (UTC)</label>
                <DateTimeInline
                  dateValue={fechaArchivoFechaI}
                  timeValue={fechaArchivoHoraI}
                  onDateChange={(e) => setFechaArchivoFechaI(e.target.value)}
                  onTimeChange={(e) => setFechaArchivoHoraI(e.target.value)}
                />
                <label>Fecha y hora de fin (UTC)</label>
                <DateTimeInline
                  dateValue={fechaArchivoFechaF}
                  timeValue={fechaArchivoHoraF}
                  onDateChange={(e) => setFechaArchivoFechaF(e.target.value)}
                  onTimeChange={(e) => setFechaArchivoHoraF(e.target.value)}
                />
              </>
              )}

              <label>Fecha y hora de generación (UTC)</label>
              <DateTimeInline
                dateValue={fecha}
                timeValue={hora}
                onDateChange={(e) => setFecha(e.target.value)}
                onTimeChange={(e) => setHora(e.target.value)}
                disabled={!!archivo}
              />

              {/* CANTIDAD */}
              <label>Cantidad</label>
              <Input
                placeholder="006"
                value={cantidad}
                onChange={(e) => setCantidad(e.target.value)}
                disabled={!!archivo}
              />

              {/* DESTINO */}
              <label>Destino</label>
              <Dropdown3
                placeholder="Seleccionar aeropuerto..."
                options={aeropuertos.map(a => ({
                  label: `${a.codigo} - ${a.ciudad} - ${a.pais}`,
                  value: a
                }))}
                value={selectedDestino}
                onSelect={(item) => setSelectedDestino(item)}
                disabled={!!archivo}
              />

              {/* CLIENTE */}
              <label>Cliente</label>
              <Dropdown3
                placeholder="Seleccionar cliente..."
                options={clientes.map(c => ({
                  label: `${c.codigo} - ${c.nombre}`,
                  value: c
                }))}
                value={selectedCliente}
                onSelect={(item) => setSelectedCliente(item)}
                disabled={!!archivo}
              />

              {/* MOSTRAR RESULTADO */}
              <label>Código generado</label>
              <Input
                value={generarCodigoPedido()}
                disabled
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
