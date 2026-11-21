import React from "react";
import { Outlet } from "react-router-dom";
import Navbar from "../components/Navbar/navbar";
import Footer from "../components/Footer/footer";
import { useAuth } from "../auth/AuthContext";
import { RUTA_INICIO_POR_ROL } from "../auth/session";
import "./layouts.scss";

// Página pública de MoraPack (publicidad + acceso)
export default function PublicLayout() {
  const { usuario, logout } = useAuth();
  const items = [{ to: "/", label: "Inicio", end: true }];
  if (usuario) items.push({ to: RUTA_INICIO_POR_ROL[usuario.tipoUsuario], label: "Mi espacio" });
  return (
    <div className="layout">
      <Navbar items={items} usuario={usuario} onLogout={logout} mostrarLogin cuenta={usuario ? `${RUTA_INICIO_POR_ROL[usuario.tipoUsuario]}/cuenta` : null} />
      <main className="layout-main"><Outlet /></main>
      <Footer />
    </div>
  );
}
