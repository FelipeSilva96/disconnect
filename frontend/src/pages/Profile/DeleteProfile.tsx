import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
import { usuarioService } from "@/services/usuarioService";
import styles from "./Profile.module.css";

export function DeleteProfilePage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [confirmation, setConfirmation] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  const currentUser = user;

  if (!currentUser) {
    return null;
  }

  async function handleDelete() {
    const usuario = currentUser;
    if (!usuario) return;

    setError("");

    if (confirmation !== usuario.login) {
      setError(`Digite ${usuario.login} para confirmar.`);
      return;
    }

    if (!usuario.token) {
      setError("Sessão expirada. Entre novamente para apagar a conta.");
      return;
    }

    setSubmitting(true);
    try {
      await usuarioService.deletarConta(usuario.id, usuario.token);
      logout();
      navigate("/landing", { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erro ao apagar conta.");
      setSubmitting(false);
    }
  }

  return (
    <main className={styles.page}>
      <div className={styles.formHeader}>
        <Link to="/profile" className={styles.backLink}>
          <span className="material-symbols-outlined">arrow_back</span>
          Voltar
        </Link>
        <h1>Apagar conta</h1>
      </div>

      <section className={styles.dangerPanel}>
        <span className="material-symbols-outlined">warning</span>
        <div>
          <h2>Esta ação é permanente</h2>
          <p>
            Sua conta será removida e os eventos criados por você também serão
            apagados pelas regras do banco.
          </p>
        </div>
      </section>

      <div className={styles.form}>
        {error && <p className={styles.error}>{error}</p>}

        <label>
          Digite seu login para confirmar
          <input
            type="text"
            value={confirmation}
            onChange={(event) => setConfirmation(event.target.value)}
            placeholder={currentUser.login}
          />
        </label>

        <button
          type="button"
          className={styles.dangerButton}
          onClick={handleDelete}
          disabled={submitting}
        >
          <span className="material-symbols-outlined">delete_forever</span>
          {submitting ? "Apagando..." : "Apagar minha conta"}
        </button>
      </div>
    </main>
  );
}
