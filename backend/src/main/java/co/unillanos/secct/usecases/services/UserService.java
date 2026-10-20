package co.unillanos.secct.usecases.services;

import co.unillanos.secct.entities.Role;
import co.unillanos.secct.entities.User;
import co.unillanos.secct.infrastructure.repositories.RoleRepository;
import co.unillanos.secct.infrastructure.repositories.UserRepository;
import co.unillanos.secct.usecases.dto.RegisterUserRequestDTO;
import co.unillanos.secct.usecases.dto.RegisterUserResponseDTO;
import co.unillanos.secct.usecases.exceptions.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Service orchestrating HU-01 user registration according to DC-HU1 and DS-HU1.
 */
@Service
public class UserService {

    private static final String DEFAULT_ROLE_CODE = "PISCICULTOR";
    private static final String USER_REGISTERED_EVENT = "USER_REGISTERED";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordService passwordService;
    private final LogService logService;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordService passwordService,
                       LogService logService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordService = passwordService;
        this.logService = logService;
    }

    /**
     * Normalizes an email by trimming leading/trailing spaces and lowercasing with Locale.ROOT (PU-09, PL-1.17).
     */
    public String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Registers a new piscicultor user enforcing business rules PL-1.01 to PL-1.26.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public RegisterUserResponseDTO register(RegisterUserRequestDTO request) {
        if (request == null) {
            throw new BusinessException("La solicitud de registro no puede ser nula", 400);
        }

        String normalizedEmail = normalizeEmail(request.getEmail());
        validateBusinessRules(request, normalizedEmail);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessException("correo ya registrado", 409);
        }

        // PQ-07: Always assign PISCICULTOR role regardless of any external attempt to inject a role
        Role role = roleRepository.findByCode(DEFAULT_ROLE_CODE)
                .orElseThrow(() -> new BusinessException("Rol " + DEFAULT_ROLE_CODE + " no configurado", 500));

        String salt = passwordService.generateSalt();
        String passwordHash = passwordService.hashPassword(request.getPassword(), salt);

        User user = new User();
        user.setRole(role);
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(normalizedEmail);
        user.setPhone(request.getPhone().trim());
        user.setPasswordHash(passwordHash);
        user.setSalt(salt);
        user.setActive(true);
        user.setDeleted(false);

        User savedUser = userRepository.save(user);

        // PL-1.26: Audit trail recording with masked description
        String auditDescription = "Registro de usuario con correo " + normalizedEmail + " y teléfono " + user.getPhone();
        logService.recordEvent(USER_REGISTERED_EVENT, savedUser.getId(), null, null, auditDescription);

        return RegisterUserResponseDTO.from(savedUser);
    }

    private void validateBusinessRules(RegisterUserRequestDTO request, String normalizedEmail) {
        if (request.getFirstName() == null || !request.getFirstName().matches("^(?!\\s+$)[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ ]{2,50}$")) {
            throw new BusinessException("Nombre inválido: debe tener entre 2 y 50 letras y espacios", 400);
        }
        if (request.getLastName() == null || !request.getLastName().matches("^(?!\\s+$)[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ ]{2,50}$")) {
            throw new BusinessException("Apellido inválido: debe tener entre 2 y 50 letras y espacios", 400);
        }
        if (request.getPhone() == null || !request.getPhone().matches("^3[0-9]{9}$")) {
            throw new BusinessException("Teléfono inválido: debe tener 10 dígitos e iniciar en 3", 400);
        }
        if (request.getPassword() == null || request.getPassword().length() < 8 || request.getPassword().length() > 20
                || request.getPassword().trim().isEmpty()) {
            throw new BusinessException("Contraseña inválida: debe tener entre 8 y 20 caracteres y no consistir solo de espacios", 400);
        }
        if (normalizedEmail == null || normalizedEmail.isEmpty()
                || !normalizedEmail.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new BusinessException("Correo electrónico no válido", 400);
        }
    }
}
