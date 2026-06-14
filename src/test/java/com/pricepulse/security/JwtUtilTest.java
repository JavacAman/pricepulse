package com.pricepulse.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtUtil.
 *
 * WHY NO @Mock HERE?
 *   JwtUtil has no external dependencies — it's a pure utility class.
 *   We don't need Mockito at all. We just create an instance and test it directly.
 *
 * HOW TO INJECT @Value FIELDS IN UNIT TESTS?
 *   @Value("${jwt.secret}") fields are set by Spring at startup.
 *   In a unit test (no Spring context), we use ReflectionTestUtils.setField()
 *   to inject values directly into the private fields.
 *   This avoids starting a full Spring context just to test pure logic.
 */
class JwtUtilTest {

    // JwtUtil has no @Mock dependencies — instantiated directly
    private JwtUtil jwtUtil;

    // Must be at least 256 bits (32 chars) for HS256 algorithm
    private static final String TEST_SECRET =
            "pricepulse-secret-key-this-must-be-very-long-for-security";
    private static final long TEST_EXPIRATION = 86400000L; // 24 hours in ms

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        // ReflectionTestUtils: injects values into private @Value fields without Spring context.
        // Field name must match exactly what's in JwtUtil.
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // generateToken() tests
    // ─────────────────────────────────────────────────────────────────────────

    // JUnit5: testing generateToken() - token is non-null and non-empty
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("generateToken() - should return a non-null, non-empty JWT token")
    void generateToken_validEmail_returnsNonNullToken() {
        // ── Arrange ──────────────────────────────────────────────────────────
        String email = "aman@example.com";

        // ── Act ──────────────────────────────────────────────────────────────
        String token = jwtUtil.generateToken(email);

        // ── Assert ───────────────────────────────────────────────────────────
        assertNotNull(token, "Token should not be null");
        assertFalse(token.isBlank(), "Token should not be empty");
        // JWT format: three Base64 sections separated by dots
        assertEquals(3, token.split("\\.").length,
                "Token should have 3 parts (header.payload.signature)");
    }

    // JUnit5: testing generateToken() - token contains three Base64 sections (JWT structure)
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("generateToken() - should produce a token with valid JWT structure (header.payload.signature)")
    void generateToken_validEmail_hasThreeParts() {
        // ── Arrange ──────────────────────────────────────────────────────────
        String email = "aman@example.com";

        // ── Act ──────────────────────────────────────────────────────────────
        String token = jwtUtil.generateToken(email);

        // ── Assert ───────────────────────────────────────────────────────────
        // JWT format is always: Base64(header) . Base64(payload) . Base64(signature)
        // Splitting on '.' must give exactly 3 non-empty parts
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT must have exactly 3 dot-separated parts");
        assertTrue(parts[0].length() > 0, "Header part must not be empty");
        assertTrue(parts[1].length() > 0, "Payload part must not be empty");
        assertTrue(parts[2].length() > 0, "Signature part must not be empty");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // extractEmail() tests
    // ─────────────────────────────────────────────────────────────────────────

    // JUnit5: testing extractEmail() - email round-trips correctly through token
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("extractEmail() - should extract the same email that was put into the token")
    void extractEmail_fromValidToken_returnsOriginalEmail() {
        // ── Arrange ──────────────────────────────────────────────────────────
        String originalEmail = "aman@example.com";
        String token = jwtUtil.generateToken(originalEmail);

        // ── Act ──────────────────────────────────────────────────────────────
        String extractedEmail = jwtUtil.extractEmail(token);

        // ── Assert ───────────────────────────────────────────────────────────
        assertEquals(originalEmail, extractedEmail,
                "Extracted email must match the email used to generate the token");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // validateToken() tests
    // ─────────────────────────────────────────────────────────────────────────

    // JUnit5: testing validateToken() - a freshly generated token should be valid
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("validateToken() - should return true for a freshly generated valid token")
    void validateToken_freshToken_returnsTrue() {
        // ── Arrange ──────────────────────────────────────────────────────────
        String token = jwtUtil.generateToken("aman@example.com");

        // ── Act ──────────────────────────────────────────────────────────────
        boolean isValid = jwtUtil.validateToken(token);

        // ── Assert ───────────────────────────────────────────────────────────
        assertTrue(isValid, "A freshly generated token must be valid");
    }

    // JUnit5: testing validateToken() - a random string is NOT a valid JWT
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("validateToken() - should return false for a completely invalid token string")
    void validateToken_randomString_returnsFalse() {
        // ── Arrange ──────────────────────────────────────────────────────────
        String fakeToken = "this.is.not.a.real.jwt.token";

        // ── Act ──────────────────────────────────────────────────────────────
        boolean isValid = jwtUtil.validateToken(fakeToken);

        // ── Assert ───────────────────────────────────────────────────────────
        assertFalse(isValid, "A random string should not pass JWT validation");
    }

    // JUnit5: testing validateToken() - a token signed with a DIFFERENT secret is invalid
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("validateToken() - should return false for a token signed with a different secret")
    void validateToken_tokenSignedWithWrongSecret_returnsFalse() {
        // ── Arrange ──────────────────────────────────────────────────────────
        // Build a JwtUtil with a DIFFERENT secret to create a tampered token
        JwtUtil otherJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(otherJwtUtil, "secret",
                "completely-different-secret-key-that-is-long-enough-for-hs256");
        ReflectionTestUtils.setField(otherJwtUtil, "expiration", TEST_EXPIRATION);

        String tokenFromDifferentSecret = otherJwtUtil.generateToken("aman@example.com");

        // ── Act ──────────────────────────────────────────────────────────────
        // Our JwtUtil tries to validate a token signed by a different key
        boolean isValid = jwtUtil.validateToken(tokenFromDifferentSecret);

        // ── Assert ───────────────────────────────────────────────────────────
        assertFalse(isValid, "Token signed with wrong secret should fail validation");
    }

    // JUnit5: testing validateToken() - expired token should be rejected
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("validateToken() - should return false for an already-expired token")
    void validateToken_expiredToken_returnsFalse() {
        // ── Arrange ──────────────────────────────────────────────────────────
        // Build a JwtUtil with 0ms expiration so the token expires instantly
        JwtUtil expiredJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(expiredJwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(expiredJwtUtil, "expiration", 0L); // expires immediately

        String expiredToken = expiredJwtUtil.generateToken("aman@example.com");

        // ── Act ──────────────────────────────────────────────────────────────
        boolean isValid = jwtUtil.validateToken(expiredToken);

        // ── Assert ───────────────────────────────────────────────────────────
        assertFalse(isValid, "An expired token must fail validation");
    }
}
