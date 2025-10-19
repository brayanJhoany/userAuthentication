package com.app.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validador personalizado para contraseñas fuertes.
 *
 * Verifica que la contraseña cumpla con los siguientes criterios:
 * - Mínimo 8 caracteres de longitud
 * - Al menos una letra mayúscula (A-Z)
 * - Al menos una letra minúscula (a-z)
 * - Al menos un dígito (0-9)
 * - Al menos un carácter especial (@$!%*?&)
 */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    // Expresión regular que valida:
    // - Al menos 8 caracteres
    // - Al menos una mayúscula
    // - Al menos una minúscula
    // - Al menos un número
    // - Al menos un carácter especial de: @$!%*?&
    private static final String PASSWORD_PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    @Override
    public void initialize(StrongPassword constraintAnnotation) {
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return true;
        }
        return pattern.matcher(password).matches();
    }
}
