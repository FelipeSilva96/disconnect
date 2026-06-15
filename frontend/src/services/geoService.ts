// --- Geolocation & geocoding helpers ---

export interface Coordinates {
  lat: number;
  lng: number;
}

export interface Place {
  /** Full display name (address) returned by the geocoder */
  label: string;
  lat: number;
  lng: number;
  /** City extracted from the address details, when available */
  city?: string;
  /** State/region extracted from the address details, when available */
  state?: string;
}

interface NominatimResult {
  display_name: string;
  lat: string;
  lon: string;
  address?: Record<string, string>;
}

function extractCity(address?: Record<string, string>): string | undefined {
  if (!address) return undefined;
  return (
    address.city ?? address.town ?? address.village ?? address.municipality
  );
}

function toPlace(result: NominatimResult): Place {
  return {
    label: result.display_name,
    lat: parseFloat(result.lat),
    lng: parseFloat(result.lon),
    city: extractCity(result.address),
    state: result.address?.state,
  };
}

// Fallback when every location source fails (Belo Horizonte)
export const DEFAULT_CENTER: Coordinates = { lat: -19.9167, lng: -43.9345 };

const GEOCODE_CACHE_KEY = "disconnect.geocodeCache";

function readGeocodeCache(): Record<string, Coordinates | null> {
  try {
    return JSON.parse(localStorage.getItem(GEOCODE_CACHE_KEY) ?? "{}");
  } catch {
    return {};
  }
}

function writeGeocodeCache(cache: Record<string, Coordinates | null>) {
  try {
    localStorage.setItem(GEOCODE_CACHE_KEY, JSON.stringify(cache));
  } catch {
    // storage full/unavailable — cache is best-effort only
  }
}

export const geoService = {
  /** Rough location estimate based on the user's IP address. */
  async locateByIp(): Promise<Coordinates | null> {
    try {
      const res = await fetch("https://ipwho.is/");
      if (!res.ok) return null;
      const data = await res.json();
      if (data?.success === false) return null;
      if (typeof data?.latitude !== "number") return null;
      return { lat: data.latitude, lng: data.longitude };
    } catch {
      return null;
    }
  },

  /** Precise location via the browser Geolocation API (asks for permission). */
  locateByBrowser(): Promise<Coordinates | null> {
    return new Promise((resolve) => {
      if (!("geolocation" in navigator)) {
        resolve(null);
        return;
      }
      navigator.geolocation.getCurrentPosition(
        (pos) =>
          resolve({ lat: pos.coords.latitude, lng: pos.coords.longitude }),
        () => resolve(null),
        { enableHighAccuracy: false, timeout: 10000, maximumAge: 300000 },
      );
    });
  },

  /**
   * Searches addresses (or only cities) by free text via Nominatim.
   * Used by autocomplete inputs.
   */
  async searchPlaces(
    query: string,
    options?: { cityOnly?: boolean },
  ): Promise<Place[]> {
    const q = query.trim();
    if (q.length < 3) return [];

    const params = new URLSearchParams({
      format: "json",
      addressdetails: "1",
      limit: "5",
      q,
    });
    if (options?.cityOnly) {
      params.set("featureType", "city");
    }

    try {
      const res = await fetch(
        `https://nominatim.openstreetmap.org/search?${params.toString()}`,
        { headers: { Accept: "application/json" } },
      );
      if (!res.ok) return [];
      const results: NominatimResult[] = await res.json();
      return Array.isArray(results) ? results.map(toPlace) : [];
    } catch {
      return [];
    }
  },

  /**
   * Geocodes a free-text address via Nominatim (OpenStreetMap).
   * Results (including misses) are cached in localStorage.
   */
  async geocode(local: string): Promise<Coordinates | null> {
    const key = local.trim().toLowerCase();
    if (!key) return null;

    const cache = readGeocodeCache();
    if (key in cache) return cache[key];

    let coords: Coordinates | null = null;
    try {
      const url = `https://nominatim.openstreetmap.org/search?format=json&limit=1&q=${encodeURIComponent(local)}`;
      const res = await fetch(url, {
        headers: { Accept: "application/json" },
      });
      if (res.ok) {
        const results = await res.json();
        if (Array.isArray(results) && results.length > 0) {
          coords = {
            lat: parseFloat(results[0].lat),
            lng: parseFloat(results[0].lon),
          };
        }
      }
    } catch {
      // network error — treat as a miss but don't cache it
      return null;
    }

    cache[key] = coords;
    writeGeocodeCache(cache);
    return coords;
  },
};
