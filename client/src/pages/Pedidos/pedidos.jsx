import React, { useState, useEffect } from "react";
import "./pedidos.scss";
import { ButtonAdd, Input, DateTimeInline, Dropdown, Table, SidebarActions, Notification, LoadingOverlay, Pagination, RemoveFileButton, Dropdown3  } from "../../components/UI/ui";
import { listarPedidos  } from "../../services/pedidoService";
import { importarPedidos, importarPedidosLista } from "../../services/generalService";
import { listarClientes } from "../../services/clienteService";
import { listarAeropuertos } from "../../services/aeropuertoService";
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
  // Nuevos estados del modal manual
  const [clientes, setClientes] = useState([]);
  const [aeropuertos, setAeropuertos] = useState([]);

  const [fecha, setFecha] = useState("");  // aaaammdd
  const [hora, setHora] = useState("");   // hh
  const [minuto, setMinuto] = useState(""); // mm
  const [cantidad, setCantidad] = useState(""); // ###

  const [selectedCliente, setSelectedCliente] = useState(null);
  const [selectedDestino, setSelectedDestino] = useState(null);
  
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
    const fetchData = async () => {
      try {
        const pedidosData = await listarPedidos();
        setPedidos(pedidosData.dtos || []);
        setPedidosOriginales(pedidosData.dtos || []);

        const clientesData = await listarClientes();
        setClientes(clientesData.dtos || []);

        const aeropuertosData = await listarAeropuertos();
        setAeropuertos(aeropuertosData.dtos || []);

      } catch (err) {
        console.error(err);
        showNotification("danger", "Error al cargar datos");
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);


  // Tabla
  const headers = [
    { label: "Código", key: "codigo" },
    { label: "Cliente", key: "codCliente" },
    { label: "Fecha de generación", key: "fechaHoraGeneracion" },
    { label: "Fecha de expiración", key: "fechaHoraExpiracion" },
    { label: "Destino", key: "codDestino" },
    { label: "Cantidad solicitada", key: "cantidadSolicitada" },
    //{ label: "Acciones", key: "acciones" },
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
      setSelectedCliente(null);
      setSelectedDestino(null);
      setFecha("");
      setHora("");
      setMinuto("");
      setCantidad("");
    }
  };

  const handleAdd = async () => {
    try {
      setProcessing(true);

      // 1. SI HAY ARCHIVO → usar importarPedidos()
      if (archivo) {
        if (archivo.name !== "Pedidos.txt") {
          showNotification("warning", "El archivo debe llamarse exactamente 'Pedidos.txt'.");
          return;
        }

        const fechaInicioUTC = convertirLocalAUTCString(fechaArchivoFechaI, fechaArchivoHoraI);
        const fechaFinUTC = convertirLocalAUTCString(fechaArchivoFechaF, fechaArchivoHoraF);

        await importarPedidos(archivo, fechaInicioUTC, fechaFinUTC);

        showNotification("success", "Pedidos importados desde archivo correctamente");
      
      } else {
        // 2. NO HAY ARCHIVO → pedido manual → usar importarPedidosLista()

        // Verificar campos obligatorios
        if (!selectedCliente || !selectedDestino || !fecha || !hora || !minuto || !cantidad) {
          showNotification("warning", "Completa todos los campos para generar un pedido manual.");
          return;
        }

        const fechaGeneracionUTC = convertirManualAUTC(
          fecha,
          hora,
          minuto
        );

        const dto = {
          codigo: obtenerSiguienteIdPedido(),
          codCliente: selectedCliente.codigo,
          codDestino: selectedDestino.codigo,
          fechaHoraGeneracion: fechaGeneracionUTC,
          cantidadSolicitada: Number(cantidad)
        };

        console.log(dto);
        await importarPedidosLista([dto]);

        showNotification("success", "Pedido manual registrado correctamente");
      }

      // Recargar tabla
      const data = await listarPedidos();
      setPedidos(data.dtos || []);
      setPedidosOriginales(data.dtos || []);

      // Cerrar modal
      setIsModalOpen(false);

      // Resetear
      setArchivo(null);

    } catch (error) {
      console.error(error);
      showNotification("danger", "Error al agregar pedido");
    } finally {
      setProcessing(false);
    }
  };


  function obtenerSiguienteIdPedido() {
    if (pedidosOriginales.length < 1) return "00000001";

    const idsNumericos = pedidosOriginales.map(p => {
      if (!p.codigo) return 0;

      // Últimos 8 caracteres del código
      const ultimos8 = p.codigo.slice(-8);

      const num = parseInt(ultimos8, 10);
      return isNaN(num) ? 0 : num;
    });

    const maxId = Math.max(...idsNumericos);
    return String(maxId + 1).padStart(8, "0"); // → siempre 8 dígitos
  }



  function generarCodigoPedido() {
    if (!selectedCliente || !selectedDestino || !fecha || !hora || !minuto || !cantidad) {
      return "";
    }

    return (
      obtenerSiguienteIdPedido() +
      "-" +
      fecha +
      "-" +
      hora +
      "-" +
      minuto +
      "-" +
      selectedDestino.codigo +
      "-" +
      cantidad.padStart(3, "0") +
      "-" +
      selectedCliente.codigo.padStart(7, "0")
    );
  }


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
    const fechaString = fechaDDMMYYYY + " " + horaHHmm+":00";
    console.log(fechaString)
    return fechaString;
  }

  function convertirManualAUTC(fechaAAAAMMDD, horaHH, minutoMM) {
    if (!fechaAAAAMMDD || !horaHH || !minutoMM) return "";

    const anio = fechaAAAAMMDD.substring(0, 4);
    const mes = fechaAAAAMMDD.substring(4, 6);
    const dia = fechaAAAAMMDD.substring(6, 8);

    const fechaLocal = new Date(anio, mes - 1, dia, horaHH, minutoMM);

    const fechaString = fechaAAAAMMDD + " " + horaHH+":"+minutoMM+":00";
    console.log(fechaString)
    return fechaString;  // yyyy-MM-ddTHH:mm:ssZ
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
              <input type="file" id="fileInput" className="file-input" onChange={handleFileChange} disabled={generarCodigoPedido() !== ""}/>
            </div>

            <div className="file-name">
              {archivo ? archivo.name : "Ningún archivo seleccionado"}
              {archivo && (
                <RemoveFileButton onClick={() => setArchivo(null)} />
              )}
            </div>


            <div className="modal-body">
              {/* FECHA */}
              <label>Fecha (AAAAMMDD)</label>
              <Input
                placeholder="20250102"
                value={fecha}
                onChange={(e) => setFecha(e.target.value)}
                disabled={!!archivo}
              />

              {/* HORA */}
              <label>Hora (HH)</label>
              <Input
                placeholder="01"
                value={hora}
                onChange={(e) => setHora(e.target.value)}
                disabled={!!archivo}
              />

              {/* MINUTO */}
              <label>Minuto (MM)</label>
              <Input
                placeholder="38"
                value={minuto}
                onChange={(e) => setMinuto(e.target.value)}
                disabled={!!archivo}
              />

              {/* CANTIDAD */}
              <label>Cantidad (###)</label>
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
