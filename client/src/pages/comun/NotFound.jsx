import React from "react";
import { Link } from "react-router-dom";
import "./comun.scss";

export default function NotFound() {
  return (
    <div className="en-construccion">
      <div className="en-construccion-card">
        <span className="en-construccion-badge">404</span>
        <h2>Página no encontrada</h2>
        <p><Link to="/" className="link-oscuro">Volver al inicio</Link></p>
      </div>
    </div>
  );
}
