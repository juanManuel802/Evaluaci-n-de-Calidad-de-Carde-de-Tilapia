package co.unillanos.secct.infrastructure.security;

import co.unillanos.secct.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Service managing JWT token generation, parsing, and cryptographic validation according to DC-HU2.
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final int expirationMinutes;

    public JwtService(@Value("${secct.jwt.secret}") String secret,
                      @Value("${secct.jwt.expirationMinutes:30}") int expirationMinutes) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }

    /**
     * Generates a signed JWT token with HMAC-SHA256 including sub (userId), roleCode, iat, and exp (CB-05).
     */
    public String generateToken(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User and user ID must not be null");
        }
        Instant now = Instant.now();
        Instant exp = now.plus(expirationMinutes, ChronoUnit.MINUTES);
        String roleCode = user.getRole() != null ? user.getRole().getCode() : null;
        String firstName = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
        String fullName = (firstName + " " + lastName).trim();

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("roleCode", roleCode)
                .claim("fullName", fullName.isEmpty() ? null : fullName)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Validates that the token signature is valid, algorithm is not "none" (PQ-08), and has not expired (PU-11 to PU-13).
     */
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);

            // PQ-08: Reject unsecured "none" algorithm
            if ("none".equalsIgnoreCase(jws.getHeader().getAlgorithm())) {
                return false;
            }

            Claims claims = jws.getPayload();
            if (claims.getSubject() == null) {
                return false;
            }
            Integer.parseInt(claims.getSubject());
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extracts the userId (sub claim) as Integer from a valid token (PU-14).
     */
    public Integer getUserId(String token) {
        return Integer.valueOf(parseClaims(token).getSubject());
    }

    /**
     * Extracts the roleCode claim from a valid token (PU-14).
     */
    public String getRoleCode(String token) {
        return parseClaims(token).get("roleCode", String.class);
    }

    /**
     * Extracts the fullName claim from a valid token if present.
     */
    public String getFullName(String token) {
        return parseClaims(token).get("fullName", String.class);
    }

    public int getExpirationMinutes() {
        return expirationMinutes;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
