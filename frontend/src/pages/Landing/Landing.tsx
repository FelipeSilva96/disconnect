import { useState } from "react";
import { Modal } from "@/components/Modal";
import { LoginPage } from "@/pages/Login";
import { RegisterPage } from "@/pages/Register";
import styles from "./Landing.module.css";

const features = [
  {
    icon: "person_add",
    title: "Crie seu perfil",
    description:
      "Cadastre-se e adicione seus interesses, hobbies e nível de experiência em cada atividade.",
  },
  {
    icon: "search",
    title: "Encontre eventos",
    description:
      "Busque atividades por categoria, localização, data ou nível de habilidade.",
  },
  {
    icon: "event",
    title: "Organize encontros",
    description:
      "Crie seus próprios eventos e convide pessoas para participar de atividades que você ama.",
  },
  {
    icon: "groups",
    title: "Conheça pessoas",
    description:
      "Expanda seu círculo social com pessoas que compartilham suas paixões.",
  },
];

const categories = [
  { icon: "sports_soccer", label: "Esportes" },
  { icon: "menu_book", label: "Estudos" },
  { icon: "sports_esports", label: "Jogos" },
  { icon: "music_note", label: "Música" },
  { icon: "palette", label: "Arte" },
  { icon: "park", label: "Natureza" },
];

export function LandingPage() {
  const [modal, setModal] = useState<"login" | "register" | null>(null);

  function openLogin() {
    setModal("login");
  }

  function openRegister() {
    setModal("register");
  }

  function closeModal() {
    setModal(null);
  }

  return (
    <div className={styles.page}>
      {/* Modals */}
      <Modal open={modal === "login"} onClose={closeModal}>
        <LoginPage onSwitchToRegister={() => setModal("register")} />
      </Modal>
      <Modal open={modal === "register"} onClose={closeModal}>
        <RegisterPage onSwitchToLogin={() => setModal("login")} />
      </Modal>

      {/* Header */}
      <header className={styles.header}>
        <div className={styles.logo}>
          <span className={styles.logoBold}>&lt;dis&gt;</span>connect
        </div>
        <button onClick={openLogin} className={styles.btnLogin}>
          <span className="material-symbols-outlined">account_circle</span>
          Entrar
        </button>
      </header>

      {/* Hero */}
      <section className={styles.hero}>
        <div className={styles.heroContent}>
          <h1 className={styles.heroTitle}>
            Desconecte-se do virtual.
            <br />
            <span className={styles.highlight}>Conecte-se ao real.</span>
          </h1>
          <p className={styles.heroSubtitle}>
            Uma plataforma para encontrar pessoas com interesses em comum e
            participar de atividades presenciais. Esportes, estudos, hobbies — o
            que você quiser fazer, com quem quiser conhecer.
          </p>
          <button onClick={openRegister} className={styles.btnCta}>
            Comece agora
          </button>
        </div>
      </section>

      {/* Features */}
      <section className={styles.features}>
        <h2 className={styles.sectionTitle}>Como funciona</h2>
        <div className={styles.featureGrid}>
          {features.map((f) => (
            <div key={f.icon} className={styles.featureCard}>
              <div className={styles.featureIcon}>
                <span className="material-symbols-outlined">{f.icon}</span>
              </div>
              <h3 className={styles.featureTitle}>{f.title}</h3>
              <p className={styles.featureDescription}>{f.description}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Categories */}
      <section className={styles.categories}>
        <h2 className={styles.sectionTitle}>Explore atividades</h2>
        <div className={styles.categoriesGrid}>
          {categories.map((c) => (
            <div key={c.icon} className={styles.categoryCard}>
              <span className="material-symbols-outlined">{c.icon}</span>
              <span>{c.label}</span>
            </div>
          ))}
        </div>
      </section>

      {/* CTA */}
      <section className={styles.cta}>
        <h2 className={styles.ctaTitle}>Pronto para se conectar?</h2>
        <p className={styles.ctaSubtitle}>
          Junte-se a milhares de pessoas que estão descobrindo novas amizades e
          experiências.
        </p>
        <button onClick={openRegister} className={styles.btnCtaLarge}>
          Criar minha conta
        </button>
      </section>

      {/* Footer */}
      <footer className={styles.footer}>
        <div className={styles.footerContent}>
          <div className={styles.footerLogo}>
            <span className={styles.logoBold}>&lt;dis&gt;</span>connect
          </div>
          <p className={styles.footerText}>
            Projeto desenvolvido para a disciplina de Trabalho Interdisciplinar
            II — PUC Minas
          </p>
          <div className={styles.footerLinks}>
            <a
              href="https://github.com/ICEI-PUC-Minas-CC-TI/plmg-cc-ti2-2026-1-v2-g07-dis-connect"
              target="_blank"
              rel="noopener noreferrer"
            >
              GitHub
            </a>
          </div>
        </div>
      </footer>
    </div>
  );
}
