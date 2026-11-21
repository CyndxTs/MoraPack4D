import { useEffect } from "react";
import L from "leaflet";
import { feature } from "topojson-client";
import { useMap } from "react-leaflet";

// Mapa base sin mosaicos: países de Natural Earth (1:50m, dominio público) con su nombre en español.
// Los nombres se muestran según el zoom (primero los países más grandes).

let cargaPaises = null;
function cargarPaises() {
  if (!cargaPaises) {
    cargaPaises = fetch("/geo/paises.json")
      .then((r) => { if (!r.ok) throw new Error("No se pudo cargar el mapa base"); return r.json(); })
      .then((topo) => desenrollarAntimeridiano(feature(topo, topo.objects.countries)))
      .catch((e) => { cargaPaises = null; throw e; });
  }
  return cargaPaises;
}

// Rusia y Fiji cruzan el meridiano 180: sin esto aparece una línea horizontal de lado a lado del mapa
function desenrollarAntimeridiano(fc) {
  fc.features.forEach((f) => {
    if (!f.geometry || f.properties?.n === "Antártida") return;
    const polys = f.geometry.type === "Polygon" ? [f.geometry.coordinates] : f.geometry.coordinates;
    polys.forEach((poly) => poly.forEach((anillo) => {
      for (let i = 1; i < anillo.length; i++) {
        const d = anillo[i][0] - anillo[i - 1][0];
        if (d > 180) anillo[i][0] -= 360;
        else if (d < -180) anillo[i][0] += 360;
      }
    }));
  });
  return fc;
}

const LIMITE_POR_ZOOM = { 2: 18, 3: 45, 4: 110, 5: 190 };
const limiteDeEtiquetas = (z) => (z <= 1 ? 0 : z >= 6 ? Infinity : LIMITE_POR_ZOOM[z] ?? 22);

export default function Basemap() {
  const map = useMap();

  useEffect(() => {
    let vivo = true;
    if (!map.getPane("paises")) {
      map.createPane("paises").style.zIndex = 200;
      map.createPane("etiquetas").style.zIndex = 350;
      map.getPane("etiquetas").style.pointerEvents = "none";
    }
    const capaPaises = L.geoJSON(null, {
      pane: "paises",
      interactive: false,
      renderer: L.canvas({ pane: "paises", padding: 0.5 }),
      style: { color: "#4A5A80", weight: 0.7, fillColor: "#253154", fillOpacity: 1 },
    });
    const capaEtiquetas = L.layerGroup([], { pane: "etiquetas" });
    let etiquetas = [];

    const actualizar = () => {
      const limite = limiteDeEtiquetas(map.getZoom());
      etiquetas.forEach(({ marker, rank }) => {
        const visible = rank < limite;
        if (visible && !capaEtiquetas.hasLayer(marker)) capaEtiquetas.addLayer(marker);
        else if (!visible && capaEtiquetas.hasLayer(marker)) capaEtiquetas.removeLayer(marker);
      });
    };

    cargarPaises().then((fc) => {
      if (!vivo) return;
      capaPaises.addData(fc).addTo(map);
      etiquetas = fc.features
        .filter((f) => f.properties?.n && f.properties.r !== undefined && f.properties.n !== "Antártida")
        .map((f) => ({
          rank: f.properties.r,
          marker: L.marker([f.properties.ly, f.properties.lx], {
            pane: "etiquetas",
            interactive: false,
            keyboard: false,
            icon: L.divIcon({ className: "etiqueta-pais", html: `<span>${f.properties.n}</span>`, iconSize: null }),
          }),
        }));
      capaEtiquetas.addTo(map);
      actualizar();
    }).catch(() => {});

    map.on("zoomend", actualizar);
    return () => {
      vivo = false;
      map.off("zoomend", actualizar);
      capaPaises.remove();
      capaEtiquetas.remove();
    };
  }, [map]);

  return null;
}
