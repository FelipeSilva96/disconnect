import { type FormEvent, useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import type { Avaliacao, Evento, Participacao } from "@/types";
import { useAuth } from "@/contexts/AuthContext";
import { eventoService } from "@/services/eventoService";
import { participacaoService } from "@/services/participacaoService";
import { avaliacaoService } from "@/services/avaliacaoService";
import { Modal } from "@/components/Modal";
import { LocationMap } from "@/components/LocationMap";
import { formatAddress } from "@/utils/address";
import styles from "./EventDetails.module.css";

function getInitials(name: string) {
  return name
    .split(" ")
    .slice(0, 2)
    .map((w) => w[0])
    .join("")
    .toUpperCase();
}

const FREQ_LABELS: Record<string, string> = {
  UNICO: "Único",
  SEMANAL: "Semanal",
  MENSAL: "Mensal",
  ANUAL: "Anual",
};

const RATING_OPTIONS = [1, 2, 3, 4, 5];

type ParticipacaoResponseAction = "APROVADO" | "RECUSADO";

function getAvaliadorId(avaliacao: Avaliacao) {
  return avaliacao.avaliadorId ?? avaliacao.avaliador?.id;
}

function getParticipacaoKey(participacao: Participacao) {
  return `${participacao.idEvento}-${participacao.idSolicitante}`;
}

function calcularIdade(dataNascimento?: string): number | undefined {
  if (!dataNascimento) return undefined;
  const nascimento = new Date(`${dataNascimento}T00:00:00`);
  if (Number.isNaN(nascimento.getTime())) return undefined;

  const hoje = new Date();
  let idade = hoje.getFullYear() - nascimento.getFullYear();
  const aniversarioPendente =
    hoje.getMonth() < nascimento.getMonth() ||
    (hoje.getMonth() === nascimento.getMonth() &&
      hoje.getDate() < nascimento.getDate());
  if (aniversarioPendente) idade -= 1;
  return idade;
}

/** Items missing for a "complete profile" (photo, bio, city and hobbies). */
function getMissingProfileItems(user: {
  urlFoto?: string;
  biografia?: string;
  localizacao?: string;
  hobbies?: string[];
}): string[] {
  const missing: string[] = [];
  if (!user.urlFoto?.trim()) missing.push("foto");
  if (!user.biografia?.trim()) missing.push("biografia");
  if (!user.localizacao?.trim()) missing.push("cidade");
  if (!user.hobbies || user.hobbies.length === 0) missing.push("hobbies");
  return missing;
}

export function EventDetailsPage() {
  const { id } = useParams<{ id: string }>();
  const { user } = useAuth();
  const navigate = useNavigate();

  const [evento, setEvento] = useState<Evento | null>(null);
  const [participacoes, setParticipacoes] = useState<Participacao[]>([]);
  const [avaliacoes, setAvaliacoes] = useState<Avaliacao[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [toast, setToast] = useState<string | null>(null);
  const [confirmDelete, setConfirmDelete] = useState(false);
  const [reviewModalOpen, setReviewModalOpen] = useState(false);
  const [reviewNota, setReviewNota] = useState(5);
  const [reviewComentario, setReviewComentario] = useState("");
  const [submittingReview, setSubmittingReview] = useState(false);
  const [participacaoModalOpen, setParticipacaoModalOpen] = useState(false);
  const [participacaoMessage, setParticipacaoMessage] = useState("");
  const [submittingParticipacao, setSubmittingParticipacao] = useState(false);
  const [responseTarget, setResponseTarget] = useState<Participacao | null>(
    null,
  );
  const [responseAction, setResponseAction] =
    useState<ParticipacaoResponseAction>("APROVADO");
  const [responseMessage, setResponseMessage] = useState("");
  const [submittingResponse, setSubmittingResponse] = useState(false);

  useEffect(() => {
    if (!id) return;
    const eventoId = Number(id);

    Promise.all([
      eventoService.getById(eventoId),
      participacaoService.listByEvento(eventoId),
      avaliacaoService.listByEvento(eventoId).catch(() => [] as Avaliacao[]),
    ])
      .then(([ev, parts, reviews]) => {
        if (!ev) {
          setError(true);
          return;
        }
        setEvento(ev);
        setParticipacoes(parts);
        setAvaliacoes(reviews);
      })
      .catch(() => setError(true))
      .finally(() => setLoading(false));
  }, [id]);

  const isOrganizer = user !== null && evento?.organizador.id === user.id;
  const isPast = evento ? new Date(evento.dataEvento) < new Date() : false;

  const confirmed = participacoes.filter((p) => p.status === "APROVADO");
  const pending = participacoes.filter((p) => p.status === "PENDENTE");

  const currentParticipation = user
    ? participacoes.find((p) => p.idSolicitante === user.id)
    : undefined;
  const alreadyJoined = currentParticipation?.status === "APROVADO";
  const requestPending = currentParticipation?.status === "PENDENTE";
  const requestRejected = currentParticipation?.status === "RECUSADO";
  const currentUserReview = user
    ? avaliacoes.find((avaliacao) => getAvaliadorId(avaliacao) === user.id)
    : undefined;
  const averageRating =
    avaliacoes.length > 0
      ? avaliacoes.reduce((total, avaliacao) => total + avaliacao.nota, 0) /
        avaliacoes.length
      : 0;
  const missingProfileItems = user ? getMissingProfileItems(user) : [];
  const userIdade = user ? (user.idade ?? calcularIdade(user.dataNascimento)) : undefined;
  const isUnderage = userIdade === undefined || userIdade < 18;
  const participationBlocked = missingProfileItems.length > 0 || isUnderage;

  const reviewTarget = evento?.organizador ?? null;
  const canReview = Boolean(
    evento &&
    user &&
    reviewTarget &&
    isPast &&
    !isOrganizer &&
    !currentUserReview,
  );
  const reviewStatusMessage = !isPast
    ? "As avaliações serão liberadas depois da data do evento."
    : currentUserReview
      ? "Você já avaliou este evento."
      : isOrganizer
        ? "Organizadores não avaliam o próprio evento."
        : null;

  function showToast(msg: string) {
    setToast(msg);
    setTimeout(() => setToast(null), 3000);
  }

  async function refreshParticipacoes(eventoId?: number) {
    const targetEventoId = eventoId ?? evento?.id;
    if (!targetEventoId) return;
    const parts = await participacaoService.listByEvento(targetEventoId);
    setParticipacoes(parts);
  }

  function openParticipacaoModal() {
    setParticipacaoMessage(currentParticipation?.mensagemSolicitacao ?? "");
    setParticipacaoModalOpen(true);
  }

  async function handleSubmitParticipacao(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (!evento || !user) return;

    setSubmittingParticipacao(true);
    try {
      if (requestPending) {
        await participacaoService.update(evento.id, user.id, {
          mensagemSolicitacao: participacaoMessage,
        });
        showToast("Solicitação atualizada.");
      } else {
        await participacaoService.create(
          { eventoId: evento.id, mensagemSolicitacao: participacaoMessage },
          user.id,
        );
        showToast("Solicitação enviada!");
      }

      setParticipacaoModalOpen(false);
      setParticipacaoMessage("");
      await refreshParticipacoes(evento.id);
    } catch (error) {
      showToast(
        error instanceof Error
          ? error.message
          : "Erro ao solicitar participação.",
      );
    } finally {
      setSubmittingParticipacao(false);
    }
  }

  async function handleRemoveParticipacao() {
    if (!evento || !user || !currentParticipation) return;

    try {
      await participacaoService.remove(evento.id, user.id);
      showToast(
        requestPending ? "Solicitação cancelada." : "Solicitação removida.",
      );
      await refreshParticipacoes(evento.id);
    } catch (error) {
      showToast(
        error instanceof Error
          ? error.message
          : "Erro ao remover participação.",
      );
    }
  }

  function openResponseModal(
    participacao: Participacao,
    action: ParticipacaoResponseAction,
  ) {
    setResponseTarget(participacao);
    setResponseAction(action);
    setResponseMessage("");
  }

  async function handleSubmitResponse(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (!responseTarget) return;

    const target = responseTarget;
    setSubmittingResponse(true);
    try {
      await participacaoService.respond(target.idEvento, target.idSolicitante, {
        status: responseAction,
        mensagemResposta: responseMessage,
      });
      showToast(
        responseAction === "APROVADO"
          ? "Participante aprovado!"
          : "Participante recusado.",
      );
      setResponseTarget(null);
      setResponseMessage("");
      await refreshParticipacoes(target.idEvento);
    } catch (error) {
      showToast(
        error instanceof Error
          ? error.message
          : "Erro ao responder participação.",
      );
    } finally {
      setSubmittingResponse(false);
    }
  }

  async function handleDelete() {
    if (!evento) return;
    await eventoService.remove(evento.id);
    navigate("/events", { replace: true });
  }

  async function handleSubmitReview(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (!evento || !user || !reviewTarget || !canReview) return;

    setSubmittingReview(true);
    try {
      const created = await avaliacaoService.create(
        {
          eventoId: evento.id,
          avaliadoId: reviewTarget.id,
          nota: reviewNota,
          comentario: reviewComentario,
        },
        user.id,
      );
      setAvaliacoes((prev) => [created, ...prev]);
      setReviewComentario("");
      setReviewNota(5);
      setReviewModalOpen(false);
      showToast("Avaliação registrada!");
    } catch (error) {
      showToast(
        error instanceof Error ? error.message : "Erro ao registrar avaliação.",
      );
    } finally {
      setSubmittingReview(false);
    }
  }

  // --- Render states ---
  if (loading) {
    return (
      <div className={styles.loading}>
        <p>Carregando detalhes do evento...</p>
      </div>
    );
  }

  if (error || !evento) {
    return (
      <div className={styles.error}>
        <span className="material-symbols-outlined">error</span>
        <p>Evento não encontrado.</p>
      </div>
    );
  }

  const date = new Date(evento.dataEvento);
  const formattedDate = date.toLocaleDateString("pt-BR", {
    day: "2-digit",
    month: "long",
    year: "numeric",
  });
  const formattedTime = date.toLocaleTimeString("pt-BR", {
    hour: "2-digit",
    minute: "2-digit",
  });

  return (
    <div className={styles.page}>
      <button
        type="button"
        className={styles.backBtn}
        onClick={() => navigate(-1)}
      >
        <span className="material-symbols-outlined">arrow_back</span>
        Voltar
      </button>

      <div className={styles.card}>
        {/* Event image */}
        {evento.fotoUrl && (
          <div className={styles.imageContainer}>
            <img
              src={evento.fotoUrl}
              alt={evento.nome}
              className={styles.image}
            />
          </div>
        )}

        {/* Header */}
        <div className={styles.header}>
          <div
            className={`${styles.statusBadge} ${isPast ? styles.statusPast : ""}`}
          >
            <span className={styles.statusDot} />
            {isPast ? "Encerrado" : "Ativo"}
          </div>

          <h1 className={styles.title}>{evento.nome}</h1>

          <div className={styles.organizer}>
            <span className="material-symbols-outlined">person</span>
            Organizado por{" "}
            <Link
              to={`/profile/${evento.organizador.id}`}
              className={styles.organizerName}
            >
              {evento.organizador.nome}
            </Link>
          </div>
        </div>

        {/* Meta info */}
        <div className={styles.meta}>
          <div className={styles.metaItem}>
            <span className="material-symbols-outlined">calendar_today</span>
            <div>
              <span className={styles.metaLabel}>Data e hora</span>
              <span className={styles.metaValue}>
                {formattedDate} às {formattedTime}
              </span>
            </div>
          </div>

          <div className={styles.metaItem}>
            <span className="material-symbols-outlined">location_on</span>
            <div>
              <span className={styles.metaLabel}>Local</span>
              <span className={styles.metaValue}>
                {formatAddress(evento.local)}
              </span>
            </div>
          </div>

          <div className={styles.metaItem}>
            <span className="material-symbols-outlined">repeat</span>
            <div>
              <span className={styles.metaLabel}>Frequência</span>
              <span className={styles.metaValue}>
                {FREQ_LABELS[evento.frequencia] ?? evento.frequencia}
              </span>
            </div>
          </div>

          <div className={styles.metaItem}>
            <span className="material-symbols-outlined">group</span>
            <div>
              <span className={styles.metaLabel}>Participantes</span>
              <span className={styles.metaValue}>
                {evento.quantMaximaPessoas
                  ? `${confirmed.length} / ${evento.quantMaximaPessoas}`
                  : confirmed.length}
              </span>
            </div>
          </div>

          {evento.nivelDeHabilidade && (
            <div className={styles.metaItem}>
              <span className="material-symbols-outlined">trending_up</span>
              <div>
                <span className={styles.metaLabel}>Nível</span>
                <span className={styles.metaValue}>
                  {evento.nivelDeHabilidade}
                </span>
              </div>
            </div>
          )}
        </div>

        {/* Description */}
        {evento.descricao && (
          <div className={styles.section}>
            <h3 className={styles.sectionTitle}>
              <span className="material-symbols-outlined">description</span>
              Sobre o evento
            </h3>
            <p className={styles.description}>{evento.descricao}</p>
          </div>
        )}

        {/* Location map */}
        <div className={styles.section}>
          <h3 className={styles.sectionTitle}>
            <span className="material-symbols-outlined">map</span>
            Localização
          </h3>
          <LocationMap
            label={evento.nome}
            address={evento.local}
            latitude={evento.latitude}
            longitude={evento.longitude}
          />
        </div>

        {/* Categories */}
        {evento.categorias.length > 0 && (
          <div className={styles.section}>
            <h3 className={styles.sectionTitle}>
              <span className="material-symbols-outlined">sell</span>
              Categorias
            </h3>
            <div className={styles.categories}>
              {evento.categorias.map((c) => (
                <span key={c.id} className={styles.catBadge}>
                  {c.modalidade}
                </span>
              ))}
            </div>
          </div>
        )}

        {/* Confirmed participants */}
        <div className={styles.section}>
          <h3 className={styles.sectionTitle}>
            <span className="material-symbols-outlined">people</span>
            Participantes confirmados
            <span className={styles.countBadge}>{confirmed.length}</span>
          </h3>

          {confirmed.length === 0 ? (
            <div className={styles.emptySmall}>
              <span className="material-symbols-outlined">person_off</span>
              <p>Nenhum participante confirmado ainda</p>
            </div>
          ) : (
            <div className={styles.participantList}>
              {confirmed.map((p) => (
                <Link
                  key={getParticipacaoKey(p)}
                  to={`/profile/${p.solicitante.id}`}
                  className={styles.participantCard}
                >
                  <div className={styles.avatar}>
                    {getInitials(p.solicitante.nome)}
                  </div>
                  <div className={styles.participantContent}>
                    <div className={styles.participantName}>
                      {p.solicitante.nome}
                    </div>
                    <div className={styles.participantLogin}>
                      @{p.solicitante.login}
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </div>

        {/* Pending participants (organizer only) */}
        {isOrganizer && pending.length > 0 && (
          <div className={styles.section}>
            <h3 className={styles.sectionTitle}>
              <span className="material-symbols-outlined">hourglass_empty</span>
              Participantes pendentes
              <span className={styles.countBadge}>{pending.length}</span>
            </h3>

            <div className={styles.participantList}>
              {pending.map((p) => (
                <div
                  key={getParticipacaoKey(p)}
                  className={styles.participantCard}
                >
                  <div className={styles.avatar}>
                    {getInitials(p.solicitante.nome)}
                  </div>
                  <div className={styles.participantContent}>
                    <div className={styles.participantName}>
                      {p.solicitante.nome}
                    </div>
                    <div className={styles.participantLogin}>
                      @{p.solicitante.login}
                    </div>
                    {p.mensagemSolicitacao && (
                      <p className={styles.participantMessage}>
                        {p.mensagemSolicitacao}
                      </p>
                    )}
                  </div>
                  <div className={styles.pendingActions}>
                    <button
                      className={styles.approveBtn}
                      onClick={() => openResponseModal(p, "APROVADO")}
                      title="Aprovar"
                    >
                      <span className="material-symbols-outlined">check</span>
                    </button>
                    <button
                      className={styles.rejectBtn}
                      onClick={() => openResponseModal(p, "RECUSADO")}
                      title="Recusar"
                    >
                      <span className="material-symbols-outlined">close</span>
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Reviews */}
        <div className={styles.section}>
          <h3 className={styles.sectionTitle}>
            <span className="material-symbols-outlined">star</span>
            Avaliações
            <span className={styles.countBadge}>{avaliacoes.length}</span>
          </h3>

          <div className={styles.reviewSummary}>
            <div className={styles.reviewMetric}>
              <span className={styles.reviewMetricLabel}>Média do evento</span>
              <strong className={styles.reviewMetricValue}>
                {avaliacoes.length > 0 ? averageRating.toFixed(1) : "--"}
              </strong>
              <div className={styles.reviewStars} aria-hidden="true">
                {RATING_OPTIONS.map((value) => (
                  <span
                    key={value}
                    className={`material-symbols-outlined ${styles.reviewStar} ${
                      value <= Math.round(averageRating)
                        ? styles.reviewStarFilled
                        : ""
                    }`}
                  >
                    star
                  </span>
                ))}
              </div>
            </div>

            <div className={styles.reviewActionPanel}>
              {canReview ? (
                <button
                  type="button"
                  className={styles.primaryBtn}
                  onClick={() => setReviewModalOpen(true)}
                >
                  <span className="material-symbols-outlined">rate_review</span>
                  Avaliar organizador
                </button>
              ) : (
                reviewStatusMessage && (
                  <p className={styles.reviewHint}>{reviewStatusMessage}</p>
                )
              )}
            </div>
          </div>

          {avaliacoes.length === 0 ? (
            <div className={styles.emptySmall}>
              <span className="material-symbols-outlined">reviews</span>
              <p>Nenhuma avaliação registrada ainda</p>
            </div>
          ) : (
            <div className={styles.reviewList}>
              {avaliacoes.map((avaliacao) => (
                <article key={avaliacao.id} className={styles.reviewItem}>
                  <div className={styles.reviewHeader}>
                    <div
                      className={styles.reviewStars}
                      aria-label={`${avaliacao.nota} de 5`}
                    >
                      {RATING_OPTIONS.map((value) => (
                        <span
                          key={value}
                          className={`material-symbols-outlined ${styles.reviewStar} ${
                            value <= avaliacao.nota
                              ? styles.reviewStarFilled
                              : ""
                          }`}
                        >
                          star
                        </span>
                      ))}
                    </div>
                    <span className={styles.reviewDate}>
                      {new Date(avaliacao.dataAvaliacao).toLocaleDateString(
                        "pt-BR",
                      )}
                    </span>
                  </div>
                  {getAvaliadorId(avaliacao) === user?.id && (
                    <strong className={styles.reviewAuthor}>
                      Sua avaliação
                    </strong>
                  )}
                  {avaliacao.comentario && (
                    <p className={styles.reviewComment}>
                      {avaliacao.comentario}
                    </p>
                  )}
                </article>
              ))}
            </div>
          )}
        </div>

        {/* Action buttons */}
        <div className={styles.actions}>
          {isOrganizer ? (
            <>
              <Link
                to={`/events/${evento.id}/edit`}
                className={styles.primaryBtn}
              >
                <span className="material-symbols-outlined">edit</span>
                Editar
              </Link>
              <button
                className={styles.dangerBtn}
                onClick={() => setConfirmDelete(true)}
              >
                <span className="material-symbols-outlined">delete</span>
                Excluir
              </button>
            </>
          ) : currentParticipation ? (
            <div className={styles.requestPanel}>
              <div>
                <strong className={styles.requestStatus}>
                  {alreadyJoined
                    ? "Você já participa"
                    : requestPending
                      ? "Solicitação pendente"
                      : "Solicitação recusada"}
                </strong>
                {currentParticipation.mensagemSolicitacao && (
                  <p className={styles.requestText}>
                    Sua mensagem: {currentParticipation.mensagemSolicitacao}
                  </p>
                )}
                {currentParticipation.mensagemResposta && (
                  <p className={styles.requestText}>
                    Resposta: {currentParticipation.mensagemResposta}
                  </p>
                )}
              </div>

              {requestPending && !isPast && (
                <div className={styles.requestActions}>
                  <button
                    type="button"
                    className={styles.secondaryBtn}
                    onClick={openParticipacaoModal}
                  >
                    <span className="material-symbols-outlined">edit</span>
                    Editar mensagem
                  </button>
                  <button
                    type="button"
                    className={styles.dangerBtn}
                    onClick={handleRemoveParticipacao}
                  >
                    <span className="material-symbols-outlined">close</span>
                    Cancelar solicitação
                  </button>
                </div>
              )}

              {requestRejected && !isPast && (
                <div className={styles.requestActions}>
                  <button
                    type="button"
                    className={styles.secondaryBtn}
                    onClick={handleRemoveParticipacao}
                  >
                    <span className="material-symbols-outlined">delete</span>
                    Remover solicitação
                  </button>
                </div>
              )}
            </div>
          ) : user && !isPast ? (
            participationBlocked ? (
              <div className={styles.profileWarning}>
                <span className="material-symbols-outlined">warning</span>
                <div>
                  {isUnderage ? (
                    <p>
                      {userIdade === undefined
                        ? "Informe sua data de nascimento no perfil: é necessário ter 18 anos ou mais para participar de eventos."
                        : "É necessário ter 18 anos ou mais para participar de eventos."}
                    </p>
                  ) : (
                    <p>
                      Complete seu perfil para participar de eventos. Falta
                      adicionar: <strong>{missingProfileItems.join(", ")}</strong>.
                    </p>
                  )}
                  {(!isUnderage || userIdade === undefined) && (
                    <Link to="/profile/edit" className={styles.warningLink}>
                      Completar perfil
                    </Link>
                  )}
                </div>
              </div>
            ) : (
              <button
                className={styles.primaryBtn}
                onClick={openParticipacaoModal}
              >
                <span className="material-symbols-outlined">person_add</span>
                Participar
              </button>
            )
          ) : null}
        </div>
      </div>

      <Modal
        open={participacaoModalOpen}
        onClose={() => {
          if (!submittingParticipacao) setParticipacaoModalOpen(false);
        }}
      >
        <form className={styles.reviewForm} onSubmit={handleSubmitParticipacao}>
          <div>
            <span className={styles.reviewFormEyebrow}>Participação</span>
            <h2>
              {requestPending ? "Editar solicitação" : "Solicitar participação"}
            </h2>
          </div>

          <label
            className={styles.reviewFormLabel}
            htmlFor="participacaoMessage"
          >
            Mensagem
          </label>
          <textarea
            id="participacaoMessage"
            className={styles.reviewTextarea}
            value={participacaoMessage}
            onChange={(e) => setParticipacaoMessage(e.target.value)}
            maxLength={300}
            rows={5}
            placeholder="Conte ao organizador por que você quer participar"
          />

          <button
            type="submit"
            className={styles.primaryBtn}
            disabled={submittingParticipacao}
          >
            <span className="material-symbols-outlined">send</span>
            {submittingParticipacao
              ? "Enviando..."
              : requestPending
                ? "Salvar solicitação"
                : "Enviar solicitação"}
          </button>
        </form>
      </Modal>

      <Modal
        open={responseTarget !== null}
        onClose={() => {
          if (!submittingResponse) setResponseTarget(null);
        }}
      >
        <form className={styles.reviewForm} onSubmit={handleSubmitResponse}>
          <div>
            <span className={styles.reviewFormEyebrow}>Solicitação</span>
            <h2>
              {responseAction === "APROVADO" ? "Aprovar" : "Recusar"}{" "}
              {responseTarget?.solicitante.nome}
            </h2>
          </div>

          <label className={styles.reviewFormLabel} htmlFor="responseMessage">
            Mensagem de resposta
          </label>
          <textarea
            id="responseMessage"
            className={styles.reviewTextarea}
            value={responseMessage}
            onChange={(e) => setResponseMessage(e.target.value)}
            maxLength={300}
            rows={5}
            placeholder="Escreva uma resposta opcional para o participante"
          />

          <button
            type="submit"
            className={
              responseAction === "APROVADO"
                ? styles.primaryBtn
                : styles.dangerBtn
            }
            disabled={submittingResponse}
          >
            <span className="material-symbols-outlined">
              {responseAction === "APROVADO" ? "check" : "close"}
            </span>
            {submittingResponse
              ? "Enviando..."
              : responseAction === "APROVADO"
                ? "Aprovar solicitação"
                : "Recusar solicitação"}
          </button>
        </form>
      </Modal>

      <Modal
        open={reviewModalOpen}
        onClose={() => {
          if (!submittingReview) setReviewModalOpen(false);
        }}
      >
        <form className={styles.reviewForm} onSubmit={handleSubmitReview}>
          <div>
            <span className={styles.reviewFormEyebrow}>Avaliação</span>
            <h2>Avaliar {reviewTarget?.nome}</h2>
          </div>

          <div
            className={styles.ratingInput}
            role="radiogroup"
            aria-label="Nota"
          >
            {RATING_OPTIONS.map((value) => (
              <button
                key={value}
                type="button"
                className={`${styles.ratingButton} ${
                  value <= reviewNota ? styles.ratingButtonActive : ""
                }`}
                onClick={() => setReviewNota(value)}
                aria-pressed={value <= reviewNota}
              >
                <span className="material-symbols-outlined">star</span>
              </button>
            ))}
          </div>

          <label className={styles.reviewFormLabel} htmlFor="reviewComentario">
            Comentário
          </label>
          <textarea
            id="reviewComentario"
            className={styles.reviewTextarea}
            value={reviewComentario}
            onChange={(e) => setReviewComentario(e.target.value)}
            maxLength={250}
            rows={5}
            placeholder="Conte como foi a experiência"
          />

          <button
            type="submit"
            className={styles.primaryBtn}
            disabled={submittingReview}
          >
            <span className="material-symbols-outlined">send</span>
            {submittingReview ? "Enviando..." : "Enviar avaliação"}
          </button>
        </form>
      </Modal>

      {/* Delete confirmation modal */}
      {confirmDelete && (
        <div
          className={styles.overlay}
          onClick={() => setConfirmDelete(false)}
          role="presentation"
        >
          <div
            className={styles.confirmBox}
            onClick={(e) => e.stopPropagation()}
          >
            <h3>Excluir evento?</h3>
            <p>
              Tem certeza que deseja excluir &ldquo;{evento.nome}&rdquo;? Esta
              ação não pode ser desfeita.
            </p>
            <div className={styles.confirmActions}>
              <button
                className={styles.secondaryBtn}
                onClick={() => setConfirmDelete(false)}
              >
                Cancelar
              </button>
              <button className={styles.dangerBtn} onClick={handleDelete}>
                Excluir
              </button>
            </div>
          </div>
        </div>
      )}

      {toast && <div className={styles.toast}>{toast}</div>}
    </div>
  );
}
