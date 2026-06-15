import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
import styles from "../Login/Login.module.css";

interface RegisterPageProps {
  onSwitchToLogin: () => void;
}

export function RegisterPage({ onSwitchToLogin }: RegisterPageProps) {
  const { register, loading } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    nome: "",
    email: "",
    login: "",
    senha: "",
    confirmarSenha: "",
  });
  const [error, setError] = useState("");

  function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");

    if (form.senha !== form.confirmarSenha) {
      setError("As senhas não coincidem.");
      return;
    }

    try {
      await register({
        nome: form.nome,
        email: form.email,
        login: form.login,
        senha: form.senha,
      });
      navigate("/");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erro ao cadastrar.");
    }
  }

  return (
    <div>
      <h2 className={styles.title}>
        <span className={styles.logoTag}>&lt;dis&gt;</span>connect
      </h2>
      <p className={styles.subtitle}>Crie sua conta</p>

      <form onSubmit={handleSubmit} className={styles.form}>
        {error && <p className={styles.error}>{error}</p>}

        <label className={styles.label}>
          Nome
          <input
            type="text"
            name="nome"
            className={styles.input}
            value={form.nome}
            onChange={handleChange}
            required
          />
        </label>

        <label className={styles.label}>
          E-mail
          <input
            type="email"
            name="email"
            className={styles.input}
            value={form.email}
            onChange={handleChange}
            required
          />
        </label>

        <label className={styles.label}>
          Login
          <input
            type="text"
            name="login"
            className={styles.input}
            value={form.login}
            onChange={handleChange}
            required
          />
        </label>

        <label className={styles.label}>
          Senha
          <input
            type="password"
            name="senha"
            className={styles.input}
            value={form.senha}
            onChange={handleChange}
            required
          />
        </label>

        <label className={styles.label}>
          Confirmar senha
          <input
            type="password"
            name="confirmarSenha"
            className={styles.input}
            value={form.confirmarSenha}
            onChange={handleChange}
            required
          />
        </label>

        <button type="submit" className={styles.btn} disabled={loading}>
          {loading ? "Cadastrando..." : "Cadastrar"}
        </button>
      </form>

      <p className={styles.footer}>
        Já tem conta?{" "}
        <button
          type="button"
          className={styles.switchLink}
          onClick={onSwitchToLogin}
        >
          Entrar
        </button>
      </p>
    </div>
  );
}
