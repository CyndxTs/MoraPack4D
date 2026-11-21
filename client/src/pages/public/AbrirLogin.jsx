import React, { useEffect } from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { RUTA_INICIO_POR_ROL } from "../../auth/session";

// Compatibilidad con enlaces viejos a /login: abre el modal sobre la portada
export default function AbrirLogin() {
  const { usuario, abrirAuth } = useAuth();
  useEffect(() => { if (!usuario) abrirAuth("login"); }, [usuario, abrirAuth]);
  return <Navigate to={usuario ? RUTA_INICIO_POR_ROL[usuario.tipoUsuario] ?? "/" : "/"} replace />;
}
