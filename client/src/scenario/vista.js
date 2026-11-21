import { flightStateAt } from "./geo";
import { esPedidoEntregado } from "./buildScenario";

// Todo lo que depende del instante actual (se recalcula en cada tick del reloj)

export function stockEn(airport, nowMs) {
  let stock = 0;
  (airport.registros || []).forEach((r) => {
    if (r.ingresoMs != null && nowMs >= r.ingresoMs && (r.egresoMs == null || nowMs < r.egresoMs)) stock += r.tamLote || 0;
  });
  return stock;
}

export function calcularVista(escenario, nowMs) {
  const activeFlights = [];
  const upcomingFlights = [];
  escenario.flights.forEach((f) => {
    if (typeof f.startMs !== "number" || typeof f.endMs !== "number") return;
    if (nowMs >= f.startMs && nowMs < f.endMs) activeFlights.push({ ...f, ...flightStateAt(f, nowMs) });
    else if (nowMs < f.startMs) upcomingFlights.push({ ...f, progress: 0, arrived: false });
  });
  upcomingFlights.sort((a, b) => a.startMs - b.startMs);

  const airports = Object.values(escenario.airports)
    .filter((ap) => typeof ap.lat === "number" && typeof ap.lng === "number")
    .map((ap) => {
      const stockActual = stockEn(ap, nowMs);
      return { ...ap, stockActual, ocupacion: ap.capacidad > 0 ? stockActual / ap.capacidad : 0 };
    });

  const generados = escenario.orders.filter((p) => p.generacionMs == null || nowMs >= p.generacionMs);
  const entregados = generados.filter((p) => esPedidoEntregado(p, nowMs));
  const enProceso = generados.filter((p) => !esPedidoEntregado(p, nowMs));

  const activos = new Set(activeFlights.map((f) => f.code));
  const routesNow = escenario.routes.filter((r) => (r.codVuelos || []).some((cv) => activos.has(cv)));

  const totalOccupied = activeFlights.reduce((acc, f) => acc + (f.capacity || 0), 0);
  const totalMax = activeFlights.reduce((acc, f) => acc + (f.planeCapacity || 0), 0);

  return {
    activeFlights,
    upcomingFlights,
    airports,
    orders: { generados, entregados, enProceso },
    routesNow,
    fleet: { totalOccupied, totalMax, percentage: totalMax > 0 ? (totalOccupied / totalMax) * 100 : 0 },
  };
}

// Vuelos de un aeropuerto que aún no salen / no llegan
export function vuelosDeAeropuerto(escenario, code, nowMs) {
  const salen = escenario.flights.filter((f) => f.origin?.code === code && nowMs < f.startMs).sort((a, b) => a.startMs - b.startMs);
  const llegan = escenario.flights.filter((f) => f.destination?.code === code && nowMs < f.endMs).sort((a, b) => a.endMs - b.endMs);
  return { salen, llegan };
}

export function estadoDeFlota(percentage) {
  if (percentage >= 90) return { color: "#ef4444", label: "Saturación Crítica" };
  if (percentage >= 50) return { color: "#eab308", label: "Ocupación Media" };
  return { color: "#22c55e", label: "Disponibilidad Alta" };
}

export const getAirportOccupancyClass = (ocupacion) => {
  if (ocupacion == null) return "airport-card--unknown";
  if (ocupacion < 0.5) return "airport-card--low";
  if (ocupacion < 0.8) return "airport-card--medium";
  return "airport-card--high";
};
