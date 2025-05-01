package com.app.services;

import com.app.entity.UserEntity;
import com.app.factory.UserTestFactory;
import com.app.repository.UserRepository;
import com.app.service.UserDetailServiceImp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

        UserDetails userDetails = service.loadUserByUsername("default@example.com");

        assertThat(userDetails.getUsername()).isEqualTo(user.getUsername());
        assertThat(userDetails.getPassword()).isEqualTo(user.getPassword());
        assertThat(userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList())
                .contains("USER");
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundByEmail() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("notfound@example.com"));
    }
}
