package co.unillanos.secct.usecases.dto;

import co.unillanos.secct.entities.User;

/**
 * Login response DTO exposing exclusively id, email, roleCode, accessToken, and expiresInMinutes (PU-10, PL-2.12).
 */
public record LoginResponseDTO(
        Integer id,
        String email,
        String roleCode,
        String accessToken,
        int expiresInMinutes
) {

    public static LoginResponseDTO from(User user, String token, int expiresInMinutes) {
        if (user == null) {
            return null;
        }
        String roleCode = user.getRole() != null ? user.getRole().getCode() : null;
        return new LoginResponseDTO(
                user.getId(),
                user.getEmail(),
                roleCode,
                token,
                expiresInMinutes
        );
    }
}
