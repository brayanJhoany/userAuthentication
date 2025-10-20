package com.app.config.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EmailAuthenticationToken Tests")
class EmailAuthenticationTokenTest {

    @Test
    @DisplayName("Debería crear token no autenticado con email y password")
    void shouldCreateUnauthenticatedToken() {
        // Arrange
        String email = "test@example.com";
        String password = "SecurePass1!";

        // Act
        EmailAuthenticationToken token = new EmailAuthenticationToken(email, password);

        // Assert
        assertThat(token.getEmail()).isEqualTo(email);
        assertThat(token.getPrincipal()).isEqualTo(email);
        assertThat(token.getCredentials()).isEqualTo(password);
        assertThat(token.isAuthenticated()).isFalse();
        assertThat(token.getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("Debería crear token autenticado con email y authorities")
    void shouldCreateAuthenticatedToken() {
        // Arrange
        String email = "test@example.com";
        Collection<? extends GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );

        // Act
        EmailAuthenticationToken token = new EmailAuthenticationToken(email, authorities);

        // Assert
        assertThat(token.getEmail()).isEqualTo(email);
        assertThat(token.getPrincipal()).isEqualTo(email);
        assertThat(token.getCredentials()).isNull(); // Por seguridad
        assertThat(token.isAuthenticated()).isTrue();
        assertThat(token.getAuthorities()).hasSize(2);
        assertThat(token.getAuthorities()).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    @DisplayName("Debería eliminar credenciales al llamar eraseCredentials()")
    void shouldEraseCredentials() {
        // Arrange
        String email = "test@example.com";
        String password = "SecurePass1!";
        EmailAuthenticationToken token = new EmailAuthenticationToken(email, password);

        // Act
        token.eraseCredentials();

        // Assert
        assertThat(token.getCredentials()).isNull();
        assertThat(token.getEmail()).isEqualTo(email); // El email debe permanecer
    }

    @Test
    @DisplayName("Token autenticado no debería tener credenciales por defecto")
    void authenticatedTokenShouldNotHaveCredentials() {
        // Arrange
        String email = "test@example.com";
        Collection<? extends GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER")
        );

        // Act
        EmailAuthenticationToken token = new EmailAuthenticationToken(email, authorities);

        // Assert
        assertThat(token.getCredentials()).isNull();
    }

    @Test
    @DisplayName("Debería retornar el email correctamente mediante getPrincipal()")
    void shouldReturnEmailAsPrincipal() {
        // Arrange
        String email = "user@example.com";
        EmailAuthenticationToken token = new EmailAuthenticationToken(email, "password");

        // Act
        Object principal = token.getPrincipal();

        // Assert
        assertThat(principal).isInstanceOf(String.class);
        assertThat(principal).isEqualTo(email);
    }

    @Test
    @DisplayName("Debería mantener el email después de eraseCredentials()")
    void shouldKeepEmailAfterEraseCredentials() {
        // Arrange
        String email = "test@example.com";
        EmailAuthenticationToken token = new EmailAuthenticationToken(
                email,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // Act
        token.eraseCredentials();

        // Assert
        assertThat(token.getEmail()).isEqualTo(email);
        assertThat(token.getPrincipal()).isEqualTo(email);
    }
}
