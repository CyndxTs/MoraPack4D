import React, { lazy, Suspense } from "react";
import { createBrowserRouter } from "react-router-dom";
import RequireRole from "../auth/RequireRole";
import { ROLES } from "../auth/session";
import RootLayout from "../layouts/RootLayout";
import PublicLayout from "../layouts/PublicLayout";
import TrabajadorLayout from "../layouts/TrabajadorLayout";
import ClienteLayout from "../layouts/ClienteLayout";
import AdminLayout from "../layouts/AdminLayout";
import Home from "../pages/public/Home/Home";
import AbrirLogin from "../pages/public/AbrirLogin";
const Legal = lazy(() => import("../pages/public/Legal/Legal"));
const MiCuenta = lazy(() => import("../pages/comun/MiCuenta/MiCuenta"));
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

const cuenta = { path: "cuenta", element: diferida(MiCuenta) };

const router = createBrowserRouter([
  {
    element: <RootLayout />,
    children: [
      // Público: portada + páginas legales. El acceso es un modal (no hay ruta de login)
      {
        path: "/",
        element: <PublicLayout />,
        children: [
          { index: true, element: <Home /> },
          { path: "login", element: <AbrirLogin /> },
          { path: "privacidad", element: diferida(() => <Legal tipo="privacidad" />) },
          { path: "terminos", element: diferida(() => <Legal tipo="terminos" />) },
        ],
      },
      // Trabajador
      {
        path: "/trabajador",
        element: <RequireRole roles={[ROLES.TRABAJADOR]}><TrabajadorLayout /></RequireRole>,
        children: [
          { index: true, element: diferida(Operacion) },
          { path: "simulacion", element: diferida(Simulacion) },
          { path: "simulacion/:idTransaccion", element: diferida(Simulacion) },
          { path: "pedidos", element: diferida(Pedidos) },
          { path: "planes", element: diferida(Planes) },
          cuenta,
        ],
      },
      // Cliente
      {
        path: "/cliente",
        element: <RequireRole roles={[ROLES.CLIENTE]}><ClienteLayout /></RequireRole>,
        children: [
          { index: true, element: <ClienteInicio /> },
          { path: "pedidos/nuevo", element: <NuevoPedido /> },
          cuenta,
        ],
      },
      // Administrador
      {
        path: "/admin",
        element: <RequireRole roles={[ROLES.ADMINISTRADOR]}><AdminLayout /></RequireRole>,
        children: [
          { index: true, element: <AdminParametros /> },
          { path: "ubicaciones", element: <AdminUbicaciones /> },
          { path: "aeropuertos", element: <AdminAeropuertos /> },
          cuenta,
        ],
      },
      { path: "*", element: <PublicLayout />, children: [{ path: "*", element: <NotFound /> }] },
    ],
  },
]);

export default router;
