package com.app.service;

import com.app.dto.CreateUserDTO;
import com.app.dto.PaginatedResponseDTO;
import com.app.dto.UpdateUserDTO;
import com.app.dto.UserResponseDTO;
import com.app.entity.UserEntity;
import com.app.exception.user.EmailAlreadyExistsException;
import com.app.exception.user.UserNotFoundException;
import com.app.exception.user.WeakPasswordException;
import com.app.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDTO getUser(String id) {
        Long userId = Long.valueOf(id);
        UserEntity user =  userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());
        UserResponseDTO userResponseDTO = UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .age(user.getAge())
                .enabled(user.isEnabled())
                .build();
        return userResponseDTO;
    }
    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException());
    }

    public PaginatedResponseDTO<UserResponseDTO> getAllUsersPaginated(int page, int size, String email, String username) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<UserEntity> userPage;
        email = (email != null && !email.isBlank()) ? email : null;
        username = (username != null && !username.isBlank()) ? username : null;

        if (email != null || username != null) {
            userPage = userRepository.findByFilters(email, username, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }
        List<UserResponseDTO> dtoList = userPage
                .getContent()
                .stream()
                .map(this::toUserResponseDto)
                .toList();

        return PaginatedResponseDTO.<UserResponseDTO>builder()
                .content(dtoList)
                .currentPage(userPage.getNumber())
                .totalPages(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements())
                .size(userPage.getSize())
                .last(userPage.isLast())
                .build();
    }

    public UserResponseDTO createUser(CreateUserDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException();
        }
        if (!isStrongPassword(dto.getPassword())) {
            log.info("Weak password detected for email: {}", dto.getEmail());
            throw new WeakPasswordException();
        }
        UserEntity user = UserEntity.builder()
                .email(dto.getEmail())
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .age(dto.getAge())
                .enabled(true)
                .build();

        UserEntity userDB =  userRepository.save(user);
        return toUserResponseDto(userDB);
    }

    public UserResponseDTO updateUser(String id, @Valid UpdateUserDTO updateUserDTO) {
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
        if (updateUserDTO.getPassword() != null && !updateUserDTO.getPassword().isBlank()) {
            if (!isStrongPassword(updateUserDTO.getPassword())) {
                throw new WeakPasswordException();
            }
            user.setPassword(passwordEncoder.encode(updateUserDTO.getPassword()));
        }
        UserEntity userDB = userRepository.save(user);
        return toUserResponseDto(userDB);
    }

    public void deleteUser(String id) {
        Long userId = Long.valueOf(id);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());
        userRepository.delete(user);
    }

    public boolean isStrongPassword(String password) {
        if (password == null) {
            return false;
        }
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
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
