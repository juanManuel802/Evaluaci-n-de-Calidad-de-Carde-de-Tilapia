package co.unillanos.secct.adapters.controllers;

import co.unillanos.secct.usecases.dto.RegisterUserRequestDTO;
import co.unillanos.secct.usecases.dto.RegisterUserResponseDTO;
import co.unillanos.secct.usecases.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller exposing the user registration endpoint for HU-01 (DC-HU1, DS-HU1, PP-001).
 */
@RestController
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping({"/api/users", "/api/auth/registro"})
    public ResponseEntity<RegisterUserResponseDTO> register(@Valid @RequestBody RegisterUserRequestDTO request) {
        RegisterUserResponseDTO response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
