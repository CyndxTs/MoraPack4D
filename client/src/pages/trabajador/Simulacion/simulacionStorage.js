// Persistencia de una simulación en curso:
//  - meta (pequeña) en localStorage: la misma simulación se puede abrir en otra pestaña con su URL
//  - última solución en sessionStorage: al recargar (F5) el mapa no queda vacío hasta el siguiente bloque

const KEY_META = (id) => `morapack.simulacion.${id}`;
const KEY_SOL = (id) => `morapack.simulacion.${id}.solucion`;
const DOS_DIAS_MS = 2 * 24 * 3600 * 1000;

export function leerMeta(id) {
  if (!id) return null;
  try { return JSON.parse(localStorage.getItem(KEY_META(id)) || "null"); } catch { return null; }
}

export function guardarMeta(id, meta) {
  try { localStorage.setItem(KEY_META(id), JSON.stringify(meta)); } catch { /* storage lleno */ }
  return meta;
}

export function borrarSimulacion(id) {
  localStorage.removeItem(KEY_META(id));
  sessionStorage.removeItem(KEY_SOL(id));
}

export function leerSolucion(id) {
  try { return JSON.parse(sessionStorage.getItem(KEY_SOL(id)) || "null"); } catch { return null; }
}

export function guardarSolucion(id, solucion) {
  try { sessionStorage.setItem(KEY_SOL(id), JSON.stringify(solucion)); } catch { /* demasiado grande: se omite */ }
}

// Limpia metas de simulaciones viejas que nunca se cerraron
export function limpiarSimulacionesViejas() {
  const ahora = Date.now();
  Object.keys(localStorage)
    .filter((k) => /^morapack\.simulacion\.[^.]+$/.test(k))
    .forEach((k) => {
      try {
        const meta = JSON.parse(localStorage.getItem(k));
        if (!meta?.creadaEn || ahora - meta.creadaEn > DOS_DIAS_MS) localStorage.removeItem(k);
      } catch { localStorage.removeItem(k); }
    });
}

// La simulación abierta más reciente de este navegador (para volver a ella al entrar a "Simulación")
export function simulacionActiva() {
  let mejor = null;
  Object.keys(localStorage)
    .filter((k) => /^morapack\.simulacion\.[^.]+$/.test(k))
    .forEach((k) => {
      try {
        const meta = JSON.parse(localStorage.getItem(k));
        if (meta?.creadaEn && (!mejor || meta.creadaEn > mejor.creadaEn)) mejor = { id: k.split(".")[2], creadaEn: meta.creadaEn };
      } catch { /* ignorar */ }
    });
  return mejor?.id ?? null;
}
