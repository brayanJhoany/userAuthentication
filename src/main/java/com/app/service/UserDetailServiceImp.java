package com.app.service;

import com.app.exception.user.UserNotFoundException;
import com.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImp {
    private final UserRepository userRepository;

    public UserDetails loadUserByEmail(String email) {
        var entity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException());

        return org.springframework.security.core.userdetails.User
                .withUsername(entity.getEmail())
                .password(entity.getPassword())
                .authorities(Collections.emptyList())
                .accountLocked(!entity.isEnabled())
                .disabled(!entity.isEnabled())
                .build();
    }
}
