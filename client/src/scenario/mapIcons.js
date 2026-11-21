import L from "leaflet";
import planeIconImg from "../assets/icons/planeMora.svg";

// Los íconos se cachean: crear un L.divIcon por avión en cada tick era muy costoso

export const HUB_COLORS = ["#78C6FF", "#39D98A", "#FFB547", "#FF8FA3", "#5EEAD4", "#F9E27D", "#9FB4FF", "#B7F171", "#FF9F6B", "#7DE3FF"];

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
const COLOR_NIVEL = { verde: "#39D98A", amarillo: "#FFC24D", rojo: "#FF6B7A" };

// Aeropuerto: anillo con el color de ocupación del almacén. Sede: marca celeste con la caja de MoraPack.
export function airportIcon(esSede, ocupacion) {
  const nivel = nivelDeAeropuerto(ocupacion);
  const key = `${esSede ? 1 : 0}|${nivel}`;
  if (!cacheAeropuertos.has(key)) {
    const c = COLOR_NIVEL[nivel];
    const size = esSede ? 30 : 18;
    const html = esSede
      ? `<svg class="airport-icon airport-icon--hub" width="30" height="30" viewBox="0 0 30 30" aria-hidden="true">
           <circle cx="15" cy="15" r="13.5" fill="rgba(120,198,255,0.16)" stroke="#78C6FF" stroke-width="1.5"/>
           <circle cx="15" cy="15" r="9.5" fill="#0A1228" stroke="#78C6FF" stroke-width="1.2"/>
           <path d="M15 9.6l5 2.5-5 2.5-5-2.5z" fill="#FFC266"/><path d="M10 12.1l5 2.5v5.6l-5-2.5z" fill="#F29A2E"/><path d="M20 12.1l-5 2.5v5.6l5-2.5z" fill="#C8741A"/>
         </svg>`
      : `<svg class="airport-icon" width="18" height="18" viewBox="0 0 18 18" aria-hidden="true">
           <circle cx="9" cy="9" r="7.5" fill="#0A1228" stroke="${c}" stroke-width="2.2"/>
           <circle cx="9" cy="9" r="2.6" fill="${c}"/>
         </svg>`;
    cacheAeropuertos.set(key, L.divIcon({
      html,
      className: "icono-aeropuerto",
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
