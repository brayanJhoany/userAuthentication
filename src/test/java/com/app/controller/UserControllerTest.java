package com.app.controller;

import com.app.config.SecurityBypassTestConfig;
import com.app.config.filter.JWTAuthorizationFilter;
import com.app.dto.CreateUserDTO;
import com.app.dto.PaginatedResponse;
import com.app.dto.UpdateUserDTO;
import com.app.entity.UserEntity;
import com.app.exception.user.UserNotFoundException;
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
import org.springframework.context.annotation.Import;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JWTAuthorizationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;
    @Test
    void shouldReturnPaginatedUsers() throws Exception {

        UserEntity user = UserTestFactory.anyUser();
        PaginatedResponse<UserEntity> page = PaginatedResponse.<UserEntity>builder()
                .content(List.of(user))
                .currentPage(0)
                .totalPages(1)
                .totalElements(1L)
                .size(1)
                .last(true)
                .build();

        when(userService.getAllUsersPaginated(0, 10, null, null))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email")
                        .value("default@example.com"))
                .andExpect(jsonPath("$.content[0].username")
                        .value("defaultUser"))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void shouldReturnSingleUserById() throws Exception {
        UserEntity user = UserTestFactory.anyUser();
        when(userService.getUser("1")).thenReturn(user);
        System.out.println();
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.email").isString())
                .andExpect(jsonPath("$.username").value(user.getUsername()))
                .andExpect(jsonPath("$.username").isString())
                .andExpect(jsonPath("$.age").value(user.getAge()))
                .andExpect(jsonPath("$.age").isNumber())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.enabled").isBoolean());
    }

    @Test
    void shouldCreateUser() throws Exception {
        CreateUserDTO dto = UserTestFactory.anyCreateDto();

        UserEntity user = UserTestFactory.builder().build();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setAge(dto.getAge());
        user.setEnabled(true);

        when(userService.createUser(any())).thenReturn(user);

        mockMvc.perform(post("/api/v1/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.email").isString())
                .andExpect(jsonPath("$.username").value(user.getUsername()))
                .andExpect(jsonPath("$.username").isString())
                .andExpect(jsonPath("$.age").value(user.getAge()))
                .andExpect(jsonPath("$.age").isNumber())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.enabled").isBoolean());


    }

    @Test
    void shouldUpdateUser() throws Exception {
        UpdateUserDTO dto = UserTestFactory.anyUpdateDto();
        UserEntity user = UserTestFactory.anyUser();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setAge(dto.getAge());
        user.setEnabled(true);
        when(userService.updateUser(eq("1"), any())).thenReturn(user);

        mockMvc.perform(put("/api/v1/users/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.email").value(user.getEmail()))
                    .andExpect(jsonPath("$.email").isString())
                    .andExpect(jsonPath("$.username").value(user.getUsername()))
                    .andExpect(jsonPath("$.username").isString())
                    .andExpect(jsonPath("$.age").value(user.getAge()))
                    .andExpect(jsonPath("$.age").isNumber())
                    .andExpect(jsonPath("$.enabled").value(true))
                    .andExpect(jsonPath("$.enabled").isBoolean());

    }

    @Test
    void shouldDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isNoContent());
        verify(userService).deleteUser("1");
    }

    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {
        when(userService.getUser("99"))
                .thenThrow(new UserNotFoundException());

        mockMvc.perform(get("/api/v1/users/99"))
                .andExpect(status()
                .isNotFound());
    }
@Test
void shouldReturn400WhenCreateUserInvalid() throws Exception {
    CreateUserDTO dto = new CreateUserDTO("", "notanemail", "123", -1);

    mockMvc.perform(post("/api/v1/users")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());
}


}