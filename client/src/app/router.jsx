import React, { lazy, Suspense } from "react";
import { createBrowserRouter } from "react-router-dom";
import RequireRole from "../auth/RequireRole";
import { ROLES } from "../auth/session";
import PublicLayout from "../layouts/PublicLayout";
import TrabajadorLayout from "../layouts/TrabajadorLayout";
import ClienteLayout from "../layouts/ClienteLayout";
import AdminLayout from "../layouts/AdminLayout";
import Home from "../pages/public/Home/Home";
import Login from "../pages/public/Login/Login";
const Operacion = lazy(() => import("../pages/trabajador/Operacion/Operacion"));
const Simulacion = lazy(() => import("../pages/trabajador/Simulacion/Simulacion"));
const Pedidos = lazy(() => import("../pages/trabajador/Pedidos/Pedidos"));
const Planes = lazy(() => import("../pages/trabajador/Planes/Planes"));
import ClienteInicio from "../pages/cliente/ClienteInicio";
import NuevoPedido from "../pages/cliente/NuevoPedido";
import { AdminAeropuertos, AdminParametros, AdminUbicaciones } from "../pages/admin/AdminPaginas";
import NotFound from "../pages/comun/NotFound";

const cargando = <div className="vista-cargando">Cargando...</div>;
const diferida = (Componente) => <Suspense fallback={cargando}><Componente /></Suspense>;

const router = createBrowserRouter([
  // Público: publicidad + login (lo usan los clientes; trabajadores y admins entran por el mismo login)
  {
    path: "/",
    element: <PublicLayout />,
    children: [
      { index: true, element: <Home /> },
      { path: "login", element: <Login /> },
    ],
  },
  // Trabajador: la operación es su pantalla principal
  {
    path: "/trabajador",
    element: <RequireRole rol={ROLES.TRABAJADOR}><TrabajadorLayout /></RequireRole>,
    children: [
      { index: true, element: diferida(Operacion) },
      { path: "simulacion", element: diferida(Simulacion) },
      { path: "simulacion/:idTransaccion", element: diferida(Simulacion) },
      { path: "pedidos", element: diferida(Pedidos) },
      { path: "planes", element: diferida(Planes) },
    ],
  },
  // Cliente (vistas pendientes)
  {
    path: "/cliente",
    element: <RequireRole rol={ROLES.CLIENTE}><ClienteLayout /></RequireRole>,
    children: [
      { index: true, element: <ClienteInicio /> },
      { path: "pedidos/nuevo", element: <NuevoPedido /> },
    ],
  },
  // Administrador (vistas pendientes)
  {
    path: "/admin",
    element: <RequireRole rol={ROLES.ADMINISTRADOR}><AdminLayout /></RequireRole>,
    children: [
      { index: true, element: <AdminParametros /> },
      { path: "ubicaciones", element: <AdminUbicaciones /> },
      { path: "aeropuertos", element: <AdminAeropuertos /> },
    ],
  },
  { path: "*", element: <PublicLayout />, children: [{ path: "*", element: <NotFound /> }] },
]);

export default router;
