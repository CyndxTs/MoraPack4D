import React, { useEffect } from "react";
import { Outlet, useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar/navbar";
import { useAuth } from "../auth/AuthContext";
import { useNotify } from "../notifications/NotificationContext";
import { iniciarReplanificador } from "../operacion/replanificador";
import "./layouts.scss";

const ITEMS = [
  { to: "/trabajador", label: "Operación", end: true },
  { to: "/trabajador/simulacion", label: "Simulación" },
  { to: "/trabajador/pedidos", label: "Pedidos" },
  { to: "/trabajador/planes", label: "Planes y eventos" },
];

export default function TrabajadorLayout() {
  const { usuario, logout } = useAuth();
  const notify = useNotify();
  const navigate = useNavigate();

  // La replanificación automática solo corre mientras hay un trabajador con sesión
  useEffect(() => iniciarReplanificador({
    onLanzada: () => notify("info", "Replanificación automática de la operación iniciada."),
    onError: (e) => notify("danger", `Replanificación automática: ${e.message}`),
  }), [notify]);

  const salir = async () => { await logout(); navigate("/login"); };

  return (
    <div className="layout layout--app">
      <Navbar items={ITEMS} inicio="/trabajador" usuario={usuario} onLogout={salir} />
      <main className="layout-main"><Outlet /></main>
    </div>
  );
}
