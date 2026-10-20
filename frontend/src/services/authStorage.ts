/**
 * Session storage management according to DC-HU2, DS-HU2-login, and DS-HU2-logout.
 */

const TOKEN_KEY = "secct_token";
const ROLE_KEY = "secct_role";
const EMAIL_KEY = "secct_email";
const USER_ID_KEY = "secct_user_id";
const FULL_NAME_KEY = "secct_full_name";

export interface AuthSession {
  accessToken: string;
  roleCode: string;
  email: string;
  id: number;
  fullName?: string;
}

function extractFullNameFromToken(token: string): string | null {
  try {
    const parts = token.split(".");
    if (parts.length >= 2) {
      const payloadBase64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
      const jsonPayload = decodeURIComponent(
        atob(payloadBase64)
          .split("")
          .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
          .join("")
      );
      const parsed = JSON.parse(jsonPayload);
      if (parsed.fullName && typeof parsed.fullName === "string" && parsed.fullName.trim() !== "") {
        return parsed.fullName.trim();
      }
    }
  } catch {
    // ignore
  }
  return null;
}

/**
 * Saves authenticated user session in localStorage (PL-2.12).
 */
export function saveSession(session: {
  accessToken: string;
  roleCode: string;
  email: string;
  id: number;
  fullName?: string;
}): void {
  localStorage.setItem(TOKEN_KEY, session.accessToken);
  localStorage.setItem(ROLE_KEY, session.roleCode);
  localStorage.setItem(EMAIL_KEY, session.email);
  localStorage.setItem(USER_ID_KEY, String(session.id));

  const resolvedName =
    session.fullName?.trim() ||
    extractFullNameFromToken(session.accessToken) ||
    (session.email ? session.email.split("@")[0] : "");
  if (resolvedName) {
    localStorage.setItem(FULL_NAME_KEY, resolvedName);
  }
}

/**
 * Convenience helper to save only the token.
 */
export function saveToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}

/**
 * Retrieves the stored JWT token (PI-04).
 */
export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

/**
 * Retrieves the stored role code (PISCICULTOR / ADMINISTRADOR).
 */
export function getRoleCode(): string | null {
  return localStorage.getItem(ROLE_KEY);
}

/**
 * Retrieves the stored user email.
 */
export function getUserEmail(): string | null {
  return localStorage.getItem(EMAIL_KEY);
}

/**
 * Retrieves the stored user ID.
 */
export function getUserId(): number | null {
  const raw = localStorage.getItem(USER_ID_KEY);
  if (!raw) return null;
  const parsed = parseInt(raw, 10);
  return isNaN(parsed) ? null : parsed;
}

/**
 * Retrieves the stored user full name, or falls back to resolving from token/email.
 */
export function getUserFullName(): string {
  const stored = localStorage.getItem(FULL_NAME_KEY);
  if (stored && stored.trim() !== "") {
    return stored.trim();
  }
  const token = getToken();
  if (token) {
    const fromToken = extractFullNameFromToken(token);
    if (fromToken) {
      localStorage.setItem(FULL_NAME_KEY, fromToken);
      return fromToken;
    }
  }
  const email = getUserEmail();
  if (email) {
    const prefix = email.split("@")[0].replace(/[._-]+/g, " ").trim();
    if (prefix) {
      return prefix
        .split(" ")
        .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
        .join(" ");
    }
  }
  return "Piscicultor";
}

/**
 * Retrieves full active session if present.
 */
export function getSession(): AuthSession | null {
  const token = getToken();
  if (!token) return null;
  return {
    accessToken: token,
    roleCode: getRoleCode() ?? "",
    email: getUserEmail() ?? "",
    id: getUserId() ?? 0,
    fullName: getUserFullName(),
  };
}

/**
 * Checks if a session is currently active.
 */
export function isAuthenticated(): boolean {
  return getToken() !== null && getToken() !== "";
}

/**
 * Explicit session removal function demanded by DC-HU2, DS-HU2-logout, and PL-2.23.
 */
export function removeLocalToken(): void {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(ROLE_KEY);
  localStorage.removeItem(EMAIL_KEY);
  localStorage.removeItem(USER_ID_KEY);
  localStorage.removeItem(FULL_NAME_KEY);
}

/**
 * Backward compatibility alias for clearToken.
 */
export function clearToken(): void {
  removeLocalToken();
}
