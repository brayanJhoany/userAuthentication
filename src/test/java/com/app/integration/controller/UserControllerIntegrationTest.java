package com.app.integration.controller;

import com.app.dto.AuthRequestDTO;
import com.app.entity.UserEntity;
import com.app.exception.ErrorCode;
import com.app.factory.UserTestFactory;
import com.app.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    private String obtainAccessToken() throws Exception {
        UserEntity user = UserTestFactory.builder()
                .email("default@example.com")
                .username("testuser")
                .password(passwordEncoder.encode("StrongPass1!"))
                .enabled(true)
                .id(null)
                .build();
        userRepository.save(user);
        var body = new java.util.HashMap<String, String>();
        body.put("email", user.getEmail());
        body.put("password", "StrongPass1!");
        String jsonRequest = objectMapper.writeValueAsString(body);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("token").asText();
    }

    @Test
    void shouldReturnPaginatedUsersWhenAuthorizedRequestIsMade() throws Exception {
        String token = obtainAccessToken();
        UserEntity user1 = UserTestFactory.builder()
                .id(null)
                .email("test1@gmail.com")
                .username("testuser1")
                .build();
        UserEntity user2 = UserTestFactory.builder()
                .id(null)
                .email("test2@gmail.com")
                .username("testuser2")
                .build();
        userRepository.save(user1);
        userRepository.save(user2);
        mockMvc.perform(get("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.currentPage").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.size").isNumber())
                .andExpect(jsonPath("$.last").isBoolean())
                .andExpect(jsonPath("$.content[*].username").value(hasItems(user1.getUsername(), user2.getUsername())))
                .andReturn();
    }

    @Test
    void shouldReturnUsersFilteredByEmailWhenAuthorized() throws Exception {
        String token = obtainAccessToken();

        UserEntity user1 = UserTestFactory.builder()
                .id(null)
                .email("alice@example.com")
                .username("alice")
                .build();

        UserEntity user2 = UserTestFactory.builder()
                .id(null)
                .email("bob@example.com")
                .username("bobby")
                .build();

        userRepository.save(user1);
        userRepository.save(user2);

        mockMvc.perform(get("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "10")
                        .param("email", "alice@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].email").value("alice@example.com"))
                .andExpect(jsonPath("$.content[0].username").value("alice"))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.size").isNumber())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.last").isBoolean());
    }

    @Test
    void shouldReturnUsersFilteredByUsernameWhenAuthorized() throws Exception {
        String token = obtainAccessToken();

        UserEntity user1 = UserTestFactory.builder()
                .id(null)
                .email("carol@example.com")
                .username("carol")
                .build();

        UserEntity user2 = UserTestFactory.builder()
                .id(null)
                .email("dan@example.com")
                .username("daniel")
                .build();

        userRepository.save(user1);
        userRepository.save(user2);

        mockMvc.perform(get("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "10")
                        .param("username", "carol"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].username").value("carol"))
                .andExpect(jsonPath("$.content[0].email").value("carol@example.com"))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.size").isNumber())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.last").isBoolean());
    }

    @Test
    void shouldReturnUnauthorizedWhenNoTokenProvided() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUserByIdWhenAuthorized() throws Exception {
        String token = obtainAccessToken();
        UserEntity user = UserTestFactory.builder().id(null)
                .email("byid@example.com")
                .username("byid")
                .build();
        UserEntity saved = userRepository.save(user);
        mockMvc.perform(get("/api/v1/users/{id}", saved.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.email").value("byid@example.com"))
                .andExpect(jsonPath("$.username").value("byid"));
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        String token = obtainAccessToken();
        mockMvc.perform(get("/api/v1/users/{id}", 9999)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").value(ErrorCode.USER_NOT_FOUND.getMessage()));
    }

    @Test
    void shouldUpdateEmailWhenAuthorizedAndUnique() throws Exception {
        String token = obtainAccessToken();
        UserEntity user = UserTestFactory.builder().id(null)
                .email("old@example.com").username("olduser").build();
        UserEntity existing = userRepository.save(user);

        var update = UserTestFactory.anyUpdateDto();
        update.setEmail("new@example.com");
        String json = objectMapper.writeValueAsString(update);

        mockMvc.perform(put("/api/v1/users/{id}", existing.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.id").value(existing.getId()));
    }
    @Test
    void shouldReturnConflictWhenUpdatingEmailToExistingOne() throws Exception {
        String token = obtainAccessToken();
        UserEntity userOne =  UserTestFactory.builder().id(null)
                .email("taken@example.com")
                .username("taken")
                .build();
        UserEntity userTwo = UserTestFactory
                .builder()
                .id(null)
                .email("owner@example.com")
                .username("owner")
                .build();
        userRepository.save(userOne);
        UserEntity target = userRepository.save(userTwo);

        var update = UserTestFactory.anyUpdateDto();
        update.setEmail("taken@example.com");
        String json = objectMapper.writeValueAsString(update);

        mockMvc.perform(put("/api/v1/users/{id}", target.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest()) // ajusta si tu handler usa otro status
                .andExpect(jsonPath("$.code").value(ErrorCode.EMAIL_ALREADY_EXISTS.name()))
                .andExpect(jsonPath("$.message").value(ErrorCode.EMAIL_ALREADY_EXISTS.getMessage()));
    }

    @Test
    void shouldDeleteUserWhenAuthorized() throws Exception {
        String token = obtainAccessToken();
        UserEntity user = UserTestFactory.builder()
                        .id(null)
                        .email("del@example.com")
                        .username("todel")
                        .build();
        UserEntity toDelete = userRepository.save(user);
        mockMvc.perform(delete("/api/v1/users/{id}", toDelete.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        assertFalse(userRepository.findById(toDelete.getId()).isPresent());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistingUser() throws Exception {
        String token = obtainAccessToken();
        mockMvc.perform(delete("/api/v1/users/{id}", 123456)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").value(ErrorCode.USER_NOT_FOUND.getMessage())); // adapta a tu ErrorCode
    }
}
