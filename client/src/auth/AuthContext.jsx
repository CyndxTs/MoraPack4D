import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { iniciarSesion, cerrarSesion } from "../api/auth";
import { SESION_STORAGE_KEY } from "../config";
import { borrarSesion, guardarSesion, leerSesion } from "./session";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [sesion, setSesion] = useState(() => leerSesion());

  // Otra pestaña inició o cerró sesión: esta pestaña adopta la misma cuenta
  useEffect(() => {
    const onStorage = (e) => {
      if (e.key === SESION_STORAGE_KEY || e.key === null) setSesion(leerSesion());
    };
    window.addEventListener("storage", onStorage);
    return () => window.removeEventListener("storage", onStorage);
  }, []);

  const login = useCallback(async (correo, contrasenia, tipoUsuario) => {
    const usuario = await iniciarSesion(correo.trim(), contrasenia, tipoUsuario);
    const actual = leerSesion();
    // Si había otra cuenta abierta en este navegador, se cierra (una sola cuenta por navegador)
    if (actual && actual.usuario.correo !== usuario.correo) {
      cerrarSesion(actual.usuario.correo, actual.usuario.tipoUsuario).catch(() => {});
    }
    setSesion(guardarSesion(usuario));
    return usuario;
  }, []);

  const logout = useCallback(async () => {
    const actual = leerSesion();
    borrarSesion();
    setSesion(null);
    if (actual) {
      try { await cerrarSesion(actual.usuario.correo, actual.usuario.tipoUsuario); } catch { /* sin red: igual se cierra local */ }
    }
  }, []);

  const value = useMemo(() => ({ usuario: sesion?.usuario ?? null, login, logout }), [sesion, login, logout]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth debe usarse dentro de <AuthProvider>");
  return ctx;
}
