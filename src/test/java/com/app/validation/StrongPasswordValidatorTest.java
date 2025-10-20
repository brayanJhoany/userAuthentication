package com.app.validation;

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

@DisplayName("StrongPasswordValidator Tests")
class StrongPasswordValidatorTest {

    private Validator validator;

    /**
     * Clase de prueba para validar la anotación @StrongPassword
     */
    static class TestDTO {
        @StrongPassword
        private String password;

        public TestDTO(String password) {
            this.password = password;
        }
    }

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Debería validar contraseña fuerte válida")
    void shouldValidateStrongPassword() {
        TestDTO dto = new TestDTO("P@ssw0rd");
        Set<ConstraintViolation<TestDTO>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Abcd123@",
            "SecureP@ss1",
            "MyP@ssw0rd123",
            "Test1234!",
            "Qwerty123@"
    })
    @DisplayName("Debería validar contraseñas fuertes válidas")
    void shouldValidateValidStrongPasswords(String password) {
        TestDTO dto = new TestDTO(password);
        Set<ConstraintViolation<TestDTO>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "short1@",         // Menos de 8 caracteres
            "abc",             // Muy corta
            "12345",           // Solo números
            "ABCDEFGH",        // Solo mayúsculas
            "abcdefgh",        // Solo minúsculas
            "Abcdefgh",        // Sin números ni caracteres especiales
            "Abcd1234",        // Sin caracteres especiales
            "Abcd@@@",         // Sin números
            "abcd123@",        // Sin mayúsculas
            "ABCD123@",        // Sin minúsculas
            "Password123",     // Sin caracteres especiales
            ""                 // Vacía
    })
    @DisplayName("Debería rechazar contraseñas débiles")
    void shouldRejectWeakPasswords(String password) {
        TestDTO dto = new TestDTO(password);
        Set<ConstraintViolation<TestDTO>> violations = validator.validate(dto);
        assertThat(violations)
                .isNotEmpty()
                .hasSize(1);
        ConstraintViolation<TestDTO> violation = violations.iterator().next();
        assertThat(violation.getMessage())
                .contains("La contraseña debe tener al menos 8 caracteres");
    }

    @Test
    @DisplayName("Debería permitir valor null (manejado por @NotBlank)")
    void shouldAllowNullValue() {
        TestDTO dto = new TestDTO(null);
        Set<ConstraintViolation<TestDTO>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Test@123",        // 8 caracteres exactos
            "A1@bcdef",        // Mínimo de cada requisito
            "MyV3ry$3cure",    // Con $ como carácter especial, más números
            "C0mpl3x!Pass"     // Con ! como carácter especial
    })
    @DisplayName("Debería validar contraseñas en el límite de los requisitos")
    void shouldValidateBorderlineCases(String password) {
        TestDTO dto = new TestDTO(password);
        Set<ConstraintViolation<TestDTO>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Test#123",        // # no está en la lista de caracteres especiales
            "Test_123A",       // _ no está en la lista de caracteres especiales
            "Test.123A",       // . no está en la lista de caracteres especiales
            "Test-123A",       // - no está en la lista de caracteres especiales
            "Test+123A"        // + no está en la lista de caracteres especiales
    })
    @DisplayName("Debería rechazar contraseñas con caracteres especiales no permitidos")
    void shouldRejectPasswordsWithInvalidSpecialCharacters(String password) {
        TestDTO dto = new TestDTO(password);
        Set<ConstraintViolation<TestDTO>> violations = validator.validate(dto);
        assertThat(violations)
                .isNotEmpty()
                .hasSize(1);
    }
}
