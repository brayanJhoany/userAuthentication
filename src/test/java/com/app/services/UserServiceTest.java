package com.app.services;

import com.app.dto.CreateUserDTO;
import com.app.dto.PaginatedResponse;
import com.app.dto.UpdateUserDTO;
import com.app.entity.UserEntity;
import com.app.exception.user.EmailAlreadyExistsException;
import com.app.exception.user.UserNotFoundException;
import com.app.exception.user.WeakPasswordException;
import com.app.factory.UserTestFactory;
import com.app.repository.UserRepository;
import com.app.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static com.app.factory.UserTestFactory.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private CreateUserDTO createDto;
    @BeforeEach
    void setUp() {

        createDto = UserTestFactory.anyCreateDto();
    }

    //Creacion
    @Test
    public void shouldCreateUserWhenValidInput() {
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        userService.createUser(createDto);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        UserEntity savedUser = captor.getValue();
        // Assert
        assertThat(savedUser.getEmail()).isEqualTo("default@example.com");
        assertThat(savedUser.getUsername()).isEqualTo("defaultUser");
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");

    }

    @Test
    void shouldNotCreateUserWhenInputIsInvalid() {
        when(passwordEncoder.encode(any())).thenReturn("encPwd");

        userService.createUser(createDto);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());

        UserEntity saved = captor.getValue();
        assertThat(saved)
                .extracting(UserEntity::getEmail, UserEntity::getUsername, UserEntity::getPassword, UserEntity::isEnabled)
                .containsExactly("default@example.com", "defaultUser", "encPwd", true);
    }

    @Test
    public void shouldCreateUserWithTheEnabledStatus(){
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

        userService.createUser(createDto);
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        UserEntity savedUser = captor.getValue();
        // Assert
        assertThat(savedUser)
                .extracting(UserEntity::getEmail,
                        UserEntity::getUsername,
                        UserEntity::getPassword,
                        UserEntity::isEnabled)
                .containsExactly("default@example.com",
                        "defaultUser",
                        "encodedPassword",
                        true);
    }

    @Test
    public void shouldCreateUserWhenPasswordIsStrong() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

        createDto.setPassword("StrongPassword123!");
        userService.createUser(createDto);
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    public void shouldThrowExceptionWhenEmailExists(){
        when(userRepository.existsByEmail(any())).thenReturn(true);
        assertThrows(EmailAlreadyExistsException.class, () -> userService.createUser(createDto));
        verify(userRepository, never()).save(any());
    }

    @Test
    public void shouldThrowExceptionWhenPasswordIsWeak() {
        createDto.setPassword("weakpassword");
        assertThrows(WeakPasswordException.class, () -> userService.createUser(createDto));
        verify(userRepository, never()).save(any());
    }
    //Lectura
    @Test
    public void shouldReturnUserWhenExists(){
        UserEntity defaultUser = UserTestFactory.anyUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(defaultUser));
        UserEntity user = userService.getUser("1");
        assertThat(user).isEqualTo(defaultUser);
    }
    @Test
    public void shouldThrowExceptionWhenUserNotFound(){
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUser("1"));
    }

    // Eliminacion
    @Test
    public void shouldThrowExceptionWhenUserDoesNotExistOnDelete(){
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser("1"));
    }

    @Test
    public void shouldDeleteUserWhenExists(){
        UserEntity defaultUser = UserTestFactory.builder().build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(defaultUser));
        userService.deleteUser("1");
        verify(userRepository).delete(defaultUser);
    }

    //Actualizacion
    @Test
    public void shouldThrowExceptionWhenUserNotFoundOnUpdate() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setEmail("newemail@example.com");

        assertThrows(UserNotFoundException.class, () -> userService.updateUser("1", updateUserDTO));

        verify(userRepository, never()).save(any());
    }
    @Test
    public void shouldThrowExceptionWhenNewEmailAlreadyExists() {
        UserEntity existingUser = builder().build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(true);

        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setEmail("newemail@example.com");

        assertThrows(EmailAlreadyExistsException.class, () -> userService.updateUser("1", updateUserDTO));
        verify(userRepository, never()).save(any());
    }
    @Test
    public void shouldUpdateEmailSuccessfully() {
        UserEntity existingUser = builder().build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);

        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setEmail("newemail@example.com");

        userService.updateUser("1", updateUserDTO);

        assertThat(existingUser.getEmail()).isEqualTo("newemail@example.com");
        verify(userRepository).save(existingUser);
    }

    @Test
    public void shouldUpdatePasswordSuccessfully() {
        UserEntity existingUser = builder().build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("newStrongPassword123!")).thenReturn("encodedPassword");

        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setPassword("newStrongPassword123!");

        userService.updateUser("1", updateUserDTO);

        assertThat(existingUser.getPassword()).isEqualTo("encodedPassword");
        verify(userRepository).save(existingUser);
    }
    @Test
    public void shouldNotUpdatePasswordIfItIsWeak() {
        UserEntity existingUser = builder().build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setPassword("weakpassword");

        assertThrows(WeakPasswordException.class, () -> userService.updateUser("1", updateUserDTO));
        assertThat(existingUser.getPassword()).isEqualTo("StrongPass1!");
        verify(userRepository, never()).save(existingUser);
    }

    @Test
    public void shouldNotUpdateAnythingIfDtoIsEmpty() {
        UserEntity existingUser = builder().build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        UpdateUserDTO emptyDTO = new UpdateUserDTO();

        userService.updateUser("1", emptyDTO);

        assertThat(existingUser.getEmail()).isEqualTo("default@example.com");
        assertThat(existingUser.getUsername()).isEqualTo("defaultUser");
        assertThat(existingUser.getPassword()).isEqualTo("StrongPass1!");

        verify(userRepository).save(existingUser);
    }

    //Paginacion
    @Test
    public void shouldReturnAllUsersWhenNoFiltersProvided() {
        UserEntity user = builder().build();
        Page<UserEntity> page = new PageImpl<>(List.of(user));

        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        PaginatedResponse<UserEntity> response = userService.getAllUsersPaginated(0, 1, null, null);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0)).isEqualTo(user);
        assertThat(response.getCurrentPage()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(1);
    }

    @Test
    public void shouldReturnFilteredUsersWhenFiltersProvided() {
        UserEntity user = builder().build();
        Page<UserEntity> page = new PageImpl<>(List.of(user));

        when(userRepository.findByFilters(eq("default@example.com"), eq(null), any(Pageable.class)))
                .thenReturn(page);

        PaginatedResponse<UserEntity> response = userService.getAllUsersPaginated(0, 10, "default@example.com", null);
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0)).isEqualTo(user);
        verify(userRepository).findByFilters(eq("default@example.com"), eq(null), any(Pageable.class));
    }

    @Test
    public void shouldReturnEmptyPageWhenNoUsersFound() {
        Page<UserEntity> emptyPage = new PageImpl<>(List.of());

        when(userRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        PaginatedResponse<UserEntity> response = userService.getAllUsersPaginated(0, 10, null, null);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0);
    }

}
