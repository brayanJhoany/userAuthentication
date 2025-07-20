package com.app.controller;

import com.app.config.jwt.JwtUtils;
import com.app.dto.AuthRequest;
import com.app.dto.AuthResponse;
import com.app.dto.CreateUserDTO;
import com.app.dto.UserResponseDTO;
import com.app.entity.UserEntity;
import com.app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.userdetails.User;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = (User) authentication.getPrincipal();
        if(user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        UserEntity userDB = userService.findByEmail(user.getUsername());
        if (!userDB.isEnabled()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User account is disabled");
        }
        String token = jwtUtils.generateAccessToken(request.getEmail());
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(userDB.getId());
        userResponseDTO.setEmail(userDB.getEmail());
        userResponseDTO.setUsername(userDB.getUsername());
        userResponseDTO.setAge(userDB.getAge());
        userResponseDTO.setEnabled(userDB.isEnabled());
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUser(userResponseDTO);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid CreateUserDTO request) {
        UserResponseDTO newUser = userService.createUser(request);
        String token = jwtUtils.generateAccessToken(newUser.getEmail());

        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(newUser.getId());
        userResponseDTO.setEmail(newUser.getEmail());
        userResponseDTO.setUsername(newUser.getUsername());
        userResponseDTO.setAge(newUser.getAge());
        userResponseDTO.setEnabled(newUser.isEnabled());
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUser(userResponseDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
