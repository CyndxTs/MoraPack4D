// src/components/ui/ui.jsx
import React, { useState, useEffect } from "react";
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

// INPUT DE FECHA + HORA EN LÍNEA
export function DateTimeInline({ dateValue, timeValue, onDateChange, onTimeChange, disabled = false }) {
  return (
    <div className="datetime-inline">
      <input
        type="date"
        className={`custom-input ${disabled ? "disabled" : ""}`}
        value={dateValue}
        onChange={onDateChange}
        disabled={disabled}
      />

      <input
        type="time"
        className={`custom-input ${disabled ? "disabled" : ""}`}
        value={timeValue}
        onChange={onTimeChange}
        disabled={disabled}
      />
    </div>
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

export function Radio({ name, value, checked, onChange, label,disabled }) {
  return (
    <label className={`radio ${disabled ? "radio-disabled" : ""}`}>
      <input
        type="radio"
        name={name}
        value={value}
        checked={checked}
        onChange={onChange}
        disabled={disabled} 
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

export function RemoveFileButton({ label = "❌", onClick }) {
  return (
    <button className="remove-file-btn" onClick={onClick}>
      {label}
    </button>
  );
}


export function Dropdown({ options = [], onSelect, placeholder = "Seleccionar...", value }) {
  const [open, setOpen] = useState(false);
  const [selected, setSelected] = useState("");

  // Sincroniza el valor externo (prop value) con el interno (selected)
  useEffect(() => {
    if (!value) {
      setSelected("");
    } else {
      const opt = options.find((o) => o.value === value);
      setSelected(opt ? opt.label : "");
    }
  }, [value, options]);

  const finalOptions = [{ label: placeholder, value: "" }, ...options];

  const handleSelect = (opt) => {
    setSelected(opt.value === "" ? "" : opt.label);
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

export function Dropdown2({ options = [], value = [], onChange, placeholder = "Seleccionar...", multiple = false }) {
  const [open, setOpen] = useState(false);

  const handleSelect = (opt) => {
    if (multiple) {
      let newValue;

      if (value.includes(opt.value)) {
        newValue = value.filter(v => v !== opt.value);
      } else {
        newValue = [...value, opt.value];
      }

      onChange && onChange(newValue);
    } else {
      onChange && onChange(opt.value);
      setOpen(false);
    }
  };

  const getLabel = () => {
    if (!multiple) {
      const opt = options.find(o => o.value === value);
      return opt ? opt.label : placeholder;
    }

    if (value.length === 0) return placeholder;
    return value.join(", ");
  };

  return (
    <div className={`custom-dropdown ${open ? "open" : ""}`}>
      <div
        className="selected"
        onClick={() => setOpen(!open)}
      >
        {getLabel()}
      </div>

      {open && (
        <ul className="options">
          {options.map((opt, i) => (
            <li
              key={i}
              className={value.includes(opt.value) ? "selected-option" : ""}
              onClick={() => handleSelect(opt)}
            >
              {opt.label}
              {multiple && value.includes(opt.value) && " ✔"}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export function Dropdown3({
  options = [],
  onSelect,
  placeholder = "Seleccionar...",
  value,
  disabled = false   // agregamos disabled
}) {
  const [open, setOpen] = useState(false);
  const [selected, setSelected] = useState("");

  // Sync value externo
  useEffect(() => {
    if (!value) {
      setSelected("");
    } else {
      const opt = options.find((o) => o.value === value);
      setSelected(opt ? opt.label : "");
    }
  }, [value, options]);

  const finalOptions = [{ label: placeholder, value: "" }, ...options];

  const handleSelect = (opt) => {
    if (disabled) return;  // evita seleccionar si está deshabilitado
    setSelected(opt.value === "" ? "" : opt.label);
    onSelect && onSelect(opt.value);
    setOpen(false);
  };

  return (
    <div
      className={`custom-dropdown ${open ? "open" : ""} ${disabled ? "disabled" : ""}`}
    >
      <div
        className="selected"
        style={{
          color: disabled
            ? "gray"                       //  color gris si está disabled
            : selected
            ? "var(--color-negro)"
            : "var(--color-light-grey)"
        }}
        onClick={() => !disabled && setOpen(!open)}   //  bloquea clic
      >
        {selected || placeholder}
      </div>

      {open && !disabled && (                       //  bloquea menú
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
            {headers.map((h, i) => {
              // detecta si la columna es numérica basado en alguna fila
              const someValue = data[0]?.[h.key];
              const isNumericCol =
                typeof someValue === "number" ||
                (!isNaN(someValue) && someValue !== null && someValue !== "");

              return (
                <th
                  key={i}
                  className={`${h.key === "acciones" ? "acciones" : ""} ${
                    isNumericCol ? "numeric" : ""
                  }`}
                >
                  {h.label}
                </th>
              );
            })}
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
                        <div className="acciones-container">
                          <img src={viewIcon} alt="Ver" title="Ver" className="icon" />
                          <img src={editIcon} alt="Editar" title="Editar" className="icon" />
                          <img src={deleteIcon} alt="Eliminar" title="Eliminar" className="icon" />
                        </div>
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
                  const value = row[h.key];

                  const isNumeric =
                    typeof value === "number" ||
                    (!isNaN(value) && value !== null && value !== "");

                  return (
                    <td key={j} className={isNumeric ? "numeric" : ""}>
                      {value ?? ""}
                    </td>
                  );
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

  if (totalPages <= 1) return null;

  const handleClick = (page) => {
    if (page >= 1 && page <= totalPages) {
      onPageChange(page);
    }
  };

  // ----- GENERAR LISTA LIMITADA -----
  const getVisiblePages = () => {
    if (totalPages <= 5) {
      return [...Array(totalPages)].map((_, i) => i + 1);
    }

    if (currentPage <= 3) {
      return [1, 2, 3, 4, "dots", totalPages];
    }

    if (currentPage >= totalPages - 2) {
      return [1, "dots", totalPages - 3, totalPages - 2, totalPages - 1, totalPages];
    }

    return [
      1,
      "dots",
      currentPage - 1,
      currentPage,
      currentPage + 1,
      "dots",
      totalPages,
    ];
  };

  const pages = getVisiblePages();

  return (
    <div className="pagination">
      <button
        className="page-btn"
        onClick={() => handleClick(currentPage - 1)}
        disabled={currentPage === 1}
      >
        ⟨
      </button>

      {pages.map((p, index) =>
        p === "dots" ? (
          <span key={`dots-${index}`} className="page-dots">…</span>
        ) : (
          <button
            key={p}
            className={`page-btn ${currentPage === p ? "active" : ""}`}
            onClick={() => handleClick(p)}
          >
            {p}
          </button>
        )
      )}

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
  const [seconds, setSeconds] = useState(0);

  useEffect(() => {
    const timer = setInterval(() => {
      setSeconds((prev) => prev + 1);
    }, 1000);

    return () => clearInterval(timer); // limpiar al desmontar
  }, []);

  return (
    <div className="loading-overlay">
      <div className="spinner"></div>
      <p>
        {text} <strong>({seconds}s)</strong>
      </p>
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
