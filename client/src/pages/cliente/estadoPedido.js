import { parseServerDateTime } from "../../utils/fechas";

// Estado de un pedido tal como lo entiende el cliente (a partir del pedido y la hora actual)
export function estadoDePedido(p, nowMs) {
  if (p.estado === "CANCELADO") return { clave: "cancelado", texto: "Cancelado", tono: "neutro" };
  if (p.estado === "CANCELADO_POR_ERROR") return { clave: "fallido", texto: "No se pudo enviar", tono: "alerta" };
  const salida = parseServerDateTime(p.fechaHoraPrimeraSalida);
  const llegada = parseServerDateTime(p.fechaHoraUltimaLlegada);
  if (!p.fueAtendido || salida == null) return { clave: "espera", texto: "Planificando ruta", tono: "aviso", salida, llegada };
  if (salida === llegada) return { clave: "entregado", texto: "Entregado en sede", tono: "ok", salida, llegada };
  if (nowMs < salida) return { clave: "planificado", texto: "Listo para despegar", tono: "info", salida, llegada };
  if (nowMs < llegada) {
    const avance = Math.min(1, Math.max(0, (nowMs - salida) / Math.max(1, llegada - salida)));
    return { clave: "camino", texto: "En camino", tono: "ambar", salida, llegada, avance };
  }
  return { clave: "llego", texto: "Llegó a destino", tono: "ok", salida, llegada };
}

export const ES_ACTIVO = new Set(["espera", "planificado", "camino"]);
