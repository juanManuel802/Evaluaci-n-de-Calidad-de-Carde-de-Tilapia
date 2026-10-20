package co.unillanos.secct.usecases.services;

import co.unillanos.secct.entities.Role;
import co.unillanos.secct.entities.User;
import co.unillanos.secct.infrastructure.repositories.RoleRepository;
import co.unillanos.secct.infrastructure.repositories.UserRepository;
import co.unillanos.secct.usecases.dto.RegisterUserRequestDTO;
import co.unillanos.secct.usecases.dto.RegisterUserResponseDTO;
import co.unillanos.secct.usecases.exceptions.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordService passwordService;

    @Mock
    private LogService logService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, roleRepository, passwordService, logService);
    }

    @Test
    @DisplayName("PU-09: normalizeEmail elimina espacios y convierte a minúsculas con Locale.ROOT")
    void testPU09_NormalizeEmail() {
        assertThat(userService.normalizeEmail("  JUAN.PEREZ@EMAIL.COM  ")).isEqualTo("juan.perez@email.com");
        assertThat(userService.normalizeEmail("  Usuario.Tilapia@Unillanos.Edu.CO  ")).isEqualTo("usuario.tilapia@unillanos.edu.co");
        assertThat(userService.normalizeEmail("admin@secct.co")).isEqualTo("admin@secct.co");
        assertThat(userService.normalizeEmail(null)).isNull();
    }

    @Test
    @DisplayName("Registro exitoso: flujo completo HU-01 (normalización, validación, rol PISCICULTOR, salt, hash, auditoría y DTO)")
    void testRegister_SuccessFlow() {
        // Given
        RegisterUserRequestDTO request = new RegisterUserRequestDTO(
                "Juan Carlos",
                "Pérez Gómez",
                "  Piscicultor.Juan@Unillanos.Edu.CO  ",
                "3101234567",
                "ClaveSegura123"
        );

        String normalizedEmail = "piscicultor.juan@unillanos.edu.co";
        Role piscicultorRole = new Role((short) 2, "PISCICULTOR");

        when(userRepository.existsByEmail(normalizedEmail)).thenReturn(false);
        when(roleRepository.findByCode("PISCICULTOR")).thenReturn(Optional.of(piscicultorRole));
        when(passwordService.generateSalt()).thenReturn("randomSaltBase64==");
        when(passwordService.hashPassword("ClaveSegura123", "randomSaltBase64==")).thenReturn("hashSha256Computed64HexChars1234567890abcdef1234567890abcdef12345678");

        User savedUser = new User();
        savedUser.setId(1001);
        savedUser.setEmail(normalizedEmail);
        savedUser.setRole(piscicultorRole);
        savedUser.setFirstName("Juan Carlos");
        savedUser.setLastName("Pérez Gómez");
        savedUser.setPhone("3101234567");
        savedUser.setPasswordHash("hashSha256Computed64HexChars1234567890abcdef1234567890abcdef12345678");
        savedUser.setSalt("randomSaltBase64==");
        savedUser.setActive(true);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        RegisterUserResponseDTO response = userService.register(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1001);
        assertThat(response.email()).isEqualTo(normalizedEmail);

        // Verify entity persisted with normalized email, role PISCICULTOR, salt, and passwordHash
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User userToSave = userCaptor.getValue();

        assertThat(userToSave.getEmail()).isEqualTo(normalizedEmail);
        assertThat(userToSave.getRole()).isEqualTo(piscicultorRole);
        assertThat(userToSave.getFirstName()).isEqualTo("Juan Carlos");
        assertThat(userToSave.getLastName()).isEqualTo("Pérez Gómez");
        assertThat(userToSave.getPhone()).isEqualTo("3101234567");
        assertThat(userToSave.getPasswordHash()).isEqualTo("hashSha256Computed64HexChars1234567890abcdef1234567890abcdef12345678");
        assertThat(userToSave.getSalt()).isEqualTo("randomSaltBase64==");
        assertThat(userToSave.isActive()).isTrue();
        assertThat(userToSave.isDeleted()).isFalse();

        // PL-1.26: Verify audit event recorded
        verify(logService).recordEvent(
                eq("USER_REGISTERED"),
                eq(1001),
                any(),
                any(),
                any()
        );
    }

    @Test
    @DisplayName("PL-1.18 & CN-04: Si el correo ya existe, lanza BusinessException con mensaje 'correo ya registrado' y código 409")
    void testRegister_EmailAlreadyExists() {
        RegisterUserRequestDTO request = new RegisterUserRequestDTO(
                "Carlos",
                "Gómez",
                "carlos@unillanos.edu.co",
                "3112345678",
                "MiClave890"
        );

        when(userRepository.existsByEmail("carlos@unillanos.edu.co")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("correo ya registrado")
                .extracting(e -> ((BusinessException) e).getHttpStatus())
                .isEqualTo(409);

        verify(userRepository, never()).save(any());
        verify(logService, never()).recordEvent(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("PL-1.17: Permite espacios exteriores en email antes de normalizar y lo persiste normalizado")
    void testRegister_EmailWithOuterSpaces() {
        RegisterUserRequestDTO request = new RegisterUserRequestDTO(
                "Ana",
                "Rojas",
                "   ana.rojas@correo.com   ",
                "3159876543",
                "ClaveSegura2026"
        );

        Role role = new Role((short) 2, "PISCICULTOR");
        when(userRepository.existsByEmail("ana.rojas@correo.com")).thenReturn(false);
        when(roleRepository.findByCode("PISCICULTOR")).thenReturn(Optional.of(role));
        when(passwordService.generateSalt()).thenReturn("salt==");
        when(passwordService.hashPassword(any(), any())).thenReturn("hash");

        User saved = new User();
        saved.setId(5);
        saved.setEmail("ana.rojas@correo.com");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        RegisterUserResponseDTO response = userService.register(request);

        assertThat(response.email()).isEqualTo("ana.rojas@correo.com");
        verify(userRepository).existsByEmail("ana.rojas@correo.com");
    }

    @Test
    @DisplayName("PQ-07: Obtiene el rol PISCICULTOR e ignora roles externos; falla si rol no configurado")
    void testRegister_RoleNotConfigured() {
        RegisterUserRequestDTO request = new RegisterUserRequestDTO(
                "Pedro",
                "Alonso",
                "pedro@correo.com",
                "3123456789",
                "ClaveValida123"
        );

        when(userRepository.existsByEmail("pedro@correo.com")).thenReturn(false);
        when(roleRepository.findByCode("PISCICULTOR")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Rol PISCICULTOR no configurado")
                .extracting(e -> ((BusinessException) e).getHttpStatus())
                .isEqualTo(500);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lanza BusinessException si la solicitud es nula")
    void testRegister_NullRequest() {
        assertThatThrownBy(() -> userService.register(null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("La solicitud de registro no puede ser nula");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "A", "Juan1", "Juan#", "NombreConMasDeCincuentaCaracteresMuyLargoQueDebeSerRechazadoPorElServicio"})
    @DisplayName("PL-1.01: Valida reglas de negocio para el nombre (2-50 letras y espacios, no solo espacios, sin números ni símbolos)")
    void testRegister_InvalidFirstName(String invalidName) {
        RegisterUserRequestDTO request = new RegisterUserRequestDTO(
                invalidName,
                "Pérez",
                "valido@correo.com",
                "3101234567",
                "ClaveValida123"
        );

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Nombre inválido");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "P", "Perez2", "Perez@"})
    @DisplayName("PL-1.02: Valida reglas de negocio para el apellido")
    void testRegister_InvalidLastName(String invalidLastName) {
        RegisterUserRequestDTO request = new RegisterUserRequestDTO(
                "Juan",
                invalidLastName,
                "valido@correo.com",
                "3101234567",
                "ClaveValida123"
        );

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Apellido inválido");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "31012345", "31012345678", "2101234567", "4101234567", "310123456a"})
    @DisplayName("PL-1.03: Valida teléfono (10 dígitos iniciando en 3)")
    void testRegister_InvalidPhone(String invalidPhone) {
        RegisterUserRequestDTO request = new RegisterUserRequestDTO(
                "Juan",
                "Pérez",
                "valido@correo.com",
                invalidPhone,
                "ClaveValida123"
        );

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Teléfono inválido");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "1234567", "        ", "123456789012345678901"})
    @DisplayName("PL-1.04 & PL-1.05: Valida contraseña (8-20 caracteres, no solo espacios)")
    void testRegister_InvalidPassword(String invalidPassword) {
        RegisterUserRequestDTO request = new RegisterUserRequestDTO(
                "Juan",
                "Pérez",
                "valido@correo.com",
                "3101234567",
                invalidPassword
        );

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Contraseña inválida");
    }

    @ParameterizedTest
    @ValueSource(strings = {"correo-invalido", "sin-arroba.com", "@sinusuario.com", "usuario@"})
    @DisplayName("Valida formato de correo normalizado")
    void testRegister_InvalidEmailFormat(String invalidEmail) {
        RegisterUserRequestDTO request = new RegisterUserRequestDTO(
                "Juan",
                "Pérez",
                invalidEmail,
                "3101234567",
                "ClaveValida123"
        );

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Correo electrónico no válido");
    }
}
