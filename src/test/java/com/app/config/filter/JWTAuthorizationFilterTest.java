package com.app.config.filter;

import com.app.config.jwt.JwtUtils;
import com.app.exception.ErrorCode;
import com.app.exception.auth.UnauthorizedException;
import com.app.exception.user.UserNotFoundException;
import com.app.service.UserDetailServiceImp;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JWTAuthorizationFilterTest {
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private UserDetailServiceImp userDetailServiceImp;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    private JWTAuthorizationFilter filter;




    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filter = new JWTAuthorizationFilter(jwtUtils, userDetailServiceImp);
    }
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateUserWhenTokenIsValid() throws ServletException, IOException {
        String token = "valid.token";
        String email = "test@example.com";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        UserDetails userDetails = new User(email, "password", Collections.emptyList());

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.getEmailFromToken(token)).thenReturn(email);
        when(userDetailServiceImp.loadUserByEmail(email)).thenReturn(userDetails);

        filter.doFilterInternal(request, response, filterChain);

        verify(userDetailServiceImp).loadUserByEmail(email);
        verify(filterChain).doFilter(request, response);
        assertNotNull(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication());
    }
    @Test
    void shouldThrowUnauthorizedExceptionWhenTokenIsInvalid() throws ServletException, IOException {
        String token = "invalid.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtils.validateToken(token)).thenReturn(false);

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () ->
                filter.doFilterInternal(request, response, filterChain)
        );

        assertEquals("No autorizado", exception.getMessage());

        verifyNoInteractions(userDetailServiceImp);
        verify(filterChain, never()).doFilter(request, response);
    }
    @Test
    void shouldContinueFilterChainWhenNoTokenProvided() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtils);
        verifyNoInteractions(userDetailServiceImp);
    }

    @Test
    void shouldContinueFilterChainWhenTokenDoesNotStartWithBearer() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("InvalidPrefix token");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtils);
        verifyNoInteractions(userDetailServiceImp);
    }
    @Test
    void shouldThrowExceptionWhenUserNotFound() throws ServletException, IOException {
        String token = "valid.token";
        String email = "test@example.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.getEmailFromToken(token)).thenReturn(email);
        when(userDetailServiceImp.loadUserByEmail(email)).thenThrow(new UserNotFoundException());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                filter.doFilterInternal(request, response, filterChain)
        );

        assertEquals(ErrorCode.USER_NOT_FOUND.getMessage(), exception.getMessage());
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenEmailFromTokenIsNull() throws ServletException, IOException {
        String token = "valid.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.getEmailFromToken(token)).thenReturn(null);

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () ->
                filter.doFilterInternal(request, response, filterChain)
        );

        assertEquals("No autorizado", exception.getMessage());
        verifyNoInteractions(userDetailServiceImp);
        verify(filterChain, never()).doFilter(request, response);
    }

}
