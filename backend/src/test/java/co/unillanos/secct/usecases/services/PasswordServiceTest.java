package co.unillanos.secct.usecases.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordServiceTest {

    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordService();
    }

    @Test
    @DisplayName("PU-01: generateSalt genera un salt que al decodificar representa >= 16 bytes")
    void testPU01_GenerateSaltMinimum16Bytes() {
        String salt = passwordService.generateSalt();
        assertThat(salt).isNotNull().isNotBlank();

        byte[] decodedBytes = Base64.getDecoder().decode(salt);
        assertThat(decodedBytes.length).isGreaterThanOrEqualTo(16);
    }

    @Test
    @DisplayName("PU-02: generateSalt genera sales aleatorias y distintas en invocaciones consecutivas")
    void testPU02_GenerateSaltProducesRandomUniqueSalts() {
        String salt1 = passwordService.generateSalt();
        String salt2 = passwordService.generateSalt();

        assertThat(salt1).isNotEqualTo(salt2);
    }

    @Test
    @DisplayName("PU-03: hashPassword produce el mismo hash SHA-256 para la misma contraseña y salt (determinista)")
    void testPU03_HashPasswordDeterministic() {
        String salt = passwordService.generateSalt();
        String password = "SecretPassword2026!";

        String hash1 = passwordService.hashPassword(password, salt);
        String hash2 = passwordService.hashPassword(password, salt);

        assertThat(hash1).isNotNull().isEqualTo(hash2);
    }

    @Test
    @DisplayName("PU-04: hashPassword genera hashes distintos para la misma contraseña con diferentes sales")
    void testPU04_HashPasswordDifferentSaltsProduceDifferentHashes() {
        String password = "SecretPassword2026!";
        String salt1 = passwordService.generateSalt();
        String salt2 = passwordService.generateSalt();

        String hash1 = passwordService.hashPassword(password, salt1);
        String hash2 = passwordService.hashPassword(password, salt2);

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    @DisplayName("PU-05: hashPassword devuelve un hash de longitud fija de 64 caracteres hexadecimales")
    void testPU05_HashPasswordFixedLength64Hex() {
        String salt = passwordService.generateSalt();
        String hash = passwordService.hashPassword("AnyLengthPassword123", salt);

        assertThat(hash).hasSize(64);
        assertThat(hash).matches("^[0-9a-f]{64}$");
    }

    @Test
    @DisplayName("PU-06: verifyPassword retorna true cuando contraseña y salt coinciden con el hash")
    void testPU06_VerifyPasswordSuccess() {
        String password = "PiscicultorPass2026*";
        String salt = passwordService.generateSalt();
        String hash = passwordService.hashPassword(password, salt);

        boolean matches = passwordService.verifyPassword(password, salt, hash);
        assertThat(matches).isTrue();
    }

    @Test
    @DisplayName("PU-07: verifyPassword retorna false cuando la contraseña o salt no coinciden")
    void testPU07_VerifyPasswordFailure() {
        String password = "PiscicultorPass2026*";
        String salt = passwordService.generateSalt();
        String hash = passwordService.hashPassword(password, salt);

        // Incorrect password
        assertThat(passwordService.verifyPassword("IncorrectPassword", salt, hash)).isFalse();

        // Incorrect salt
        String anotherSalt = passwordService.generateSalt();
        assertThat(passwordService.verifyPassword(password, anotherSalt, hash)).isFalse();

        // Altered hash
        String alteredHash = hash.substring(0, 63) + (hash.endsWith("a") ? "b" : "a");
        assertThat(passwordService.verifyPassword(password, salt, alteredHash)).isFalse();

        // Null inputs
        assertThat(passwordService.verifyPassword(null, salt, hash)).isFalse();
        assertThat(passwordService.verifyPassword(password, null, hash)).isFalse();
        assertThat(passwordService.verifyPassword(password, salt, null)).isFalse();
    }

    @Test
    @DisplayName("hashPassword lanza IllegalArgumentException con parámetros nulos")
    void testHashPasswordNullArguments() {
        assertThatThrownBy(() -> passwordService.hashPassword(null, "salt"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> passwordService.hashPassword("pass", null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
