import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import type { Evento, EventoRecomendadoIA } from "@/types";
import { useAuth } from "@/contexts/AuthContext";
import { eventoService } from "@/services/eventoService";
import { iaService } from "@/services/iaService";
import { EventCard } from "@/components/EventCard";
import { EventMap } from "@/components/EventMap";
import { useUserLocation } from "@/hooks/useUserLocation";
import styles from "./Home.module.css";

function matchesFilters(
  evento: Evento,
  filters: { q: string; modalidade: string; nivel: string; data: string },
): boolean {
  const { q, modalidade, nivel, data } = filters;

  if (q) {
    const matches =
      evento.nome.toLowerCase().includes(q) ||
      evento.local.toLowerCase().includes(q) ||
      evento.categorias.some(
        (c) =>
          c.modalidade.toLowerCase().includes(q) ||
          c.nome.toLowerCase().includes(q),
      );
    if (!matches) return false;
  }

  if (modalidade) {
    const matches =
      evento.categorias.some((c) =>
        c.modalidade.toLowerCase().includes(modalidade),
      ) || (evento.modalidadeHobby ?? "").toLowerCase().includes(modalidade);
    if (!matches) return false;
  }

  if (nivel && evento.nivelDeHabilidade !== nivel) return false;

  if (data && new Date(evento.dataEvento) < new Date(`${data}T00:00:00`)) {
    return false;
  }

  return true;
}

export function HomePage() {
  const { user } = useAuth();

  const [eventos, setEventos] = useState<Evento[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchParams] = useSearchParams();
  const { coords, source } = useUserLocation();

  const [recomendacoes, setRecomendacoes] = useState<EventoRecomendadoIA[]>([]);
  const [iaMensagem, setIaMensagem] = useState("");
  const [iaLoading, setIaLoading] = useState(false);
  const [iaRequested, setIaRequested] = useState(false);

  useEffect(() => {
    eventoService
      .list()
      .then(setEventos)
      .catch(() => setEventos([]))
      .finally(() => setLoading(false));
  }, []);

  function handleGerarRecomendacoes() {
    if (!user?.token) {
      setIaRequested(true);
      setIaMensagem("Faça login para receber recomendações personalizadas.");
      return;
    }

    setIaRequested(true);
    setIaLoading(true);
    setIaMensagem("");

    iaService
      .recomendarEventos(user.token, 3)
      .then((data) => {
        setRecomendacoes(data.recomendacoes ?? []);
        setIaMensagem(data.mensagem ?? "");
      })
      .catch((error) => {
        setIaMensagem(
          error instanceof Error
            ? error.message
            : "Não foi possível gerar recomendações inteligentes.",
        );
      })
      .finally(() => setIaLoading(false));
  }

  const filters = useMemo(
    () => ({
      q: (searchParams.get("q") ?? "").toLowerCase().trim(),
      modalidade: (searchParams.get("modalidade") ?? "").toLowerCase().trim(),
      nivel: searchParams.get("nivel") ?? "",
      data: searchParams.get("data") ?? "",
    }),
    [searchParams],
  );

  const hasFilters = Boolean(
    filters.q || filters.modalidade || filters.nivel || filters.data,
  );

  const filtered = useMemo(
    () => eventos.filter((evento) => matchesFilters(evento, filters)),
    [eventos, filters],
  );

  return (
    <div className={styles.split}>
      <section className={styles.listPane}>
        {!hasFilters && (
          <section className={styles.aiSection}>
            <div className={styles.aiSectionHeader}>
              <span className={`material-symbols-outlined ${styles.aiIcon}`}>
                auto_awesome
              </span>

              <div>
                <h2 className={styles.aiTitle}>Recomendados para você</h2>
                <p className={styles.aiSubtitle}>
                  Eventos selecionados pela IA com base no seu perfil, hobbies,
                  localização e nível.
                </p>
              </div>
            </div>

            {!iaRequested ? (
              <button
                type="button"
                className={styles.aiGenerateButton}
                onClick={handleGerarRecomendacoes}
              >
                <span className="material-symbols-outlined" aria-hidden="true">
                  auto_awesome
                </span>
                Gerar recomendações
              </button>
            ) : iaLoading ? (
              <p className={styles.aiMessage}>Gerando recomendações...</p>
            ) : recomendacoes.length > 0 ? (
              <div className={styles.recommendationGrid}>
                {recomendacoes.map(({ evento, pontuacao, motivo, sinais }) => (
                  <article key={evento.id} className={styles.recommendationCard}>
                    <EventCard evento={evento} />

                    <div className={styles.recommendationInfo}>
                      <strong>{pontuacao}% compatível</strong>
                      <p>{motivo}</p>

                      {sinais?.length > 0 && (
                        <ul>
                          {sinais.slice(0, 3).map((sinal) => (
                            <li key={sinal}>{sinal}</li>
                          ))}
                        </ul>
                      )}
                    </div>
                  </article>
                ))}
              </div>
            ) : (
              <div className={styles.aiMessageBox}>
                <p className={styles.aiMessage}>
                  {iaMensagem ||
                    "Complete seu perfil e participe de eventos para receber melhores recomendações."}
                </p>
                <button
                  type="button"
                  className={styles.aiGenerateButton}
                  onClick={handleGerarRecomendacoes}
                >
                  <span
                    className="material-symbols-outlined"
                    aria-hidden="true"
                  >
                    refresh
                  </span>
                  Tentar novamente
                </button>
              </div>
            )}
          </section>
        )}

        <h1 className={styles.title}>
          {hasFilters
            ? `${filtered.length} evento${filtered.length === 1 ? "" : "s"} encontrado${filtered.length === 1 ? "" : "s"}`
            : "Eventos disponíveis"}
        </h1>

        {loading ? (
          <div className={styles.grid} aria-busy="true" aria-label="Carregando eventos">
            {Array.from({ length: 6 }, (_, i) => (
              <div key={i} className={styles.skeletonCard} />
            ))}
          </div>
        ) : filtered.length === 0 ? (
          <p className={styles.empty}>Nenhum evento encontrado.</p>
        ) : (
          <div className={styles.grid}>
            {filtered.map((evento) => (
              <EventCard key={evento.id} evento={evento} />
            ))}
          </div>
        )}
      </section>

      <aside className={styles.mapPane}>
        <EventMap eventos={filtered} center={coords} centerSource={source} />
      </aside>
    </div>
  );
}
