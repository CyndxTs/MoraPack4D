import React from "react";
import { Link, NavLink } from "react-router-dom";
import "./navbar.scss";
import logo from "../../assets/images/mora.png";

const ETIQUETA_ROL = { CLIENTE: "Cliente", TRABAJADOR: "Trabajador", ADMINISTRADOR: "Administrador" };

/**
 * Barra superior genérica. 'items': [{ to, label, end? }] o grupos [{ label, children: [...] }].
 * El contenido depende del rol (lo arma cada layout); antes estaba fija con todas las páginas.
 */
export default function Navbar({ items = [], inicio = "/", usuario = null, onLogout, mostrarLogin = false }) {
  const renderLink = (it) => (
    <li key={it.to}>
      <NavLink to={it.to} end={it.end} className={({ isActive }) => (isActive ? "active" : "")}>{it.label}</NavLink>
    </li>
  );

  return (
    <nav id="top-nav">
      <div className="container nav-container">
        <div id="logo">
          <Link to={inicio}><img src={logo} alt="Logo MoraPack" className="img-fluid" /></Link>
        </div>

        <ul className="menu menu-top">
          {items.map((it) =>
            it.children ? (
              <li key={it.label} className="dropdown">
                <span className="dropdown-label">{it.label}</span>
                <ul className="dropdown-menu">{it.children.map(renderLink)}</ul>
              </li>
            ) : renderLink(it)
          )}
        </ul>

        <div className="nav-spacer" />

        {usuario ? (
          <div className="nav-user">
            <div className="nav-user-info">
              <span className="nav-user-name">{usuario.nombres} {usuario.apellidos}</span>
              <span className="nav-user-role">{ETIQUETA_ROL[usuario.tipoUsuario] ?? usuario.tipoUsuario}</span>
            </div>
            <button type="button" className="nav-logout" onClick={onLogout}>Cerrar sesión</button>
          </div>
        ) : mostrarLogin ? (
          <ul className="menu menu-top">
            <li><NavLink to="/login">Iniciar sesión</NavLink></li>
          </ul>
        ) : null}
      </div>
    </nav>
  );
}
