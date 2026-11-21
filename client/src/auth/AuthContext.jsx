import React, { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from "react";
import * as api from "../api/auth";
import { SESION_STORAGE_KEY } from "../config";
import { useNotify } from "../notifications/NotificationContext";
import { actualizarUsuarioEnSesion, borrarSesion, guardarSesion, leerSesion } from "./session";
import { emitir, escuchar } from "./eventos";
import { setZona } from "../utils/zona";

const AuthContext = createContext(null);

/**
 * Sesión + modal de acceso (login / registro / códigos / Google) que se abre SOBRE la página actual.
 * 'modal' = { vista, desde, aviso } o null. 'desde' es la ruta a la que se quería entrar.
 */
export function AuthProvider({ children }) {
  const notify = useNotify();
  const [sesion, setSesion] = useState(() => leerSesion());
  const [modal, setModal] = useState(null);
  // Cierre de sesión voluntario: las rutas protegidas no deben abrir el modal mientras se sale
  const saliendoHasta = useRef(0);
  const estaSaliendo = useCallback(() => Date.now() < saliendoHasta.current, []);

  // Las horas se muestran en el huso del país de la cuenta (sin sesión: el del navegador)
  setZona(sesion?.usuario?.husoHorario, sesion?.usuario?.pais);

  // Cambios de sesión de esta pestaña (bus) y de otras pestañas (storage)
  useEffect(() => {
    const quitar = escuchar("sesion", () => setSesion(leerSesion()));
    const onStorage = (e) => {
      if (e.key === SESION_STORAGE_KEY || e.key === null) {
        setSesion(leerSesion());
        emitir("sesion-externa", leerSesion());
      }
    };
    window.addEventListener("storage", onStorage);
    return () => { quitar(); window.removeEventListener("storage", onStorage); };
  }, []);

  // Sesión vencida o cerrada desde el server: aviso + modal de login sobre la misma página
  useEffect(() => escuchar("sesion-expirada", (mensaje) => {
    notify("warning", mensaje || "Tu sesión expiró. Vuelve a iniciar sesión.");
    setModal({ vista: "login", aviso: "Tu sesión expiró. Vuelve a iniciar sesión para continuar." });
  }), [notify]);

  useEffect(() => escuchar("requiere-login", () => setModal((m) => m ?? { vista: "login" })), []);

  // Fin de las 12 h aunque no haya peticiones
  useEffect(() => {
    if (!sesion?.expiraSesion) return undefined;
    const ms = sesion.expiraSesion - Date.now();
    const t = setTimeout(() => {
      if (leerSesion()) return;
      borrarSesion();
      emitir("sesion-expirada");
    }, Math.max(0, Math.min(ms + 500, 2 ** 31 - 1)));
    return () => clearTimeout(t);
  }, [sesion?.expiraSesion]);

  // Respuesta con tokens -> sesión guardada (si había otra cuenta abierta en este dispositivo, se cierra)
  const completarIngreso = useCallback((respuesta) => {
    const anterior = leerSesion();
    if (anterior && anterior.tokenRefresco !== respuesta.tokenRefresco) {
      api.cerrarSesion(anterior.tokenRefresco).catch(() => {});
    }
    guardarSesion(respuesta);
    return respuesta.usuario;
  }, []);

  const login = useCallback(async (correo, contrasenia) => completarIngreso(await api.iniciarSesion(correo.trim(), contrasenia)), [completarIngreso]);

  const logout = useCallback(async () => {
    const actual = leerSesion();
    saliendoHasta.current = Date.now() + 1500;
    setModal(null);
    borrarSesion();
    emitir("logout");
    if (actual) {
      try { await api.cerrarSesion(actual.tokenRefresco); } catch { /* sin red: igual se cierra local */ }
    }
  }, []);

  const abrirAuth = useCallback((vista = "login", opciones = {}) => setModal({ vista, ...opciones }), []);
  const cerrarAuth = useCallback(() => setModal(null), []);

  const value = useMemo(() => ({
    usuario: sesion?.usuario ?? null,
    expiraSesion: sesion?.expiraSesion ?? null,
    login,
    logout,
    completarIngreso,
    actualizarUsuario: actualizarUsuarioEnSesion,
    modal,
    abrirAuth,
    cerrarAuth,
    estaSaliendo,
  }), [sesion, login, logout, completarIngreso, modal, abrirAuth, cerrarAuth, estaSaliendo]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth debe usarse dentro de <AuthProvider>");
  return ctx;
}
