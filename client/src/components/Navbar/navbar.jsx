import React from "react";
import { Link } from "react-router-dom";
import "./navbar.scss";

import logo from "../../assets/images/mora.png";

const Navbar = () => {
  return (
    <nav id="top-nav">
      <div className="container nav-container">

        {/* Logo */}
        <div id="logo">
          <Link to="/">
            <img src={logo} alt="Logo MoraPack" className="img-fluid" />
          </Link>
        </div>

        {/* Menú */}
        <ul className="menu menu-top">

          <li>
            <Link to="/">Inicio</Link>
          </li>

          {/* === GRUPO 1: Catálogo === */}
          <li className="dropdown">
            <span className="dropdown-label">Logística</span>
            <ul className="dropdown-menu">
              <li><Link to="/clientes">Clientes</Link></li>
              <li><Link to="/aeropuertos">Aeropuertos</Link></li>
              <li><Link to="/planes">Planes</Link></li>
              <li><Link to="/pedidos">Pedidos</Link></li>
            </ul>
          </li>

          {/* === GRUPO 2: Escenarios === */}
          <li className="dropdown">
            <span className="dropdown-label">Escenarios</span>
            <ul className="dropdown-menu">
              <li><Link to="/planificacion">Operación Diaria</Link></li>
              <li><Link to="/simulacion">Simulación</Link></li>
              <li><Link to="/reportes">Reportes</Link></li>
            </ul>
          </li>

        </ul>
      </div>
    </nav>
  );
};

export default Navbar;
