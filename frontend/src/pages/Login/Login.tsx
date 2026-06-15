import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
import styles from "./Login.module.css";

interface LoginPageProps {
  onSwitchToRegister: () => void;
}

export function LoginPage({ onSwitchToRegister }: LoginPageProps) {
  const { login, loading } = useAuth();
  const navigate = useNavigate();
  const [loginInput, setLoginInput] = useState("");
  const [senha, setSenha] = useState("");
  const [error, setError] = useState("");

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    try {
      await login({ login: loginInput, senha });
      navigate("/");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erro ao fazer login.");
    }
  }

  return (
    <div>
      <h2 className={styles.title}>
        <span className={styles.logoTag}>&lt;dis&gt;</span>connect
      </h2>
      <p className={styles.subtitle}>Entre na sua conta</p>

      <form onSubmit={handleSubmit} className={styles.form}>
        {error && <p className={styles.error}>{error}</p>}

        <label className={styles.label}>
          Login ou e-mail
          <input
            type="text"
            className={styles.input}
            value={loginInput}
            onChange={(e) => setLoginInput(e.target.value)}
            placeholder="Seu login ou e-mail"
            required
          />
        </label>

        <label className={styles.label}>
          Senha
          <input
            type="password"
            className={styles.input}
            value={senha}
            onChange={(e) => setSenha(e.target.value)}
            required
          />
        </label>

        <button type="submit" className={styles.btn} disabled={loading}>
          {loading ? "Entrando..." : "Entrar"}
        </button>
      </form>

      <p className={styles.footer}>
        Não tem conta?{" "}
        <button
          type="button"
          className={styles.switchLink}
          onClick={onSwitchToRegister}
        >
          Cadastre-se
        </button>
      </p>
    </div>
  );
}
