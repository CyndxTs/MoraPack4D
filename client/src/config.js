// Configuración global del cliente (todo el sistema trabaja en UTC; el server convierte horas locales al importar)

// REST: Vite redirige /api -> http://localhost:8080 (ver vite.config.js)
export const API_BASE = "/api";

// STOMP sobre WebSocket nativo (el server no usa SockJS)
export const WS_URL = `${window.location.protocol === "https:" ? "wss" : "ws"}://${window.location.host}/ws`;

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
