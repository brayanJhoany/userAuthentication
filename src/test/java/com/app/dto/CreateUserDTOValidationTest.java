package com.app.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CreateUserDTO Validation Tests")
class CreateUserDTOValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Debería validar DTO válido con contraseña fuerte")
    void shouldValidateValidDTO() {
        CreateUserDTO dto = new CreateUserDTO(
                "test@example.com",
                "testuser",
                "SecureP@ss1",
                25
        );
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "weak",            // Muy corta
            "password",        // Sin mayúsculas, números ni caracteres especiales
            "Password",        // Sin números ni caracteres especiales
            "Password123",     // Sin caracteres especiales
            "password123@",    // Sin mayúsculas
            "PASSWORD123@"     // Sin minúsculas
    })
    @DisplayName("Debería rechazar contraseñas débiles en CreateUserDTO")
    void shouldRejectWeakPasswords(String weakPassword) {
        CreateUserDTO dto = new CreateUserDTO(
                "test@example.com",
                "testuser",
                weakPassword,
                25
        );
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(dto);
        assertThat(violations)
                .isNotEmpty()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    @DisplayName("Debería validar email inválido")
    void shouldRejectInvalidEmail() {
        CreateUserDTO dto = new CreateUserDTO(
                "invalid-email",
                "testuser",
                "SecureP@ss1",
                25
        );
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(dto);
        assertThat(violations)
                .isNotEmpty()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    @DisplayName("Debería validar múltiples errores simultáneamente")
    void shouldValidateMultipleErrors() {
        CreateUserDTO dto = new CreateUserDTO(
                "bad-email",
                "ab",           // Username muy corto
                "weak",         // Contraseña débil
                200             // Edad fuera de rango
        );
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(dto);
        assertThat(violations)
                .isNotEmpty()
                .hasSizeGreaterThanOrEqualTo(4);
    }

    @Test
    @DisplayName("Debería rechazar campos null o vacíos")
    void shouldRejectNullOrBlankFields() {
        // Arrange
        CreateUserDTO dto = new CreateUserDTO(
                null,
                "",
                null,
                null
        );
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(dto);
        assertThat(violations)
                .isNotEmpty()
                .hasSizeGreaterThanOrEqualTo(4);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "MyP@ssw0rd",
            "SecureP@ss123",
            "T3st!ng1",
            "Qwerty123@",
            "C0mpl3x!Pass"
    })
    @DisplayName("Debería aceptar contraseñas fuertes válidas")
    void shouldAcceptStrongPasswords(String strongPassword) {
        CreateUserDTO dto = new CreateUserDTO(
                "test@example.com",
                "testuser",
                strongPassword,
                25
        );
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Debería validar edad mínima")
    void shouldRejectAgeTooLow() {
        // Arrange
        CreateUserDTO dto = new CreateUserDTO(
                "test@example.com",
                "testuser",
                "SecureP@ss1",
                0
        );
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(dto);
        assertThat(violations)
                .isNotEmpty()
                .anyMatch(v -> v.getPropertyPath().toString().equals("age"));
    }

    @Test
    @DisplayName("Debería validar edad máxima")
    void shouldRejectAgeTooHigh() {
        CreateUserDTO dto = new CreateUserDTO(
                "test@example.com",
                "testuser",
                "SecureP@ss1",
                150
        );
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(dto);
        assertThat(violations)
                .isNotEmpty()
                .anyMatch(v -> v.getPropertyPath().toString().equals("age"));
    }
}
