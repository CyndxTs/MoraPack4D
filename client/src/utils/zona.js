// Zona horaria de la sesión: el huso del país del usuario (horas respecto de UTC, como lo guarda el server).
// Sin sesión se usa la del navegador. El server siempre trabaja en UTC; la conversión se hace solo en el front.
let offsetHoras = -new Date().getTimezoneOffset() / 60;
let pais = null;
const oyentes = new Set();

export function setZona(husoHorario, nombrePais) {
  const nuevo = Number.isFinite(husoHorario) ? husoHorario : -new Date().getTimezoneOffset() / 60;
  if (nuevo === offsetHoras && nombrePais === pais) return;
  offsetHoras = nuevo;
  pais = nombrePais ?? null;
  oyentes.forEach((fn) => fn());
}

export const offsetMs = () => offsetHoras * 3600000;
export const offsetHorasZona = () => offsetHoras;

// "UTC−5", "UTC+1", "UTC+5:30"
export function zonaCorta() {
  const signo = offsetHoras < 0 ? "−" : "+";
  const abs = Math.abs(offsetHoras);
  const h = Math.floor(abs);
  const m = Math.round((abs - h) * 60);
  return `UTC${signo}${h}${m ? `:${String(m).padStart(2, "0")}` : ""}`;
}

// "hora de Perú (UTC−5)"
export const zonaLarga = () => (pais ? `hora de ${pais} (${zonaCorta()})` : `hora local (${zonaCorta()})`);

export function alCambiarZona(fn) {
  oyentes.add(fn);
  return () => oyentes.delete(fn);
}
