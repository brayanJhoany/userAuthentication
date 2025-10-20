package com.app.config.security;

import com.app.service.UserDetailServiceImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailAuthenticationProvider Tests")
class EmailAuthenticationProviderTest {

    @Mock
    private UserDetailServiceImp userDetailService;

    private PasswordEncoder passwordEncoder;
    private EmailAuthenticationProvider authenticationProvider;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authenticationProvider = new EmailAuthenticationProvider(userDetailService, passwordEncoder);
    }

    @Test
    @DisplayName("Debería autenticar correctamente con email y password válidos")
    void shouldAuthenticateSuccessfullyWithValidCredentials() {
        // Arrange
        String email = "test@example.com";
        String rawPassword = "SecurePass1!";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        UserDetails userDetails = User.builder()
                .username(email)
                .password(encodedPassword)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        when(userDetailService.loadUserByEmail(email)).thenReturn(userDetails);

        EmailAuthenticationToken unauthenticatedToken = new EmailAuthenticationToken(email, rawPassword);

        // Act
        Authentication result = authenticationProvider.authenticate(unauthenticatedToken);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(EmailAuthenticationToken.class);
        assertThat(result.isAuthenticated()).isTrue();
        assertThat(result.getPrincipal()).isEqualTo(email);
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities()).extracting("authority").contains("ROLE_USER");
    }

    @Test
    @DisplayName("Debería lanzar BadCredentialsException con password incorrecta")
    void shouldThrowBadCredentialsExceptionWithInvalidPassword() {
        // Arrange
        String email = "test@example.com";
        String correctPassword = "CorrectPass1!";
        String wrongPassword = "WrongPass1!";
        String encodedPassword = passwordEncoder.encode(correctPassword);

        UserDetails userDetails = User.builder()
                .username(email)
                .password(encodedPassword)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        when(userDetailService.loadUserByEmail(email)).thenReturn(userDetails);

        EmailAuthenticationToken unauthenticatedToken = new EmailAuthenticationToken(email, wrongPassword);

        // Act & Assert
        assertThatThrownBy(() -> authenticationProvider.authenticate(unauthenticatedToken))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Credenciales inválidas");
    }

    @Test
    @DisplayName("Debería soportar EmailAuthenticationToken")
    void shouldSupportEmailAuthenticationToken() {
        // Act
        boolean supports = authenticationProvider.supports(EmailAuthenticationToken.class);

        // Assert
        assertThat(supports).isTrue();
    }

    @Test
    @DisplayName("No debería soportar UsernamePasswordAuthenticationToken")
    void shouldNotSupportUsernamePasswordAuthenticationToken() {
        // Act
        boolean supports = authenticationProvider.supports(UsernamePasswordAuthenticationToken.class);

        // Assert
        assertThat(supports).isFalse();
    }

    @Test
    @DisplayName("Token autenticado no debería contener credenciales")
    void authenticatedTokenShouldNotContainCredentials() {
        // Arrange
        String email = "test@example.com";
        String rawPassword = "SecurePass1!";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        UserDetails userDetails = User.builder()
                .username(email)
                .password(encodedPassword)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        when(userDetailService.loadUserByEmail(email)).thenReturn(userDetails);

        EmailAuthenticationToken unauthenticatedToken = new EmailAuthenticationToken(email, rawPassword);

        // Act
        Authentication result = authenticationProvider.authenticate(unauthenticatedToken);

        // Assert
        assertThat(result.getCredentials()).isNull();
    }

    @Test
    @DisplayName("Debería preservar las authorities del usuario")
    void shouldPreserveUserAuthorities() {
        // Arrange
        String email = "admin@example.com";
        String rawPassword = "AdminPass1!";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        UserDetails userDetails = User.builder()
                .username(email)
                .password(encodedPassword)
                .authorities(
                        List.of(
                                new SimpleGrantedAuthority("ROLE_USER"),
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                )
                .build();

        when(userDetailService.loadUserByEmail(email)).thenReturn(userDetails);

        EmailAuthenticationToken unauthenticatedToken = new EmailAuthenticationToken(email, rawPassword);

        // Act
        Authentication result = authenticationProvider.authenticate(unauthenticatedToken);

        // Assert
        assertThat(result.getAuthorities()).hasSize(2);
        assertThat(result.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }
}
