package co.unillanos.secct.adapters.controllers;

import co.unillanos.secct.usecases.dto.ErrorResponseDTO;
import co.unillanos.secct.usecases.dto.ErrorResponseDTO.FieldErrorDTO;
import co.unillanos.secct.usecases.exceptions.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException exception) {
        List<FieldErrorDTO> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDTO(error.getField(), error.getDefaultMessage()))
                .toList();
        String message = "Datos inválidos en: "
                + String.join(", ", errors.stream().map(FieldErrorDTO::field).distinct().toList());
        return ResponseEntity.badRequest().body(new ErrorResponseDTO(message, errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnreadable(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest().body(ErrorResponseDTO.of(
                "El cuerpo de la solicitud no es válido. Verifica la fecha (AAAA-MM-DD) y los valores numéricos"));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponseDTO> handleTooLarge(MaxUploadSizeExceededException exception) {
        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE).body(ErrorResponseDTO.of(
                "La imagen supera el tamaño máximo de 5 MB"));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusiness(BusinessException exception) {
        return ResponseEntity.status(exception.getHttpStatus()).body(ErrorResponseDTO.of(exception.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConflict(DataIntegrityViolationException exception) {
        logger.warn("Data integrity violation: {}", exception.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponseDTO.of(
                "No se pudo guardar el lote por un conflicto de datos. Intenta de nuevo"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleUnexpected(Exception exception) {
        logger.error("Unexpected error", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponseDTO.of(
                "Ocurrió un error inesperado. Intenta de nuevo más tarde"));
    }
}
