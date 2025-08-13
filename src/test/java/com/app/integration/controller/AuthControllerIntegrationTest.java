package com.app.integration.controller;

import com.app.dto.AuthRequestDTO;
import com.app.dto.CreateUserDTO;
import com.app.entity.UserEntity;
import com.app.exception.ErrorCode;
import com.app.factory.UserTestFactory;
import com.app.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldLoginSuccessfullyWhenCredentialsAreValid() throws Exception {
        UserEntity user = UserTestFactory.anyUser();
        user.setPassword(passwordEncoder.encode("StrongPass1!"));
        userRepository.save(user);
        var body = new java.util.HashMap<String, String>();
        body.put("email", user.getEmail());
        body.put("password", "StrongPass1!");
        String jsonRequest = objectMapper.writeValueAsString(body);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value(user.getEmail()))
                .andExpect(jsonPath("$.user.username").value(user.getUsername()))
                .andExpect(jsonPath("$.user.age").value(user.getAge()))
                .andExpect(jsonPath("$.user.enabled").value(true));
    }
    @Test
    void shouldNotLoginWhenCredentialsAreInvalid() throws Exception{
        UserEntity user = UserTestFactory.anyUser();
        user.setPassword(passwordEncoder.encode("StrongPass1!"));
        userRepository.save(user);

        var body = new java.util.HashMap<String, String>();
        body.put("email", "test@example.com");
        body.put("password", "StrongPass1!");
        String jsonRequest = objectMapper.writeValueAsString(body);


       mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_CREDENTIALS.name()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_CREDENTIALS.getMessage()))
                .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()));


    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        CreateUserDTO userDtoRequest =  UserTestFactory.anyCreateDto();
        String jsonRequest = objectMapper.writeValueAsString(userDtoRequest);
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value(userDtoRequest.getEmail()))
                .andExpect(jsonPath("$.user.username").value(userDtoRequest.getUsername()))
                .andExpect(jsonPath("$.user.age").value(userDtoRequest.getAge()))
                .andExpect(jsonPath("$.user.enabled").value(true));
    }
    @Test
    void shouldNotRegisterUserWithExistingEmail() throws Exception {
        UserEntity user = UserTestFactory.anyUser();
        user.setPassword(passwordEncoder.encode("StrongPass1!"));
        userRepository.save(user);
        CreateUserDTO userDtoRequest = UserTestFactory.anyCreateDto();
        userDtoRequest.setEmail(user.getEmail()); // Use existing email
        String jsonRequest = objectMapper.writeValueAsString(userDtoRequest);
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.code").value(ErrorCode.EMAIL_ALREADY_EXISTS.name()))
                        .andExpect(jsonPath("$.message").value(ErrorCode.EMAIL_ALREADY_EXISTS.getMessage()))
                        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                        .andReturn();

    }
    @Test
    void shouldNotRegisterUserWhenDataIsInvalid() throws Exception {
        CreateUserDTO userDtoRequest = UserTestFactory.anyCreateDto();
        userDtoRequest.setEmail("invalid-email");
        userDtoRequest.setPassword("weak");
        userDtoRequest.setAge(10);
        String jsonRequest = objectMapper.writeValueAsString(userDtoRequest);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("email: must be a well-formed email address")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("password: size must be between")))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andReturn();
    }
}
