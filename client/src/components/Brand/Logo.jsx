import React from "react";
import logoMP from "../../assets/brand/MP_Logo.png";
import "./logo.scss";

// Marca de MoraPack: imagen oficial (src/assets/brand/MP_Logo.png, PNG cuadrado con fondo transparente)
export function LogoMark({ size = 40, className = "", titulo = "MoraPack" }) {
  return <img src={logoMP} width={size} height={size} alt={titulo} className={`logo-mark ${className}`} decoding="async" />;
}

export default function Logo({ size = 36, conTexto = true, className = "" }) {
  return (
    <span className={`logo ${className}`}>
      <LogoMark size={size} titulo={conTexto ? "" : "MoraPack"} />
      {conTexto && <span className="logo__texto">MoraPack</span>}
    </span>
  );
}
