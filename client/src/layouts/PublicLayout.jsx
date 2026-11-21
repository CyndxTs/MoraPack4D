import React from "react";
import { Outlet, useLocation } from "react-router-dom";
import Navbar from "../components/Navbar/navbar";
import Footer from "../components/Footer/footer";
import { useAuth } from "../auth/AuthContext";
import { RUTA_INICIO_POR_ROL } from "../auth/session";
import { navDe } from "./navegacion";
import "./layouts.scss";

// Portada y páginas públicas. En la portada la barra flota sobre el globo.
// Con sesión, el centro de la barra muestra directamente las opciones del rol (el logo ya es el inicio).
export default function PublicLayout() {
  const { usuario, logout } = useAuth();
  const { pathname } = useLocation();
  return (
    <div className="layout layout--publico">
      <Navbar items={navDe(usuario)} usuario={usuario} onLogout={logout} mostrarLogin publica={pathname === "/"}
        cuenta={usuario ? `${RUTA_INICIO_POR_ROL[usuario.tipoUsuario]}/cuenta` : null} />
      <main className="layout-main"><Outlet /></main>
      <Footer />
    </div>
  );
}
