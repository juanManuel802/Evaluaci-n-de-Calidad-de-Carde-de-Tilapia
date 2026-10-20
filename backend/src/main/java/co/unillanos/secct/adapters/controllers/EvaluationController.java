package co.unillanos.secct.adapters.controllers;

import co.unillanos.secct.usecases.dto.BatchResponseDTO;
import co.unillanos.secct.usecases.dto.EvaluationResponseDTO;
import co.unillanos.secct.usecases.exceptions.BusinessException;
import co.unillanos.secct.usecases.services.EvaluationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    /** Evaluates one unit: the "image" part of the multipart request is classified. */
    @PostMapping("/batches/{code}/units")
    @ResponseStatus(HttpStatus.CREATED)
    public EvaluationResponseDTO evaluateUnit(@PathVariable String code,
                                              @RequestParam(value = "image", required = false) MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new BusinessException("Debes adjuntar una imagen", 400);
        }
        try {
            return evaluationService.evaluateUnit(code, getAuthenticatedUserId(), image.getBytes());
        } catch (IOException e) {
            throw new BusinessException("No se pudo leer la imagen enviada", 400);
        }
    }

    @PostMapping("/batches/{code}/close")
    public BatchResponseDTO close(@PathVariable String code) {
        return evaluationService.closeEvaluation(code, getAuthenticatedUserId());
    }

    private Integer getAuthenticatedUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new BusinessException("Usuario no autenticado", 401);
        }
        if (auth.getPrincipal() instanceof Integer id) {
            return id;
        }
        try {
            return Integer.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            throw new BusinessException("Usuario no autenticado", 401);
        }
    }
}
