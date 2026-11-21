import React, { useEffect } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "./AuthContext";
import { RUTA_INICIO_POR_ROL, puedeEntrar } from "./session";
import "./requireRole.scss";

// Protege un grupo de rutas. Sin sesión: se queda en la URL y abre el modal de acceso. Con otro rol: a su inicio.
export default function RequireRole({ roles, children }) {
  const { usuario, abrirAuth, modal, estaSaliendo } = useAuth();
  const location = useLocation();
  const desde = location.pathname + location.search;

  useEffect(() => {
    if (!usuario && !modal && !estaSaliendo()) abrirAuth("login", { desde });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [usuario]);

  if (!usuario) {
    return (
      <div className="acceso-requerido">
        <div className="acceso-requerido__card">
          <span className="acceso-requerido__icono" aria-hidden="true">🔒</span>
          <h2>Esta sección requiere iniciar sesión</h2>
          <p>Ingresa con tu cuenta para continuar donde estabas.</p>
          <button type="button" onClick={() => abrirAuth("login", { desde })}>Iniciar sesión</button>
        </div>
      </div>
    );
  }
  if (!puedeEntrar(usuario, roles)) {
    return <Navigate to={RUTA_INICIO_POR_ROL[usuario.tipoUsuario] ?? "/"} replace />;
  }
  return children;
}
