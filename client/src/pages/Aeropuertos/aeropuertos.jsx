import React, { useState, useEffect } from "react";
import "./aeropuertos.scss";
import { ButtonAdd, Input, Checkbox, Dropdown, Table, SidebarActions, Notification, LoadingOverlay, Pagination, RemoveFileButton  } from "../../components/UI/ui";
import { listarAeropuertos, filtrarAeropuertos   } from "../../services/aeropuertoService";
import { importarAeropuertos } from "../../services/generalService";
import plus from '../../assets/icons/plus.svg';
import hideIcon from '../../assets/icons/hide-sidebar.png';

export default function Aeropuertos() {
  const [collapsed, setCollapsed] = useState(false);
  const [codigoFiltro, setCodigoFiltro] = useState("");
  const [ciudadFiltro, setCiudadFiltro] = useState("");
  const [orden, setOrden] = useState("");
  const [continenteFiltro, setContinenteFiltro] = useState({ america: false, europa: false, asia: false });

  const [aeropuertos, setAeropuertos] = useState([]);
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
    { label: "Continente", key: "continente" },
    { label: "Capacidad", key: "capacidad" },
    { label: "Huso horario", key: "husoHorario" },
    { label: "Acciones", key: "acciones" },
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

      const data = await listarAeropuertos();
      setAeropuertos(data);
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
  const handleFilter = async () => {
    setProcessing(true);
    try {
      const continenteSeleccionado = Object.keys(continenteFiltro)
        .filter((key) => continenteFiltro[key])
        .map((c) => {
          if (c === "america") return "América del Sur.";
          if (c === "europa") return "Europa";
          if (c === "asia") return "Asia";
          return c;
        })
        .join(",");

      const ordenCapacidad = orden === "ascendente" ? "ascendente" : orden === "descendente" ? "descendente" : "";


      const data = await filtrarAeropuertos({
        codigo: codigoFiltro || null,
        ciudad: ciudadFiltro || null,
        continente: continenteSeleccionado || null,
        ordenCapacidad: ordenCapacidad || null,
      });

      setAeropuertos(data);
      setCurrentPage(1);
      showNotification("success", "Filtro aplicado correctamente");
    } catch (err) {
      console.error(err);
      showNotification("danger", "Error al filtrar aeropuertos");
    } finally {
      setProcessing(false);
    }
  };

  //Limpiar filtros
  const handleCleanFilters = async () => {
    setCodigoFiltro("");
    setCiudadFiltro("");
    setContinenteFiltro({ america: false, europa: false, asia: false });
    setOrden("");
    setProcessing(true);
    try {
      const data = await listarAeropuertos();
      setAeropuertos(data);
      showNotification("info", "Filtros limpiados");
    } catch (err) {
      showNotification("danger", "Error al limpiar filtros");
    } finally {
      setProcessing(false);
    }
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
                <Input placeholder="Escribir..." value={ciudadFiltro} onChange={(e) => setCiudadFiltro(e.target.value)} />
              </div>

              <div className="filter-group">
                <span className="sidebar-subtitle-strong">Continente</span>
                <Checkbox label="América del Sur." value="america" checked={continenteFiltro.america} onChange={(e) => setContinenteFiltro({ ...continenteFiltro, america: e.target.checked })} />
                <Checkbox label="Europa" value="europa" checked={continenteFiltro.europa} onChange={(e) => setContinenteFiltro({ ...continenteFiltro, europa: e.target.checked })} />
                <Checkbox label="Asia" value="asia" checked={continenteFiltro.asia} onChange={(e) => setContinenteFiltro({ ...continenteFiltro, asia: e.target.checked })} />
              </div>

              <div className="filter-group">
                <span className="sidebar-subtitle-strong">Orden de capacidad</span>
                <Dropdown
                  placeholder="Seleccionar..."
                  value={orden} // hace que el valor dependa del estado
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

            <div className="file-name">
              {archivo ? archivo.name : "Ningún archivo seleccionado"}
              {archivo && (
                <RemoveFileButton onClick={() => setArchivo(null)} />
              )}
            </div>


            <div className="modal-body">
              <label htmlFor="codigoModal">Código</label>
              <Input id="codigoModal" placeholder="Escribe el código" value={codigo} onChange={(e) => setCodigo(e.target.value)} disabled={!!archivo}/>

              <label htmlFor="ciudadModal">Ciudad</label>
              <Input id="ciudadModal" placeholder="Escribe la ciudad" value={ciudad} onChange={(e) => setCiudad(e.target.value)} disabled={!!archivo}/>

              <label htmlFor="paisModal">País</label>
              <Input id="paisModal" placeholder="Escribe el pais" value={pais} onChange={(e) => setPais(e.target.value)} disabled={!!archivo}/>

              <label htmlFor="continenteModal">Continente</label>
              <Input id="continenteModal" placeholder="Escribe el continente" value={continente} onChange={(e) => setContinente(e.target.value)} disabled={!!archivo}/>

              <label htmlFor="capacidadModal">Capacidad</label>
              <Input id="capacidadModal" placeholder="Escribe la capacidad" value={capacidad} onChange={(e) => setCapacidad(e.target.value)} disabled={!!archivo}/>


              <label htmlFor="husoHorarioModal">Huso horario</label>
              <Input id="husoHorarioModal" placeholder="Escribe el huso horario" value={husoHorario} onChange={(e) => setHusoHorario(e.target.value)} disabled={!!archivo}/>

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
