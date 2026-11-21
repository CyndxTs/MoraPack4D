import React from "react";
import { Outlet, useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar/navbar";
import { useAuth } from "../auth/AuthContext";
import { navDe } from "./navegacion";
import "./layouts.scss";

// Logo (inicio) a la izquierda, opciones del rol al centro y la cuenta a la derecha
export default function TrabajadorLayout() {
  const { usuario, logout } = useAuth();
  const navigate = useNavigate();
  const salir = async () => { await logout(); navigate("/"); };
  return (
    <div className="layout layout--app">
      <Navbar items={navDe(usuario)} usuario={usuario} onLogout={salir} cuenta="/trabajador/cuenta" />
      <main className="layout-main"><Outlet /></main>
    </div>
  );
}
