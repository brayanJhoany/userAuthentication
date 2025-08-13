package com.app.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        UserEntity userDB = userService.findByEmail(request.getEmail());
        if (!userDB.isEnabled()) {
            throw new DisabledException("User account is disabled");
        }

        String token = jwtUtils.generateAccessToken(request.getEmail());
        UserResponseDTO dto = toUserResponseDto(userDB);

        return new AuthResponseDTO(token, dto);
    }

    public AuthResponseDTO register(CreateUserDTO request) {
        UserResponseDTO newUser = userService.createUser(request);
        String token = jwtUtils.generateAccessToken(newUser.getEmail());

        return new AuthResponseDTO(token, newUser);
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
