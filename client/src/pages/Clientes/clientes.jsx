import React, { useState } from "react";
import "./clientes.scss";
import { ButtonAdd, Input, Dropdown, Table } from "../../components/UI/ui";

import plus from "../../assets/icons/plus.svg";
import hideIcon from "../../assets/icons/hide-sidebar.png";

export default function Clientes() {
  const [collapsed, setCollapsed] = useState(false);
  const [orden, setOrden] = useState("");
  const [nombreFiltro, setNombreFiltro] = useState("");

  const [nombre, setNombre] = useState("");
  const [correo, setCorreo] = useState("");
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [archivo, setArchivo] = useState(null);

  const headers = [
    { label: "Código", key: "codigo" },
    { label: "Nombre completo", key: "nombre" },
    { label: "Correo", key: "correo" },
    { label: "Acciones", key: "acciones" },
  ];

  // Datos de ejemplo
  const data = [
    { codigo: "0000001", nombre: "Victoria Isabella Sofía Pacheco Ramírez", correo: "blink.frost_crux@G4D.com" },
    { codigo: "0000002", nombre: "Elena Denisovna", correo: "pulse_onyx.glint@G4D.com" },
    { codigo: "0000003", nombre: "Ji-hun Lim", correo: "blu.shine_pulse@G4D.com" },
    { codigo: "0000004", nombre: "Hye-jin Sim", correo: "crux_joy.fizz.crux51@G4D.com" },
    { codigo: "0000005", nombre: "Ha-eun Ye-seo Baek", correo: "lunar_zen.lux@G4D.com" },
  ];

  // Manejo de archivo
  const handleFileChange = (e) => {
    if (e.target.files.length > 0) {
      setArchivo(e.target.files[0].name);
    } else {
      setArchivo(null);
    }
  };

  // Manejo del modal
  const handleAdd = () => {
    console.log({ nombre, correo });
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
            <span className="sidebar-subtitle-strong">Nombre</span>
            <Input
              placeholder="Escribir..."
              value={nombreFiltro}
              onChange={(e) => setNombreFiltro(e.target.value)}
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
          <ButtonAdd
            icon={plus}
            label="Agregar cliente"
            onClick={() => setIsModalOpen(true)}
          />
        </div>

        {/* Tabla de clientes */}
        <Table
          headers={headers}
          data={data}
        />
      </section>

      {/* Modal */}
      {isModalOpen && (
        <div className="modal" onClick={() => setIsModalOpen(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">Agregar cliente</h3>
              <label htmlFor="fileInput" className="file-label">
                Agregar archivo
              </label>
              <input
                type="file"
                id="fileInput"
                className="file-input"
                onChange={handleFileChange}
              />
            </div>

            <div className="file-name">
              {archivo || "Ningún archivo seleccionado"}
            </div>

            <div className="modal-body">
              <label htmlFor="nombreModal">Nombre</label>
              <Input
                id="nombreModal"
                placeholder="Escribe el nombre"
                value={nombre}
                onChange={(e) => setNombre(e.target.value)}
              />

              <label htmlFor="correoModal">Correo</label>
              <Input
                id="correoModal"
                placeholder="Escribe el correo electrónico"
                value={correo}
                onChange={(e) => setCorreo(e.target.value)}
              />
            </div>

            <div className="modal-footer">
              <button
                className="btn red"
                onClick={() => setIsModalOpen(false)}
              >
                Cancelar
              </button>
              <button className="btn green" onClick={handleAdd}>
                Agregar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
