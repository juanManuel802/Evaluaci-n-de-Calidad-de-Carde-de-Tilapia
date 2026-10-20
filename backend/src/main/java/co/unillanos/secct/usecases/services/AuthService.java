package co.unillanos.secct.usecases.services;

import co.unillanos.secct.entities.User;
import co.unillanos.secct.infrastructure.repositories.UserRepository;
import co.unillanos.secct.infrastructure.security.JwtService;
import co.unillanos.secct.usecases.dto.LoginRequestDTO;
import co.unillanos.secct.usecases.dto.LoginResponseDTO;
import co.unillanos.secct.usecases.exceptions.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

/**
 * Authentication and session service implementing login and logout according to DC-HU2, DS-HU2-login, and DS-HU2-logout.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final LogService logService;

    public AuthService(UserRepository userRepository,
                       PasswordService passwordService,
                       JwtService jwtService,
                       LogService logService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
        this.logService = logService;
    }

    /**
     * Authenticates user credentials, records audit events, and issues a JWT token (DS-HU2-login).
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public LoginResponseDTO login(LoginRequestDTO request) {
        if (request == null) {
            recordFailedLogin(null, null);
            throw new BusinessException("credenciales inválidas", 401);
        }

        String rawEmail = request.getEmail();
        String normalizedEmail = (rawEmail != null) ? rawEmail.trim().toLowerCase(Locale.ROOT) : "";
        String password = request.getPassword();

        boolean isPasswordInvalid = (password == null || password.length() < 8 || password.length() > 20 || password.isBlank());
        boolean isEmailInvalid = (normalizedEmail.length() < 6 || normalizedEmail.length() > 100);

        if (isPasswordInvalid || isEmailInvalid) {
            recordFailedLogin(null, normalizedEmail);
            throw new BusinessException("credenciales inválidas", 401);
        }

        Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);
        if (userOpt.isEmpty()) {
            recordFailedLogin(null, normalizedEmail);
            throw new BusinessException("credenciales inválidas", 401);
        }

        User user = userOpt.get();
        Integer userId = user.getId();

        if (user.isDeleted() || !user.isActive()) {
            recordFailedLogin(userId, normalizedEmail);
            throw new BusinessException("credenciales inválidas", 401);
        }

        boolean passwordValid = passwordService.verifyPassword(password, user.getSalt(), user.getPasswordHash());
        if (!passwordValid) {
            recordFailedLogin(userId, normalizedEmail);
            throw new BusinessException("credenciales inválidas", 401);
        }

        String accessToken = jwtService.generateToken(user);
        int expiresInMinutes = jwtService.getExpirationMinutes();
        String maskedEmail = logService.maskSensitiveData(user.getEmail());
        logService.recordEvent("LOGIN_SUCCESS", userId, null, null,
                "Inicio de sesión exitoso para " + maskedEmail);

        return LoginResponseDTO.from(user, accessToken, expiresInMinutes);
    }

    /**
     * Closes user session and records the logout audit event (DS-HU2-logout, PI-05, PL-2.23).
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void logout(Integer userId) {
        logService.recordEvent("LOGOUT", userId, null, null, "Sesión cerrada");
    }

    private void recordFailedLogin(Integer userId, String email) {
        String masked = (email != null && !email.isBlank()) ? logService.maskSensitiveData(email) : "desconocido";
        String description = "Intento de inicio de sesión fallido para " + masked;
        logService.recordEvent("LOGIN_FAILED", userId, null, null, description);
    }
}
