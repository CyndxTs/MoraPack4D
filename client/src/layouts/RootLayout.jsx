import React from "react";
import { Outlet } from "react-router-dom";
import AuthModal from "../auth/modal/AuthModal";

// Raíz de todas las rutas: el modal de acceso vive aquí (se abre sobre cualquier página)
export default function RootLayout() {
  return (
    <>
      <Outlet />
      <AuthModal />
    </>
  );
}
