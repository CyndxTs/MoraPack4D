// Geometría de los vuelos (gran círculo) y posición de un avión en un instante dado

const toRad = (d) => (d * Math.PI) / 180;
const toDeg = (r) => (r * 180) / Math.PI;

export function generateGeodesicPath(lat1, lon1, lat2, lon2, numPoints = 100) {
  const φ1 = toRad(lat1), λ1 = toRad(lon1), φ2 = toRad(lat2), λ2 = toRad(lon2);
  const d = 2 * Math.asin(Math.sqrt(Math.sin((φ2 - φ1) / 2) ** 2 + Math.cos(φ1) * Math.cos(φ2) * Math.sin((λ2 - λ1) / 2) ** 2));
  if (d === 0) return [{ lat: lat1, lng: lon1 }];
  const points = [];
  for (let i = 0; i <= numPoints; i++) {
    const f = i / numPoints;
    const A = Math.sin((1 - f) * d) / Math.sin(d);
    const B = Math.sin(f * d) / Math.sin(d);
    const x = A * Math.cos(φ1) * Math.cos(λ1) + B * Math.cos(φ2) * Math.cos(λ2);
    const y = A * Math.cos(φ1) * Math.sin(λ1) + B * Math.cos(φ2) * Math.sin(λ2);
    const z = A * Math.sin(φ1) + B * Math.sin(φ2);
    points.push({ lat: toDeg(Math.atan2(z, Math.sqrt(x ** 2 + y ** 2))), lng: toDeg(Math.atan2(y, x)) });
  }
  return points;
}

function bearing(a, b) {
  const φ1 = toRad(a.lat), φ2 = toRad(b.lat), Δλ = toRad(b.lng - a.lng);
  const θ = Math.atan2(Math.sin(Δλ) * Math.cos(φ2), Math.cos(φ1) * Math.sin(φ2) - Math.sin(φ1) * Math.cos(φ2) * Math.cos(Δλ));
  return (toDeg(θ) + 360) % 360;
}

// Estado del vuelo en 'nowMs': progreso [0,1], posición interpolada, rotación del ícono y tramo restante
export function flightStateAt(flight, nowMs) {
  const path = flight.path;
  if (!path || path.length === 0) return { progress: 0, position: null, rotation: 0, arrived: false, remaining: [] };
  if (nowMs <= flight.startMs) return { progress: 0, position: path[0], rotation: 0, arrived: false, remaining: path };
  if (nowMs >= flight.endMs) {
    const last = path[path.length - 1];
    return { progress: 1, position: last, rotation: 0, arrived: true, remaining: [last] };
  }
  const total = Math.max(flight.endMs - flight.startMs, 60000);
  const progress = Math.min((nowMs - flight.startMs) / total, 1);
  const maxIndex = path.length - 1;
  const raw = progress * maxIndex;
  const idx = Math.floor(raw);
  const next = Math.min(idx + 1, maxIndex);
  const frac = raw - idx;
  const position = {
    lat: path[idx].lat + (path[next].lat - path[idx].lat) * frac,
    lng: path[idx].lng + (path[next].lng - path[idx].lng) * frac,
  };
  const rotation = bearing(path[idx], path[next]) - 45; // el ícono apunta a 45°
  return { progress, position, rotation, arrived: false, remaining: [position, ...path.slice(next)] };
}
