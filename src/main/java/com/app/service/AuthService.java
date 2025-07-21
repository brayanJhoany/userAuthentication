package com.app.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.User;
import com.app.config.jwt.JwtUtils;
import com.app.dto.AuthRequest;
import com.app.dto.AuthResponse;
import com.app.dto.CreateUserDTO;
import com.app.dto.UserResponseDTO;
import com.app.entity.UserEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;

     public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = (User) authentication.getPrincipal();
        if (user == null) {
            throw new BadCredentialsException("Invalid credentials");
        }

        UserEntity userDB = userService.findByEmail(user.getUsername());
        if (!userDB.isEnabled()) {
            throw new DisabledException("User account is disabled");
        }

        String token = jwtUtils.generateAccessToken(request.getEmail());
        UserResponseDTO dto = toUserResponseDto(userDB);

        return new AuthResponse(token, dto);
    }

    public AuthResponse register(CreateUserDTO request) {
        UserResponseDTO newUser = userService.createUser(request);
        String token = jwtUtils.generateAccessToken(newUser.getEmail());

        return new AuthResponse(token, newUser);
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
