import React, { useEffect, useRef, useState } from "react";
import { Link, NavLink, useLocation } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { ETIQUETA_ROL } from "../../auth/session";
import Logo from "../Brand/Logo";
import "./navbar.scss";

/**
 * Barra superior. 'items': [{ to, label, end? }] o grupos [{ label, children: [...] }]; la arma cada layout según el rol.
 * 'publica': en la portada se superpone al hero y se vuelve sólida al hacer scroll.
 */
export default function Navbar({ items = [], inicio = "/", usuario = null, onLogout, mostrarLogin = false, cuenta = null, publica = false }) {
  const { abrirAuth } = useAuth();
  const location = useLocation();
  const [abierto, setAbierto] = useState(false);
  const [solida, setSolida] = useState(!publica);
  const [menuUsuario, setMenuUsuario] = useState(false);
  const refUsuario = useRef(null);

  useEffect(() => { setAbierto(false); setMenuUsuario(false); }, [location.pathname]);

  useEffect(() => {
    if (!publica) return undefined;
    const onScroll = () => setSolida(window.scrollY > 24);
    onScroll();
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, [publica]);

  useEffect(() => {
    if (!menuUsuario) return undefined;
    const fuera = (e) => { if (!refUsuario.current?.contains(e.target)) setMenuUsuario(false); };
    const esc = (e) => { if (e.key === "Escape") setMenuUsuario(false); };
    document.addEventListener("mousedown", fuera);
    document.addEventListener("keydown", esc);
    return () => { document.removeEventListener("mousedown", fuera); document.removeEventListener("keydown", esc); };
  }, [menuUsuario]);

  const enlace = (it) => (
    <li key={it.to}>
      <NavLink to={it.to} end={it.end} className={({ isActive }) => `nav-enlace${isActive ? " is-activo" : ""}`}>{it.label}</NavLink>
    </li>
  );

  const iniciales = usuario ? `${usuario.nombres?.[0] ?? ""}${usuario.apellidos?.[0] ?? ""}`.toUpperCase() : "";

  return (
    <nav id="top-nav" className={`nav${publica ? " nav--publica" : ""}${solida ? " is-solida" : ""}${abierto ? " is-abierta" : ""}`}>
      <div className="nav__fila">
        <Link to={inicio} className="nav__marca" aria-label="MoraPack, inicio"><Logo size={34} /></Link>

        <button type="button" className="nav__hamburguesa" aria-expanded={abierto} aria-controls="nav-menu" onClick={() => setAbierto((v) => !v)}>
          <span className="sr-only">Menú</span>
          <span aria-hidden="true" /><span aria-hidden="true" />
        </button>

        <div id="nav-menu" className="nav__menu">
          <ul className="nav__enlaces">
            {items.map((it) =>
              it.children ? (
                <li key={it.label} className="nav-grupo">
                  <span className="nav-enlace">{it.label}</span>
                  <ul className="nav-grupo__lista">{it.children.map(enlace)}</ul>
                </li>
              ) : enlace(it)
            )}
          </ul>

          <div className="nav__acciones">
            {usuario ? (
              <div className="nav-usuario" ref={refUsuario}>
                <button type="button" className="nav-usuario__boton" aria-expanded={menuUsuario} onClick={() => setMenuUsuario((v) => !v)}>
                  <span className="nav-usuario__avatar" aria-hidden="true">{iniciales || "·"}</span>
                  <span className="nav-usuario__texto">
                    <span className="nav-usuario__nombre">{usuario.nombres} {usuario.apellidos ?? ""}</span>
                    <span className="nav-usuario__rol">{ETIQUETA_ROL[usuario.tipoUsuario] ?? usuario.tipoUsuario}</span>
                  </span>
                </button>
                {menuUsuario && (
                  <div className="nav-usuario__menu" role="menu">
                    <span className="nav-usuario__correo">{usuario.correo}</span>
                    {cuenta && <Link to={cuenta} role="menuitem" className="nav-usuario__item">Mi cuenta</Link>}
                    <button type="button" role="menuitem" className="nav-usuario__item nav-logout" onClick={onLogout}>Cerrar sesión</button>
                  </div>
                )}
              </div>
            ) : mostrarLogin ? (
              <>
                <button type="button" className="boton boton--fantasma nav-login" onClick={() => abrirAuth("login")}>Iniciar sesión</button>
                <button type="button" className="boton boton--primario" onClick={() => abrirAuth("registro")}>Crear cuenta</button>
              </>
            ) : null}
          </div>
        </div>
      </div>
    </nav>
  );
}
