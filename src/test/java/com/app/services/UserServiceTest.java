package com.app.services;

import com.app.dto.CreateUserDTO;
import com.app.dto.PaginatedResponseDTO;
import com.app.dto.UpdateUserDTO;
import com.app.dto.UserResponseDTO;
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
import org.springframework.data.jpa.domain.Specification;
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
        UserEntity userEntity = UserTestFactory.anyUser();

        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        UserResponseDTO response = userService.createUser(createDto);

        // Verificar que se guardó correctamente
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        UserEntity savedUser = captor.getValue();

        // Verificaciones sobre el UserEntity guardado
        assertThat(savedUser.getEmail()).isEqualTo("default@example.com");
        assertThat(savedUser.getUsername()).isEqualTo("defaultUser");
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");

        // Verificaciones sobre el UserResponseDTO retornado
        assertThat(response.getEmail()).isEqualTo(userEntity.getEmail());
        assertThat(response.getUsername()).isEqualTo(userEntity.getUsername());
        assertThat(response.getAge()).isEqualTo(userEntity.getAge());
        assertThat(response.isEnabled()).isEqualTo(userEntity.isEnabled());
        assertThat(response.getId()).isEqualTo(userEntity.getId());

    }

    @Test
    public void shouldCreateUserWithTheEnabledStatus(){
        UserEntity userEntity = UserTestFactory.anyUser();

        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        UserResponseDTO response = userService.createUser(createDto);

        // Verificar que se guardó correctamente
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        UserEntity savedUser = captor.getValue();

        // Verificaciones sobre el UserEntity guardado
        assertThat(savedUser.getEmail()).isEqualTo(userEntity.getEmail());
        assertThat(savedUser.getUsername()).isEqualTo(userEntity.getUsername());
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(savedUser.isEnabled()).isEqualTo(true);

        // Verificaciones sobre el UserResponseDTO retornado
        assertThat(response.getEmail()).isEqualTo(userEntity.getEmail());
        assertThat(response.getUsername()).isEqualTo(userEntity.getUsername());
        assertThat(response.getAge()).isEqualTo(userEntity.getAge());
        assertThat(response.isEnabled()).isEqualTo(userEntity.isEnabled());
        assertThat(response.getId()).isEqualTo(userEntity.getId());
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

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(defaultUser));

        UserResponseDTO response = userService.getUser("1");
        assertThat(response.getEmail()).isEqualTo(defaultUser.getEmail());
        assertThat(response.getUsername()).isEqualTo(defaultUser.getUsername());
        assertThat(response.getAge()).isEqualTo(defaultUser.getAge());
        assertThat(response.isEnabled()).isEqualTo(defaultUser.isEnabled());
        assertThat(response.getId()).isEqualTo(defaultUser.getId());
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
        UserEntity user = UserTestFactory.anyUser();
        UserEntity userBuild = builder().build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(userBuild));
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);

        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setEmail("newemail@example.com");
        user.setEmail("newemail@example.com");

        when(userRepository.save(any(UserEntity.class))).thenReturn(user);
        UserResponseDTO response = userService.updateUser("1", updateUserDTO);


        assertThat(userBuild.getId()).isEqualTo(response.getId());
        assertThat(user.getEmail()).isEqualTo(response.getEmail());
        assertThat(user.getUsername()).isEqualTo(response.getUsername());
        assertThat(user.getAge()).isEqualTo(response.getAge());
        assertThat(user.isEnabled()).isEqualTo(response.isEnabled());

        verify(userRepository).save(user);
    }

    @Test
    public void shouldUpdatePasswordSuccessfully() {
        // Arrange
        UserEntity existingUser = UserTestFactory.builder().build();

        String rawPassword = "newStrongPassword123!";
        String encodedPassword = "encodedPassword";

        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setPassword(rawPassword);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserResponseDTO response = userService.updateUser("1", updateUserDTO);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(existingUser.getId());
        assertThat(existingUser.getPassword()).isEqualTo(encodedPassword);

        // Verifica que se guardó el usuario con la nueva contraseña
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo(encodedPassword);

        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).findById(1L);
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

    //Paginacion
    @Test
    public void shouldReturnAllUsersWhenNoFiltersProvided() {
        UserEntity user = builder().build();
        UserResponseDTO expected = mapToResponse(user);
        Page<UserEntity> page = new PageImpl<>(List.of(user));
        Pageable pageable = Pageable.ofSize(1).withPage(0);
        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        PaginatedResponseDTO<UserResponseDTO> response =
                userService.getAllUsersPaginated(pageable, null, null);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0)).isEqualTo(expected);
        assertThat(response.getCurrentPage()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(1);

        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    public void shouldReturnFilteredUsersWhenFiltersProvided() {
        // Arrange
        UserEntity user = builder().build();
        UserResponseDTO expected = mapToResponse(user);
        Page<UserEntity> page = new PageImpl<>(List.of(user));
        Pageable pageable = Pageable.ofSize(1).withPage(0);

        when(userRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

        PaginatedResponseDTO<UserResponseDTO> response =
                userService.getAllUsersPaginated(pageable, "default@example.com", null);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getContent()).containsExactly(expected);
        assertThat(response.getCurrentPage()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(1);

        // Verificación de la invocación con Specification + Pageable
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Specification<UserEntity>> specCaptor =
                ArgumentCaptor.forClass(Specification.class);

        verify(userRepository).findAll(specCaptor.capture(), eq(pageable));
        assertThat(specCaptor.getValue()).isNotNull();
    }

    @Test
    public void shouldReturnEmptyPageWhenNoUsersFound() {
        Page<UserEntity> emptyPage = new PageImpl<>(List.of());
        Pageable pageable = Pageable.ofSize(1).withPage(0);
        when(userRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(emptyPage);

        PaginatedResponseDTO<UserResponseDTO> response = userService.getAllUsersPaginated(pageable, null, null);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Specification<UserEntity>> specCaptor =
                ArgumentCaptor.forClass(Specification.class);
        verify(userRepository).findAll(specCaptor.capture(), eq(pageable));
    }

}
