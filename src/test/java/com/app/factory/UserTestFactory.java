package com.app.factory;

import com.app.dto.CreateUserDTO;
import com.app.entity.UserEntity;

public class UserTestFactory {
    private static final String STRONG_PASS = "StrongPass1!";

    private UserTestFactory() { }

    public static UserEntity createDefaultUser() {
        return UserEntity.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .password("StrongPassword123!")
                .age(30)
                .build();
    }

    public static UserEntity anyUser() {
        return builder().build();
    }

    public static UserEntity.UserEntityBuilder builder() {
        return UserEntity.builder()
                .id(1L)
                .email("default@example.com")
                .username("defaultUser")
                .password(STRONG_PASS)
                .age(30)
                .enabled(true);
    }

    public static CreateUserDTO anyCreateDto() {
        CreateUserDTO dto = new CreateUserDTO();
        dto.setEmail("default@example.com");
        dto.setUsername("defaultUser");
        dto.setPassword(STRONG_PASS);
        dto.setAge(30);
        return dto;
    }


}
