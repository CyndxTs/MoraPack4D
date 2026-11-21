import React from "react";
import { Outlet, useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar/navbar";
import { useAuth } from "../auth/AuthContext";
import "./layouts.scss";

const ITEMS = [
  { to: "/admin", label: "Parámetros", end: true },
  { to: "/admin/ubicaciones", label: "Países y continentes" },
  { to: "/admin/aeropuertos", label: "Aeropuertos y sedes" },
  {
    label: "Trabajador",
    children: [
      { to: "/trabajador", label: "Operación", end: true },
      { to: "/trabajador/simulacion", label: "Simulación" },
      { to: "/trabajador/pedidos", label: "Pedidos" },
      { to: "/trabajador/planes", label: "Planes y eventos" },
    ],
  },
];

export default function AdminLayout() {
  const { usuario, logout } = useAuth();
  const navigate = useNavigate();
  return (
    <div className="layout layout--app">
      <Navbar items={ITEMS} inicio="/admin" usuario={usuario} onLogout={async () => { await logout(); navigate("/"); }} cuenta="/admin/cuenta" />
      <main className="layout-main"><Outlet /></main>
    </div>
  );
}
