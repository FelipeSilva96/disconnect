// `evento.local` stores the full Nominatim display_name ("Rua X, 147,
// Bairro, Regional Centro-Sul, Belo Horizonte, Região Geográfica Imediata
// de Belo Horizonte, Minas Gerais, Região Sudeste, 30110-000, Brasil").
// Most of it is administrative noise; the UI only needs street + number,
// neighborhood and city. The full string is still kept in the model for
// geocoding.

const NOISE_PART =
  /^(Região|Regional|Microrregião|Mesorregião|Brasil|Brazil)\b|^\d{5}-?\d{3}$/i;

export function formatAddress(local: string): string {
  const parts = local
    .split(",")
    .map((part) => part.trim())
    .filter((part) => part && !NOISE_PART.test(part));

  // A standalone house number belongs to the preceding street part
  const merged: string[] = [];
  for (const part of parts) {
    if (/^\d+\w?$/.test(part) && merged.length > 0) {
      merged[merged.length - 1] += `, ${part}`;
    } else {
      merged.push(part);
    }
  }

  return merged.slice(0, 3).join(", ") || local;
}
