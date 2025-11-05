import React, { useState } from "react";
import "./planificacion.scss";
import { ButtonAdd, Input, Checkbox, Dropdown, Table, SidebarActions } from "../../components/UI/ui";
import plus from '../../assets/icons/plus.svg';
import hideIcon from '../../assets/icons/hide-sidebar.png';

export default function Planificacion() {
  const [collapsed, setCollapsed] = useState(false);

  // Filtros
  const [codigoVuelo, setCodigoVuelo] = useState("");
  const [ciudadDestino, setCiudadDestino] = useState("");
  const [continente, setContinente] = useState({ america: false, europa: false, asia: false });

  // Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [archivo, setArchivo] = useState(null);
  const [parametro1, setParametro1] = useState("");
  const [parametro2, setParametro2] = useState("");
  const [parametro3, setParametro3] = useState("");

  // Tabla
  const headers = [
    { label: "Código", key: "codigo" },
    { label: "Origen", key: "origen" },
    { label: "Destino", key: "destino" },
    { label: "Salida", key: "salida" },
    { label: "Llegada", key: "llegada" },
    { label: "Estado", key: "estado" },
    { label: "Acciones", key: "acciones" },
  ];

  const data = [
    {
      codigo: "PLA0001",
      origen: "LIM - Jorge Chávez",
      destino: "JFK - New York",
      salida: "2025-10-22 08:30",
      llegada: "2025-10-22 17:15",
      estado: "En ruta",
    },
    {
      codigo: "PLA0002",
      origen: "JFK - New York",
      destino: "CDG - París",
      salida: "2025-10-21 12:00",
      llegada: "2025-10-21 23:40",
      estado: "Finalizado",
    },
    {
      codigo: "PLA0003",
      origen: "CDG - París",
      destino: "FRA - Frankfurt",
      salida: "2025-10-20 10:45",
      llegada: "2025-10-20 13:00",
      estado: "Programado",
    },
    {
      codigo: "PLA0004",
      origen: "FRA - Frankfurt",
      destino: "NRT - Tokio",
      salida: "2025-10-19 20:15",
      llegada: "2025-10-20 11:00",
      estado: "En ruta",
    },
    {
      codigo: "PLA0005",
      origen: "NRT - Tokio",
      destino: "SYD - Sydney",
      salida: "2025-10-18 09:20",
      llegada: "2025-10-18 19:05",
      estado: "Programado",
    },
  ];

  const handleFileChange = (e) => {
    if (e.target.files.length > 0) setArchivo(e.target.files[0].name);
    else setArchivo(null);
  };

  const handleAdd = () => {
    console.log({ codigoVuelo, archivo, ciudadDestino, continente });
    setIsModalOpen(false);
  };

  //Filtros
  const handleFilter = async () => {

  };

  //Limpiar filtros
  const handleCleanFilters = async () => {

  };

  return (
    <div className="page">
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
                <span className="sidebar-subtitle-strong">Código de vuelo</span>
                <Input placeholder="Escribir..." value={codigoVuelo} onChange={e => setCodigoVuelo(e.target.value)} />
              </div>

              <div className="filter-group">
                <span className="sidebar-subtitle-strong">Ciudad destino</span>
                <Dropdown
                  placeholder="Seleccionar..."
                  options={[
                    { label: "Ejemplo 1", value: "ejemplo1" },
                    { label: "Ejemplo 2", value: "ejemplo2" }
                  ]}
                  onSelect={val => setCiudadDestino(val)}
                />
              </div>

              <div className="filter-group">
                <span className="sidebar-subtitle-strong">Continente</span>
                <Checkbox label="América" value="america" checked={continente.america} onChange={e => setContinente({ ...continente, america: e.target.checked })} />
                <Checkbox label="Europa" value="europa" checked={continente.europa} onChange={e => setContinente({ ...continente, europa: e.target.checked })} />
                <Checkbox label="Asia" value="asia" checked={continente.asia} onChange={e => setContinente({ ...continente, asia: e.target.checked })} />
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
          <h4>Planificación de vuelos</h4>
          <ButtonAdd icon={plus} label="Generar plan" onClick={() => setIsModalOpen(true)} />
        </div>

        <Table headers={headers} data={data} />
      </section>

      {/* Modal */}
      {isModalOpen && (
        <div className="modal" onClick={() => setIsModalOpen(false)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">Planificar</h3>
              <label htmlFor="fileInput" className="file-label">Agregar archivo</label>
              <input type="file" id="fileInput" className="file-input" onChange={handleFileChange} />
            </div>

            <div className="file-name">{archivo || "Ningún archivo seleccionado"}</div>

            <div className="modal-body">
              <label htmlFor="parametro1">Parámetro 1</label>
              <Input id="parametro1" placeholder="Escribe el parámetro 1" value={parametro1} onChange={e => setParametro1(e.target.value)} />

              <label htmlFor="parametro2">Parámetro 2</label>
              <Input id="parametro2" placeholder="Escribe el parámetro 2" value={parametro2} onChange={e => setParametro2(e.target.value)} />

              <label htmlFor="parametro3">Parámetro 3</label>
              <Dropdown
                placeholder="Seleccionar..."
                options={[
                  { label: "Ejemplo 1", value: "ejemplo1" },
                  { label: "Ejemplo 2", value: "ejemplo2" }
                ]}
                onSelect={val => setParametro3(val)}
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
