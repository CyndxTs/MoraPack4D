import { parseServerDateTime } from "../utils/fechas";
import { generateGeodesicPath } from "./geo";

/**
 * Convierte el SolucionDTO del server en las estructuras que usan el mapa y el sidebar.
 *
 *   airports : { [codigo]: { code, city, country, continent, lat, lng, name, capacidad, esSede, registros[] } }
 *   flights  : [{ code, origin, destination, startTime, endTime, startMs, endMs, capacity, planeCapacity, path, rutas[] }]
 *   routes   : [{ ...RutaDTO, originCity, destinationCity }]
 *   orders   : [{ codigo, codCliente, codDestino, cantidadSolicitada, estado, fechaHoraGeneracion, generacionMs, segmentaciones[] }]
 *   noAtendidos : [PedidoNoAtendidoDTO]
 *   lotesPorVuelo : Map<codVuelo, [{ pedidoCodigo, pedidoDestino, loteCodigo, cantidad }]>
 */

export function aeropuertosDesdeCatalogo(dtos = []) {
  const map = {};
  dtos.forEach((a) => {
    map[a.codigo] = {
      code: a.codigo,
      city: a.ciudad,
      country: a.pais,
      continent: a.continente,
      name: a.alias || a.ciudad,
      lat: a.latitud,
      lng: a.longitud,
      capacidad: a.capacidad ?? 0,
      esSede: !!a.esSede,
      registros: [],
    };
  });
  return map;
}

export function crearEscenarioVacio(airports = {}) {
  return { airports, flights: [], routes: [], orders: [], noAtendidos: [], metricas: null, lotesPorVuelo: new Map(), recibidoEn: null };
}

// Vacía lo dinámico (al detener una simulación) pero conserva el catálogo de aeropuertos
export function limpiarEscenario(prev) {
  const airports = {};
  Object.values(prev.airports).forEach((ap) => (airports[ap.code] = { ...ap, registros: [] }));
  return crearEscenarioVacio(airports);
}

const porCodigo = (lista, key = "codigo") => new Map(lista.map((x) => [x[key], x]));

/**
 * @param prev      escenario anterior
 * @param solucion  SolucionDTO
 * @param nowMs     instante actual del escenario (real en operación, simulado en simulación)
 * @param acumular  true en simulación: los pedidos/rutas que ya salieron de la RAM del server se conservan
 */
