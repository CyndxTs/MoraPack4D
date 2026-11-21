// Configuración global del cliente (todo el sistema trabaja en UTC; el server convierte horas locales al importar)

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

// Operación: minutos que se espera, desde el primer pedido nuevo, antes de replanificar automáticamente
export const REPLANIFICACION_DIFERIDA_MIN = 15;

// Cada cuánto se recalcula la posición de los aviones (ms reales). No usar requestAnimationFrame: re-renderiza 60 veces/s
export const RELOJ_TICK_MS = 250;

// Sesión única por navegador (todas las pestañas comparten la misma cuenta)
export const SESION_STORAGE_KEY = "morapack.sesion";

// Mosaicos del mapa (Leaflet). Por defecto Carto "light", que NO requiere API key.
// Se puede cambiar sin tocar código con un .env.local (ver .env.example). Las variables de Vite deben empezar con VITE_.
export const MAPA_TILES_URL = import.meta.env.VITE_MAP_TILES_URL || "https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png";
export const MAPA_TILES_ATRIBUCION = import.meta.env.VITE_MAP_TILES_ATTRIBUTION || '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> &copy; <a href="https://carto.com/">Carto</a>';
