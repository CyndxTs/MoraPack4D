import React, { useEffect } from "react";
import L from "leaflet";
import { MapContainer, TileLayer, Marker, Polyline, Tooltip, useMapEvent } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import { AirportTooltipContent, PlaneTooltipContent } from "./MapTooltips";
import { airportIcon, planeIcon } from "./mapIcons";

function ClickHandler({ onMapClick }) {
  useMapEvent("click", () => onMapClick());
  return null;
}

// Los tooltips son interactivos: sin esto, un clic dentro del tooltip también "limpia" el mapa
function useTooltipSinPropagacion(selector, dependencia) {
  useEffect(() => {
    const t = setTimeout(() => {
      document.querySelectorAll(selector).forEach((el) => {
        L.DomEvent.disableClickPropagation(el);
        L.DomEvent.disableScrollPropagation(el);
        el.style.pointerEvents = "auto";
      });
    }, 50);
    return () => clearTimeout(t);
  }, [selector, dependencia]);
}

export default function ScenarioMap({
  mapRef,
  airports, activeFlights, mostrarVuelos = true,
  hubColorMap, highlightedFlights, highlightedAirportCode,
  openTooltip, setOpenTooltip,
  vuelosDeAeropuerto, getOrdersForFlight,
  onAirportClick, onFlightClick, onMapClick,
}) {
  useTooltipSinPropagacion(".airport-tooltip", openTooltip?.code);
  useTooltipSinPropagacion(".plane-tooltip", openTooltip?.code);

  const hayResaltados = highlightedFlights.length > 0;

  return (
    <MapContainer
      id="map"
      ref={mapRef}
      center={[20, 0]}
      zoom={3}
      minZoom={2}
      maxBounds={[[-90, -180], [90, 180]]}
      worldCopyJump={false}
    >
      <TileLayer url="https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png" attribution='&copy; <a href="https://carto.com/">Carto</a>' />

      {airports.map((ap) => {
        const atenuado = (highlightedAirportCode && highlightedAirportCode !== ap.code) || hayResaltados;
        const abierto = openTooltip?.type === "airport" && openTooltip.code === ap.code;
        const vuelos = abierto ? vuelosDeAeropuerto(ap.code) : null;
        return (
          <Marker
            key={ap.code}
            position={[ap.lat, ap.lng]}
            icon={airportIcon(ap.esSede, ap.esSede ? 0 : ap.ocupacion)}
            opacity={atenuado ? 0.3 : 1}
            eventHandlers={{
              mouseover: () => setOpenTooltip({ type: "airport", code: ap.code }),
              click: () => onAirportClick(ap),
            }}
          >
            {abierto && (
              <Tooltip direction="top" opacity={0.95} interactive permanent className="airport-tooltip">
                <AirportTooltipContent
                  airport={ap}
                  vuelosQueSalen={vuelos.salen}
                  vuelosQueLlegan={vuelos.llegan}
                  getOrdersForFlight={getOrdersForFlight}
                  onOpenPanel={() => onAirportClick(ap)}
                />
              </Tooltip>
            )}
          </Marker>
        );
      })}

      {mostrarVuelos && activeFlights.map((flight) => {
        if (!flight.position) return null;
        const resaltado = highlightedFlights.includes(flight.code);
        const colorNormal = (flight.origin?.esSede && hubColorMap[flight.origin.code]) || "#eb6774";
        const abierto = openTooltip?.type === "flight" && openTooltip.code === flight.code;
        return (
          <React.Fragment key={flight.code}>
            <Polyline
              positions={flight.remaining}
              interactive={false}
              pathOptions={{
                color: !hayResaltados ? colorNormal : resaltado ? "#ff0019" : "#e5e7eb",
                weight: resaltado ? 4 : 2,
                opacity: !hayResaltados ? 0.8 : resaltado ? 1 : 0.03,
                dashArray: "6, 10",
              }}
            />
            <Marker
              position={flight.position}
              opacity={!hayResaltados || resaltado ? 1 : 0.25}
              zIndexOffset={1000}
              icon={planeIcon(flight.capacity, flight.planeCapacity, flight.rotation)}
              riseOnHover
              eventHandlers={{
                mouseover: () => setOpenTooltip({ type: "flight", code: flight.code }),
                click: () => onFlightClick(flight),
              }}
            >
              {abierto && (
                <Tooltip direction="top" opacity={0.95} permanent interactive className="plane-tooltip">
                  <PlaneTooltipContent flight={flight} getOrdersForFlight={getOrdersForFlight} onOpenPanel={() => onFlightClick(flight)} />
                </Tooltip>
              )}
            </Marker>
          </React.Fragment>
        );
      })}

      <ClickHandler onMapClick={onMapClick} />
    </MapContainer>
  );
}
