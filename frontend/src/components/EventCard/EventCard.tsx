import type { Evento } from "@/types";
import { Link } from "react-router-dom";
import { formatAddress } from "@/utils/address";
import styles from "./EventCard.module.css";

interface EventCardProps {
  evento: Evento;
}

export function EventCard({ evento }: EventCardProps) {
  const date = new Date(evento.dataEvento);
  const formattedDate = date.toLocaleDateString("pt-BR", {
    day: "2-digit",
    month: "short",
  });
  const formattedTime = date.toLocaleTimeString("pt-BR", {
    hour: "2-digit",
    minute: "2-digit",
  });

  const modalidade = evento.categorias[0]?.modalidade;

  const bgStyle = evento.fotoUrl
    ? {
        backgroundImage: `linear-gradient(rgba(0,0,0,0.25), rgba(0,0,0,0.55)), url('${evento.fotoUrl}')`,
        backgroundSize: "cover",
        backgroundPosition: "center",
      }
    : undefined;

  return (
    <Link to={`/events/${evento.id}`} className={styles.card} style={bgStyle}>
      {modalidade && <span className={styles.badge}>{modalidade}</span>}
      <div className={styles.content}>
        <h3 className={styles.title}>{evento.nome}</h3>
        <p className={styles.location}>
          <span className="material-symbols-outlined" aria-hidden="true">
            location_on
          </span>
          <span className={styles.locationText}>
            {formatAddress(evento.local)}
          </span>
        </p>
        <p className={styles.date}>
          <span className="material-symbols-outlined" aria-hidden="true">
            calendar_today
          </span>
          {formattedDate} &bull; {formattedTime}
        </p>
      </div>
    </Link>
  );
}
