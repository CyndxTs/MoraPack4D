import React from "react";
import "./status.scss";

/**
 * - value: string (nombre del estado)
 * - colorMap: objeto { ESTADO: "#COLOR" }
 * - defaultColor: color de fallback si no está definido
 */
export default function status({ value, colorMap = {}, defaultColor = "#777" }) {
  const color = colorMap[value?.toUpperCase()] || defaultColor;

  return (
    <span
      className="status-badge"
      style={{
        backgroundColor: color,
        color: "#fff",
      }}
    >
      {value || "—"}
    </span>
  );
}
