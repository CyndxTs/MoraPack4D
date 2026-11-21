// Utilidades de fecha/hora. TODO se maneja en UTC (milisegundos epoch).
// El server envía "dd/MM/yyyy HH:mm" y acepta "dd/MM/yyyy HH:mm" o "yyyy-MM-dd HH:mm:ss".

const pad = (n) => String(n).padStart(2, "0");

// "dd/MM/yyyy HH:mm" | "yyyy-MM-dd HH:mm[:ss]" -> ms (UTC). Devuelve null si no se puede leer.
export function parseServerDateTime(str) {
  if (!str || typeof str !== "string") return null;
  const s = str.trim();
  let m = s.match(/^(\d{2})\/(\d{2})\/(\d{4})\s+(\d{2}):(\d{2})(?::(\d{2}))?$/);
  if (m) return Date.UTC(+m[3], +m[2] - 1, +m[1], +m[4], +m[5], +(m[6] || 0));
  m = s.match(/^(\d{4})-(\d{2})-(\d{2})[ T](\d{2}):(\d{2})(?::(\d{2}))?$/);
  if (m) return Date.UTC(+m[1], +m[2] - 1, +m[3], +m[4], +m[5], +(m[6] || 0));
  return null;
}

// ms -> "yyyy-MM-dd HH:mm:ss" (formato que el server acepta)
export function toServerDateTime(ms) {
  if (ms == null || Number.isNaN(ms)) return null;
  const d = new Date(ms);
  return `${d.getUTCFullYear()}-${pad(d.getUTCMonth() + 1)}-${pad(d.getUTCDate())} ${pad(d.getUTCHours())}:${pad(d.getUTCMinutes())}:${pad(d.getUTCSeconds())}`;
}

// ms -> "dd/MM/yyyy HH:mm" (para mostrar)
export function formatUTC(ms, conSegundos = false) {
  if (ms == null || Number.isNaN(ms)) return "-";
  const d = new Date(ms);
  const base = `${pad(d.getUTCDate())}/${pad(d.getUTCMonth() + 1)}/${d.getUTCFullYear()} ${pad(d.getUTCHours())}:${pad(d.getUTCMinutes())}`;
  return conSegundos ? `${base}:${pad(d.getUTCSeconds())}` : base;
}

// Inputs <input type="date"> + <input type="time"> interpretados como UTC -> ms
export function fromInputsUTC(fecha, hora) {
  if (!fecha || !hora) return null;
  const ms = Date.parse(`${fecha}T${hora.length === 5 ? `${hora}:00` : hora}Z`);
  return Number.isNaN(ms) ? null : ms;
}

export const toInputDateUTC = (ms) => new Date(ms).toISOString().slice(0, 10);
export const toInputTimeUTC = (ms) => new Date(ms).toISOString().slice(11, 16);

// Segundos -> "1d : 02h : 03m : 04s"
export function formatDuration(totalSeconds) {
  const s = Math.max(0, Math.floor(totalSeconds || 0));
  const days = Math.floor(s / 86400);
  const parts = [];
  if (days > 0) parts.push(`${days}d`);
  parts.push(`${pad(Math.floor((s % 86400) / 3600))}h`, `${pad(Math.floor((s % 3600) / 60))}m`, `${pad(s % 60)}s`);
  return parts.join(" : ");
}

// Milisegundos restantes -> "mm:ss"
export function formatCountdown(ms) {
  const s = Math.max(0, Math.ceil(ms / 1000));
  return `${pad(Math.floor(s / 60))}:${pad(s % 60)}`;
}
