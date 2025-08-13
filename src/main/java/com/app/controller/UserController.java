package com.app.controller;

import com.app.dto.CreateUserDTO;
import com.app.dto.UpdateUserDTO;
import com.app.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> getUsersPaginate(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String username
    ) {
        return ResponseEntity.ok(userService.getAllUsersPaginated(page, size, email, username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserDTO createUserDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(createUserDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        return ResponseEntity.ok(userService.updateUser(id, updateUserDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
