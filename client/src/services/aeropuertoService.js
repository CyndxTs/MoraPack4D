import axios from "axios";

const API = "http://localhost:8080/api/aeropuertos";

export async function getAeropuertosMap(signal) {
  const res = await fetch(API, { signal });
  if (!res.ok) throw new Error("No se pudieron obtener los aeropuertos");
  const data = await res.json(); 
  return Object.fromEntries(
    data.map(a => [
      a.codigo,
      {
        name: a.ciudad,
        lat: a.latitudDEC,
        lng: a.longitudDEC,
        gmt: a.husoHorario,
        capacity: a.capacidad,
        isHQ: a.esSede,
      },
    ])
  );
}