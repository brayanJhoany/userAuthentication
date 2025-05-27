package com.app.controller;

import com.app.config.filter.JWTAuthorizationFilter;
import com.app.config.jwt.JwtUtils;
import com.app.dto.AuthRequest;
import com.app.dto.CreateUserDTO;
import com.app.entity.UserEntity;
import com.app.factory.UserTestFactory;
import com.app.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JWTAuthorizationFilter.class)
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldReturnTokenWhenCredentialsAreValid() throws Exception {
        AuthRequest request = new AuthRequest("test@example.com", "password");
        UserDetails userDetails = new User(request.getEmail(), request.getPassword(), Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null);

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);
        when(jwtUtils.generateAccessToken(request.getEmail()))
                .thenReturn("fake-jwt-token");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.email").value(request.getEmail()));

        verify(authenticationManager).authenticate(any());
        verify(jwtUtils).generateAccessToken(request.getEmail());
    }

    @Test
    void shouldReturnUnauthorizedWhenCredentialsAreInvalid() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest("wrong@example.com", "wrongpass");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(authenticationManager).authenticate(any());
        verifyNoInteractions(jwtUtils);
    }

    @Test
    void ShouldCreateUserAndReturnToken() throws Exception {
        // Arrange
        CreateUserDTO userDTO = UserTestFactory.anyCreateDto();
        UserEntity savedUser = UserTestFactory.builder()
                .email(userDTO.getEmail())
                .username(userDTO.getUsername())
                .age(userDTO.getAge())
                .build();

        when(userService.createUser(any(CreateUserDTO.class)))
                .thenReturn(savedUser);
        when(jwtUtils.generateAccessToken(savedUser.getEmail()))
                .thenReturn("fake-jwt-token");
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        ResultActions result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)));

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.email").value(userDTO.getEmail()))
                .andExpect(jsonPath("$.username").value(userDTO.getUsername()))
                .andExpect(jsonPath("$.age").value(userDTO.getAge()));

        verify(userService).createUser(any(CreateUserDTO.class));
        verify(jwtUtils).generateAccessToken(savedUser.getEmail());
    }

    @Test
    void shouldReturnBadRequestWhenInputIsInvalid() throws Exception {
        CreateUserDTO invalidRequest = new CreateUserDTO();
        invalidRequest.setEmail("invalid-email@gmail.com");
        invalidRequest.setUsername("defaultUser");
        invalidRequest.setPassword("123");
        invalidRequest.setAge(-1);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("age:")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("password:")))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(userService);
        verifyNoInteractions(jwtUtils);
    }

}