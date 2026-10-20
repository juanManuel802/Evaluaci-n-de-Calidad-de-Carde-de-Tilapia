import { useState } from "react";
import type { ChangeEvent, SyntheticEvent } from "react";
import type { RegisterUserFormField, RegisterUserFormValues, RegisterUserRequestDTO } from "../types/auth";
import { ApiError } from "../services/apiClient";
import { toRegisterUserRequest, validateAuthForm } from "../validation/authValidation";
import type { AuthFormErrors } from "../validation/authValidation";

interface UserRegistrationFormProps {
  onSubmit: (request: RegisterUserRequestDTO) => Promise<void>;
  onCancel?: () => void;
}

const emptyValues: RegisterUserFormValues = {
  firstName: "",
  lastName: "",
  email: "",
  phone: "",
  password: "",
  confirmPassword: "",
};

const knownFields: RegisterUserFormField[] = [
  "firstName",
  "lastName",
  "email",
  "phone",
  "password",
  "confirmPassword",
];

export default function UserRegistrationForm({ onSubmit }: UserRegistrationFormProps) {
  const [values, setValues] = useState<RegisterUserFormValues>(emptyValues);
  const [errors, setErrors] = useState<AuthFormErrors>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  function handleChange(event: ChangeEvent<HTMLInputElement>) {
    const field = event.target.name as RegisterUserFormField;
    setValues((prev) => ({ ...prev, [field]: event.target.value }));
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }));
    }
  }

  async function handleSubmit(event: SyntheticEvent<HTMLFormElement>) {
    event.preventDefault();
    setFormError(null);

    const validationErrors = validateAuthForm(values);
    setErrors(validationErrors);
    if (Object.keys(validationErrors).length > 0) {
      return;
    }

    setIsSubmitting(true);
    try {
      await onSubmit(toRegisterUserRequest(values));
    } catch (error) {
      if (error instanceof ApiError) {
        const serverErrors: AuthFormErrors = {};
        for (const fieldError of error.fieldErrors) {
          if (knownFields.includes(fieldError.field as RegisterUserFormField)) {
            serverErrors[fieldError.field as RegisterUserFormField] = fieldError.message;
          }
        }
        setErrors(serverErrors);
        setFormError(error.message);
      } else {
        setFormError("Ocurrió un error inesperado al registrar el usuario. Intenta de nuevo.");
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  function renderError(field: RegisterUserFormField) {
    const message = errors[field];
    if (!message) return null;
    return (
      <small id={`${field}Error`} role="alert" className="text-red-600 font-semibold text-xs mt-1">
        {message}
      </small>
    );
  }

  function describedBy(field: RegisterUserFormField): string {
    return errors[field] ? `${field}Error` : `${field}Hint`;
  }

  return (
    <div className="w-full">
      <header className="mb-6">
        <h2 id="form-title" className="text-3xl sm:text-4xl font-serif font-bold text-[#0d3b66]">
          Registrar usuario
        </h2>
        <p className="text-[#5b738b] text-sm mt-1">
          Para gestionar tus lotes y evaluaciones.
        </p>
      </header>

      <form
        onSubmit={handleSubmit}
        noValidate
        className="flex flex-col gap-4 w-full"
        aria-label="Formulario de registro de usuario"
      >
        {formError && (
          <aside
            role="alert"
            aria-live="assertive"
            className="p-3 bg-red-50 border border-red-200 text-red-700 rounded-xl text-sm font-semibold flex items-center gap-2 mb-2"
          >
            <svg
              className="w-5 h-5 flex-shrink-0"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              aria-hidden="true"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
            <span>{formError}</span>
          </aside>
        )}

        {/* Grupo 1: Información personal y de contacto */}
        <fieldset
          className="p-0 m-0 border-0 flex flex-col gap-4"
          style={{ display: "flex", flexDirection: "column" }}
        >
          <legend className="sr-only">Información personal y de contacto</legend>

          {/* Fila 1: Nombres y Apellidos */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 w-full">
            <div className="flex flex-col gap-1.5">
              <label htmlFor="firstName" className="text-sm font-medium text-[#0d3b66]">
                Nombres
              </label>
              <input
                id="firstName"
                name="firstName"
                type="text"
                required
                autoComplete="given-name"
                placeholder="Máximo 50 caracteres"
                value={values.firstName}
                onChange={handleChange}
                aria-invalid={Boolean(errors.firstName)}
                aria-describedby={describedBy("firstName")}
                className={`w-full h-11 px-3.5 rounded-xl border bg-white transition-colors outline-none focus:ring-2 focus:ring-[#094b72] text-[#0d3b66] text-sm ${
                  errors.firstName ? "border-red-500" : "border-[#8fb0c9]"
                }`}
              />
              {renderError("firstName")}
            </div>

            <div className="flex flex-col gap-1.5">
              <label htmlFor="lastName" className="text-sm font-medium text-[#0d3b66]">
                Apellidos
              </label>
              <input
                id="lastName"
                name="lastName"
                type="text"
                required
                autoComplete="family-name"
                placeholder="Máximo 50 caracteres"
                value={values.lastName}
                onChange={handleChange}
                aria-invalid={Boolean(errors.lastName)}
                aria-describedby={describedBy("lastName")}
                className={`w-full h-11 px-3.5 rounded-xl border bg-white transition-colors outline-none focus:ring-2 focus:ring-[#094b72] text-[#0d3b66] text-sm ${
                  errors.lastName ? "border-red-500" : "border-[#8fb0c9]"
                }`}
              />
              {renderError("lastName")}
            </div>
          </div>

          {/* Fila 2: Correo electrónico y Teléfono celular */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 w-full">
            <div className="flex flex-col gap-1.5">
              <label htmlFor="email" className="text-sm font-medium text-[#0d3b66]">
                Correo electrónico
              </label>
              <input
                id="email"
                name="email"
                type="email"
                required
                autoComplete="email"
                placeholder="nombre@correo.com"
                value={values.email}
                onChange={handleChange}
                aria-invalid={Boolean(errors.email)}
                aria-describedby={describedBy("email")}
                className={`w-full h-11 px-3.5 rounded-xl border bg-white transition-colors outline-none focus:ring-2 focus:ring-[#094b72] text-[#0d3b66] text-sm ${
                  errors.email ? "border-red-500" : "border-[#8fb0c9]"
                }`}
              />
              <small id="emailHint" className="text-xs text-[#5b738b] mt-0.5">
                Máximo 100 caracteres. Con él iniciarás sesión.
              </small>
              {renderError("email")}
            </div>

            <div className="flex flex-col gap-1.5">
              <label htmlFor="phone" className="text-sm font-medium text-[#0d3b66]">
                Teléfono celular
              </label>
              <input
                id="phone"
                name="phone"
                type="tel"
                required
                autoComplete="tel"
                placeholder="3001234567"
                value={values.phone}
                onChange={handleChange}
                aria-invalid={Boolean(errors.phone)}
                aria-describedby={describedBy("phone")}
                className={`w-full h-11 px-3.5 rounded-xl border bg-white transition-colors outline-none focus:ring-2 focus:ring-[#094b72] text-[#0d3b66] text-sm ${
                  errors.phone ? "border-red-500" : "border-[#8fb0c9]"
                }`}
              />
              <small id="phoneHint" className="text-xs text-[#5b738b] mt-0.5">
                10 dígitos, inicia en 3
              </small>
              {renderError("phone")}
            </div>
          </div>
        </fieldset>

        {/* Grupo 2: Credenciales de seguridad */}
        <fieldset
          className="p-0 m-0 border-0 flex flex-col gap-4"
          style={{ display: "flex", flexDirection: "column" }}
        >
          <legend className="sr-only">Credenciales de seguridad</legend>

          {/* Fila 3: Contraseña y Confirmar contraseña */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 w-full">
            <div className="flex flex-col gap-1.5">
              <label htmlFor="password" className="text-sm font-medium text-[#0d3b66]">
                Contraseña
              </label>
              <input
                id="password"
                name="password"
                type="password"
                required
                autoComplete="new-password"
                placeholder=""
                value={values.password}
                onChange={handleChange}
                aria-invalid={Boolean(errors.password)}
                aria-describedby={describedBy("password")}
                className={`w-full h-11 px-3.5 rounded-xl border bg-white transition-colors outline-none focus:ring-2 focus:ring-[#094b72] text-[#0d3b66] text-sm ${
                  errors.password ? "border-red-500" : "border-[#8fb0c9]"
                }`}
              />
              <small id="passwordHint" className="text-xs text-[#5b738b] mt-0.5">
                8 a 20 caracteres
              </small>
              {renderError("password")}
            </div>

            <div className="flex flex-col gap-1.5">
              <label htmlFor="confirmPassword" className="text-sm font-medium text-[#0d3b66]">
                Confirmar contraseña
              </label>
              <input
                id="confirmPassword"
                name="confirmPassword"
                type="password"
                required
                autoComplete="new-password"
                placeholder=""
                value={values.confirmPassword}
                onChange={handleChange}
                aria-invalid={Boolean(errors.confirmPassword)}
                aria-describedby={describedBy("confirmPassword")}
                className={`w-full h-11 px-3.5 rounded-xl border bg-white transition-colors outline-none focus:ring-2 focus:ring-[#094b72] text-[#0d3b66] text-sm ${
                  errors.confirmPassword ? "border-red-500" : "border-[#8fb0c9]"
                }`}
              />
              {renderError("confirmPassword")}
            </div>
          </div>
        </fieldset>

        {/* Botón único de ancho completo Crear cuenta (Mockup HU-01) */}
        <button
          type="submit"
          id="register-submit-button"
          disabled={isSubmitting}
          className="w-full bg-[#094b72] hover:bg-[#073a5a] text-white font-semibold py-3.5 rounded-xl transition-all shadow-sm disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 mt-4 text-base cursor-pointer"
        >
          {isSubmitting ? (
            <>
              <svg
                className="animate-spin h-5 w-5 text-white"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                aria-hidden="true"
              >
                <circle
                  className="opacity-25"
                  cx="12"
                  cy="12"
                  r="10"
                  stroke="currentColor"
                  strokeWidth="4"
                />
                <path
                  className="opacity-75"
                  fill="currentColor"
                  d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"
                />
              </svg>
              <span>Creando cuenta...</span>
            </>
          ) : (
            "Crear cuenta"
          )}
        </button>

        {/* Enlace inferior */}
        <footer className="mt-4 text-center text-sm text-[#5b738b]">
          ¿Ya tienes cuenta?{" "}
          <a
            href="#/login"
            id="register-to-login-link"
            className="text-[#094b72] font-semibold underline underline-offset-4 hover:text-[#073a5a]"
          >
            Inicia sesión
          </a>
        </footer>
      </form>
    </div>
  );
}
