package com.app.controller;

import com.app.config.filter.JWTAuthorizationFilter;
import com.app.dto.CreateUserDTO;
import com.app.dto.PaginatedResponseDTO;
import com.app.dto.UpdateUserDTO;
import com.app.dto.UserResponseDTO;
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
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

        UserResponseDTO userResponseDTO = UserTestFactory.mapToResponse(user);
        PaginatedResponseDTO<UserResponseDTO> page = PaginatedResponseDTO.<UserResponseDTO>builder()
                .content(List.of(userResponseDTO))
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
                .andExpect(jsonPath("$.content[0].id")
                        .value(userResponseDTO.getId()))
                .andExpect(jsonPath("$.content[0].email")
                        .value(userResponseDTO.getEmail()))
                .andExpect(jsonPath("$.content[0].username")
                        .value(userResponseDTO.getUsername()))
                .andExpect(jsonPath("$.content[0].age")
                        .value(userResponseDTO.getAge()))
                .andExpect(jsonPath("$.content[0].enabled")
                        .value(userResponseDTO.isEnabled()))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.last").value(true));
        //
        verify(userService).getAllUsersPaginated(0, 10, null, null);
    }

    @Test
    void shouldReturnSingleUserById() throws Exception {
        UserEntity user = UserTestFactory.anyUser();
        UserResponseDTO userResponseDTO = UserTestFactory.mapToResponse(user);
        when(userService.getUser("1")).thenReturn(userResponseDTO);
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.id").value(user.getId()))
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

        var body = new java.util.HashMap<String, Object>();
        body.put("email", dto.getEmail());
        body.put("username", dto.getUsername());
        body.put("password", dto.getPassword());
        body.put("age", dto.getAge());

        UserEntity user = UserTestFactory.builder().build();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setAge(dto.getAge());
        user.setEnabled(true);

        UserResponseDTO userResponseDTO = UserTestFactory.mapToResponse(user);

        when(userService.createUser(any())).thenReturn(userResponseDTO);

        mockMvc.perform(post("/api/v1/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
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

        UserResponseDTO userResponseDTO = UserTestFactory.mapToResponse(user);

        when(userService.updateUser(eq("1"), any())).thenReturn(userResponseDTO);

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