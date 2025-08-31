package com.app.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // --- Auth ---
    INVALID_CREDENTIALS("Credenciales incorrectas"),
    EMAIL_ALREADY_EXISTS("El email ya está registrado"),
    UNAUTHORIZED("No autorizado"),
    DISABLED_ACCOUNT("La cuenta está deshabilitada"),
    // --- Users ---
    USER_NOT_FOUND("Usuario no encontrado"),
    WEAK_PASSWORD("La contraseña no cumple con los requisitos de seguridad"),

    // --- Genéricos ---
    VALIDATION_ERROR("Validación fallida"),
    UNEXPECTED_ERROR("Error inesperado");

    private final String defaultMessage;

    ErrorCode(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }
    public String getMessage() {
        return this.defaultMessage;
    }
}
