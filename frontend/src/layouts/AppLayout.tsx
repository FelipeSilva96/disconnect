import { Outlet, useLocation } from "react-router-dom";
import { Header } from "@/components/Header";
import styles from "./AppLayout.module.css";

export function AppLayout() {
  const location = useLocation();

  // The home page manages its own split layout (cards + map), full-bleed
  const fullBleed = location.pathname === "/";

  return (
    <div className={styles.layout}>
      <Header />
      <main className={`${styles.main} ${fullBleed ? styles.fullBleed : ""}`}>
        <Outlet />
      </main>
    </div>
  );
}
