// src/utils/formatDate.js

export const formatISOToDDMMYYYY = (isoString) => {
  if (!isoString) return "";

  const fecha = new Date(isoString);

  const dia = String(fecha.getUTCDate()).padStart(2, "0");
  const mes = String(fecha.getUTCMonth() + 1).padStart(2, "0");
  const anio = fecha.getUTCFullYear();

  const horas = String(fecha.getUTCHours()).padStart(2, "0");
  const minutos = String(fecha.getUTCMinutes()).padStart(2, "0");
  const segundos = String(fecha.getUTCSeconds()).padStart(2, "0");

  return `${dia}/${mes}/${anio} ${horas}:${minutos}:${segundos}`;
};


// Para ordenar fechas en tu frontend
export const parseDDMMYYYYToDate = (fechaString) => {
  if (!fechaString) return null;

  const [fecha, hora] = fechaString.split(" ");
  const [dia, mes, anio] = fecha.split("/");

  return new Date(`${anio}-${mes}-${dia}T${hora}`);
};
