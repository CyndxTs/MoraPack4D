// src/components/ui/ui.jsx
import React, { useState } from "react";
import "./ui.scss";
import StatusBadge from "../Status/status";

import planeMora from "../../assets/icons/planeMora.svg"; 
import viewIcon from "../../assets/icons/view.svg";
import editIcon from "../../assets/icons/edit.svg";
import deleteIcon from "../../assets/icons/delete.svg";

import successIcon from "../../assets/icons/success.svg";
import dangerIcon from "../../assets/icons/danger.svg";
import infoIcon from "../../assets/icons/info.svg";
import careIcon from "../../assets/icons/care.svg";

import filterIcon from "../../assets/icons/filter.svg";
import cleanIcon from "../../assets/icons/clean.svg";

export function Button({ icon, label, onClick, type = "button" }) {
  return (
    <button className="btn-icon" type={type} onClick={onClick}>
      {icon && <img src={icon} alt="" />}
      <span>{label}</span>
    </button>
  );
}

export function ButtonAdd({ icon, label, onClick, type = "button" }) {
  return (
    <button className="btnAdd-icon" type={type} onClick={onClick}>
      {icon && <img src={icon} alt="" />}
      <span>{label}</span>
    </button>
  );
}

export function Input({ placeholder, value, onChange, disabled = false }) {
  return (
    <input
      type="text"
      className={`custom-input ${disabled ? "disabled" : ""}`}
      placeholder={placeholder}
      value={value}
      onChange={onChange}
      disabled={disabled}
    />
  );
}


export function Checkbox({ label, value, checked, onChange }) {
  return (
    <label className="checkbox-item">
      <input type="checkbox" value={value} checked={checked} onChange={onChange} />
      <span className="checkmark"></span>
      {label}
    </label>
  );
}

export function Radio({ name, value, checked, onChange, label }) {
  return (
    <label className="radio-item">
      <input
        type="radio"
        name={name}
        value={value}
        checked={checked}
        onChange={onChange}
      />
      <span className="radiomark"></span>
      {label}
    </label>
  );
}

export function FileInput({ onChange, label = "Seleccionar archivo" }) {
  return (
    <label className="file-input">
      <input type="file" onChange={onChange} />
      <span className="file-label">{label}</span>
    </label>
  );
}

