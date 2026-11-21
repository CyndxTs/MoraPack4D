import React from "react";
import { Link, NavLink } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { ETIQUETA_ROL } from "../../auth/session";
import "./navbar.scss";
import logo from "../../assets/images/mora.png";

/**
 * Barra superior genérica. 'items': [{ to, label, end? }] o grupos [{ label, children: [...] }].
 * El contenido depende del rol (lo arma cada layout); antes estaba fija con todas las páginas.
 */
export default function Navbar({ items = [], inicio = "/", usuario = null, onLogout, mostrarLogin = false, cuenta = null }) {
  const { abrirAuth } = useAuth();
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
              {cuenta
                ? <Link to={cuenta} className="nav-user-name" title="Mi cuenta">{usuario.nombres} {usuario.apellidos}</Link>
                : <span className="nav-user-name">{usuario.nombres} {usuario.apellidos}</span>}
              <span className="nav-user-role">{ETIQUETA_ROL[usuario.tipoUsuario] ?? usuario.tipoUsuario}</span>
            </div>
            <button type="button" className="nav-logout" onClick={onLogout}>Cerrar sesión</button>
          </div>
        ) : mostrarLogin ? (
          <button type="button" className="nav-login" onClick={() => abrirAuth("login")}>Iniciar sesión</button>
        ) : null}
      </div>
    </nav>
  );
}
