import { useEffect, useRef, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
import { SearchBar } from "@/components/SearchBar";
import styles from "./Header.module.css";

export function Header() {
  const { user, logout } = useAuth();
  const location = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  // Don't show header on landing page
  const hidden = location.pathname === "/landing";

  // Close dropdown on outside click and on navigation
  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setMenuOpen(false);
      }
    }
    document.addEventListener("click", handleClickOutside);
    return () => document.removeEventListener("click", handleClickOutside);
  }, []);

  useEffect(() => {
    setMenuOpen(false);
  }, [location]);

  if (hidden) return null;

  return (
    <header className={styles.header}>
      <Link to="/" className={styles.logoLink}>
        <div className={styles.logo}>
          <span className={styles.logoBold}>&lt;dis&gt;</span>connect
        </div>
      </Link>

      <div className={styles.searchSlot}>
        <SearchBar />
      </div>

      {user && (
        <div className={styles.actions}>
          <Link to="/events/create" className={styles.createBtn}>
            <span className="material-symbols-outlined" aria-hidden="true">
              add
            </span>
            <span className={styles.createLabel}>Criar evento</span>
          </Link>

          <div className={styles.userMenu} ref={menuRef}>
            <button
              className={styles.userMenuToggle}
              aria-haspopup="menu"
              aria-expanded={menuOpen}
              onClick={(e) => {
                e.stopPropagation();
                setMenuOpen((prev) => !prev);
              }}
            >
              <svg
                className={styles.userIcon}
                width="24"
                height="24"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                <circle cx="12" cy="7" r="4" />
              </svg>
              <span className={styles.userName}>{user.nome}</span>
              <svg
                className={styles.chevronIcon}
                width="16"
                height="16"
                viewBox="0 0 16 16"
                fill="currentColor"
              >
                <path d="M4.427 6.573l3.396 3.396a.25.25 0 00.354 0l3.396-3.396A.25.25 0 0011.396 6H4.604a.25.25 0 00-.177.427z" />
              </svg>
            </button>

            <div
              className={`${styles.userDropdown} ${menuOpen ? styles.show : ""}`}
            >
              <Link to="/events" className={styles.dropdownItem}>
                <span className="material-symbols-outlined">event</span>
                Meus eventos
              </Link>
              <Link to="/profile" className={styles.dropdownItem}>
                <span className="material-symbols-outlined">
                  account_circle
                </span>
                Perfil
              </Link>
              <div className={styles.dropdownDivider} />
              <button className={styles.logoutBtn} onClick={logout}>
                <svg
                  width="16"
                  height="16"
                  viewBox="0 0 16 16"
                  fill="currentColor"
                >
                  <path d="M10 12.5a.5.5 0 0 1-.5.5h-8a.5.5 0 0 1-.5-.5v-9a.5.5 0 0 1 .5-.5h8a.5.5 0 0 1 .5.5v2a.5.5 0 0 0 1 0v-2A1.5 1.5 0 0 0 9.5 2h-8A1.5 1.5 0 0 0 0 3.5v9A1.5 1.5 0 0 0 1.5 14h8a1.5 1.5 0 0 0 1.5-1.5v-2a.5.5 0 0 0-1 0v2z" />
                  <path d="M15.854 8.354a.5.5 0 0 0 0-.708l-3-3a.5.5 0 0 0-.708.708L14.293 7.5H5.5a.5.5 0 0 0 0 1h8.793l-2.147 2.146a.5.5 0 0 0 .708.708l3-3z" />
                </svg>
                Sair
              </button>
            </div>
          </div>
        </div>
      )}
    </header>
  );
}