export function Dropdown({ options = [], onSelect, placeholder = "Seleccionar..." }) {
  const [open, setOpen] = useState(false);
  const [selected, setSelected] = useState("");

  // Insertamos la opción "Seleccionar..." al inicio
  const finalOptions = [{ label: placeholder, value: "" }, ...options];

  const handleSelect = (opt) => {
    if (opt.value === "") {
      setSelected(""); // placeholder
    } else {
      setSelected(opt.label);
    }
    onSelect && onSelect(opt.value);
    setOpen(false);
  };

  return (
    <div className={`custom-dropdown ${open ? "open" : ""}`}>
      <div
        className="selected"
        style={{ color: selected ? "var(--color-negro)" : "var(--color-light-grey)" }}
        onClick={() => setOpen(!open)}
      >
        {selected || placeholder}
      </div>
      {open && (
        <ul className="options">
          {finalOptions.map((opt, i) => (
            <li
              key={i}
              className={opt.value === "" ? "placeholder-option" : ""}
              onClick={() => handleSelect(opt)}
            >
              {opt.label}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}


export function Table({ headers = [], data = [], statusColors = {} }) {
  return (
    <div className="table-container">
      <table className="custom-table">
        <thead>
          <tr>
            {headers.map((h, i) => (
              <th key={i}>{h.label}</th>
            ))}
          </tr>
        </thead>

        <tbody>
          {data.length ? (
            data.map((row, i) => (
              <tr key={i}>
                {headers.map((h, j) => {
                  // Columna de acciones
                  if (h.key === "acciones") {
                    return (
                      <td key={j} className="acciones">
                        <img
                          src={viewIcon}
                          alt="Ver"
                          title="Ver"
                          className="icon"
                        />
                        <img
                          src={editIcon}
                          alt="Editar"
                          title="Editar"
                          className="icon"
                        />
                        <img
                          src={deleteIcon}
                          alt="Eliminar"
                          title="Eliminar"
                          className="icon"
                        />
                      </td>
                    );
                  }

                  // Columna de estado (rectángulo de color)
                  if (h.key === "estado") {
                    return (
                      <td key={j}>
                        <StatusBadge value={row[h.key]} colorMap={statusColors} />
                      </td>
                    );
                  }

                  // Celdas normales
                  return <td key={j}>{row[h.key] ?? ""}</td>;
                })}
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan={headers.length} className="no-data">
                No hay datos
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

//          PAGINACIÓN REUTILIZABLE
export function Pagination({ totalItems, itemsPerPage, currentPage, onPageChange }) {
  const totalPages = Math.ceil(totalItems / itemsPerPage);

  if (totalPages <= 1) return null; // no mostrar si no hace falta

  const handleClick = (page) => {
    if (page >= 1 && page <= totalPages) {
      onPageChange(page);
    }
  };

  return (
    <div className="pagination">
      <button
        className="page-btn"
        onClick={() => handleClick(currentPage - 1)}
        disabled={currentPage === 1}
      >
        ⟨
      </button>

      {Array.from({ length: totalPages }, (_, i) => (
        <button
          key={i + 1}
          className={`page-btn ${currentPage === i + 1 ? "active" : ""}`}
          onClick={() => handleClick(i + 1)}
        >
          {i + 1}
        </button>
      ))}

      <button
        className="page-btn"
        onClick={() => handleClick(currentPage + 1)}
        disabled={currentPage === totalPages}
      >
        ⟩
      </button>
    </div>
  );
}

export function Legend({ items }) {
  return (
    <ul className="legend-list">
      {items.map((item, i) => (
        <li key={i}>
          <img
            src={planeMora}
            alt={item.label}
            className={`legend-icon ${item.status}`}
          />
          <span className="legend-text">{item.label}</span>
        </li>
      ))}
    </ul>
  );
}

export function LoadingOverlay({ text = "Cargando..." }) {
  return (
    <div className="loading-overlay">
      <div className="spinner"></div>
      <p>{text}</p>
    </div>
  );
}

//NOTIFICACIONES
export function Notification({ type = "success", message = "", onClose }) {
  const config = {
    success: {
      title: "Éxito",
      icon: successIcon,
      bg: "#E7FFEC",
      color: "#008027",
    },
    danger: {
      title: "Error",
      icon: dangerIcon,
      bg: "#FFE7E9",
      color: "#AA000E",
    },
    info: {
      title: "Información",
      icon: infoIcon,
      bg: "#E6F4FF",
      color: "#00448D",
    },
    warning: {
      title: "Cuidado",
      icon: careIcon,
      bg: "#FFF5E7",
      color: "#B26000",
    },
  };

  const { title, icon, bg, color } = config[type] || config.info;

  return (
    <div
      className="notification"
      style={{
        backgroundColor: bg,
        border: `1px solid ${color}`,
        color,
      }}
    >
      <img src={icon} alt={title} className="notification-icon" />
      <div className="notification-content">
        <strong>{title}</strong>
        <p>{message}</p>
      </div>
      <button
        className="notification-close"
        onClick={onClose}
        style={{ color }}
      >
        ✕
      </button>
    </div>
  );
}

// --- BOTONES DE FILTRO Y LIMPIEZA ---
export function SidebarActions({ onFilter, onClean }) {
  return (
    <div className="sidebar-actions">
      <button className="action-btn blue" onClick={onFilter}>
        <img src={filterIcon} alt="Filtrar" />
        <span>Aplicar filtro</span>
      </button>

      <button className="action-btn grey" onClick={onClean}>
        <img src={cleanIcon} alt="Limpiar" />
        <span>Limpiar filtros</span>
      </button>
    </div>
  );
}
