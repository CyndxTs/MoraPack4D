import React, { useId, useState } from "react";

export function Campo({ label, type = "text", value, onChange, autoComplete, autoFocus, disabled, placeholder, hint }) {
  const id = useId();
  const [ver, setVer] = useState(false);
  const esClave = type === "password";
  return (
    <div className="auth-campo">
      <label htmlFor={id}>{label}</label>
      <div className="auth-campo__control">
        <input
          id={id}
          type={esClave && ver ? "text" : type}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          autoComplete={autoComplete}
          autoFocus={autoFocus}
          disabled={disabled}
          placeholder={placeholder}
          spellCheck={false}
        />
        {esClave && (
          <button type="button" className="auth-campo__ver" onClick={() => setVer((v) => !v)} aria-label={ver ? "Ocultar contraseña" : "Mostrar contraseña"} tabIndex={-1}>
            {ver ? "Ocultar" : "Ver"}
          </button>
        )}
      </div>
      {hint && <small className="auth-campo__hint">{hint}</small>}
    </div>
  );
}

export function SelectPais({ value, onChange, paises, disabled }) {
  const id = useId();
  return (
    <div className="auth-campo">
      <label htmlFor={id}>País</label>
      <div className="auth-campo__control">
        <select id={id} value={value} onChange={(e) => onChange(e.target.value)} disabled={disabled}>
          <option value="">Selecciona tu país</option>
          {paises.map((p) => <option key={p.nombre} value={p.nombre}>{p.nombre}</option>)}
        </select>
      </div>
    </div>
  );
}
