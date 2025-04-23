package com.app.factory;

import com.app.entity.UserEntity;

public class UserTestFactory {

    public static UserEntity createDefaultUser() {
        return UserEntity.builder()
                .email("test@example.com")
                .username("testuser")
                .password("password")
                .age(30)
                .build();
    }

    public static UserEntity createUser(String email, String username, String password, int age) {
        return UserEntity.builder()
                .email(email)
                .username(username)
                .password(password)
                .age(age)
                .build();
    }
}
