import { useCallback, useEffect, useRef, useState } from "react";
import { TAMANIO_PAGINA } from "../config";
import { escuchar } from "../auth/eventos";

/**
 * Paginación desde el servidor por bloques: cada petición trae PAGINAS_POR_BLOQUE páginas de la tabla
 * (p. ej. 3 × 10 filas). Al pasar a la página 4 se pide el bloque siguiente y se suma a la memoria de la sesión,
 * que se guarda por recurso + filtros (cambiar de pantalla y volver no vuelve a pedir nada).
 */
export const PAGINAS_POR_BLOQUE = 3;

const cache = new Map(); // clave -> { bloques: Map<n, filas[]>, total: number|null }

export function invalidarPaginado(recurso) {
  [...cache.keys()].forEach((k) => { if (!recurso || k.startsWith(`${recurso}|`)) cache.delete(k); });
  oyentes.forEach((fn) => fn(recurso));
}
const oyentes = new Set();
escuchar("logout", () => cache.clear());
escuchar("sesion-expirada", () => cache.clear());

// cargarBloque(paginaServidor, tamanioServidor) -> Promise<{ filas, total }>
export function usePaginado({ recurso, filtros, cargarBloque, tamanio = TAMANIO_PAGINA, onError }) {
  const clave = `${recurso}|${JSON.stringify(filtros ?? {})}`;
  const [pagina, setPagina] = useState(1);
  const [estado, setEstado] = useState({ filas: [], total: null, hayMas: false, cargando: true });
  const [version, setVersion] = useState(0);
  const cargarRef = useRef(cargarBloque);
  cargarRef.current = cargarBloque;
  const errorRef = useRef(onError);
  errorRef.current = onError;
  const claveAnterior = useRef(clave);

  // Filtros nuevos -> primera página
  useEffect(() => {
    if (claveAnterior.current !== clave) { claveAnterior.current = clave; setPagina(1); }
  }, [clave]);

  // Otra pantalla invalidó el recurso (importación, registro, cancelación)
  useEffect(() => {
    const fn = (r) => { if (!r || r === recurso) setVersion((v) => v + 1); };
    oyentes.add(fn);
    return () => oyentes.delete(fn);
  }, [recurso]);

  useEffect(() => {
    let vivo = true;
    const tamBloque = tamanio * PAGINAS_POR_BLOQUE;
    const nBloque = Math.floor((pagina - 1) / PAGINAS_POR_BLOQUE);
    const desde = ((pagina - 1) % PAGINAS_POR_BLOQUE) * tamanio;
    if (!cache.has(clave)) cache.set(clave, { bloques: new Map(), total: null });
    const entrada = cache.get(clave);

    const mostrar = () => {
      const filasBloque = entrada.bloques.get(nBloque) ?? [];
      const filas = filasBloque.slice(desde, desde + tamanio);
      const hayMas = entrada.total != null
        ? pagina * tamanio < entrada.total
        : filasBloque.length > desde + tamanio || filasBloque.length === tamBloque;
      setEstado({ filas, total: entrada.total, hayMas, cargando: false });
    };

    if (entrada.bloques.has(nBloque)) { mostrar(); return undefined; }
    setEstado((e) => ({ ...e, cargando: true }));
    cargarRef.current(nBloque, tamBloque)
      .then(({ filas, total }) => {
        if (!vivo) return;
        entrada.bloques.set(nBloque, filas ?? []);
        if (typeof total === "number") entrada.total = total;
        mostrar();
      })
      .catch((e) => {
        if (!vivo) return;
        setEstado((s) => ({ ...s, cargando: false }));
        errorRef.current?.(e);
      });
    return () => { vivo = false; };
  }, [clave, pagina, tamanio, version]);

  const recargar = useCallback(() => {
    cache.delete(clave);
    setVersion((v) => v + 1);
  }, [clave]);

  const totalPaginas = estado.total != null ? Math.max(1, Math.ceil(estado.total / tamanio)) : null;
  return { ...estado, pagina, setPagina, totalPaginas, recargar };
}
