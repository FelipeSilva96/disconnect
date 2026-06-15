import { useEffect, useState } from "react";
import { MapContainer, TileLayer, Marker, Popup, useMap } from "react-leaflet";
import L from "leaflet";
import { geoService } from "@/services/geoService";
import type { Coordinates } from "@/services/geoService";
import { useUserLocation } from "@/hooks/useUserLocation";
import "leaflet/dist/leaflet.css";
import styles from "./LocationMap.module.css";

interface LocationMapProps {
  /** Display label for the place marker (e.g. the event name) */
  label: string;
  address: string;
  latitude?: number;
  longitude?: number;
}

function pinIcon(html: string, className: string) {
  return L.divIcon({ className: "", html: `<span class="${className}">${html}</span>`, iconSize: undefined });
}

function escapeHtml(value: string) {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

function FitBounds({
  place,
  user,
}: {
  place: Coordinates;
  user: Coordinates | null;
}) {
  const map = useMap();

  useEffect(() => {
    if (user) {
      const bounds = L.latLngBounds([
        [place.lat, place.lng],
        [user.lat, user.lng],
      ]);
      map.fitBounds(bounds, { padding: [56, 56], maxZoom: 15 });
    } else {
      map.setView([place.lat, place.lng], 15);
    }
  }, [map, place, user]);

  return null;
}

/**
 * Map for the event details page: shows the event location (stored lat/lng,
 * falling back to geocoding the address) plus the user's own location.
 */
export function LocationMap({
  label,
  address,
  latitude,
  longitude,
}: LocationMapProps) {
  const userLocation = useUserLocation();
  const [place, setPlace] = useState<Coordinates | null>(
    latitude != null && longitude != null
      ? { lat: latitude, lng: longitude }
      : null,
  );
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    if (latitude != null && longitude != null) {
      setPlace({ lat: latitude, lng: longitude });
      return;
    }

    let cancelled = false;
    geoService.geocode(address).then((coords) => {
      if (cancelled) return;
      if (coords) setPlace(coords);
      else setFailed(true);
    });
    return () => {
      cancelled = true;
    };
  }, [latitude, longitude, address]);

  if (failed) {
    return (
      <p className={styles.unavailable}>
        Não foi possível localizar este endereço no mapa.
      </p>
    );
  }

  if (!place) {
    return <p className={styles.unavailable}>Carregando mapa...</p>;
  }

  const showUser = userLocation.source !== "default";

  return (
    <MapContainer
      center={[place.lat, place.lng]}
      zoom={15}
      scrollWheelZoom={false}
      className={styles.map}
    >
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      <FitBounds place={place} user={showUser ? userLocation.coords : null} />

      <Marker
        position={[place.lat, place.lng]}
        icon={pinIcon(escapeHtml(label), styles.eventPin)}
      >
        <Popup>{address}</Popup>
      </Marker>

      {showUser && (
        <Marker
          position={[userLocation.coords.lat, userLocation.coords.lng]}
          icon={pinIcon("Você", styles.userPin)}
        >
          <Popup>
            {userLocation.source === "browser"
              ? "Sua localização"
              : "Sua localização aproximada (via IP)"}
          </Popup>
        </Marker>
      )}
    </MapContainer>
  );
}
