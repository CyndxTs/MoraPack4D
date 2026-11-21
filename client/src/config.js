// Configuración global del cliente. El server trabaja en UTC; el front muestra y pide las horas en la zona de la sesión.

// URL del server. En desarrollo va vacía: Vite redirige /api y /ws a http://localhost:8080 (vite.config.js).
// En producción (Render) se define VITE_API_URL al compilar, p. ej. https://morapack-server.onrender.com
const SERVER_URL = (import.meta.env.VITE_API_URL || "").replace(/\/+$/, "");

export const API_BASE = `${SERVER_URL}/api`;

// STOMP sobre WebSocket nativo (el server no usa SockJS). http -> ws, https -> wss
export const WS_URL = SERVER_URL
  ? `${SERVER_URL.replace(/^http/, "ws")}/ws`
  : `${window.location.protocol === "https:" ? "wss" : "ws"}://${window.location.host}/ws`;

// Tamaño de página para "traer todo" en catálogos pequeños (aeropuertos, planes, países)
export const TAMANIO_CATALOGO = 5000;

// Tamaño de página de las tablas
export const TAMANIO_PAGINA = 10;

// Google Identity Services (mismo Client ID que GOOGLE_CLIENT_ID del server). Sin valor no se muestra el botón.
export const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID || "";

// Cada cuánto se recalcula la posición de los aviones (ms reales). No usar requestAnimationFrame: re-renderiza 60 veces/s
export const RELOJ_TICK_MS = 250;

// Sesión por dispositivo (todas las pestañas del navegador comparten la misma cuenta y sus tokens)
export const SESION_STORAGE_KEY = "morapack.sesion";

// Mapa base: países de Natural Earth en /public/geo/paises.json (sin mosaicos ni API key).
