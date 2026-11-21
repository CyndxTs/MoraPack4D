import { offsetMs } from "./zona";

// Utilidades de fecha/hora. Internamente TODO es epoch ms (UTC) y el server habla en UTC.
// Lo que se muestra y lo que se escribe en formularios está en la zona de la sesión (utils/zona.js).
// El server envía "dd/MM/yyyy HH:mm" y acepta "dd/MM/yyyy HH:mm" o "yyyy-MM-dd HH:mm:ss".

const pad = (n) => String(n).padStart(2, "0");

// "dd/MM/yyyy HH:mm" | "yyyy-MM-dd HH:mm[:ss]" (UTC) -> ms. Devuelve null si no se puede leer.
export function parseServerDateTime(str) {
  if (!str || typeof str !== "string") return null;
  const s = str.trim();
  let m = s.match(/^(\d{2})\/(\d{2})\/(\d{4})\s+(\d{2}):(\d{2})(?::(\d{2}))?$/);
  if (m) return Date.UTC(+m[3], +m[2] - 1, +m[1], +m[4], +m[5], +(m[6] || 0));
  m = s.match(/^(\d{4})-(\d{2})-(\d{2})[ T](\d{2}):(\d{2})(?::(\d{2}))?$/);
  if (m) return Date.UTC(+m[1], +m[2] - 1, +m[3], +m[4], +m[5], +(m[6] || 0));
  return null;
}

// ms -> "yyyy-MM-dd HH:mm:ss" en UTC (formato que el server acepta)
export function toServerDateTime(ms) {
  if (ms == null || Number.isNaN(ms)) return null;
  const d = new Date(ms);
  return `${d.getUTCFullYear()}-${pad(d.getUTCMonth() + 1)}-${pad(d.getUTCDate())} ${pad(d.getUTCHours())}:${pad(d.getUTCMinutes())}:${pad(d.getUTCSeconds())}`;
}

// ms -> "dd/MM/yyyy HH:mm[:ss]" en la zona de la sesión
export function formatFecha(ms, conSegundos = false) {
  if (ms == null || Number.isNaN(ms)) return "-";
  const d = new Date(ms + offsetMs());
  const base = `${pad(d.getUTCDate())}/${pad(d.getUTCMonth() + 1)}/${d.getUTCFullYear()} ${pad(d.getUTCHours())}:${pad(d.getUTCMinutes())}`;
  return conSegundos ? `${base}:${pad(d.getUTCSeconds())}` : base;
}

// ms -> "HH:mm" en la zona de la sesión
export function formatHora(ms) {
  if (ms == null || Number.isNaN(ms)) return "-";
  const d = new Date(ms + offsetMs());
  return `${pad(d.getUTCHours())}:${pad(d.getUTCMinutes())}`;
}

// Texto de fecha del server (UTC) -> texto en la zona de la sesión
export function fechaServerALocal(str) {
  const ms = parseServerDateTime(str);
  return ms == null ? (str || "-") : formatFecha(ms);
}

// "HH:mm" (UTC, hora diaria de un plan) -> "HH:mm" en la zona de la sesión, con "+1"/"−1" si cambia de día
export function horaUTCALocal(hhmm) {
  const m = /^(\d{1,2}):(\d{2})/.exec(hhmm || "");
  if (!m) return hhmm || "-";
  let min = (+m[1]) * 60 + (+m[2]) + Math.round(offsetMs() / 60000);
  const dia = Math.floor(min / 1440);
  min = ((min % 1440) + 1440) % 1440;
  return `${pad(Math.floor(min / 60))}:${pad(min % 60)}${dia > 0 ? " (+1)" : dia < 0 ? " (−1)" : ""}`;
}

// Inputs <input type="date"> + <input type="time"> en la zona de la sesión -> ms
export function fromInputs(fecha, hora) {
  if (!fecha || !hora) return null;
  const ms = Date.parse(`${fecha}T${hora.length === 5 ? `${hora}:00` : hora}Z`);
  return Number.isNaN(ms) ? null : ms - offsetMs();
}

export const toInputDate = (ms) => new Date(ms + offsetMs()).toISOString().slice(0, 10);
export const toInputTime = (ms) => new Date(ms + offsetMs()).toISOString().slice(11, 16);

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
