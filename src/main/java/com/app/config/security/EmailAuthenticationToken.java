package com.app.config.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Token de autenticación personalizado para autenticación basada en email.
 *
 * Esta clase representa una autenticación basada en email y password
 *
 * El principal (getPrincipal()) siempre retorna el email del usuario.
 */
public class EmailAuthenticationToken extends AbstractAuthenticationToken {

    private final String email;
    private Object credentials;

    /**
     * Constructor para crear un token NO autenticado (antes de la autenticación).
     * Se usa típicamente durante el proceso de login con email y password.
     *
     * @param email El email del usuario
     * @param password La contraseña del usuario (será eliminada después de la autenticación)
     */
    public EmailAuthenticationToken(String email, String password) {
        super(null);
        this.email = email;
        this.credentials = password;
        setAuthenticated(false);
    }

    /**
     * Constructor para crear un token AUTENTICADO (después de la autenticación exitosa).
     * Se usa típicamente después de validar el JWT o credenciales.
     *
     * @param email El email del usuario autenticado
     * @param authorities Los roles/permisos del usuario
     */
    public EmailAuthenticationToken(String email, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.email = email;
        this.credentials = null;
        setAuthenticated(true);
    }

    /**
     * Retorna el email del usuario.
     *
     * @return El email como objeto principal de la autenticación
     */
    @Override
    public Object getPrincipal() {
        return email;
    }

    /**
     * Retorna las credenciales (password).
     * Será null para tokens autenticados por seguridad.
     *
     * @return Las credenciales o null si ya está autenticado
     */
    @Override
    public Object getCredentials() {
        return credentials;
    }

    /**
     * Retorna el email del usuario de forma tipada.
     *
     * @return El email del usuario
     */
    public String getEmail() {
        return email;
    }

    /**
     * Elimina las credenciales del token por seguridad.
     * Se debe llamar después de una autenticación exitosa.
     */
    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.credentials = null;
    }
}
