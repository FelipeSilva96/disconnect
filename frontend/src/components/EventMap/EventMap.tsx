import { useEffect, useMemo, useState } from "react";
import { MapContainer, TileLayer, Marker, Popup, useMap } from "react-leaflet";
import L from "leaflet";
import { Link } from "react-router-dom";
import type { Evento } from "@/types";
import { geoService } from "@/services/geoService";
import type { Coordinates } from "@/services/geoService";
import type { LocationSource } from "@/hooks/useUserLocation";
import { formatAddress } from "@/utils/address";
import "leaflet/dist/leaflet.css";
import styles from "./EventMap.module.css";

export interface MappedEvento {
  evento: Evento;
  coords: Coordinates;
}

interface EventMapProps {
  eventos: Evento[];
  center: Coordinates;
  /** Where `center` came from; the user marker is hidden for the default fallback */
  centerSource?: LocationSource;
}

/**
 * Geocodes each event's `local` (sequentially, results cached in
 * localStorage) and returns the ones that resolved to coordinates.
 */
function useMappedEventos(eventos: Evento[]): MappedEvento[] {
  const [mapped, setMapped] = useState<MappedEvento[]>([]);

  useEffect(() => {
    let cancelled = false;
    setMapped([]);

    (async () => {
      for (const evento of eventos) {
        // Prefer the coordinates stored with the event; geocode as fallback
        const coords =
          evento.latitude != null && evento.longitude != null
            ? { lat: evento.latitude, lng: evento.longitude }
            : await geoService.geocode(evento.local);
        if (cancelled) return;
        if (coords) {
          setMapped((prev) => [...prev, { evento, coords }]);
        }
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [eventos]);

  return mapped;
}

/** Re-centers the map when the resolved user location or markers change. */
function MapViewport({
  center,
  mapped,
}: {
  center: Coordinates;
  mapped: MappedEvento[];
}) {
  const map = useMap();

  useEffect(() => {
    if (mapped.length > 0) {
      const bounds = L.latLngBounds(
        mapped.map((m) => [m.coords.lat, m.coords.lng] as [number, number]),
      );
      bounds.extend([center.lat, center.lng]);
      map.fitBounds(bounds, { padding: [48, 48], maxZoom: 14 });
    } else {
      map.setView([center.lat, center.lng], 12);
    }
  }, [map, center, mapped]);

  return null;
}

function makePinIcon(label: string, className = styles.pin) {
  const safe = label
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
  return L.divIcon({
    className: "",
    html: `<span class="${className}">${safe}</span>`,
    iconSize: undefined,
  });
}

export function EventMap({ eventos, center, centerSource }: EventMapProps) {
  const mapped = useMappedEventos(eventos);
  const showUser = centerSource === "ip" || centerSource === "browser";

  const icons = useMemo(
    () =>
      new Map(
        mapped.map(({ evento }) => [
          evento.id,
          makePinIcon(evento.categorias[0]?.modalidade ?? evento.nome),
        ]),
      ),
    [mapped],
  );

  return (
    <MapContainer
      center={[center.lat, center.lng]}
      zoom={12}
      scrollWheelZoom
      className={styles.map}
    >
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      <MapViewport center={center} mapped={mapped} />

      {showUser && (
        <Marker
          position={[center.lat, center.lng]}
          icon={makePinIcon("Você", styles.userPin)}
        >
          <Popup>
            {centerSource === "browser"
              ? "Sua localização"
              : "Sua localização aproximada (via IP)"}
          </Popup>
        </Marker>
      )}

      {mapped.map(({ evento, coords }) => (
        <Marker
          key={evento.id}
          position={[coords.lat, coords.lng]}
          icon={icons.get(evento.id)}
        >
          <Popup>
            <Link to={`/events/${evento.id}`} className={styles.popupLink}>
              <strong>{evento.nome}</strong>
              <span>{formatAddress(evento.local)}</span>
              <span>
                {new Date(evento.dataEvento).toLocaleDateString("pt-BR", {
                  day: "2-digit",
                  month: "short",
                  hour: "2-digit",
                  minute: "2-digit",
                })}
              </span>
            </Link>
          </Popup>
        </Marker>
      ))}
    </MapContainer>
  );
}
