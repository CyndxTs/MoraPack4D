import React from "react";
import { Outlet, useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar/navbar";
import { useAuth } from "../auth/AuthContext";
import "./layouts.scss";

const ITEMS = [
  { to: "/trabajador", label: "Operación", end: true },
  { to: "/trabajador/simulacion", label: "Simulación" },
  { to: "/trabajador/pedidos", label: "Pedidos" },
  { to: "/trabajador/planes", label: "Planes y eventos" },
];

// La replanificación automática de la operación ahora la programa el server (PARAMETROS)
export default function TrabajadorLayout() {
  const { usuario, logout } = useAuth();
  const navigate = useNavigate();
  const salir = async () => { await logout(); navigate("/"); };

  return (
    <div className="layout layout--app">
      <Navbar items={ITEMS} inicio="/trabajador" usuario={usuario} onLogout={salir} cuenta="/trabajador/cuenta" />
      <main className="layout-main"><Outlet /></main>
    </div>
  );
}
