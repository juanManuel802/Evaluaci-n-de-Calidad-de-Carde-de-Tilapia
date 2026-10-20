import type { ErrorResponseDTO, FieldErrorDTO } from "../types/batch";
import { getToken, removeLocalToken } from "./authStorage";

const baseUrl: string = import.meta.env.VITE_API_URL ?? "";

export class ApiError extends Error {
  readonly status: number;
  readonly fieldErrors: FieldErrorDTO[];

  constructor(message: string, status: number, fieldErrors: FieldErrorDTO[] = []) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

/**
 * Handles 401 Unauthorized responses according to PS-06.
 */
function handleUnauthorized(path: string): void {
  removeLocalToken();
  if (!path.includes("/api/auth/login")) {
    window.location.replace("#/login");
  }
}

async function extractError(response: Response, path: string): Promise<ApiError> {
  if (response.status === 401) {
    handleUnauthorized(path);
  }

  let errorBody: Partial<ErrorResponseDTO> = {};
  try {
    errorBody = (await response.json()) as Partial<ErrorResponseDTO>;
  } catch {
    // Non-JSON response
  }

  const defaultMessage =
    response.status === 401
      ? "Tu sesión no es válida o expiró. Inicia sesión de nuevo."
      : "No se pudo completar la solicitud.";

  return new ApiError(
    errorBody.message ?? defaultMessage,
    response.status,
    errorBody.errors ?? [],
  );
}

export async function apiPost<TRequest, TResponse>(path: string, body?: TRequest): Promise<TResponse> {
  const headers: Record<string, string> = { "Content-Type": "application/json" };
  const token = getToken();
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  let response: Response;
  try {
    response = await fetch(`${baseUrl}${path}`, {
      method: "POST",
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new ApiError("No se pudo conectar con el servidor. Verifica tu conexión e intenta de nuevo.", 0);
  }

  if (response.ok) {
    if (response.status === 204) {
      return undefined as TResponse;
    }
    return (await response.json()) as TResponse;
  }

  throw await extractError(response, path);
}

/**
 * GET request attaching Authorization header (PI-04) and handling 401 redirection (PS-06).
 */
export async function apiGet<TResponse>(path: string): Promise<TResponse> {
  const headers: Record<string, string> = {};
  const token = getToken();
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  let response: Response;
  try {
    response = await fetch(`${baseUrl}${path}`, { method: "GET", headers });
  } catch {
    throw new ApiError("No se pudo conectar con el servidor. Verifica tu conexión e intenta de nuevo.", 0);
  }

  if (response.ok) {
    return (await response.json()) as TResponse;
  }

  throw await extractError(response, path);
}

/**
 * Multipart POST request (HU-04) attaching token and handling 401.
 */
export async function apiPostForm<TResponse>(path: string, body: FormData): Promise<TResponse> {
  const headers: Record<string, string> = {};
  const token = getToken();
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  let response: Response;
  try {
    response = await fetch(`${baseUrl}${path}`, { method: "POST", headers, body });
  } catch {
    throw new ApiError("No se pudo conectar con el servidor. Verifica tu conexión e intenta de nuevo.", 0);
  }

  if (response.ok) {
    return (await response.json()) as TResponse;
  }

  throw await extractError(response, path);
}
