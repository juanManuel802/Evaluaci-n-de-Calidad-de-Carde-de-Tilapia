package co.unillanos.secct.usecases.dto;

import co.unillanos.secct.entities.User;

/**
 * Registration response DTO exposing solely id and email (PU-08).
 */
public record RegisterUserResponseDTO(Integer id, String email) {

    public static RegisterUserResponseDTO from(User user) {
        if (user == null) {
            return null;
        }
        return new RegisterUserResponseDTO(user.getId(), user.getEmail());
    }
}
