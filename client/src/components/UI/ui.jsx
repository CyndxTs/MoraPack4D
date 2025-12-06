// src/components/ui/ui.jsx
import React, { useState, useEffect, useRef } from "react";
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

import { Client } from "@stomp/stompjs";

export function Button({ icon, label, onClick, type = "button" }) {
  return (
    <button className="btn-icon" type={type} onClick={onClick}>
      {icon && <img src={icon} alt="" />}
      <span>{label}</span>
    </button>
  );
}

export function ButtonAdd({
  icon,
  label,
  onClick,
  type = "button",
  className = "",
  disabled = false,
  ...rest
}) {
  return (
    <button
      className={`btnAdd-icon ${className}`.trim()}
      type={type}
      onClick={onClick}
      disabled={disabled}
      {...rest}
    >
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


export function RangeSelector({ min, max, step, value, onChange }) {
  const sliderRef = useRef(null);

  const isDecimal = String(min).includes(".") || String(max).includes(".") || String(step).includes(".");
  const decimals = isDecimal ? 3 : 0;

  const format = (n) => (isDecimal ? Number(n).toFixed(3) : String(Math.round(n)));

  const safeValue = typeof value === "number" ? value : min;

  const [inputValue, setInputValue] = useState(format(safeValue));

  // Valores válidos (normalizados)
  const values = [];
  for (let v = min; v <= max + 1e-9; v += step) {
    values.push(Number(v.toFixed(decimals)));
  }

  // Sync externo → input
  useEffect(() => {
    setInputValue(format(value ?? min));
  }, [value, min]);

  const handleSelectValue = (v) => {
    onChange(v);
    setInputValue(format(v));
  };

  // ---------------------------------------------------
  // INPUT CHANGE
  // ---------------------------------------------------
  const handleInputChange = (e) => {
    let text = e.target.value;

    // Validación según si usa decimales
    if (isDecimal) {
      if (!/^\d*\.?\d*$/.test(text)) return;
    } else {
      if (!/^\d*$/.test(text)) return;
    }

    setInputValue(text);

    if (text === "" || text === ".") return;

    let num = Number(text);
    if (isNaN(num)) return;

    if (num < min || num > max) return;

    const closest = values.reduce((a, b) =>
      Math.abs(b - num) < Math.abs(a - num) ? b : a
    );

    onChange(closest);
  };

  // ---------------------------------------------------
  // BLUR → normaliza y ajusta formato
  // ---------------------------------------------------
  const handleBlur = () => {
    if (inputValue === "" || inputValue === ".") {
      setInputValue(format(safeValue));
      return;
    }

    let num = Number(inputValue);

    if (num < min) num = min;
    if (num > max) num = max;

    const closest = values.reduce((a, b) =>
      Math.abs(b - num) < Math.abs(a - num) ? b : a
    );

    setInputValue(format(closest));
    onChange(closest);
  };

  return (
    <div className="range-row">
      <input
        type="text"
        className="range-input"
        value={inputValue}
        onChange={handleInputChange}
        onBlur={handleBlur}
        disabled
      />

      <div className="range-slider" ref={sliderRef}>
        <div
          className="range-track-fill"
          style={{
            width: `${((safeValue - min) / (max - min)) * 100}%`
          }}
        />

        {values.map((v, i) => (
          <div
            key={i}
            className={`range-point ${v <= safeValue ? "active" : ""}`}
            style={{ left: `${((v - min) / (max - min)) * 100}%` }}
            onClick={() => handleSelectValue(v)}
          >
            <div className="dot" />
          </div>
        ))}
      </div>
    </div>
  );
}


export function TriPieSelector({ 
  labels,
  valores,
  setters
}) {
  const allowedValues = [1000, 2000, 3000, 4000, 5000];
  const total = 10000;

  const adjustValues = (index, newValue) => {
    const newVals = [...valores];
    newVals[index] = newValue;

    const remaining = total - newValue;
    const otherIdx = [0, 1, 2].filter(i => i !== index);

    let combinations = [];

    for (let v1 of allowedValues) {
      for (let v2 of allowedValues) {
        if (v1 + v2 === remaining) {
          combinations.push([v1, v2]);
        }
      }
    }

    if (combinations.length === 0) return;

    combinations.sort((a, b) => {
      const diffA = Math.abs(a[0] - valores[otherIdx[0]]) +
                    Math.abs(a[1] - valores[otherIdx[1]]);
      const diffB = Math.abs(b[0] - valores[otherIdx[0]]) +
                    Math.abs(b[1] - valores[otherIdx[1]]);
      return diffA - diffB;
    });

    newVals[otherIdx[0]] = combinations[0][0];
    newVals[otherIdx[1]] = combinations[0][1];

    setters[0](newVals[0]);
    setters[1](newVals[1]);
    setters[2](newVals[2]);
  };

  const angles = valores.map(v => (v / total) * 360);

  const buildArcPath = (startAngle, endAngle) => {
    const rad = deg => (deg * Math.PI) / 180;
    const r = 80;
    const x0 = 100 + r * Math.cos(rad(startAngle));
    const y0 = 100 + r * Math.sin(rad(startAngle));
    const x1 = 100 + r * Math.cos(rad(endAngle));
    const y1 = 100 + r * Math.sin(rad(endAngle));

    const largeArc = endAngle - startAngle > 180 ? 1 : 0;
    return `M100,100 L${x0},${y0} A${r},${r} 0 ${largeArc} 1 ${x1},${y1} Z`;
  };

  const colors = ["#3b82f6", "#10b981", "#f59e0b"];
  let cumulative = 0;

  return (
    <>
      {/* TÍTULO/INDICACIÓN */}
      <div style={{ textAlign: "left", width: "100%", marginBottom: "1px" }}>
        <em className="tri-pie-instruction-title">
          Haz clic en un sector para cambiar su valor
        </em>
      </div>

      <div className="tri-pie-container">
        
        {/* COLUMNA IZQUIERDA */}
        <div className="tri-pie-left">
          <div className="tri-pie-inputs">
            {labels.map((lbl, i) => (
              <div key={i} className="tri-pie-input-group">
                <label className="tri-pie-label">{lbl}</label>
                <input
                  type="text"
                  className="custom-tri-pie-input" 
                  value={valores[i]}
                />
              </div>
            ))}
          </div>
        </div>

        {/* COLUMNA DERECHA - PIE */}
        <div className="tri-pie-right">

          <svg width="200" height="200">
            {angles.map((ang, i) => {
              const start = cumulative;
              const end = cumulative + ang;
              cumulative = end;

              return (
                <path
                  key={i}
                  d={buildArcPath(start, end)}
                  fill={colors[i]}
                  stroke="#fff"
                  strokeWidth="1"
                  style={{ cursor: "pointer" }}
                  onClick={() => {
                    const curr = valores[i];
                    const idx = allowedValues.indexOf(curr);
                    const next = allowedValues[(idx + 1) % allowedValues.length];
                    adjustValues(i, next);
                  }}
                >
                  {/* TOOLTIP */}
                  <title>{labels[i]}</title>
                </path>
              );
            })}
          </svg>

        </div>
      </div>
    </>
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
    <label className={`radio-item ${disabled ? "radio-disabled" : ""}`}>
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
  disabled = false
}) {
  const [open, setOpen] = useState(false);
  const [inputValue, setInputValue] = useState(""); 
  const [selected, setSelected] = useState("");

  const dropdownRef = useRef(null); // 👈 REFERENCIA AL DROPDOWN

  // Sincronizar valor externo
  useEffect(() => {
    if (!value) {
      setSelected("");
      setInputValue("");
    } else {
      const opt = options.find((o) => o.value === value);
      const label = opt ? opt.label : "";
      setSelected(label);
      setInputValue(label);
    }
  }, [value, options]);

  // 🔍 Filtrar opciones
  const filteredOptions = options.filter((opt) =>
    opt.label.toLowerCase().includes(inputValue.toLowerCase())
  );

  const handleSelect = (opt) => {
    if (disabled) return;
    setSelected(opt.label);
    setInputValue(opt.label);
    onSelect && onSelect(opt.value);
    setOpen(false);
  };

  const handleInputClick = () => {
    if (!disabled) setOpen(true);
  };

  // 👇 CERRAR CUANDO SE HACE CLIC AFUERA
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  return (
    <div
      ref={dropdownRef} // 👈 APLICAR EL REF
      className={`custom-dropdown ${open ? "open" : ""} ${disabled ? "disabled" : ""}`}
    >
      {/* Input editable */}
      <input
        type="text"
        className="selected-input"
        placeholder={placeholder}
        value={inputValue}
        disabled={disabled}
        onClick={handleInputClick}
        onChange={(e) => {
          setInputValue(e.target.value);
          setOpen(true);
        }}
        style={{
          color: disabled
            ? "gray"
            : selected
            ? "var(--color-negro)"
            : "var(--color-light-grey)"
        }}
      />

      {/* Opciones */}
      {open && !disabled && (
        <ul className="options">
          {filteredOptions.length === 0 ? (
            <li className="no-results">Sin resultados</li>
          ) : (
            filteredOptions.map((opt, idx) => (
              <li key={idx} onClick={() => handleSelect(opt)}>
                {opt.label}
              </li>
            ))
          )}
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

                  // Cualquier columna que deba usar StatusBadge
                  if (h.useStatusColors) {
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
                      {(() => {
                        // Booleanos → SI / NO
                        if (value === true || value === 1) return "SI";
                        if (value === false || value === 0) return "NO";

                        return value ?? "";
                      })()}
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
export function Pagination({ currentPage, onPageChange, hasMorePages }) {

  const handleClick = (page) => {
    if (page < 1) return;
    if (page > currentPage + 1) return;
    onPageChange(page);
  };

  const pages = [];

  // Siempre mostrar 1
  if (currentPage > 2) pages.push(1);

  // Dots:
  if (currentPage > 3) pages.push("dots");

  // Página anterior
  if (currentPage > 1) pages.push(currentPage - 1);

  // Página actual
  pages.push(currentPage);

  // Página siguiente
  if (hasMorePages) pages.push(currentPage + 1);

  // Dots a la derecha
  if (hasMorePages && currentPage > 1) pages.push("dots");

  return (
    <div className="pagination">
      <button
        className="page-btn"
        onClick={() => handleClick(currentPage - 1)}
        disabled={currentPage === 1}
      >
        ⟨
      </button>

      {pages.map((p, i) =>
        p === "dots" ? (
          <span key={`dots-${i}`} className="page-dots">…</span>
        ) : (
          <button
            key={`page-${p}-${i}`}
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
        disabled={!hasMorePages}
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

/*export function LoadingOverlay({ text = "Cargando..." }) {
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
}*/

export function useLoaderProgress() {
  const [payload, setPayload] = useState(null);

  useEffect(() => {
    const client = new Client({
      brokerURL: "ws://localhost:8080/ws",  // 👈 WebSocket nativo
      reconnectDelay: 500,
      onConnect: () => {
        client.subscribe("/topic/loader", (msg) => {
          const data = JSON.parse(msg.body);
          setPayload(data);
        });
      },
      debug: () => {} // opcional para silenciar logs
    });

    client.activate();

    return () => {
      client.deactivate();
    };
  }, []);

  return payload;
}


export function LoadingOverlay() {
  const [seconds, setSeconds] = useState(0);
  const [progress, setProgress] = useState(null);

  useEffect(() => {
    const timer = setInterval(() => {
      setSeconds((prev) => prev + 1);
    }, 1000);

    return () => clearInterval(timer);
  }, []);

  // 🔥 Hook que escucha el WebSocket
  const payload = useLoaderProgress();

  useEffect(() => {
    if (payload) {
      setProgress(payload);
    }
  }, [payload]);

  const text = progress
    ? `${progress.proceso} (${progress.completado} / ${progress.total})`
    : `Cargando... (${seconds}s)`;

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
