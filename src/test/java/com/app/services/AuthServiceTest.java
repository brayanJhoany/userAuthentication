package com.app.services;

import com.app.config.jwt.JwtUtils;
import com.app.dto.AuthRequestDTO;
import com.app.dto.AuthResponseDTO;
import com.app.dto.CreateUserDTO;
import com.app.dto.UserResponseDTO;
import com.app.entity.UserEntity;
import com.app.factory.UserTestFactory;
import com.app.service.AuthService;
import com.app.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserService userService;

    @Test
    void shouldLoginSuccessfullyWhenCredentialsAreValidAndUserEnabled() {
        // Arrange
        String email = "test@example.com";
        String password = "StrongPass1!";
        String fakeToken = "fake-jwt-token";

        AuthRequestDTO request = new AuthRequestDTO(email, password);

        User user = new User(email, password, Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null);

        UserEntity userEntity = UserTestFactory.builder()
                .email(email)
                .enabled(true)
                .build();

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userService.findByEmail(email)).thenReturn(userEntity);
        when(jwtUtils.generateAccessToken(email)).thenReturn(fakeToken);

        // Act
        AuthResponseDTO response = authService.login(request);

        // Assert
        assertEquals(fakeToken, response.getToken());
        assertEquals(email, response.getUser().getEmail());
        assertTrue(response.getUser().isEnabled());
        verify(authenticationManager).authenticate(any());
        verify(userService).findByEmail(email);
        verify(jwtUtils).generateAccessToken(email);
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenAuthenticationFails() {
        // Arrange
        AuthRequestDTO request = new AuthRequestDTO("wrong@example.com", "wrongpass");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(authenticationManager).authenticate(any());
        verifyNoInteractions(userService);
        verifyNoInteractions(jwtUtils);
    }

    @Test
    void shouldThrowDisabledExceptionWhenUserIsNotEnabled() {
        // Arrange
        String email = "disabled@example.com";
        AuthRequestDTO request = new AuthRequestDTO(email, "pass");

        User user = new User(email, "pass", Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null);

        UserEntity userEntity = UserTestFactory.builder()
                .email(email)
                .enabled(false)
                .build();

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userService.findByEmail(email)).thenReturn(userEntity);

        // Act & Assert
        assertThrows(DisabledException.class, () -> authService.login(request));
        verify(authenticationManager).authenticate(any());
        verify(userService).findByEmail(email);
        verifyNoInteractions(jwtUtils);
    }

    @Test
    void shouldRegisterUserSuccessfullyAndReturnTokenAndUserDto() {
        // Arrange
        CreateUserDTO request = UserTestFactory.anyCreateDto();
        UserResponseDTO userDto = UserTestFactory.mapToResponse(
                UserTestFactory.builder()
                        .email(request.getEmail())
                        .username(request.getUsername())
                        .age(request.getAge())
                        .build()
        );
        String fakeToken = "register-token";

        when(userService.createUser(any())).thenReturn(userDto);
        when(jwtUtils.generateAccessToken(userDto.getEmail())).thenReturn(fakeToken);

        // Act
        AuthResponseDTO response = authService.register(request);

        // Assert
        assertEquals(fakeToken, response.getToken());
        assertEquals(userDto.getEmail(), response.getUser().getEmail());
        verify(userService).createUser(any());
        verify(jwtUtils).generateAccessToken(userDto.getEmail());
    }
}
