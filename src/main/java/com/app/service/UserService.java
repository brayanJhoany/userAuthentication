package com.app.service;

import com.app.dto.CreateUserDTO;
import com.app.dto.PaginatedResponse;
import com.app.dto.UpdateUserDTO;
import com.app.entity.UserEntity;
import com.app.exception.user.EmailAlreadyExistsException;
import com.app.exception.user.UserNotFoundException;
import com.app.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserEntity createUser(CreateUserDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        UserEntity user = UserEntity.builder()
                .email(dto.getEmail())
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .age(dto.getAge())
                .build();

        return userRepository.save(user);
    }
    public void deleteUser(String id) {
        Long userId = Long.valueOf(id);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());
        userRepository.delete(user);
    }
    public UserEntity getUser(String id) {
        Long userId = Long.valueOf(id);
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());
    }
    public PaginatedResponse<UserEntity> getAllUsersPaginated(int page, int size, String email, String username) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<UserEntity> userPage;
        if ((email != null && !email.isBlank()) || (username != null && !username.isBlank())) {
            userPage = userRepository.findByFilters(email, username, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        return PaginatedResponse.<UserEntity>builder()
                .content(userPage.getContent())
                .currentPage(userPage.getNumber())
                .totalPages(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements())
                .size(userPage.getSize())
                .last(userPage.isLast())
                .build();
    }
    public UserEntity updateUser(String id, @Valid UpdateUserDTO updateUserDTO) {
        Long userId = Long.valueOf(id);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());
        if (updateUserDTO.getEmail() != null && !updateUserDTO.getEmail().isBlank()) {
            if (userRepository.existsByEmail(updateUserDTO.getEmail())) {
                throw new EmailAlreadyExistsException();
            }
            user.setEmail(updateUserDTO.getEmail());
        }
        if (updateUserDTO.getUsername() != null && !updateUserDTO.getUsername().isBlank()) {
            user.setUsername(updateUserDTO.getUsername());
        }
        //if (updateUserDTO.getPassword() != null && !updateUserDTO.getPassword().isBlank()) {
        //    user.setPassword(passwordEncoder.encode(updateUserDTO.getPassword()));
        //}

        return userRepository.save(user);
    }
}
