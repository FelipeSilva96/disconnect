import { useEffect, useRef, useState } from "react";
import { geoService } from "@/services/geoService";
import type { Place } from "@/services/geoService";
import styles from "./LocationAutocomplete.module.css";

interface LocationAutocompleteProps {
  id?: string;
  value: string;
  placeholder?: string;
  required?: boolean;
  /** Restrict the search to cities (used for the user's localização) */
  cityOnly?: boolean;
  className?: string;
  /** Fired on every keystroke; selection becomes invalid until a new pick */
  onChangeText: (text: string) => void;
  /** Fired when the user picks one of the suggestions */
  onSelect: (place: Place) => void;
}

export function LocationAutocomplete({
  id,
  value,
  placeholder,
  required,
  cityOnly,
  className,
  onChangeText,
  onSelect,
}: LocationAutocompleteProps) {
  const [suggestions, setSuggestions] = useState<Place[]>([]);
  const [open, setOpen] = useState(false);
  const [searching, setSearching] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);
  const debounceRef = useRef<number | null>(null);
  const skipNextSearchRef = useRef(false);

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (
        containerRef.current &&
        !containerRef.current.contains(e.target as Node)
      ) {
        setOpen(false);
      }
    }
    document.addEventListener("click", handleClickOutside);
    return () => document.removeEventListener("click", handleClickOutside);
  }, []);

  useEffect(() => {
    if (skipNextSearchRef.current) {
      skipNextSearchRef.current = false;
      return;
    }

    if (debounceRef.current !== null) {
      window.clearTimeout(debounceRef.current);
    }

    if (value.trim().length < 3) {
      setSuggestions([]);
      setOpen(false);
      return;
    }

    debounceRef.current = window.setTimeout(async () => {
      setSearching(true);
      const places = await geoService.searchPlaces(value, { cityOnly });
      setSearching(false);
      setSuggestions(places);
      setOpen(true);
    }, 450);

    return () => {
      if (debounceRef.current !== null) {
        window.clearTimeout(debounceRef.current);
      }
    };
  }, [value, cityOnly]);

  function handlePick(place: Place) {
    skipNextSearchRef.current = true;
    setOpen(false);
    setSuggestions([]);
    onSelect(place);
  }

  return (
    <div className={styles.container} ref={containerRef}>
      <input
        id={id}
        type="text"
        className={className}
        placeholder={placeholder}
        value={value}
        required={required}
        autoComplete="off"
        onChange={(e) => onChangeText(e.target.value)}
        onFocus={() => {
          if (suggestions.length > 0) setOpen(true);
        }}
      />

      {open && (
        <ul className={styles.dropdown}>
          {searching && suggestions.length === 0 ? (
            <li className={styles.hint}>Buscando...</li>
          ) : suggestions.length === 0 ? (
            <li className={styles.hint}>Nenhum resultado encontrado</li>
          ) : (
            suggestions.map((place) => (
              <li key={`${place.lat}-${place.lng}`}>
                <button
                  type="button"
                  className={styles.option}
                  onClick={() => handlePick(place)}
                >
                  <span
                    className={`material-symbols-outlined ${styles.optionIcon}`}
                  >
                    location_on
                  </span>
                  <span className={styles.optionLabel}>{place.label}</span>
                </button>
              </li>
            ))
          )}
        </ul>
      )}
    </div>
  );
}
