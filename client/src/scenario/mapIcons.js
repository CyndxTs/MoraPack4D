import L from "leaflet";
import airportIconImg from "../assets/icons/airport.svg";
import sedeIconImg from "../assets/icons/sede.svg";
import planeIconImg from "../assets/icons/planeMora.svg";

// Los íconos se cachean: crear un L.divIcon por avión en cada tick era muy costoso

export const HUB_COLORS = ["#3b82f6", "#10b981", "#8b5cf6", "#f59e0b", "#06b6d4", "#d946ef", "#6366f1", "#84cc16", "#ec4899", "#14b8a6"];

const FILTRO = {
  verde: "invert(54%) sepia(81%) saturate(356%) hue-rotate(85deg) brightness(78%) contrast(115%)",
  amarillo: "invert(80%) sepia(72%) saturate(657%) hue-rotate(3deg) brightness(86%) contrast(118%)",
  rojo: "invert(37%) sepia(79%) saturate(844%) hue-rotate(338deg) brightness(78%) contrast(120%)",
};

// Mismos umbrales que la leyenda: < 50% verde, < 90% amarillo, >= 90% rojo
function nivelDeAvion(capacity, maxCapacity) {
  if (!maxCapacity || maxCapacity <= 0) return "amarillo";
  const ratio = capacity / maxCapacity;
  if (ratio < 0.5) return "verde";
  if (ratio < 0.9) return "amarillo";
  return "rojo";
}

const FILTRO_AEROPUERTO = {
  verde: "invert(38%) sepia(77%) saturate(510%) hue-rotate(85deg) brightness(55%) contrast(120%)",
  amarillo: "invert(74%) sepia(94%) saturate(750%) hue-rotate(2deg) brightness(60%) contrast(125%)",
  rojo: "invert(26%) sepia(88%) saturate(900%) hue-rotate(350deg) brightness(55%) contrast(130%)",
};

function nivelDeAeropuerto(ocupacion) {
  if (ocupacion == null || ocupacion < 0.5) return "verde";
  if (ocupacion < 0.8) return "amarillo";
  return "rojo";
}

const cachePlanes = new Map();
export function planeIcon(capacity, maxCapacity, rotation) {
  const nivel = nivelDeAvion(capacity, maxCapacity);
  const rot = Math.round((rotation || 0) / 5) * 5;
  const key = `${nivel}|${rot}`;
  if (!cachePlanes.has(key)) {
    cachePlanes.set(key, L.divIcon({
      html: `<img src="${planeIconImg}" style="width:18px;transform:rotate(${rot}deg);transform-origin:center;filter:${FILTRO[nivel]} drop-shadow(0 0 2px black) drop-shadow(0 0 1px black);">`,
      className: "",
      iconSize: [18, 18],
      iconAnchor: [11, 8],
    }));
  }
  return cachePlanes.get(key);
}

const cacheAeropuertos = new Map();
export function airportIcon(esSede, ocupacion) {
  const nivel = nivelDeAeropuerto(ocupacion);
  const key = `${esSede ? 1 : 0}|${nivel}`;
  if (!cacheAeropuertos.has(key)) {
    const size = esSede ? 30 : 24;
    cacheAeropuertos.set(key, L.divIcon({
      html: `<img src="${esSede ? sedeIconImg : airportIconImg}" class="airport-icon ${esSede ? "airport-icon--hub" : ""}" style="width:${size}px;height:${size}px;filter:${FILTRO_AEROPUERTO[nivel]};">`,
      className: "",
      iconSize: [size, size],
      iconAnchor: [size / 2, size / 2],
      popupAnchor: [0, -(size / 2)],
    }));
  }
  return cacheAeropuertos.get(key);
}

export function mapaDeColoresDeSedes(airports) {
  const map = {};
  Object.values(airports)
    .filter((ap) => ap.esSede)
    .sort((a, b) => a.code.localeCompare(b.code))
    .forEach((sede, i) => (map[sede.code] = HUB_COLORS[i % HUB_COLORS.length]));
  return map;
}
