package co.unillanos.secct.usecases.dto;

import java.util.List;

/** Error body. "errors" lists the invalid fields so the client can point at them. */
public record ErrorResponseDTO(String message, List<FieldErrorDTO> errors) {

    public record FieldErrorDTO(String field, String message) {
    }

    public static ErrorResponseDTO of(String message) {
        return new ErrorResponseDTO(message, List.of());
    }
}
