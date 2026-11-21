import React from "react";
import { Outlet, useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar/navbar";
import { useAuth } from "../auth/AuthContext";
import "./layouts.scss";

const ITEMS = [
  { to: "/", label: "MoraPack", end: true },
  { to: "/cliente", label: "Mis envíos", end: true },
  { to: "/cliente/pedidos/nuevo", label: "Nuevo pedido" },
];

export default function ClienteLayout() {
  const { usuario, logout } = useAuth();
  const navigate = useNavigate();
  return (
    <div className="layout layout--app">
      <Navbar items={ITEMS} inicio="/" usuario={usuario} onLogout={async () => { await logout(); navigate("/"); }} cuenta="/cliente/cuenta" />
      <main className="layout-main"><Outlet /></main>
    </div>
  );
}
