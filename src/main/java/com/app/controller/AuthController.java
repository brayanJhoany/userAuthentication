package com.app.controller;

import com.app.config.jwt.JwtUtils;
import com.app.dto.AuthRequest;
import com.app.dto.AuthResponse;
import com.app.dto.CreateUserDTO;
import com.app.dto.RegisterResponse;
import com.app.entity.UserEntity;
import com.app.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = (User) authentication.getPrincipal();
        String token = jwtUtils.generateAccessToken(user.getUsername());

        return ResponseEntity.ok(new AuthResponse(token, user.getUsername()));
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid CreateUserDTO request) {
        UserEntity newUser = userService.createUser(request);
        String token = jwtUtils.generateAccessToken(newUser.getUsername());
        RegisterResponse response = new RegisterResponse();
        response.setEmail(newUser.getEmail());
        response.setUsername(newUser.getUsername());
        response.setAge(newUser.getAge());
        response.setToken(token);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
