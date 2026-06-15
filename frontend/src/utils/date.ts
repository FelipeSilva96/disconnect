// Native <input type="date|time"> render in the browser's locale (often en-US).
// We keep state as "YYYY-MM-DD" / "HH:mm" strings and convert at the picker
// boundary so the UI is always dd/MM/yyyy + 24h regardless of the browser.

export function dateStringToDate(value: string): Date | null {
  if (!value) return null;
  const [y, m, d] = value.split("-").map(Number);
  if (!y || !m || !d) return null;
  return new Date(y, m - 1, d);
}

export function dateToDateString(date: Date | null): string {
  if (!date) return "";
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
}

export function timeStringToDate(value: string): Date | null {
  if (!value) return null;
  const [h, min] = value.split(":").map(Number);
  const date = new Date();
  date.setHours(h ?? 0, min ?? 0, 0, 0);
  return date;
}

export function timeToTimeString(date: Date | null): string {
  if (!date) return "";
  const h = String(date.getHours()).padStart(2, "0");
  const min = String(date.getMinutes()).padStart(2, "0");
  return `${h}:${min}`;
}
