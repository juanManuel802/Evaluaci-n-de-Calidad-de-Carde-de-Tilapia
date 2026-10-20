import SideNav from "../components/SideNav";
import { getRoleCode, getUserEmail } from "../services/authStorage";

/**
 * Admin panel page enforcing role check and displaying 403 Acceso Denegado for non-administrators (PL-2.15).
 */
export default function AdminPage() {
  const roleCode = getRoleCode();
  const userEmail = getUserEmail();

  // PL-2.15: If role is not ADMINISTRADOR, block access and display 403 Acceso Denegado
  if (roleCode !== "ADMINISTRADOR") {
    return (
      <div className="flex min-h-screen bg-slate-950 text-slate-100">
        <SideNav current="admin" />
        <main id="main-content" className="flex-1 flex items-center justify-center p-6">
          <section
            id="access-denied-card"
            aria-labelledby="access-denied-title"
            className="max-w-lg w-full bg-slate-900 border border-red-500/30 rounded-2xl p-8 text-center shadow-2xl shadow-red-950/30"
            role="alert"
          >
            <div className="w-20 h-20 mx-auto mb-6 rounded-2xl bg-red-950/80 border border-red-500/40 flex items-center justify-center text-red-400">
              <svg className="w-10 h-10" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
            </div>
            <span
              id="status-403-badge"
              className="px-3 py-1 bg-red-950 text-red-400 border border-red-500/40 rounded-full text-xs font-black uppercase tracking-widest inline-block mb-3"
            >
              Estado 403
            </span>
            <h1 id="access-denied-title" className="text-3xl font-extrabold text-white tracking-tight mb-3">
              Acceso denegado
            </h1>
            <p className="text-slate-300 text-sm mb-6 leading-relaxed">
              No tienes los permisos requeridos para acceder al panel de administración del sistema SECCT.
              Este módulo está restringido exclusivamente a usuarios con rol{" "}
              <strong className="text-purple-400">ADMINISTRADOR</strong>.
            </p>
            <dl className="p-4 rounded-xl bg-slate-800/80 border border-slate-700/60 text-xs text-slate-400 mb-6 text-left m-0 flex flex-col gap-2">
              <div className="flex gap-2">
                <dt className="text-slate-200 font-bold">Usuario:</dt>
                <dd className="m-0">{userEmail ?? "Desconocido"}</dd>
              </div>
              <div className="flex gap-2">
                <dt className="text-slate-200 font-bold">Rol detectado:</dt>
                <dd className="m-0 text-cyan-400 font-semibold">{roleCode ?? "SIN ROL"}</dd>
              </div>
            </dl>
            <footer className="mt-6">
              <a
                href="#/register"
                id="return-to-batches-button"
                className="inline-flex items-center justify-center px-6 py-3 rounded-xl bg-gradient-to-r from-cyan-500 to-sky-500 text-slate-950 font-bold hover:from-cyan-400 hover:to-sky-400 transition-all shadow-lg shadow-cyan-500/20"
              >
                Volver a gestión de lotes
              </a>
            </footer>
          </section>
        </main>
      </div>
    );
  }

  return (
    <div className="flex min-h-screen bg-slate-950 text-slate-100">
      <SideNav current="admin" />
      <main id="main-content" className="flex-1 p-6 sm:p-10 overflow-y-auto">
        <header className="mb-8 border-b border-sky-500/20 pb-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h1 className="text-3xl font-black text-white tracking-tight">
              Panel de Administración
            </h1>
            <p className="text-sky-300/70 text-sm mt-1">
              Supervisión de usuarios, auditoría y métricas de calidad de tilapia.
            </p>
          </div>
          <div className="flex items-center space-x-3">
            <span className="px-3 py-1 bg-purple-950 text-purple-300 border border-purple-500/40 rounded-full text-xs font-bold uppercase tracking-wider">
              Rol: {roleCode}
            </span>
          </div>
        </header>

        {/* Dashboard Grid */}
        <section aria-label="Métricas del sistema" className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <article className="p-6 rounded-2xl bg-slate-900 border border-sky-500/20 shadow-lg">
            <header className="flex items-center justify-between mb-2">
              <h2 className="text-sm text-slate-400 font-medium m-0">Gestión de Usuarios</h2>
              <svg className="w-6 h-6 text-cyan-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
              </svg>
            </header>
            <p className="text-2xl font-black text-white m-0">Usuarios Activos</p>
            <footer className="mt-2">
              <a href="#/registro" className="text-xs text-sky-400 underline hover:text-cyan-300">
                Registrar nuevo usuario piscicultor →
              </a>
            </footer>
          </article>

          <article className="p-6 rounded-2xl bg-slate-900 border border-sky-500/20 shadow-lg">
            <header className="flex items-center justify-between mb-2">
              <h2 className="text-sm text-slate-400 font-medium m-0">Trazabilidad</h2>
              <svg className="w-6 h-6 text-sky-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
              </svg>
            </header>
            <p className="text-2xl font-black text-white m-0">Eventos de Auditoría</p>
            <footer className="mt-2">
              <p className="text-xs text-sky-400 m-0">Registro de inicios de sesión y operaciones</p>
            </footer>
          </article>

          <article className="p-6 rounded-2xl bg-slate-900 border border-sky-500/20 shadow-lg">
            <header className="flex items-center justify-between mb-2">
              <h2 className="text-sm text-slate-400 font-medium m-0">Calidad Tilapia</h2>
              <svg className="w-6 h-6 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
            </header>
            <p className="text-2xl font-black text-white m-0">Modelo CNN ResNet</p>
            <footer className="mt-2">
              <p className="text-xs text-emerald-400 m-0">Inferencia activa para ojo y piel</p>
            </footer>
          </article>
        </section>
      </main>
    </div>
  );
}
