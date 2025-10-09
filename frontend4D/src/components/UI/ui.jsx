// src/components/ui/ui.jsx
import React, { useState } from "react";
import "./ui.scss";

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

export function Input({ placeholder, value, onChange }) {
  return (
    <input
      type="text"
      className="custom-input"
      placeholder={placeholder}
      value={value}
      onChange={onChange}
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

export function Table({ headers = [], data = [] }) {
  return (
    <div className="table-container">
      <table className="custom-table">
        <thead>
          <tr>
            {headers.map((h, i) => (
              <th key={i}>{h}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data.length ? (
            data.map((row, i) => (
              <tr key={i}>
                {headers.map((h, j) => {
                  const key = h.toLowerCase().replace(/\s/g, '');
                  return <td key={j}>{row[key] || ""}</td>;
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

export function Legend({ items }) {
  return (
    <ul className="legend-list">
      {items.map((item, i) => (
        <li key={i}>
          <img
            src="src/assets/icons/planeMora.svg"
            alt={item.label}
            className={`legend-icon ${item.status}`}
          />
          <span className="legend-text">{item.label}</span>
        </li>
      ))}
    </ul>
  );
}

