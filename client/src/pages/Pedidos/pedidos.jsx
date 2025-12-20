import React, { useState, useEffect } from "react";
import "./pedidos.scss";

import { 
  ButtonAdd, Input, DateTimeInline, Dropdown, Table, SidebarActions, 
  Notification, LoadingOverlay, Pagination, RemoveFileButton, Dropdown3, Radio , DateTimeColumn
} from "../../components/UI/ui";

import plus from '../../assets/icons/plus.svg';
import hideIcon from '../../assets/icons/hide-sidebar.png';

import { useAppData } from "../../dataProvider";
import { listarPedidos, importarPedido, importarPedidos, filtrarPedidos, iniciarImportacion } from "../../services/pedidoService";
import { notifyNewOrder, onEvent, initOperationManager } from "../../services/operationManager";

export default function Pedidos() {

  const { clientes, aeropuertos } = useAppData();

  const [collapsed, setCollapsed] = useState(true);

  // --- Filtros ---
  const [codigoFiltro, setCodigoFiltro] = useState("");
  const [filtroFecha, setFiltroFecha] = useState("");
  const [filtroHora, setFiltroHora] = useState("");
  const [tipoEscenarioFiltro, setTipoEscenarioFiltro] = useState("");
  const [codDestinoFiltro, setCodDestinoFiltro] = useState("");

  // --- Modal ---
  const [isModalOpen, setIsModalOpen] = useState(false);

  // Pedido manual
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
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);
  const [notification, setNotification] = useState(null);
  const [importToken, setImportToken] = useState(null);

  const [pedidos,setPedidos]=useState([]);
  const [pedidosOriginales,setPedidosOriginales]=useState([]);

  // =============================
  // CARGA INICIAL DESDE CONTEXTO
  // =============================


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
  const statusColors = {
    SIMULACION: "#0B6623",   // verde oscuro
    OPERACION: "#B5651D",    // naranja marrón
  };

  const headers = [
    { label: "Código", key: "codigo" },
    { label: "Tipo de escenario", key: "tipoEscenario", useStatusColors: true },
    { label: "Cliente", key: "codCliente" },
    { label: "Fecha de generación (UTC)", key: "fechaHoraGeneracion" },
    { label: "¿Está planificado?", key: "fueAtendido" },
    { label: "Fecha de expiración (UTC)", key: "fechaHoraExpiracion" },
    { label: "Destino", key: "codDestino" },
    { label: "Cantidad solicitada", key: "cantidadSolicitada" },
  ];

  // =============================
  // HELPERS
  // =============================
  function unirFechaHoraUTC(f, h) {
    if (!f || !h) return null;

    // Construir fecha base: "yyyy-MM-dd HH:mm:00"
    const base = new Date(`${f}T${h}:00`);

    // Sumar 5 horas
    base.setHours(base.getHours() + 5);

    // Formatear nuevamente a yyyy-MM-dd HH:mm:ss
    const yyyy = base.getFullYear();
    const MM = String(base.getMonth() + 1).padStart(2, "0");
    const dd = String(base.getDate()).padStart(2, "0");

    const HH = String(base.getHours()).padStart(2, "0");
    const mm = String(base.getMinutes()).padStart(2, "0");
    const ss = String(base.getSeconds()).padStart(2, "0");

    return `${yyyy}-${MM}-${dd} ${HH}:${mm}:${ss}`;
  }

  function obtenerFechaHoraActual() {
    const ahora = new Date();

    const yyyy = ahora.getFullYear();
    const MM = String(ahora.getMonth() + 1).padStart(2, "0");
    const dd = String(ahora.getDate()).padStart(2, "0");

    const HH = String(ahora.getHours()).padStart(2, "0");
    const mm = String(ahora.getMinutes()).padStart(2, "0");

    const fecha = `${yyyy}-${MM}-${dd}`; // yyyy-MM-dd
    const hora = `${HH}:${mm}`;          // HH:mm

    return { fecha, hora };
  }

  const { fecha, hora } = obtenerFechaHoraActual();

  // =============================
  // AGREGAR
  // =============================
  const isSimulacion = tipoEscenario === "SIMULACION";
  const isOperacion = tipoEscenario === "OPERACION";
  const hayArchivo = !!archivo;

  useEffect(() => {
    initOperationManager().catch(err => {
      console.error("Error iniciando OperationManager", err);
    });
  }, []);

  const handleAdd = async () => {
    try {
      setProcessing(true);

      if (isSimulacion && !hayArchivo) {
        showNotification("warning", "Para escenarios de simulación debes subir un archivo.");
        return;
      }

      // ================= ARCHIVO =================
      if (hayArchivo) {
        const req = {
          tipoArchivo: "PEDIDOS", // 👈 OBLIGATORIO
          tipoEscenario: tipoEscenario,
          fechaHoraInicio: unirFechaHoraUTC(fechaArchivoFechaI, fechaArchivoHoraI),
          fechaHoraFin: unirFechaHoraUTC(fechaArchivoFechaF, fechaArchivoHoraF),
        };

        const respuesta = await iniciarImportacion(archivo, req);

        if (respuesta.exito) {
          if(tipoEscenario=="OPERACION") notifyNewOrder();
          showNotification("success", respuesta.mensaje || "Importación iniciada");
          console.log("Token de importación:", respuesta.token);
          setImportToken(respuesta.token); // ✅ CLAVE
          setIsModalOpen(false); 
          setArchivo(null);
          limpiar();
        } else {
          showNotification("danger", respuesta.mensaje || "Error al iniciar importación");
        }
      }

      // ================= MANUAL =================
      else if (isOperacion && !hayArchivo) {
        if (!selectedCliente || !selectedDestino || !fecha || !hora || !cantidad) {
          showNotification("warning", "Completa todos los campos del pedido manual.");
          setProcessing(false);
          return;
        }

        const fechaGeneracion = unirFechaHoraUTC(fecha, hora);

        const dto = {
          codigo: null,
          codCliente: selectedCliente.codigo,
          codDestino: selectedDestino.codigo,
          fechaHoraGeneracion: fechaGeneracion,
          cantidadSolicitada: Number(cantidad),
          lotesPorRuta: [],
          tipoEscenario: tipoEscenario,
        };

        await importarPedido(dto);
        notifyNewOrder();
        showNotification("success", "Pedido manual registrado correctamente");

        // --- SOLUCIÓN AQUÍ ---
        // Esperamos 0.5 segundos para asegurar que la DB ya tiene el dato listo
        await new Promise(resolve => setTimeout(resolve, 500));
        // Recargar tabla
        await fetchPedidos(1);
        
        limpiar();
        setIsModalOpen(false);
      }

    } catch (error) {
      console.error(error);
      showNotification("danger", "Error al agregar pedido");
    } finally {
      setProcessing(false);
    }
  };

  useEffect(() => {
    const off = onEvent(msg => {
      if (msg.type === "operation-status") {
        console.log("[WS][STATUS]", msg.payload);
      }

      if (msg.type === "operation-solution") {
        console.log("[WS][SOLUTION]", msg.payload);
      }

      if (msg.type === "notification-global") {
        showNotification(msg.variant, msg.message);
      }
    });

    return off;
  }, []);



  const handleCantidadChange = (e) => {
    const value = e.target.value;

    // Solo números
    if (!/^\d*$/.test(value)) return;

    // Evitar más de 3 dígitos
    if (value.length > 3) return;

    // Convertir a número para validar rango
    const num = Number(value);

    // Si está vacío → permitir porque el usuario está editando
    if (value === "") {
      setCantidad("");
      return;
    }

    // Rango 1–999
    if (num >= 1 && num <= 999) {
      setCantidad(value);
    }
  };


  // =============================
  // FILTROS
  // =============================
  const hayFiltrosActivos = () => {
    return (
      codigoFiltro.trim() !== "" ||
      filtroFecha.trim() !== "" ||
      filtroHora.trim() !== "" ||
      tipoEscenarioFiltro !== "" ||
      codDestinoFiltro.trim() !== ""
    );
  };

  const handleFilter = async () => {
    try {
      setLoading(true);

      const backendPage = 0; // siempre arrancar desde página 0

      let fechaHoraGeneracion = null;

      if (filtroFecha) {
        fechaHoraGeneracion = filtroFecha; // yyyy-MM-dd
        if (filtroHora) {
          fechaHoraGeneracion += " " + filtroHora + ":00"; // añade HH:mm:00
        } else {
          fechaHoraGeneracion += " 00:00:00"; // si no hay hora
        }
      }

      const modelo = {
        codigo: codigoFiltro || null,
        fechaHoraGeneracion: fechaHoraGeneracion,  
        fechaHoraExpiracion: null,   // no usamos aquí
        codCliente: null,
        fueAtendido: null,
        tipoEscenario: tipoEscenarioFiltro || null,
        codDestino: codDestinoFiltro || null,
      };

      const data = await filtrarPedidos(
        backendPage,
        itemsPerPage,
        modelo
      );

      let lista = data.dtos || [];

      setPedidos(lista);
      setCurrentPage(1);
      setHasMorePages(lista.length === itemsPerPage);

      showNotification("success", "Filtros aplicados");

    } catch (err) {
      console.error(err);
      showNotification("danger", "Error al filtrar pedidos");
    } finally {
      setLoading(false);
    }
  };


  const handleCleanFilters = async () => {
    setCodigoFiltro("");
    setFiltroFecha("");
    setFiltroHora("");
    setCodDestinoFiltro("");
    setTipoEscenarioFiltro(""); 

    await fetchPedidos(1);

    showNotification("info", "Presionar 2 veces para limpiar filtros");
  };


  // =============================
  // PAGINACIÓN
  // =============================
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;
  const [hasMorePages, setHasMorePages] = useState(false);

  useEffect(() => {
    fetchPedidos(1); // página 1 visual → backend página 0
  }, []);

  const fetchPedidos = async (paginaVisual) => {
    try {
      setLoading(true);

      const backendPage = paginaVisual - 1;

      let data;

      let fechaHoraGeneracion = null;

      if (filtroFecha) {
        fechaHoraGeneracion = filtroFecha; // yyyy-MM-dd
        if (filtroHora) {
          fechaHoraGeneracion += " " + filtroHora + ":00"; // añade HH:mm:00
        } else {
          fechaHoraGeneracion += " 00:00:00"; // si no hay hora
        }
      }

      if (hayFiltrosActivos()) {
        const modelo = {
          codigo: codigoFiltro || null,
          fechaHoraGeneracion: fechaHoraGeneracion, 
          fechaHoraExpiracion: null,
          codCliente: null,
          fueAtendido: null,
          tipoEscenario: tipoEscenarioFiltro || null,
          codDestino: codDestinoFiltro || null,
        };

        data = await filtrarPedidos(backendPage, itemsPerPage, modelo);
      } else {
        data = await listarPedidos(backendPage, itemsPerPage);
      }

      const lista = data.dtos || [];

      setPedidos(lista);
      setCurrentPage(paginaVisual);
      setHasMorePages(lista.length === itemsPerPage);
      

    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const limpiar = () => {
    setCantidad("");
    setSelectedCliente(null);
    setSelectedDestino(null);
  }


  // =============================
  // RENDER
  // =============================
  return (
    <div className="page">

      {importToken && (
        <LoadingOverlay
          token={importToken}
          onFinish={() => {
            setImportToken(null);   // ❌ overlay
            fetchPedidos(1);       // 🔄 refrescar tabla
          }}
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
              <span className="sidebar-subtitle-strong">Código del pedido</span>
              <Input
                placeholder="Escribir..."
                value={codigoFiltro}
                onChange={(e) => setCodigoFiltro(e.target.value)}
              />
            </div>

            <div className="filter-group">
              <span className="sidebar-subtitle-strong">Tipo de escenario</span>

              <Radio
                name="tipoEscenarioFiltro"
                value="SIMULACION"
                checked={tipoEscenarioFiltro === "SIMULACION"}
                onChange={() => setTipoEscenarioFiltro("SIMULACION")}
                label="Simulación"
              />

              <Radio
                name="tipoEscenarioFiltro"
                value="OPERACION"
                checked={tipoEscenarioFiltro === "OPERACION"}
                onChange={() => setTipoEscenarioFiltro("OPERACION")}
                label="Operación"
              />
            </div>

            <div className="filter-group">
              <span className="sidebar-subtitle-strong">Código de destino</span>
              <Input
                placeholder="Escribir..."
                value={codDestinoFiltro}
                onChange={(e) => setCodDestinoFiltro(e.target.value)}
              />
            </div>

            <div className="filter-group">
              <span className="sidebar-subtitle-strong">Fecha generación (mayor o igual a...)</span>

              <DateTimeColumn
                dateValue={filtroFecha}
                timeValue={filtroHora}
                onDateChange={(e) => setFiltroFecha(e.target.value)}
                onTimeChange={(e) => setFiltroHora(e.target.value)}
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
              setIsModalOpen(true);
            }}
          />
        </div>

        <Table headers={headers} data={pedidos} statusColors={statusColors}/>

        <Pagination
          currentPage={currentPage}
          onPageChange={fetchPedidos}
          hasMorePages={hasMorePages}
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
              />
            </div>

            <div className="file-name">
              {archivo ? archivo.name : "Ningún archivo seleccionado"}
              {archivo && <RemoveFileButton onClick={() => setArchivo(null)} />}
            </div>

            <div className="modal-body">
              <span className="sidebar-subtitle">Tipo de escenario</span>
                <Radio
                  name="tipoEscenario"
                  label="OPERACION"
                  value="OPERACION"
                  checked={tipoEscenario === "OPERACION"}
                  onChange={(e) => setTipoEscenario(e.target.value)}
                />
                <Radio
                  name="tipoEscenario"
                  label="SIMULACION"
                  value="SIMULACION"
                  checked={tipoEscenario === "SIMULACION"}
                  onChange={(e) => setTipoEscenario(e.target.value)}
                />


              {hayArchivo && (
                <>
                  <span className="sidebar-subtitle">Parámetros de lectura</span>
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


              {isOperacion && !hayArchivo && (
                <>
                  <span className="sidebar-subtitle">Datos del pedido</span>
                  <label>Cantidad</label>
                  <Input
                    value={cantidad}
                    onChange={handleCantidadChange}
                    maxLength={3}
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

