package co.unillanos.secct.usecases.dto;

import co.unillanos.secct.entities.Role;
import co.unillanos.secct.entities.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterUserResponseDTOTest {

    @Test
    @DisplayName("PU-08: RegisterUserResponseDTO expone únicamente id y email, sin contraseñas ni sales")
    void testPU08_RegisterUserResponseDTOExposesOnlyIdAndEmail() {
        // Given
        Role role = new Role((short) 2, "PISCICULTOR");
        User user = new User();
        user.setId(101);
        user.setRole(role);
        user.setFirstName("Juan");
        user.setLastName("Perez");
        user.setEmail("piscicultor@unillanos.edu.co");
        user.setPhone("3101234567");
        user.setPasswordHash("sha256HashVerySecretDoNotExpose1234567890abcdef");
        user.setSalt("saltVerySecret1234");
        user.setActive(true);
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());

        // When
        RegisterUserResponseDTO responseDTO = RegisterUserResponseDTO.from(user);

        // Then
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.id()).isEqualTo(101);
        assertThat(responseDTO.email()).isEqualTo("piscicultor@unillanos.edu.co");

        // Verify through reflection that RegisterUserResponseDTO declares ONLY id and email fields
        Field[] declaredFields = RegisterUserResponseDTO.class.getDeclaredFields();
        assertThat(declaredFields).hasSize(2);
        assertThat(declaredFields).extracting(Field::getName).containsExactlyInAnyOrder("id", "email");

        // Verify null handling
        assertThat(RegisterUserResponseDTO.from(null)).isNull();
    }
}
