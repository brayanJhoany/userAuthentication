package com.app.config.jwt;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = { JwtUtilsTest.TestConfig.class })
class JwtUtilsTest {

    @Autowired
    private JwtUtils jwtUtils;

    private final String email = "test@example.com";

    @Test
    void shouldGenerateAndValidateToken() {
        String token = jwtUtils.generateAccessToken(email);
        assertNotNull(token);
        assertTrue(jwtUtils.validateToken(token));
    }

    @Test
    void shouldExtractEmailFromToken() {
        String token = jwtUtils.generateAccessToken(email);
        String extracted = jwtUtils.getEmailFromToken(token);
        assertEquals(email, extracted);
    }

    @Test
    void shouldExtractAllClaimsFromValidToken() {
        String token = jwtUtils.generateAccessToken(email);
        Claims claims = jwtUtils.extractAllClaims(token);
        assertEquals(email, claims.getSubject());
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        assertFalse(jwtUtils.validateToken("invalid.token"));
    }

    @Test
    void shouldReturnFalseForExpiredToken() {
        JwtUtils utils = new JwtUtils();
        ReflectionTestUtils.setField(utils, "secretKey", "VGhpcyBpcyBhIHZlcnkgc2VjdXJlIHNlY3JldCBrZXkgZm9yIHRlc3RzIQ==");
        ReflectionTestUtils.setField(utils, "timeExpiration", "1"); // 1 ms

        String token = utils.generateAccessToken(email);

        // Esperar para forzar expiración
        try { Thread.sleep(5); } catch (InterruptedException ignored) {}

        assertFalse(utils.validateToken(token));
    }

    @Test
    void shouldReturnExpirationDateFromToken() {
        String token = jwtUtils.generateAccessToken(email);
        Date expiration = jwtUtils.getClaim(token, Claims::getExpiration);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void shouldThrowExceptionForMalformedToken() {
        assertThrows(Exception.class, () -> jwtUtils.extractAllClaims("malformed.token"));
    }


    @TestConfiguration
    static class TestConfig {
        @Bean
        public JwtUtils jwtUtils() {
            JwtUtils utils = new JwtUtils();
            String secret = "VGhpcyBpcyBhIHZlcnkgc2VjdXJlIHNlY3JldCBrZXkgZm9yIHRlc3RzIQ=="; // Base64
            String expiration = String.valueOf(1000 * 60 * 60); // 1 hora
            ReflectionTestUtils.setField(utils, "secretKey", secret);
            ReflectionTestUtils.setField(utils, "timeExpiration", expiration);
            return utils;
        }
    }
}
