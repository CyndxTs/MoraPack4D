import React, { useState } from "react";
import { formatCompactNumber } from "../utils/format";
import { estadoDeFlota } from "./vista";

function Card({ icon, iconStyle, title, collapsedArrow = "▲", expandedArrow = "▼", defaultCollapsed = false, children }) {
  const [collapsed, setCollapsed] = useState(defaultCollapsed);
  return (
    <div className={`legend-card ${collapsed ? "legend-card--collapsed" : ""}`}>
      <button type="button" className="legend-card-header" onClick={() => setCollapsed((c) => !c)}>
        <span className="legend-card-info-icon" style={iconStyle}>{icon}</span>
        <span className="legend-card-title">{title}</span>
        <span className="legend-card-toggle">{collapsed ? collapsedArrow : expandedArrow}</span>
      </button>
      {!collapsed && <div className="legend-card-body">{children}</div>}
    </div>
  );
}

export function LegendOverlay() {
  return (
    <div className="legend-overlay">
      <Card icon="i" title="Leyenda">
        <div className="legend-item"><span className="legend-dot legend-dot--green" /><span>Menos del 50% de capacidad</span></div>
        <div className="legend-item"><span className="legend-dot legend-dot--yellow" /><span>Aviones: 50% a 90% · Aeropuertos: 50% a 80%</span></div>
        <div className="legend-item"><span className="legend-dot legend-dot--red" /><span>Aviones: 90% o más · Aeropuertos: 80% o más</span></div>
        <p className="legend-footnote">Las sedes tienen capacidad ilimitada.</p>
      </Card>
    </div>
  );
}

export function FleetOverlay({ activos, fleet }) {
  const { color, label } = estadoDeFlota(fleet.percentage);
  return (
    <div className="fleet-overlay">
      <Card icon="%" iconStyle={{ backgroundColor: color, color: "#fff" }} title="Estado de flota" collapsedArrow="▼" expandedArrow="▲">
        <div className="fleet-body">
          <div className="fleet-status">
            <span className="fleet-dot" style={{ backgroundColor: color, boxShadow: `0 0 6px ${color}` }} />
            <span className="fleet-status-label">{label}</span>
          </div>
          <div className="fleet-row fleet-row--divider"><span>Vuelos en tránsito:</span><strong>{activos}</strong></div>
          <div className="fleet-row"><span>Ocupación total:</span><strong>{fleet.percentage.toFixed(1)}%</strong></div>
          <div className="fleet-bar"><div style={{ width: `${Math.min(100, fleet.percentage)}%`, background: color }} /></div>
          <div className="fleet-total" title={`Exacto: ${fleet.totalOccupied.toLocaleString()} / ${fleet.totalMax.toLocaleString()}`}>
            Carga: <strong>{formatCompactNumber(fleet.totalOccupied)}</strong> / {formatCompactNumber(fleet.totalMax)} u.
          </div>
        </div>
      </Card>
    </div>
  );
}

// Panel de controles colapsable (arriba a la izquierda del mapa)
export function ControlsDropdown({ children, defaultOpen = false }) {
  const [open, setOpen] = useState(defaultOpen);
  return (
    <div className={`controls-dropdown ${open ? "open" : ""}`}>
      <button className="controls-toggle" onClick={() => setOpen((o) => !o)}>
        {open ? "Ocultar controles ▲" : "Mostrar controles ▼"}
      </button>
      {open && <div className="control-bar">{children}</div>}
    </div>
  );
}

export function ControlRow({ label, value, color }) {
  return (
    <div className="control-row">
      <span className="info-label" style={color ? { color } : undefined}>{label}</span>
      <span className="value">{value}</span>
    </div>
  );
}

export const ControlDivider = () => <hr className="control-divider" />;
