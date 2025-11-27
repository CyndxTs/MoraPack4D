import React, { useState, useEffect } from "react";
import "./pedidos.scss";

import { 
  ButtonAdd, Input, DateTimeInline, Dropdown, Table, SidebarActions, 
  Notification, LoadingOverlay, Pagination, RemoveFileButton, Dropdown3, Radio 
} from "../../components/UI/ui";

import plus from '../../assets/icons/plus.svg';
import hideIcon from '../../assets/icons/hide-sidebar.png';

import { useAppData } from "../../dataProvider";
import { listarPedidos, importarPedido, importarPedidos } from "../../services/pedidoService";

export default function Pedidos() {

  const { loadingData, pedidos, clientes, aeropuertos, setPedidos } = useAppData();

  const [collapsed, setCollapsed] = useState(false);

  // --- Filtros ---
  const [codigoFiltro, setCodigoFiltro] = useState("");
  const [ordenFecha, setOrdenFecha] = useState("");

  // --- Modal ---
  const [isModalOpen, setIsModalOpen] = useState(false);

  // Pedido manual
  const [fecha, setFecha] = useState("");
  const [hora, setHora] = useState("");
  const [cantidad, setCantidad] = useState("");
  const [selectedCliente, setSelectedCliente] = useState(null);
  const [selectedDestino, setSelectedDestino] = useState(null);

  // Archivo
  const [archivo, setArchivo] = useState(null);
  const [fechaArchivoFechaI, setFechaArchivoFechaI] = useState("");
  const [fechaArchivoHoraI, setFechaArchivoHoraI] = useState("");
  const [fechaArchivoFechaF, setFechaArchivoFechaF] = useState("");
  const [fechaArchivoHoraF, setFechaArchivoHoraF] = useState("");
  const [tipoEscenario, setTipoEscenario] = useState("");

  // Tabla y filtros
  const [pedidosOriginales, setPedidosOriginales] = useState([]);
  const [processing, setProcessing] = useState(false);
  const [notification, setNotification] = useState(null);

  // =============================
  // CARGA INICIAL DESDE CONTEXTO
  // =============================
  useEffect(() => {
    if (!loadingData) {
      setPedidosOriginales(pedidos);
    }
  }, [loadingData, pedidos]);

  // =============================
  // NOTIFICACIONES
  // =============================
  const showNotification = (type, message) => {
    setNotification({ type, message });
    setTimeout(() => setNotification(null), 5000);
  };

  // =============================
  // HEADERS
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
  // HELPERS
  // =============================
  function unirFechaHora(f, h) {
    if (!f || !h) return null;
    return `${f}T${h}:00`;
  }

  const formatearFechaInput = (f) => f.replace(/-/g, "");
  const formatearHoraInput = (h) => h.replace(/:/g, "-");

  function generarCodigoPedido() {
    if (!selectedCliente || !selectedDestino || !fecha || !hora || !cantidad)
      return "";

    return (
      "XXXXXXXX" +
      "-" + formatearFechaInput(fecha) +
      "-" + formatearHoraInput(hora) +
      "-" + cantidad.padStart(3, "0") +
      "-" + selectedDestino.codigo +
      "-" + selectedCliente.codigo.padStart(7, "0")
    );
  }

  // =============================
  // AGREGAR
  // =============================
  const handleAdd = async () => {
    try {
      setProcessing(true);

      // --- ARCHIVO ---
      if (archivo) {
        /*if (archivo.name !== "Pedidos.txt") {
          showNotification("warning", "El archivo debe llamarse 'Pedidos.txt'.");
          return;
        }*/

        console.log(fechaArchivoFechaI);
        console.log(fechaArchivoHoraI);
        console.log(fechaArchivoFechaF);
        console.log(fechaArchivoHoraF);

        console.log("Final Inicio:", unirFechaHora(fechaArchivoFechaI, fechaArchivoHoraI));
        console.log("Final Fin:", unirFechaHora(fechaArchivoFechaF, fechaArchivoHoraF));

        const req = {
          tipoEscenario: tipoEscenario,
          fechaHoraInicio: unirFechaHora(fechaArchivoFechaI, fechaArchivoHoraI),
          fechaHoraFin: unirFechaHora(fechaArchivoFechaF, fechaArchivoHoraF)
        };

        console.log(archivo);
        console.log(req);

        const respuesta = await importarPedidos(archivo, req);
        if (respuesta.success) {
          showNotification("success", respuesta.message || "Pedidos importados correctamente");
        } else {
          showNotification("danger", respuesta.message || "Ocurrió un error al importar los pedidos");
        }
      } 

      // --- MANUAL ---
      else {
        if (!selectedCliente || !selectedDestino || !fecha || !hora || !cantidad) {
          showNotification("warning", "Completa todos los campos del pedido manual.");
          return;
        }

        const fechaGeneracion = unirFechaHora(fecha, hora);

        const dto = {
          codigo: null,
          codCliente: selectedCliente.codigo,
          codDestino: selectedDestino.codigo,
          fechaHoraGeneracion: fechaGeneracion,
          cantidadSolicitada: Number(cantidad),
          lotesPorRuta: [],
          tipoEscenario: tipoEscenario
        };

        console.log(dto);
        await importarPedido(dto);

        showNotification("success", "Pedido manual registrado correctamente");
      }

      // Recargar tabla DESDE BACKEND
      const nuevos = await listarPedidos(0,10000);
      setPedidos(nuevos.dtos || []);
      setPedidosOriginales(nuevos.dtos || []);

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

    if (codigoFiltro.trim()) {
      lista = lista.filter(p =>
        p.codigo.toLowerCase().includes(codigoFiltro.toLowerCase())
      );
    }

    if (ordenFecha) {
      lista.sort((a, b) => {
        const fa = new Date(a.fechaHoraGeneracion);
        const fb = new Date(b.fechaHoraGeneracion);
        return ordenFecha === "ascendente" ? fa - fb : fb - fa;
      });
    }

    setPedidos(lista);
    setCurrentPage(1);
  };

  const handleCleanFilters = () => {
    setCodigoFiltro("");
    setOrdenFecha("");
    setPedidos(pedidosOriginales);
    setCurrentPage(1);
  };

  // =============================
  // PAGINACIÓN
  // =============================
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 20;
  const indexOfLast = currentPage * itemsPerPage;
  const indexOfFirst = indexOfLast - itemsPerPage;
  const currentPedidos = pedidos.slice(indexOfFirst, indexOfLast);

  // =============================
  // RENDER
  // =============================
  return (
    <div className="page">

      {(processing) && (
        <LoadingOverlay text="Procesando pedidos..." />
      )}

      {loadingData && (
        <LoadingOverlay text="Cargando pedidos..." />
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
              <Input
                placeholder="Escribir..."
                value={codigoFiltro}
                onChange={(e) => setCodigoFiltro(e.target.value)}
              />
            </div>

            <div className="filter-group">
              <span className="sidebar-subtitle-strong">Orden fecha generación</span>
              <Dropdown
                placeholder="Seleccionar..."
                value={ordenFecha}
                options={[
                  { label: "Ascendente", value: "ascendente" },
                  { label: "Descendente", value: "descendente" },
                ]}
                onSelect={(v) => setOrdenFecha(v)}
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
          <h4>Gestión de pedidos</h4>

          <ButtonAdd
            icon={plus}
            label="Agregar pedido"
            onClick={() => {
              const now = new Date();
              const yyyy = now.getUTCFullYear();
              const mm = String(now.getUTCMonth() + 1).padStart(2, "0");
              const dd = String(now.getUTCDate()).padStart(2, "0");
              const HH = String(now.getUTCHours()).padStart(2, "0");
              const MM = String(now.getUTCMinutes()).padStart(2, "0");

              setFecha(`${yyyy}-${mm}-${dd}`);
              setHora(`${HH}:${MM}`);

              setIsModalOpen(true);
            }}
          />
        </div>

        <Table headers={headers} data={currentPedidos} />

        <Pagination
          totalItems={pedidos.length}
          itemsPerPage={itemsPerPage}
          currentPage={currentPage}
          onPageChange={setCurrentPage}
        />
      </section>

      {/* Modal */}
      {isModalOpen && (
        <div className="modal" onClick={() => setIsModalOpen(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">Agregar pedido</h3>
              <label htmlFor="fileInput" className="file-label">Agregar archivo</label>
              <input
                type="file"
                id="fileInput"
                className="file-input"
                onChange={(e) => setArchivo(e.target.files[0])}
                disabled={generarCodigoPedido() !== ""}
              />
            </div>

            <div className="file-name">
              {archivo ? archivo.name : "Ningún archivo seleccionado"}
              {archivo && <RemoveFileButton onClick={() => setArchivo(null)} />}
            </div>

            <div className="modal-body">
              {archivo && (
                <>
                  <label>Fecha y hora inicio (UTC)</label>
                  <DateTimeInline
                    dateValue={fechaArchivoFechaI}
                    timeValue={fechaArchivoHoraI}
                    onDateChange={(e) => setFechaArchivoFechaI(e.target.value)}
                    onTimeChange={(e) => setFechaArchivoHoraI(e.target.value)}
                  />

                  <label>Fecha y hora fin (UTC)</label>
                  <DateTimeInline
                    dateValue={fechaArchivoFechaF}
                    timeValue={fechaArchivoHoraF}
                    onDateChange={(e) => setFechaArchivoFechaF(e.target.value)}
                    onTimeChange={(e) => setFechaArchivoHoraF(e.target.value)}
                  />
                </>
              )}

              <label>Tipo de escenario</label>
                <Radio
                  name="tipoEscenario"
                  label="OPERACION"
                  value="OPERACION"
                  checked={tipoEscenario === "OPERACION"}
                  onChange={(e) => setTipoEscenario(e.target.value)}
                />
                <Radio
                  name="tipoEscenario"
                  label="SIMLULACION"
                  value="SIMLULACION"
                  checked={tipoEscenario === "SIMLULACION"}
                  onChange={(e) => setTipoEscenario(e.target.value)}
                />

              <label>Fecha y hora generación (UTC)</label>
              <DateTimeInline
                dateValue={fecha}
                timeValue={hora}
                onDateChange={(e) => setFecha(e.target.value)}
                onTimeChange={(e) => setHora(e.target.value)}
                disabled={!!archivo}
              />

              <label>Cantidad</label>
              <Input
                value={cantidad}
                onChange={(e) => setCantidad(e.target.value)}
                disabled={!!archivo}
              />

              <label>Destino</label>
              <Dropdown3
                placeholder="Seleccionar aeropuerto..."
                options={aeropuertos.map(a => ({
                  label: `${a.codigo} - ${a.ciudad} - ${a.pais}`,
                  value: a
                }))}
                value={selectedDestino}
                onSelect={(a) => setSelectedDestino(a)}
                disabled={!!archivo}
              />

              <label>Cliente</label>
              <Dropdown3
                placeholder="Seleccionar cliente..."
                options={clientes.map(c => ({
                  label: `${c.codigo} - ${c.nombre}`,
                  value: c
                }))}
                value={selectedCliente}
                onSelect={(c) => setSelectedCliente(c)}
                disabled={!!archivo}
              />

              <label>Código generado</label>
              <Input value={generarCodigoPedido()} disabled />
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

