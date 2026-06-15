import { useEffect, useState } from "react";
import { geoService, DEFAULT_CENTER } from "@/services/geoService";
import type { Coordinates } from "@/services/geoService";

export type LocationSource = "default" | "ip" | "browser";

export interface UserLocation {
  coords: Coordinates;
  source: LocationSource;
}

/**
 * Resolves the user's location in two steps:
 * 1. Immediately estimates it from the IP address (no permission needed).
 * 2. Then asks the browser for the precise position and upgrades if granted.
 */
export function useUserLocation(): UserLocation {
  const [location, setLocation] = useState<UserLocation>({
    coords: DEFAULT_CENTER,
    source: "default",
  });

  useEffect(() => {
    let cancelled = false;
    let browserResolved = false;

    geoService.locateByIp().then((coords) => {
      // The precise browser position always wins over the IP estimate
      if (!cancelled && !browserResolved && coords) {
        setLocation({ coords, source: "ip" });
      }
    });

    geoService.locateByBrowser().then((coords) => {
      if (!cancelled && coords) {
        browserResolved = true;
        setLocation({ coords, source: "browser" });
      }
    });

    return () => {
      cancelled = true;
    };
  }, []);

  return location;
}
