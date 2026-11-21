import React from "react";
import { Link } from "react-router-dom";
import Logo from "../Brand/Logo";
import "./footer.scss";

export default function Footer() {
  return (
    <footer id="main-footer" className="pie">
      <div className="container pie__fila">
        <div className="pie__marca">
          <Logo size={30} />
          <p>Envíos aéreos entre América, Europa y Asia desde Lima, Bruselas y Bakú.</p>
        </div>
        <nav className="pie__enlaces" aria-label="Legal">
          <Link to="/terminos">Términos y condiciones</Link>
          <Link to="/privacidad">Política de privacidad</Link>
        </nav>
      </div>
      <div className="container pie__creditos">
        <span>Proyecto de Diseño y Desarrollo de Software · Grupo 4D · PUCP</span>
        <span>Mapas: Natural Earth (dominio público)</span>
      </div>
    </footer>
  );
}
