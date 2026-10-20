import { useState } from "react";
import UserRegistrationForm from "../components/UserRegistrationForm";
import UserRegistrationResultCard from "../components/UserRegistrationResultCard";
import { apiPost } from "../services/apiClient";
import type { RegisterUserRequestDTO, RegisterUserResponseDTO } from "../types/auth";

export default function RegisterUserPage() {
  const [registeredUser, setRegisteredUser] = useState<RegisterUserResponseDTO | null>(null);

  /**
   * Implements submitRegistration from DC-HU1 diagram using apiPost.
   */
  async function submitRegistration(request: RegisterUserRequestDTO): Promise<void> {
    const response = await apiPost<RegisterUserRequestDTO, RegisterUserResponseDTO>("/api/users", request);
    showRegistrationResult(response);
  }

  /**
   * Implements showRegistrationResult from DC-HU1 diagram.
   */
  function showRegistrationResult(response: RegisterUserResponseDTO): void {
    setRegisteredUser(response);
  }

  return (
    <main className="min-h-screen w-full flex flex-col md:flex-row bg-white">
      {/* Panel Izquierdo Azul (Mockup HU-01) */}
      <aside
        aria-label="Información del sistema"
        className="w-full md:w-1/2 bg-[#094b72] text-white p-8 sm:p-12 md:p-16 flex flex-col justify-center min-h-[360px] md:min-h-screen flex-shrink-0"
      >
        <header className="max-w-md mx-auto md:mx-0 w-full flex flex-col gap-4">
          <p className="font-serif text-xl sm:text-2xl text-white/90 m-0">
            Calidad de Tilapia
          </p>
          <h1 id="hero-title" className="font-serif font-bold text-4xl sm:text-5xl text-white leading-tight m-0">
            Evalúa tu cosecha con confianza
          </h1>
          <p className="text-white/85 text-base sm:text-lg m-0 mt-2">
            Registra tus lotes y conoce la calidad de cada unidad.
          </p>
        </header>
        <footer className="max-w-md mx-auto md:mx-0 w-full pt-4">
          <span className="inline-block bg-[#073856] text-white/95 text-xs font-semibold px-4 py-2 rounded-full border border-white/10 shadow-sm">
            Clasificación NTC 1443
          </span>
        </footer>
      </aside>

      {/* Panel Derecho Blanco (Formulario de Registro) */}
      <section
        aria-labelledby="form-title"
        className="w-full md:w-1/2 bg-white p-6 sm:p-10 md:p-16 flex flex-col justify-center items-center min-h-screen"
      >
        <div className="w-full max-w-[500px]">
          {registeredUser ? (
            <UserRegistrationResultCard
              user={registeredUser}
              onRegisterAnother={() => setRegisteredUser(null)}
            />
          ) : (
            <UserRegistrationForm onSubmit={submitRegistration} />
          )}
        </div>
      </section>
    </main>
  );
}
