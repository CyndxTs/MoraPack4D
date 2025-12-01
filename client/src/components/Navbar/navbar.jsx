import React from "react";
import { Link } from "react-router-dom";
import "./navbar.scss";

import logo from "../../assets/images/mora.png";

const Navbar = () => {
  return (
    <nav id="top-nav">
      <div className="container nav-container">
        {/* Logo a la izquierda */}
        <div id="logo">
          <Link to="/">
            <img src={logo} alt="Logo MoraPack" className="img-fluid" />
          </Link>
        </div>

        {/* Menú completo */}
        <ul className="menu menu-top">
          <li>
            <Link to="/">Inicio</Link>
          </li>
          <li>
            <Link to="/clientes">Clientes</Link>
          </li>
          <li>
            <Link to="/aeropuertos">Aeropuertos</Link>
          </li>
          <li>
            <Link to="/planes">Planes de vuelos</Link>
          </li>

          {/* pequeño espacio extra, pero SIN empujarlo a la derecha */}
          <li className="separator">
            <Link to="/pedidos">Pedidos</Link>
          </li>
          <li>
            <Link to="/planificacion">Operación Diaria</Link>
          </li>
          <li>
            <Link to="/simulacion">Simulación Semanal</Link>
          </li>
          <li>
            <Link to="/reportes">Reportes</Link>
          </li>
        </ul>
      </div>
    </nav>
  );
};

export default Navbar;
