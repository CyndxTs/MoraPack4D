import React from 'react'
import { Link } from 'react-router-dom'
import './navbar.scss'

import logo from '../../assets/images/mora.png'

const Navbar = () => {
  return (
    <nav id="top-nav">
      <div className="container">
        <ul className="menu menu-top">
          <li><Link to="/">Inicio</Link></li>
          <li><Link to="/clientes">Clientes</Link></li>
          <li><Link to="/aeropuertos">Aeropuertos</Link></li>
          <li><Link to="/planes">Planes de vuelos</Link></li>
        </ul>
      </div>

      <header id="main-header">
        <div id="header-collapsed">
          <span className="arrow">▼</span>
        </div>
        <div className="container" id="header-content">
          <div id="logo">
            <Link to="/">
              <img src={logo} alt="Logo MoraPack" className="img-fluid" />
            </Link>
          </div>
          <ul className="menu menu-main">
            <li><Link to="/pedidos">Pedidos</Link></li>
            <li><Link to="/planificacion">Operación Diaria</Link></li>
            <li><Link to="/simulacion">Simulación Semanal</Link></li>
            <li><Link to="/reportes">Reportes</Link></li>
          </ul>
        </div>
      </header>
    </nav>
  )
}

export default Navbar
