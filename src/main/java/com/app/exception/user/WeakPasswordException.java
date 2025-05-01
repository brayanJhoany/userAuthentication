package com.app.exception.user;

public class WeakPasswordException extends RuntimeException {
    public WeakPasswordException() {
        super("La contraseña no cumple con los requisitos de seguridad.");
    }
}
