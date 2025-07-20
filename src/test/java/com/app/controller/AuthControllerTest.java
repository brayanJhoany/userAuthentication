package com.app.controller;

import com.app.config.filter.JWTAuthorizationFilter;
import com.app.config.jwt.JwtUtils;
import com.app.dto.AuthRequest;
import com.app.dto.CreateUserDTO;
import com.app.dto.UserResponseDTO;
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
    void shouldReturnTokenAndUserWhenCredentialsAreValid() throws Exception {
        // Arrange
        String email = "test@example.com";
        String password = "StrongPass1!";
        String fakeToken = "fake-jwt-token";

        AuthRequest request = new AuthRequest(email, password);
        UserDetails userDetails = new User(email, password, Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null);

        UserEntity userDB = new UserEntity();
        userDB.setId(1L);
        userDB.setEmail(email);
        userDB.setUsername("testUser");
        userDB.setAge(30);
        userDB.setEnabled(true);

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);
        when(jwtUtils.generateAccessToken(email))
                .thenReturn(fakeToken);
        when(userService.findByEmail(email))
                .thenReturn(userDB);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(fakeToken))
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.username").value("testUser"))
                .andExpect(jsonPath("$.user.age").value(30))
                .andExpect(jsonPath("$.user.enabled").value(true));

        // Verify
        verify(authenticationManager).authenticate(any());
        verify(jwtUtils).generateAccessToken(email);
        verify(userService).findByEmail(email);
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
        UserResponseDTO userResponseDTO = UserTestFactory.mapToResponse(savedUser);

        when(userService.createUser(any(CreateUserDTO.class)))
                .thenReturn(userResponseDTO);
        when(jwtUtils.generateAccessToken(savedUser.getEmail()))
                .thenReturn("fake-jwt-token");

        // Act & Assert
        ResultActions result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)));

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.user.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.user.email").value(userDTO.getEmail()))
                .andExpect(jsonPath("$.user.username").value(userDTO.getUsername()))
                .andExpect(jsonPath("$.user.age").value(userDTO.getAge()))
                .andExpect(jsonPath("$.user.enabled").value(true));
        // Verify interactions
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