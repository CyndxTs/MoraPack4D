import React from "react";
import "./logo.scss";

/**
 * Marca de MoraPack: una caja de envío isométrica con una órbita que la rodea como si la caja fuera el mundo.
 * La mitad trasera de la órbita pasa detrás de la caja y la delantera termina en flecha.
 */
export function LogoMark({ size = 40, className = "", titulo = "MoraPack" }) {
  return (
    <svg className={`logo-mark ${className}`} width={size} height={size} viewBox="0 0 64 64" role="img" aria-label={titulo}>
      <g transform="rotate(-16 32 32)" fill="none" strokeLinecap="round">
        <path className="logo-mark__orbita logo-mark__orbita--atras" d="M4 32 A28 10 0 0 1 60 32" />
      </g>
      <g className="logo-mark__caja">
        <path d="M32 13 L49 21.5 L32 30 L15 21.5 Z" fill="var(--logo-cara-1, #FFC266)" />
        <path d="M15 21.5 L32 30 L32 49 L15 40.5 Z" fill="var(--logo-cara-2, #F29A2E)" />
        <path d="M32 30 L49 21.5 L49 40.5 L32 49 Z" fill="var(--logo-cara-3, #C8741A)" />
        <path d="M23.5 17.2 L40.5 25.8 L40.5 34 L37 35.8 L37 27.6 L20 19 Z" fill="var(--logo-cinta, #8C4F10)" opacity="0.55" />
      </g>
      <g transform="rotate(-16 32 32)" fill="none" strokeLinecap="round" strokeLinejoin="round">
        <path className="logo-mark__orbita" d="M60 32 A28 10 0 0 1 6.4 35.4" />
        <path className="logo-mark__flecha" d="M11.6 35.8 L4.6 33.6 L7 40.6" />
      </g>
    </svg>
  );
}

export default function Logo({ size = 36, conTexto = true, className = "" }) {
  return (
    <span className={`logo ${className}`}>
      <LogoMark size={size} />
      {conTexto && <span className="logo__texto">MoraPack</span>}
    </span>
  );
}
