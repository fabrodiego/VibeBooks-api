package com.vibebooks.api.service;

import com.vibebooks.api.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the TokenService.
 * Verifies JWT token generation and validation logic.
 */
class TokenServiceTest {

    private TokenService tokenService;
    private User validUser;

    @BeforeEach
    void setup() {
        tokenService = new TokenService();

        ReflectionTestUtils.setField(tokenService, "secret", "this-is-a-very-secure-secret-key-for-jwt-testing");
        ReflectionTestUtils.setField(tokenService, "expirationHours", 2L);

        validUser = new User();
        validUser.setId(UUID.randomUUID());
    }

    /**
     * Tests token generation.
     * Verifies that the returned string is not empty and has the 3 parts of a standard JWT.
     */
    @Test
    @DisplayName("Generate: Should create a valid JWT string")
    void shouldGenerateTokenSuccessfully() {
        String token = tokenService.generateToken(validUser);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(3, token.split("\\.").length);
    }

    /**
     * Tests subject extraction from a valid token.
     * Verifies that the extracted subject matches the user's ID.
     */
    @Test
    @DisplayName("Subject: Should return user ID from valid token")
    void shouldGetSubjectFromValidToken() {
        String token = tokenService.generateToken(validUser);

        String subject = tokenService.getSubject(token);

        assertEquals(validUser.getId().toString(), subject);
    }

    /**
     * Tests validation of a forged or invalid token.
     * Verifies that a RuntimeException is thrown with the expected message.
     */
    @Test
    @DisplayName("Subject: Should throw exception for invalid token")
    void shouldThrowExceptionForInvalidToken() {
        String invalidToken = "header.payload.invalidSignature";

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> tokenService.getSubject(invalidToken));

        assertEquals("Invalid or Expired JWT Token", exception.getMessage());
    }
}