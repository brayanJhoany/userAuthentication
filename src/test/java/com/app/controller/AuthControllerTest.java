package com.app.controller;

import com.app.config.filter.JWTAuthorizationFilter;
import com.app.dto.AuthRequestDTO;
import com.app.dto.AuthResponseDTO;
import com.app.dto.CreateUserDTO;
import com.app.dto.UserResponseDTO;
import com.app.factory.UserTestFactory;
import com.app.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    private AuthService authService;

    @Test
    void shouldReturnTokenAndUserWhenLoginIsSuccessful() throws Exception {
        UserResponseDTO userDto = new UserResponseDTO(1L, "test@example.com", "testUser", 30, true);
        AuthResponseDTO response = AuthResponseDTO.builder()
                                                .token("fake-jwt-token")
                                                .user(userDto)
                                                .build();

        when(authService.login(any(AuthRequestDTO.class))).thenReturn(response);

        var body = new java.util.HashMap<String, String>();
        body.put("email", "test@example.com");
        body.put("password", "StrongPass1!");
        String json = objectMapper.writeValueAsString(body);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.username").value("testUser"))
                .andExpect(jsonPath("$.user.age").value(30))
                .andExpect(jsonPath("$.user.enabled").value(true));

        verify(authService).login(any(AuthRequestDTO.class));
    }

    @Test
    void shouldReturnTokenAndUserWhenRegisterIsSuccessful() throws Exception {
        CreateUserDTO request = UserTestFactory.anyCreateDto();
        var body = new java.util.HashMap<String, Object>();
        body.put("email", request.getEmail());
        body.put("username", request.getUsername());
        body.put("password", request.getPassword());
        body.put("age", request.getAge());

        UserResponseDTO userDto = UserTestFactory.mapToResponse(
                UserTestFactory.builder()
                        .email(request.getEmail())
                        .username(request.getUsername())
                        .age(request.getAge())
                        .build()
        );
        AuthResponseDTO response =AuthResponseDTO.builder()
                .token("fake-jwt-token")
                .user(userDto)
                .build();
        when(authService.register(any(CreateUserDTO.class))).thenReturn(response);
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.user.id").value(userDto.getId()))
                .andExpect(jsonPath("$.user.email").value(request.getEmail()))
                .andExpect(jsonPath("$.user.username").value(request.getUsername()))
                .andExpect(jsonPath("$.user.age").value(request.getAge()))
                .andExpect(jsonPath("$.user.enabled").value(true));

        verify(authService).register(any(CreateUserDTO.class));
    }

    @Test
    void shouldReturnBadRequestWhenRegisterInputIsInvalid() throws Exception {
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

        verifyNoInteractions(authService);
    }
}