package co.unillanos.secct.usecases.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterUserRequestDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("DTO válido no produce violaciones de validación")
    void testValidDTO() {
        RegisterUserRequestDTO dto = new RegisterUserRequestDTO(
                "Carlos Andrés",
                "Gutiérrez Gómez",
                "carlos.andres@correo.com",
                "3101234567",
                "MiPassword2026"
        );

        Set<ConstraintViolation<RegisterUserRequestDTO>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("PL-1.17: Permite espacios exteriores en email en Bean Validation")
    void testEmailWithOuterSpacesIsValidInBeanValidation() {
        RegisterUserRequestDTO dto = new RegisterUserRequestDTO(
                "Carlos",
                "Gómez",
                "   carlos@correo.com   ",
                "3101234567",
                "MiPassword2026"
        );

        Set<ConstraintViolation<RegisterUserRequestDTO>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("toString() NUNCA debe imprimir la contraseña en texto plano (RNF-SEC-04)")
    void testToStringNeverPrintsPassword() {
        String secretPassword = "SuperSecretPassword123!";
        RegisterUserRequestDTO dto = new RegisterUserRequestDTO(
                "Carlos",
                "Gómez",
                "carlos@correo.com",
                "3101234567",
                secretPassword
        );

        String toStringResult = dto.toString();
        assertThat(toStringResult).doesNotContain(secretPassword);
        assertThat(toStringResult).contains("[PROTECTED]");
        assertThat(toStringResult).contains("carlos@correo.com");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "A", "Carlos123", "Carlos@"})
    @DisplayName("Rechaza nombres inválidos")
    void testInvalidFirstName(String invalidFirstName) {
        RegisterUserRequestDTO dto = new RegisterUserRequestDTO(
                invalidFirstName,
                "Gómez",
                "carlos@correo.com",
                "3101234567",
                "MiPassword2026"
        );

        Set<ConstraintViolation<RegisterUserRequestDTO>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "G", "Gomez123", "Gomez#"})
    @DisplayName("Rechaza apellidos inválidos")
    void testInvalidLastName(String invalidLastName) {
        RegisterUserRequestDTO dto = new RegisterUserRequestDTO(
                "Carlos",
                invalidLastName,
                "carlos@correo.com",
                "3101234567",
                "MiPassword2026"
        );

        Set<ConstraintViolation<RegisterUserRequestDTO>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "310", "310123456", "31012345678", "2101234567", "4001234567", "telefono10"})
    @DisplayName("Rechaza teléfonos que no cumplan ^3[0-9]{9}$")
    void testInvalidPhone(String invalidPhone) {
        RegisterUserRequestDTO dto = new RegisterUserRequestDTO(
                "Carlos",
                "Gómez",
                "carlos@correo.com",
                invalidPhone,
                "MiPassword2026"
        );

        Set<ConstraintViolation<RegisterUserRequestDTO>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "short", "1234567", "        ", "123456789012345678901"})
    @DisplayName("Rechaza contraseñas < 8, > 20 o compuestas únicamente por espacios")
    void testInvalidPassword(String invalidPassword) {
        RegisterUserRequestDTO dto = new RegisterUserRequestDTO(
                "Carlos",
                "Gómez",
                "carlos@correo.com",
                "3101234567",
                invalidPassword
        );

        Set<ConstraintViolation<RegisterUserRequestDTO>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("Getters y setters de RegisterUserRequestDTO")
    void testGettersAndSetters() {
        RegisterUserRequestDTO dto = new RegisterUserRequestDTO();
        dto.setFirstName("Laura");
        dto.setLastName("Mendoza");
        dto.setEmail("laura@correo.com");
        dto.setPhone("3101234567");
        dto.setPassword("Password2026");

        assertThat(dto.getFirstName()).isEqualTo("Laura");
        assertThat(dto.getLastName()).isEqualTo("Mendoza");
        assertThat(dto.getEmail()).isEqualTo("laura@correo.com");
        assertThat(dto.getPhone()).isEqualTo("3101234567");
        assertThat(dto.getPassword()).isEqualTo("Password2026");
    }
}
