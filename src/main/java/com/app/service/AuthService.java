package com.app.service;

import com.app.config.security.EmailAuthenticationToken;
import com.app.exception.auth.DisabledAccountException;
import com.app.exception.auth.InvalidCredentialsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import com.app.config.jwt.JwtUtils;
import com.app.dto.AuthRequestDTO;
import com.app.dto.AuthResponseDTO;
import com.app.dto.CreateUserDTO;
import com.app.dto.UserResponseDTO;
import com.app.entity.UserEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;


@Service
@RequiredArgsConstructor
@Log4j2
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;

     public AuthResponseDTO login(AuthRequestDTO request) {
        try {
            authenticationManager.authenticate(
                    new EmailAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException();
        }
        UserEntity userDB = userService.findByEmail(request.getEmail());
        if (!userDB.isEnabled()) {
            throw new DisabledAccountException();
        }

        String token = jwtUtils.generateAccessToken(request.getEmail());
        UserResponseDTO dto = toUserResponseDto(userDB);
        AuthResponseDTO authResponse = AuthResponseDTO.builder()
                .token(token)
                .user(dto)
                .build();
        return authResponse;
    }

    public AuthResponseDTO register(CreateUserDTO request) {
        UserResponseDTO newUser = userService.createUser(request);
        String token = jwtUtils.generateAccessToken(newUser.getEmail());
        AuthResponseDTO authResponse = AuthResponseDTO.builder()
                .token(token)
                .user(newUser)
                .build();
        return authResponse;
    }

    private UserResponseDTO toUserResponseDto(UserEntity user){
        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .age(user.getAge())
                .enabled(user.isEnabled())
                .build();
    }
}
