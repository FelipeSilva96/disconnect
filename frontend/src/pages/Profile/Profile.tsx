import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
import { usuarioService } from "@/services/usuarioService";
import type { Usuario } from "@/types";
import styles from "./Profile.module.css";

function getInitials(name: string) {
  return name
    .split(" ")
    .slice(0, 2)
    .map((part) => part[0])
    .join("")
    .toUpperCase();
}

const PROFILE_ITEMS: Array<{
  key: string;
  label: string;
  isComplete: (profile: Usuario) => boolean;
}> = [
  { key: "foto", label: "Foto", isComplete: (p) => Boolean(p.urlFoto?.trim()) },
  {
    key: "bio",
    label: "Biografia",
    isComplete: (p) => Boolean(p.biografia?.trim()),
  },
  {
    key: "cidade",
    label: "Cidade",
    isComplete: (p) => Boolean(p.localizacao?.trim()),
  },
  {
    key: "hobbies",
    label: "Hobbies",
    isComplete: (p) => (p.hobbies ?? []).length > 0,
  },
];

export function ProfilePage() {
  const { user } = useAuth();
  const { id } = useParams<{ id: string }>();
  const [profile, setProfile] = useState<Usuario | null>(user);
  const [loading, setLoading] = useState(false);
  const [notFound, setNotFound] = useState(false);

  const profileId = id ? Number(id) : user?.id;
  const isOwnProfile = user !== null && profileId === user.id;

  useEffect(() => {
    if (!profileId) {
      return;
    }

    if (isOwnProfile) {
      setProfile(user);
      setNotFound(false);
      return;
    }

    setLoading(true);
    usuarioService
      .buscarPorId(profileId)
      .then((usuario) => {
        setProfile(usuario ?? null);
        setNotFound(!usuario);
      })
      .catch(() => setNotFound(true))
      .finally(() => setLoading(false));
  }, [isOwnProfile, profileId, user]);

  if (loading) {
    return <p className={styles.status}>Carregando perfil...</p>;
  }

  if (notFound || !profile) {
    return <p className={styles.status}>Perfil não encontrado.</p>;
  }

  const memberSince = profile.dataCriacao
    ? new Date(profile.dataCriacao).toLocaleDateString("pt-BR", {
        month: "short",
        year: "numeric",
      })
    : null;
  const hobbies = profile.hobbies ?? [];
  const niveis = profile.nivelExperiencia ?? {};
  const totalAvaliacoes = profile.totalAvaliacoes ?? 0;
  const missingItems = PROFILE_ITEMS.filter(
    (item) => !item.isComplete(profile),
  );

  return (
    <main className={styles.page}>
      {/* Hero */}
      <section className={styles.hero}>
        <div className={styles.banner} />
        <div className={styles.heroBody}>
          <div className={styles.avatar}>
            {profile.urlFoto ? (
              <img src={profile.urlFoto} alt={profile.nome} />
            ) : (
              getInitials(profile.nome)
            )}
          </div>

          <div className={styles.identity}>
            <h1>{profile.nome}</h1>
            <p className={styles.handle}>@{profile.login}</p>
            <div className={styles.identityMeta}>
              {profile.localizacao && (
                <span>
                  <span className="material-symbols-outlined">location_on</span>
                  {profile.localizacao}
                </span>
              )}
              {memberSince && (
                <span>
                  <span className="material-symbols-outlined">
                    calendar_month
                  </span>
                  Desde {memberSince}
                </span>
              )}
            </div>
          </div>

          {isOwnProfile && (
            <Link to="/profile/edit" className={styles.editButton}>
              <span className="material-symbols-outlined">edit</span>
              Editar perfil
            </Link>
          )}
        </div>

        {/* Stats */}
        <div className={styles.stats}>
          <div className={styles.stat}>
            <strong>
              {totalAvaliacoes > 0 && profile.avaliacaoMedia !== undefined
                ? profile.avaliacaoMedia.toFixed(1)
                : "--"}
            </strong>
            <small>
              <span className="material-symbols-outlined">star</span>
              {totalAvaliacoes}{" "}
              {totalAvaliacoes === 1 ? "avaliação" : "avaliações"}
            </small>
          </div>
          <div className={styles.stat}>
            <strong>{profile.idade ?? "--"}</strong>
            <small>
              <span className="material-symbols-outlined">cake</span>
              {profile.idade ? "anos" : "idade"}
            </small>
          </div>
          <div className={styles.stat}>
            <strong>{hobbies.length}</strong>
            <small>
              <span className="material-symbols-outlined">interests</span>
              {hobbies.length === 1 ? "hobby" : "hobbies"}
            </small>
          </div>
        </div>
      </section>

      {/* Incomplete profile nudge (own profile only) */}
      {isOwnProfile && missingItems.length > 0 && (
        <section className={styles.completeCard}>
          <span className="material-symbols-outlined">info</span>
          <div>
            <strong>Complete seu perfil para participar de eventos</strong>
            <p>
              Falta adicionar:{" "}
              {missingItems.map((item) => item.label.toLowerCase()).join(", ")}.
            </p>
          </div>
          <Link to="/profile/edit" className={styles.completeLink}>
            Completar
          </Link>
        </section>
      )}

      {/* About */}
      <section className={styles.card}>
        <h2>
          <span className="material-symbols-outlined">person</span>
          Sobre
        </h2>
        {profile.biografia ? (
          <p className={styles.bio}>{profile.biografia}</p>
        ) : (
          <p className={styles.emptyBio}>
            Este perfil ainda não tem biografia.
          </p>
        )}
        {isOwnProfile && profile.email && (
          <p className={styles.contact}>
            <span className="material-symbols-outlined">mail</span>
            {profile.email}
          </p>
        )}
      </section>

      {/* Hobbies + experience */}
      <section className={styles.card}>
        <h2>
          <span className="material-symbols-outlined">interests</span>
          Hobbies e interesses
        </h2>
        {hobbies.length === 0 ? (
          <p className={styles.emptyBio}>Nenhum hobby cadastrado ainda.</p>
        ) : (
          <div className={styles.chipList}>
            {hobbies.map((hobby) => (
              <span key={hobby} className={styles.chip}>
                {hobby}
                {niveis[hobby] && (
                  <em className={styles.chipLevel}>{niveis[hobby]}</em>
                )}
              </span>
            ))}
          </div>
        )}
      </section>

      {isOwnProfile && (
        <section className={styles.dangerZone}>
          <Link to="/profile/delete" className={styles.deleteLink}>
            <span className="material-symbols-outlined">delete</span>
            Apagar conta
          </Link>
        </section>
      )}
    </main>
  );
}