export function aplicarSolucion(prev, solucion, nowMs, { acumular = false } = {}) {
  if (!solucion) return prev;

  // 1. Aeropuertos: el catálogo base + lo que trae la solución (los no transitados quedan sin registros)
  const airports = {};
  Object.values(prev.airports).forEach((ap) => (airports[ap.code] = { ...ap, registros: [] }));
  (solucion.aeropuertosTransitados || []).forEach((a) => {
    const base = airports[a.codigo] || {};
    airports[a.codigo] = {
      ...base,
      code: a.codigo,
      city: a.ciudad ?? base.city,
      country: a.pais ?? base.country,
      continent: a.continente ?? base.continent,
      name: a.alias || a.ciudad || base.name,
      lat: a.latitud ?? base.lat,
      lng: a.longitud ?? base.lng,
      capacidad: a.capacidad ?? base.capacidad ?? 0,
      esSede: a.esSede ?? base.esSede ?? false,
      registros: (a.registros || [])
        .filter((r) => r.sigueVigente)
        .map((r) => ({ ...r, ingresoMs: parseServerDateTime(r.fechaHoraIngreso), egresoMs: parseServerDateTime(r.fechaHoraEgreso) })),
    };
  });

  // 2. Rutas
  const rutasNuevas = (solucion.rutasEnOperacion || [])
    .filter((r) => r.estado === "OPERATIVA" || r.estado === "FINALIZADA")
    .map((r) => ({
      ...r,
      originCity: airports[r.codOrigen]?.city || r.codOrigen,
      destinationCity: airports[r.codDestino]?.city || r.codDestino,
    }));
  const routes = acumular ? [...new Map([...porCodigo(prev.routes), ...porCodigo(rutasNuevas)]).values()] : rutasNuevas;
  const rutasPorCodigo = porCodigo(routes);

  // 3. Vuelos: se actualiza la carga de los conocidos y se conservan los que siguen en el aire
  const rutasDeVuelo = new Map();
  routes.forEach((r) => (r.codVuelos || []).forEach((cv) => {
    if (!rutasDeVuelo.has(cv)) rutasDeVuelo.set(cv, new Set());
    rutasDeVuelo.get(cv).add(`${r.codOrigen} → ${r.codDestino}`);
  }));
  const previos = porCodigo(prev.flights, "code");
  const nuevos = new Set();
  const flights = [];
  (solucion.vuelosEnTransito || []).forEach((v) => {
    const origin = airports[v.codOrigen];
    const destination = airports[v.codDestino];
    if (!origin || !destination) return;
    nuevos.add(v.codigo);
    const anterior = previos.get(v.codigo);
    const startMs = parseServerDateTime(v.fechaHoraSalida);
    const endMs = parseServerDateTime(v.fechaHoraLlegada);
    flights.push({
      code: v.codigo,
      origin,
      destination,
      originName: origin.name,
      destinationName: destination.name,
      startTime: v.fechaHoraSalida,
      endTime: v.fechaHoraLlegada,
      startMs,
      endMs,
      capacity: v.capacidadOcupada ?? 0,
      planeCapacity: v.capacidadMaxima ?? 0,
      path: anterior?.path ?? generateGeodesicPath(origin.lat, origin.lng, destination.lat, destination.lng, 120),
      rutas: [...(rutasDeVuelo.get(v.codigo) ?? [])],
    });
  });
  prev.flights.forEach((f) => {
    if (!nuevos.has(f.code) && nowMs < f.endMs) flights.push(f);
  });
  const vuelosPorCodigo = porCodigo(flights, "code");

  // 4. Pedidos (segmentación vigente -> lotes con su cadena de vuelos)
  const pedidosNuevos = (solucion.pedidosAtendidos || []).map((p) => {
    const segs = p.segmentaciones?.length ? p.segmentaciones : p.segmentacionVigente ? [p.segmentacionVigente] : [];
    const segmentaciones = segs.map((seg) => ({
      codigo: seg.codigo,
      fechaHoraAplicacion: seg.fechaHoraAplicacion,
      fechaHoraSustitucion: seg.fechaHoraSustitucion || null,
      lotes: (seg.lotesPorRuta || []).map((lpr) => {
        const ruta = rutasPorCodigo.get(lpr.codRuta);
        const lote = lpr.lote || {};
        return {
          codRuta: lpr.codRuta,
          loteCodigo: lote.codigo,
          loteTamanio: lote.tamanio,
          loteEstado: lote.estado,
          vuelos: lpr.codVuelos?.length ? lpr.codVuelos : ruta?.codVuelos || [],
          origenCode: ruta?.codOrigen,
          destinoCode: ruta?.codDestino,
          origenNombre: airports[ruta?.codOrigen]?.city || ruta?.codOrigen,
          destinoNombre: airports[ruta?.codDestino]?.city || ruta?.codDestino,
        };
      }),
    }));
    return {
      codigo: p.codigo,
      codCliente: p.codCliente,
      codDestino: p.codDestino,
      cantidadSolicitada: p.cantidadSolicitada,
      fueAtendido: p.fueAtendido,
      estado: p.estado,
      fechaHoraGeneracion: p.fechaHoraGeneracion,
      generacionMs: parseServerDateTime(p.fechaHoraGeneracion),
      fechaHoraExpiracion: p.fechaHoraExpiracion,
      segmentaciones,
    };
  });
  const orders = acumular ? [...new Map([...porCodigo(prev.orders), ...porCodigo(pedidosNuevos)]).values()] : pedidosNuevos;

  // 5. Índice vuelo -> lotes a bordo, y fin de entrega por pedido
  const lotesPorVuelo = new Map();
  orders.forEach((pedido) => {
    let entregaMs = null;
    pedido.segmentaciones.forEach((seg) => seg.lotes.forEach((lote) => {
      lote.vuelos.forEach((cv) => {
        if (!lotesPorVuelo.has(cv)) lotesPorVuelo.set(cv, []);
        lotesPorVuelo.get(cv).push({ pedidoCodigo: pedido.codigo, pedidoDestino: pedido.codDestino, loteCodigo: lote.loteCodigo, cantidad: lote.loteTamanio });
        const f = vuelosPorCodigo.get(cv);
        if (f && (entregaMs === null || f.endMs > entregaMs)) entregaMs = f.endMs;
      });
    }));
    pedido.entregaMs = entregaMs;
  });

  return {
    airports,
    flights,
    routes,
    orders,
    noAtendidos: solucion.pedidosNoAtendidos || [],
    lotesPorVuelo,
    metricas: {
      utilizacionTemporal: solucion.ratioPromedioDeUtilizacionTemporal,
      desviacionEspacial: solucion.ratioPromedioDeDesviacionEspacial,
      disposicionOperacional: solucion.ratioPromedioDeDisposicionOperacional,
    },
    recibidoEn: Date.now(),
  };
}

// Un pedido se considera entregado cuando aterriza el último vuelo de sus lotes
export function esPedidoEntregado(pedido, nowMs) {
  return pedido.entregaMs != null && nowMs >= pedido.entregaMs;
}
