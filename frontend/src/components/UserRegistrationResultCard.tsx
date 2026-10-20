import type { RegisterUserResponseDTO } from "../types/auth";

interface UserRegistrationResultCardProps {
  user: RegisterUserResponseDTO;
  onRegisterAnother: () => void;
}

export default function UserRegistrationResultCard({
  user,
  onRegisterAnother,
}: UserRegistrationResultCardProps) {
  return (
    <article
      className="card w-full max-w-[860px] mx-auto p-6 sm:p-8 bg-white border border-[#d5e3ee] rounded-2xl shadow-sm"
      aria-live="polite"
      role="status"
    >
      <header className="flex items-center gap-3 text-emerald-700 mb-2">
        <svg
          className="w-8 h-8 flex-shrink-0"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
          aria-hidden="true"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
          />
        </svg>
        <h2 className="text-2xl sm:text-3xl font-serif text-[#0b3a5a]">Usuario registrado exitosamente</h2>
      </header>

      <p className="text-sm text-[#4d6a80] mb-6">
        El nuevo usuario piscicultor ha sido registrado en el sistema y se le ha asignado su identificador único.
      </p>

      <dl className="bg-[#f3f8fc] rounded-xl p-4 sm:p-5 flex flex-col gap-3 border border-[#dcebf6] text-sm m-0">
        <div className="flex justify-between items-center border-b border-[#e6eef5] pb-2">
          <dt className="text-[#4d6a80]">ID de Usuario:</dt>
          <dd className="m-0"><strong className="text-[#0b3a5a] text-base font-mono">#{user.id}</strong></dd>
        </div>
        <div className="flex justify-between items-center border-b border-[#e6eef5] pb-2">
          <dt className="text-[#4d6a80]">Correo Electrónico:</dt>
          <dd className="m-0"><strong className="text-[#0b3a5a] text-base">{user.email}</strong></dd>
        </div>
        <div className="flex justify-between items-center">
          <dt className="text-[#4d6a80]">Rol Asignado:</dt>
          <dd className="m-0">
            <span className="bg-[#dcebf6] text-[#0b4f7a] text-xs font-semibold px-3 py-1 rounded-full">
              PISCICULTOR
            </span>
          </dd>
        </div>
      </dl>

      <footer className="flex flex-col-reverse sm:flex-row justify-end gap-3 mt-6 pt-4 border-t border-[#e6eef5]">
        <button
          type="button"
          id="registered-go-to-login-button"
          onClick={() => {
            window.location.hash = "#/login";
          }}
          className="o h-12 px-6 rounded-xl border border-[#0b4f7a] text-[#0b4f7a] font-semibold hover:bg-slate-50 transition-colors"
        >
          Iniciar Sesión
        </button>
        <button
          type="button"
          onClick={() => {
            window.location.hash = "#/register";
          }}
          className="o h-12 px-6 rounded-xl border border-[#0b4f7a] text-[#0b4f7a] font-semibold hover:bg-slate-50 transition-colors"
        >
          Ir a Registrar Lote
        </button>
        <button
          type="button"
          onClick={onRegisterAnother}
          className="p h-12 px-8 rounded-xl bg-[#0b4f7a] hover:bg-[#083a5a] text-white font-semibold transition-all shadow-sm"
        >
          Registrar otro usuario
        </button>
      </footer>
    </article>
  );
}
