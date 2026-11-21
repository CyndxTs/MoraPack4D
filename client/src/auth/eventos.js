// Bus mínimo de eventos de autenticación (fuera de React: lo usan axios y el cliente STOMP)
//  - "sesion"           : la sesión guardada cambió (login, logout, renovación, otra pestaña)
//  - "sesion-expirada"  : la sesión de 12 h terminó o fue cerrada desde el server
//  - "requiere-login"   : una petición necesitaba sesión y no había
const oyentes = new Map();

export function escuchar(evento, fn) {
  if (!oyentes.has(evento)) oyentes.set(evento, new Set());
  oyentes.get(evento).add(fn);
  return () => oyentes.get(evento)?.delete(fn);
}

export function emitir(evento, dato) {
  oyentes.get(evento)?.forEach((fn) => {
    try { fn(dato); } catch (e) { console.error(e); }
  });
}
