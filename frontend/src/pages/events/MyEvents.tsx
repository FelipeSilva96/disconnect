import { useEffect, useState } from "react";
import type { Evento } from "@/types";
import { useAuth } from "@/contexts/AuthContext";
import { eventoService } from "@/services/eventoService";
import { participacaoService } from "@/services/participacaoService";
import { EventCard } from "@/components/EventCard";
import styles from "./MyEvents.module.css";

export function MyEventsPage() {
  const { user } = useAuth();
  const [created, setCreated] = useState<Evento[]>([]);
  const [joined, setJoined] = useState<Evento[]>([]);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState<"joined" | "created">("joined");

  useEffect(() => {
    if (!user) return;
    Promise.all([
      eventoService.listByOrganizador(user.id),
      participacaoService.listByUsuario(user.id),
    ])
      .then(([createdEvents, parts]) => {
        setCreated(createdEvents);
        setJoined(
          parts.filter((p) => p.status === "APROVADO").map((p) => p.evento),
        );
      })
      .catch(() => {
        setCreated([]);
        setJoined([]);
      })
      .finally(() => setLoading(false));
  }, [user]);

  const list = tab === "created" ? created : joined;

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>Meus eventos</h1>

      <div className={styles.tabs} role="tablist" aria-label="Meus eventos">
        <button
          role="tab"
          aria-selected={tab === "joined"}
          className={`${styles.tab} ${tab === "joined" ? styles.tabActive : ""}`}
          onClick={() => setTab("joined")}
        >
          Inscritos
        </button>
        <button
          role="tab"
          aria-selected={tab === "created"}
          className={`${styles.tab} ${tab === "created" ? styles.tabActive : ""}`}
          onClick={() => setTab("created")}
        >
          Criados
        </button>
      </div>

      {loading ? (
        <div
          className={styles.grid}
          aria-busy="true"
          aria-label="Carregando eventos"
        >
          {Array.from({ length: 3 }, (_, i) => (
            <div key={i} className={styles.skeletonCard} />
          ))}
        </div>
      ) : list.length === 0 ? (
        <div className={styles.empty}>
          <span className="material-symbols-outlined">event_busy</span>
          <p>
            {tab === "joined"
              ? "Você ainda não está inscrito em nenhum evento."
              : "Você ainda não criou nenhum evento."}
          </p>
        </div>
      ) : (
        <div className={styles.grid}>
          {list.map((evento) => (
            <EventCard key={evento.id} evento={evento} />
          ))}
        </div>
      )}
    </div>
  );
}
