import type {
  LoginRequestDTO,
  LoginResponseDTO,
  RegisterUserRequestDTO,
  RegisterUserResponseDTO,
} from "../types/auth";
import { apiPost } from "./apiClient";

/**
 * Authentication service communicating with the Spring Boot backend according to DC-HU1, DC-HU2, DS-HU2-login, and DS-HU2-logout.
 */
export async function login(request: LoginRequestDTO): Promise<LoginResponseDTO> {
  return apiPost<LoginRequestDTO, LoginResponseDTO>("/api/auth/login", request);
}

export async function logout(): Promise<void> {
  return apiPost<void, void>("/api/auth/logout");
}

export async function register(request: RegisterUserRequestDTO): Promise<RegisterUserResponseDTO> {
  return apiPost<RegisterUserRequestDTO, RegisterUserResponseDTO>("/api/users", request);
}
