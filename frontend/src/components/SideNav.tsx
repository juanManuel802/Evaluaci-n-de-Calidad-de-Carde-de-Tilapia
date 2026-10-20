import { useState } from "react";
import { logout } from "../services/authService";
import {
  getRoleCode,
  removeLocalToken as clearStoredSession,
} from "../services/authStorage";

export type SideNavPage = "register" | "evaluate" | "registro" | "admin" | "evaluated" | "evaluados";

interface SideNavProps {
  current?: SideNavPage;
}

/**
 * Navigation sidebar implementing DC-HU2, DS-HU2-logout, matching Mockups HU-03, HU-04, HU-05.
 * Background: #094b72 (petroleum blue). Clean white serif branding without icons.
 * Inactive items have the exact same white color and hover effect as "Cerrar sesión".
 */
export default function SideNav({ current }: SideNavProps) {
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const roleCode = getRoleCode();

  // DC-HU2 explicit method: removeLocalToken(): void
  const removeLocalToken = (): void => {
    clearStoredSession();
  };

  // DC-HU2 explicit method: submitLogout(): void (DS-HU2-logout, PS-07, CN-10)
  const submitLogout = async (): Promise<void> => {
    if (isLoggingOut) return;
    setIsLoggingOut(true);
    try {
      await logout();
    } catch {
      // Even if network fails, ensure local session is cleared
    } finally {
      removeLocalToken();
      window.location.replace("#/login");
    }
  };

  const isEvaluatedActive = current === "evaluados" || current === "evaluated";

  const getLinkStyle = (isActive: boolean) => ({
    color: isActive ? "#094b72" : "rgba(255, 255, 255, 0.9)",
    textDecoration: "none",
  });

  return (
    <aside
      aria-label="Barra lateral del sistema"
      className="side w-56 md:w-60 bg-[#094b72] text-white flex flex-col justify-between p-6 min-h-screen flex-shrink-0"
    >
      <div>
        {/* Brand Header: Clean 2-line Serif without icons */}
        <header className="pt-2 pb-6 mb-4">
          <h2 className="text-2xl font-serif text-white leading-tight">
            Calidad de<br />Tilapia
          </h2>
        </header>

        {/* Navigation links */}
        <nav aria-label="Navegación principal">
          <ul className="flex flex-col gap-2 p-0 m-0 list-none">
            {roleCode === "ADMINISTRADOR" ? (
              <>
                <li className="m-0 p-0">
                  <a
                    href="#/admin"
                    id="nav-admin-link"
                    aria-current={current === "admin" ? "page" : undefined}
                    style={getLinkStyle(current === "admin")}
                    className={`block w-full px-4 py-3 rounded-xl text-sm transition-colors ${
                      current === "admin"
                        ? "bg-white font-semibold shadow-sm"
                        : "hover:bg-white/10"
                    }`}
                  >
                    Panel Administrador
                  </a>
                </li>
                <li className="m-0 p-0">
                  <a
                    href="#/registro"
                    id="nav-registro-link"
                    aria-current={current === "registro" ? "page" : undefined}
                    style={getLinkStyle(current === "registro")}
                    className={`block w-full px-4 py-3 rounded-xl text-sm transition-colors ${
                      current === "registro"
                        ? "bg-white font-semibold shadow-sm"
                        : "hover:bg-white/10"
                    }`}
                  >
                    Registrar usuario
                  </a>
                </li>
              </>
            ) : (
              <>
                <li className="m-0 p-0">
                  <a
                    href="#/register"
                    id="nav-register-batch-link"
                    aria-current={current === "register" ? "page" : undefined}
                    style={getLinkStyle(current === "register")}
                    className={`block w-full px-4 py-3 rounded-xl text-sm transition-colors ${
                      current === "register"
                        ? "bg-white font-semibold shadow-sm"
                        : "hover:bg-white/10"
                    }`}
                  >
                    Registrar lote
                  </a>
                </li>
                <li className="m-0 p-0">
                  <a
                    href="#/evaluate"
                    id="nav-evaluate-batch-link"
                    aria-current={current === "evaluate" ? "page" : undefined}
                    style={getLinkStyle(current === "evaluate")}
                    className={`block w-full px-4 py-3 rounded-xl text-sm transition-colors ${
                      current === "evaluate"
                        ? "bg-white font-semibold shadow-sm"
                        : "hover:bg-white/10"
                    }`}
                  >
                    Evaluar lote
                  </a>
                </li>
                <li className="m-0 p-0">
                  <a
                    href="#/evaluados"
                    id="nav-evaluated-batches-link"
                    aria-current={isEvaluatedActive ? "page" : undefined}
                    style={getLinkStyle(isEvaluatedActive)}
                    className={`block w-full px-4 py-3 rounded-xl text-sm transition-colors ${
                      isEvaluatedActive
                        ? "bg-white font-semibold shadow-sm"
                        : "hover:bg-white/10"
                    }`}
                  >
                    Lotes evaluados
                  </a>
                </li>
              </>
            )}

            {/* Cerrar sesión with identical styling */}
            <li className="m-0 p-0">
              <button
                type="button"
                id="nav-logout-button"
                onClick={submitLogout}
                disabled={isLoggingOut}
                style={{ color: "rgba(255, 255, 255, 0.9)" }}
                className="w-full text-left px-4 py-3 rounded-xl text-sm hover:bg-white/10 transition-colors cursor-pointer disabled:opacity-50 mt-1 block"
              >
                {isLoggingOut ? "Cerrando sesión..." : "Cerrar sesión"}
              </button>
            </li>
          </ul>
        </nav>
      </div>

      <div />
    </aside>
  );
}
