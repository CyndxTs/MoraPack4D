import { suscribir, esperarConexion, normalizarEstado } from "./stompClient";

/**
 * Seguimiento de tareas en segundo plano del server (importaciones, exportaciones).
 * El server genera el id y empieza a publicar de inmediato, así que una suscripción hecha
 * DESPUÉS de recibir el id puede perder los mensajes (sobre todo en tareas rápidas).
 * Por eso se escucha con comodín desde antes y se guardan los mensajes por id.
 */
export function crearSeguidor(prefijoProgreso, prefijoEstado) {
  const estados = new Map(); // id -> último estado
  const progresos = new Map(); // id -> último progreso
  const esperas = new Map(); // id -> Set<{ onEstado, onProgreso }>
  let listo = null;

  const idDe = (destino, prefijo) => (destino?.startsWith(prefijo) ? destino.substring(prefijo.length) : null);

  function preparar() {
    if (!listo) {
      if (prefijoProgreso) {
        suscribir(`${prefijoProgreso}*`, (cuerpo, destino) => {
          const id = idDe(destino, prefijoProgreso);
          if (!id) return;
          progresos.set(id, cuerpo);
          esperas.get(id)?.forEach((w) => w.onProgreso?.(cuerpo));
        });
      }
      suscribir(`${prefijoEstado}*`, (cuerpo, destino) => {
        const id = idDe(destino, prefijoEstado);
        if (!id) return;
        const estado = normalizarEstado(cuerpo);
        estados.set(id, estado);
        esperas.get(id)?.forEach((w) => w.onEstado?.(estado));
      });
      listo = esperarConexion();
    }
    return listo;
  }

  /**
   * Promesa que se resuelve con el estado final (DETENIDO). Si el server no publica nada en
   * 'silencioMs', se resuelve con estadoFinalizacion "DESCONOCIDO" (para no colgar la pantalla).
   */
  function esperar(id, onProgreso, silencioMs = 20000) {
    return new Promise((resolve) => {
      let timer = null;
      let terminado = false;
      let quitarExactas = [];
      const terminar = (estado) => {
        if (terminado) return;
        terminado = true;
        clearTimeout(timer);
        quitarExactas.forEach((q) => q());
        esperas.get(id)?.delete(espera);
        estados.delete(id);
        progresos.delete(id);
        resolve(estado);
      };
      const reiniciarTimer = () => {
        clearTimeout(timer);
        timer = setTimeout(() => terminar({ estadoEjecucion: "DETENIDO", estadoFinalizacion: "DESCONOCIDO" }), silencioMs);
      };
      const espera = {
        onProgreso: (p) => { reiniciarTimer(); onProgreso?.(p); },
        onEstado: (e) => { reiniciarTimer(); if (e.estadoEjecucion === "DETENIDO") terminar(e); },
      };
      if (!esperas.has(id)) esperas.set(id, new Set());
      esperas.get(id).add(espera);
      // Además del comodín, el tópico exacto (por si el broker no soportara patrones)
      quitarExactas = [
        suscribir(`${prefijoEstado}${id}`, (cuerpo) => espera.onEstado(normalizarEstado(cuerpo))),
        ...(prefijoProgreso ? [suscribir(`${prefijoProgreso}${id}`, (cuerpo) => espera.onProgreso(cuerpo))] : []),
      ];
      reiniciarTimer();
      // Mensajes que llegaron antes de que conociéramos el id
      if (progresos.has(id)) espera.onProgreso(progresos.get(id));
      if (estados.has(id)) espera.onEstado(estados.get(id));
    });
  }

  return { preparar, esperar };
}
