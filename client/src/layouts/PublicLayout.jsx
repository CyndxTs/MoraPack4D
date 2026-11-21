import React from "react";
import { Outlet, useLocation } from "react-router-dom";
import Navbar from "../components/Navbar/navbar";
import Footer from "../components/Footer/footer";
import { useAuth } from "../auth/AuthContext";
import { RUTA_INICIO_POR_ROL } from "../auth/session";
import "./layouts.scss";

// Portada y páginas públicas. En la portada la barra flota sobre el globo.
export default function PublicLayout() {
  const { usuario, logout } = useAuth();
  const { pathname } = useLocation();
  const items = [{ to: "/", label: "Inicio", end: true }];
  if (usuario) items.push({ to: RUTA_INICIO_POR_ROL[usuario.tipoUsuario], label: "Mi espacio" });
  return (
    <div className="layout layout--publico">
      <Navbar items={items} usuario={usuario} onLogout={logout} mostrarLogin publica={pathname === "/"}
        cuenta={usuario ? `${RUTA_INICIO_POR_ROL[usuario.tipoUsuario]}/cuenta` : null} />
      <main className="layout-main"><Outlet /></main>
      <Footer />
    </div>
  );
}
