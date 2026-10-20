/**
 * DTO sent to POST /api/users according to DC-HU1.
 */
export interface RegisterUserRequestDTO {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  password: string;
}

/**
 * DTO received from POST /api/users according to DC-HU1 (PU-08).
 */
export interface RegisterUserResponseDTO {
  id: number;
  email: string;
}

/**
 * Form values for user registration including password confirmation.
 */
export interface RegisterUserFormValues {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  password: string;
  confirmPassword: string;
}

export type RegisterUserFormField = keyof RegisterUserFormValues;

/**
 * DTO sent to POST /api/auth/login according to DC-HU2.
 */
export interface LoginRequestDTO {
  email: string;
  password: string;
}

/**
 * DTO received from POST /api/auth/login according to DC-HU2 (PU-10, PL-2.12).
 */
export interface LoginResponseDTO {
  id: number;
  email: string;
  roleCode: string;
  accessToken: string;
  expiresInMinutes: number;
}

export interface LoginFormValues {
  email: string;
  password: string;
}

export type LoginFormField = keyof LoginFormValues;
