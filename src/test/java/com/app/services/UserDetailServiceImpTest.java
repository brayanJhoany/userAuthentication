package com.app.services;

import com.app.entity.UserEntity;
import com.app.exception.user.UserNotFoundException;
import com.app.factory.UserTestFactory;
import com.app.repository.UserRepository;
import com.app.service.UserDetailServiceImp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserDetailServiceImpTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailServiceImp service;

    @Test
    void shouldLoadUserByEmailWhenUserExists() {
        UserEntity user = UserTestFactory.anyUser();
        when(userRepository.findByEmail("default@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = service.loadUserByEmail("default@example.com");

        assertThat(userDetails.getUsername()).isEqualTo(user.getEmail());
        assertThat(userDetails.getPassword()).isEqualTo(user.getPassword());
        assertThat(userDetails.getAuthorities()).isEmpty();
        assertThat(userDetails.isAccountNonLocked()).isEqualTo(user.isEnabled());
        assertThat(userDetails.isEnabled()).isEqualTo(user.isEnabled());
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundByEmail() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByEmail("notfound@example.com"))
                .isInstanceOf(UserNotFoundException.class);
    }
}
