import { useEffect, useRef, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import DatePicker, { registerLocale } from "react-datepicker";
import { ptBR } from "date-fns/locale";
import "react-datepicker/dist/react-datepicker.css";
import { dateStringToDate, dateToDateString } from "@/utils/date";
import styles from "./SearchBar.module.css";

registerLocale("pt-BR", ptBR);

const NIVEIS = ["Iniciante", "Intermediário", "Avançado"];

export function SearchBar() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [expanded, setExpanded] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  const [query, setQuery] = useState(searchParams.get("q") ?? "");
  const [modalidade, setModalidade] = useState(
    searchParams.get("modalidade") ?? "",
  );
  const [nivel, setNivel] = useState(searchParams.get("nivel") ?? "");
  const [data, setData] = useState(searchParams.get("data") ?? "");

  // Keep inputs in sync when filters are cleared elsewhere (e.g. clicking the logo)
  useEffect(() => {
    setQuery(searchParams.get("q") ?? "");
    setModalidade(searchParams.get("modalidade") ?? "");
    setNivel(searchParams.get("nivel") ?? "");
    setData(searchParams.get("data") ?? "");
  }, [searchParams]);

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (
        containerRef.current &&
        !containerRef.current.contains(e.target as Node)
      ) {
        setExpanded(false);
      }
    }
    document.addEventListener("click", handleClickOutside);
    return () => document.removeEventListener("click", handleClickOutside);
  }, []);

  function handleSubmit(e?: React.FormEvent) {
    e?.preventDefault();
    const params = new URLSearchParams();
    if (query.trim()) params.set("q", query.trim());
    if (modalidade.trim()) params.set("modalidade", modalidade.trim());
    if (nivel) params.set("nivel", nivel);
    if (data) params.set("data", data);
    setExpanded(false);
    navigate(`/?${params.toString()}`);
  }

  function handleClear() {
    setQuery("");
    setModalidade("");
    setNivel("");
    setData("");
    setExpanded(false);
    navigate("/");
  }

  const hasFilters = Boolean(modalidade || nivel || data);

  return (
    <div className={styles.container} ref={containerRef}>
      <form className={styles.bar} onSubmit={handleSubmit}>
        <span
          className={`material-symbols-outlined ${styles.searchIcon}`}
          aria-hidden="true"
        >
          search
        </span>
        <input
          type="text"
          className={styles.input}
          aria-label="Buscar eventos por nome ou local"
          placeholder="Buscar eventos por nome ou local..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onFocus={() => setExpanded(true)}
        />
        {hasFilters && (
          <span className={styles.filterDot} title="Filtros ativos" />
        )}
        <button type="submit" className={styles.submitBtn}>
          Buscar
        </button>
      </form>

      {expanded && (
        <div className={styles.panel}>
          <div className={styles.panelGrid}>
            <label className={styles.field}>
              <span className={styles.fieldLabel}>Modalidade</span>
              <input
                type="text"
                className={styles.fieldInput}
                placeholder="Ex: Futebol, Xadrez..."
                value={modalidade}
                onChange={(e) => setModalidade(e.target.value)}
              />
            </label>

            <label className={styles.field}>
              <span className={styles.fieldLabel}>Nível</span>
              <select
                className={styles.fieldInput}
                value={nivel}
                onChange={(e) => setNivel(e.target.value)}
              >
                <option value="">Qualquer nível</option>
                {NIVEIS.map((n) => (
                  <option key={n} value={n}>
                    {n}
                  </option>
                ))}
              </select>
            </label>

            <label className={styles.field}>
              <span className={styles.fieldLabel}>A partir de</span>
              <DatePicker
                className={styles.fieldInput}
                wrapperClassName={styles.datePickerWrapper}
                locale="pt-BR"
                dateFormat="dd/MM/yyyy"
                placeholderText="dd/mm/aaaa"
                selected={dateStringToDate(data)}
                onChange={(date: Date | null) =>
                  setData(dateToDateString(date))
                }
              />
            </label>
          </div>

          <div className={styles.panelActions}>
            <button
              type="button"
              className={styles.clearBtn}
              onClick={handleClear}
            >
              Limpar filtros
            </button>
            <button
              type="button"
              className={styles.applyBtn}
              onClick={() => handleSubmit()}
            >
              Aplicar
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
