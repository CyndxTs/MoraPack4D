import React, { useState } from "react";
import "./pedidos.scss";
import { ButtonAdd, Input, Checkbox, Dropdown, Table } from "../../components/UI/ui";
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
  const [vueloFiltro, setVueloFiltro] = useState("");

  // Modal y datos
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [archivo, setArchivo] = useState(null);

  const [codigo, setCodigo] = useState("");

  // Tabla
  const headers = [
    { label: "Código", key: "codigo" },
    { label: "Cliente", key: "cliente" },
    { label: "Fecha de generación", key: "fecha" },
    { label: "Estado", key: "estado" },
    { label: "Acciones", key: "acciones" },
  ];

  const data = [
    { codigo: "P001", cliente: "Victoria Pacheco", fecha: "2025-10-22 07:45", estado: "En curso" },
    { codigo: "P002", cliente: "Elena Denisovna", fecha: "2025-10-21 14:30", estado: "Pendiente" },
    { codigo: "P003", cliente: "Ji-hun Lim", fecha: "2025-10-20 09:12", estado: "Entregado" },
    { codigo: "P004", cliente: "Hye-jin Sim", fecha: "2025-10-19 18:05", estado: "Cancelado" },
    { codigo: "P005", cliente: "Ha-eun Baek", fecha: "2025-10-18 22:47", estado: "En curso" },
  ];

  const handleFileChange = (e) => {
    if (e.target.files.length > 0) setArchivo(e.target.files[0].name);
    else setArchivo(null);
  };

  const handleAdd = () => {
    console.log({ archivo, codigoFiltro, estadoFiltro, clienteFiltro, vueloFiltro });
    setIsModalOpen(false);
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

        <div className="sidebar-content">
          <span className="sidebar-subtitle">Filtros</span>
          <div className="filter-group">
            <span className="sidebar-subtitle-strong">Código</span>
            <Input placeholder="Escribir..." value={codigoFiltro} onChange={e => setCodigoFiltro(e.target.value)} />
          </div>

          <div className="filter-group">
            <span className="sidebar-subtitle-strong">Estado</span>
            <Checkbox label="En curso" value="enCurso" checked={estadoFiltro.enCurso} onChange={e => setEstadoFiltro({ ...estadoFiltro, enCurso: e.target.checked })} />
            <Checkbox label="Pendiente" value="pendiente" checked={estadoFiltro.pendiente} onChange={e => setEstadoFiltro({ ...estadoFiltro, pendiente: e.target.checked })} />
            <Checkbox label="Entregado" value="entregado" checked={estadoFiltro.entregado} onChange={e => setEstadoFiltro({ ...estadoFiltro, entregado: e.target.checked })} />
            <Checkbox label="Cancelado" value="cancelado" checked={estadoFiltro.cancelado} onChange={e => setEstadoFiltro({ ...estadoFiltro, cancelado: e.target.checked })} />
          </div>

          <div className="filter-group">
            <span className="sidebar-subtitle-strong">Cliente</span>
            <Dropdown
              placeholder="Seleccionar..."
              options={[
                { label: "Ejemplo 1", value: "ejemplo1" },
                { label: "Ejemplo 2", value: "ejemplo2" }
              ]}
              onSelect={val => setClienteFiltro(val)}
            />
          </div>

          <div className="filter-group">
            <span className="sidebar-subtitle-strong">Vuelo</span>
            <Dropdown
              placeholder="Seleccionar..."
              options={[
                { label: "Ejemplo 1", value: "ejemplo1" },
                { label: "Ejemplo 2", value: "ejemplo2" }
              ]}
              onSelect={val => setVueloFiltro(val)}
            />
          </div>
        </div>
      </aside>

      {/* Contenido principal */}
      <section className="contenido">
        <div className="content-header">
          <h4>Gestión de Pedidos</h4>
          <ButtonAdd icon={plus} label="Agregar pedido" onClick={() => setIsModalOpen(true)} />
        </div>

        <Table headers={headers} data={data} />
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
