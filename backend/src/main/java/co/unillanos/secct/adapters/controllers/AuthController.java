package co.unillanos.secct.adapters.controllers;

import co.unillanos.secct.usecases.dto.LoginRequestDTO;
import co.unillanos.secct.usecases.dto.LoginResponseDTO;
import co.unillanos.secct.usecases.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller exposing authentication endpoints for login and logout according to DC-HU2, DS-HU2-login, and DS-HU2-logout.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * User login endpoint returning 200 OK with LoginResponseDTO (DC-HU2, DS-HU2-login).
     */
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }

    /**
     * User logout endpoint taking userId from SecurityContext and returning 204 No Content (DS-HU2-logout).
     */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Integer userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof Integer id) {
            userId = id;
        } else if (authentication != null && authentication.getPrincipal() instanceof String strId) {
            try {
                userId = Integer.parseInt(strId);
            } catch (NumberFormatException ignored) {
            }
        }
        authService.logout(userId);
    }
}
