import React from "react";
import EnConstruccion from "../comun/EnConstruccion";

export const AdminParametros = () => (
  <EnConstruccion titulo="Parámetros de la operación" descripcion="Parámetros por defecto que usa la replanificación de la operación (plazos, estancias, GVNS y pesos)." />
);

export const AdminUbicaciones = () => (
  <EnConstruccion titulo="Países y continentes" descripcion="Gestión de las ubicaciones soportadas (nombre del país, huso horario y continente)." />
);

export const AdminAeropuertos = () => (
  <EnConstruccion titulo="Aeropuertos y sedes" descripcion="Gestión de aeropuertos (capacidad, coordenadas) y marcado de sedes." />
);
