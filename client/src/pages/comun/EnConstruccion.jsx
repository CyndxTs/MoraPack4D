import React from "react";
import "./comun.scss";

// Pantalla provisional para las vistas que aún no se implementan (las rutas ya existen)
export default function EnConstruccion({ titulo, descripcion, puntos = [] }) {
  return (
    <div className="en-construccion">
      <div className="en-construccion-card">
        <span className="en-construccion-badge">En construcción</span>
        <h2>{titulo}</h2>
        {descripcion && <p>{descripcion}</p>}
        {puntos.length > 0 && <ul>{puntos.map((p) => <li key={p}>{p}</li>)}</ul>}
      </div>
    </div>
  );
}
