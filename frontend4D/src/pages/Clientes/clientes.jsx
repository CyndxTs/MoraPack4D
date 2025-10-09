// src/pages/Aeropuertos/aeropuertos.jsx
import React, { useState } from "react";
import "./clientes.scss";
import { Button, ButtonAdd, Input, Checkbox, Dropdown, Table } from "../../components/UI/ui";

import plus from '../../assets/icons/plus.svg';
import hideIcon from '../../assets/icons/hide-sidebar.png';

export default function Clientes() {
  const [collapsed, setCollapsed] = useState(false);
  const [estado, setEstado] = useState({ activo: false, inactivo: false});
  const [orden, setOrden] = useState("");
  const [nombreFiltro, setNombreFiltro] = useState("");

  const [nombre, setNombre] = useState("");
  const [telefono, setTelefono] = useState("");
  const [pais, setPais] = useState("");

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [archivo, setArchivo] = useState(null);

  const headers = ["ID", "Nombre completo", "Dirección", "País", "Teléfono", "Estado", "Acciones"];
  const data = []; 

  const handleFileChange = (e) => {
    if (e.target.files.length > 0) {
      setArchivo(e.target.files[0].name);
    } else {
      setArchivo(null);
    }
  };

  const handleAdd = () => {
    // Aquí podrías agregar la lógica para guardar el cliente
    console.log({ nombre, pais, telefono, estado, archivo });
    setIsModalOpen(false); // cerramos modal después de agregar
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
            <span className="sidebar-subtitle-strong">Nombre</span>
            <Input
              placeholder="Escribir..."
              value={nombreFiltro}
              onChange={(e) => setNombreFiltro(e.target.value)}
            />
          </div>

          <div className="filter-group">
            <span className="sidebar-subtitle-strong">Estado</span>
            <Checkbox
              label="Activo"
              value="activo"
              checked={estado.activo}
              onChange={(e) => setEstado({ ...estado, activo: e.target.checked })}
            />
            <Checkbox
              label="Inactivo"
              value="inactivo"
              checked={estado.europa}
              onChange={(e) => setEstado({ ...estado, inactivo: e.target.checked })}
            />
          </div>

          <div className="filter-group">
            <span className="sidebar-subtitle-strong">Orden</span>
            <Dropdown
              placeholder="Seleccionar..."
              options={[
                { label: "Ascendente", value: "ascendente" },
                { label: "Descendente", value: "descendente" },          
              ]}
              onSelect={(val) => setOrden(val)}
            />
          </div>
        </div>
      </aside>

      {/* Contenido principal */}
      <section className="contenido">
        <div className="content-header">
          <h4>Gestión de clientes</h4>
          <ButtonAdd icon={plus} label="Agregar cliente" onClick={() => setIsModalOpen(true)} />
        </div>

        <Table headers={headers} data={data} />
      </section>

      {/* Modal */}
      {isModalOpen && (
        <div className="modal" onClick={() => setIsModalOpen(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">Agregar cliente</h3>
              <label htmlFor="fileInput" className="file-label">Agregar archivo</label>
              <input type="file" id="fileInput" className="file-input" onChange={handleFileChange} />
            </div>

            <div className="file-name">{archivo || "Ningún archivo seleccionado"}</div>

            <div className="modal-body">
              <label htmlFor="nombreModal">Nombre</label>
              <Input id="nombreModal" placeholder="Escribe el nombre" value={nombre} onChange={(e) => setNombre(e.target.value)} />

              <label htmlFor="paisModal">País</label>
              <Dropdown
                placeholder="Seleccionar..."
                options={[
                  { label: "Ejemplo 1", value: "ejemplo1" },
                  { label: "Ejemplo 2", value: "ejemplo2" },
                ]}
                onSelect={(val) => setPais(val)}
              />

              <label htmlFor="telefonoModal">Teléfono</label>
              <Input id="telefonoModal" placeholder="Escribe el teléfono" />
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
