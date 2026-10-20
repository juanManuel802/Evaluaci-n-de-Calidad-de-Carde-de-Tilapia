package co.unillanos.secct.usecases.services;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Service for cryptographic password hashing and salt generation (PU-01 to PU-07).
 */
@Service
public class PasswordService {

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a 16-byte random salt using SecureRandom and encodes it in Base64 (PU-01, PU-02).
     * Decoding the returned salt yields >= 16 bytes.
     */
    public String generateSalt() {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Calculates SHA-256 hash by combining salt and password in UTF-8 (PU-03, PU-04, PU-05).
     * Returns a 64-character lowercase hexadecimal string.
     */
    public String hashPassword(String password, String salt) {
        if (password == null || salt == null) {
            throw new IllegalArgumentException("Password and salt must not be null");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Securely compares hashPassword(password, salt) with passwordHash using constant-time comparison (PU-06, PU-07).
     */
    public boolean verifyPassword(String password, String salt, String passwordHash) {
        if (password == null || salt == null || passwordHash == null) {
            return false;
        }
        String computedHash = hashPassword(password, salt);
        return MessageDigest.isEqual(
                computedHash.getBytes(StandardCharsets.UTF_8),
                passwordHash.getBytes(StandardCharsets.UTF_8)
        );
    }
}
