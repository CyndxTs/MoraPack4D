import React, { useRef } from "react";

// 6 casillas para el código. Acepta pegar el código completo.
export default function CodigoInput({ value, onChange, disabled, autoFocus = true }) {
  const refs = useRef([]);
  const digitos = Array.from({ length: 6 }, (_, i) => value[i] ?? "");

  const poner = (i, d) => {
    const arr = [...digitos];
    arr[i] = d;
    onChange(arr.join("").replace(/\s/g, "").slice(0, 6));
  };

  const onInput = (i, e) => {
    const v = e.target.value.replace(/\D/g, "");
    if (!v) { poner(i, ""); return; }
    if (v.length > 1) {
      const todo = (digitos.slice(0, i).join("") + v).slice(0, 6);
      onChange(todo);
      refs.current[Math.min(todo.length, 5)]?.focus();
      return;
    }
    poner(i, v);
    if (i < 5) refs.current[i + 1]?.focus();
  };

  const onKeyDown = (i, e) => {
    if (e.key === "Backspace" && !digitos[i] && i > 0) {
      poner(i - 1, "");
      refs.current[i - 1]?.focus();
      e.preventDefault();
    } else if (e.key === "ArrowLeft" && i > 0) refs.current[i - 1]?.focus();
    else if (e.key === "ArrowRight" && i < 5) refs.current[i + 1]?.focus();
  };

  const onPaste = (e) => {
    const v = (e.clipboardData.getData("text") || "").replace(/\D/g, "").slice(0, 6);
    if (v) {
      e.preventDefault();
      onChange(v);
      refs.current[Math.min(v.length, 5)]?.focus();
    }
  };

  return (
    <div className="auth-codigo" onPaste={onPaste}>
      {digitos.map((d, i) => (
        <input
          key={i}
          ref={(el) => (refs.current[i] = el)}
          inputMode="numeric"
          autoComplete={i === 0 ? "one-time-code" : "off"}
          aria-label={`Dígito ${i + 1}`}
          maxLength={6}
          value={d}
          disabled={disabled}
          autoFocus={autoFocus && i === 0}
          onChange={(e) => onInput(i, e)}
          onKeyDown={(e) => onKeyDown(i, e)}
          onFocus={(e) => e.target.select()}
        />
      ))}
    </div>
  );
}
