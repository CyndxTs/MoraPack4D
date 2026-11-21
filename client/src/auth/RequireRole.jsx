import React from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "./AuthContext";
import { RUTA_INICIO_POR_ROL } from "./session";

// Protege un grupo de rutas: sin sesión -> /login; con otro rol -> su propio inicio
export default function RequireRole({ rol, children }) {
  const { usuario } = useAuth();
  const location = useLocation();
  if (!usuario) {
    return <Navigate to="/login" replace state={{ desde: location.pathname + location.search }} />;
  }
  if (usuario.tipoUsuario !== rol) {
    return <Navigate to={RUTA_INICIO_POR_ROL[usuario.tipoUsuario] ?? "/"} replace />;
  }
  return children;
}
