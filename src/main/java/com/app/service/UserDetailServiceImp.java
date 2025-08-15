package com.app.service;

import com.app.entity.UserEntity;
import com.app.exception.user.UserNotFoundException;
import com.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImp implements UserDetailsService {
    private final UserRepository userRepository;

    public UserDetails loadUserByEmail(String email) {
        var entity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException());

        return org.springframework.security.core.userdetails.User
                .withUsername(entity.getEmail())   // username = email
                .password(entity.getPassword())
                .authorities(Collections.emptyList())
                .accountLocked(!entity.isEnabled())
                .disabled(!entity.isEnabled())
                .build();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return User.builder()
                .username(userEntity.getUsername())
                .password(userEntity.getPassword())
                .authorities("USER")
                .accountLocked(false)
                .accountExpired(false)
                .disabled(false)
                .credentialsExpired(false)
                .build();
    }
}
