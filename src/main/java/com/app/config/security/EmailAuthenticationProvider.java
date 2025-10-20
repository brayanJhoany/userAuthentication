package com.app.config.security;

import com.app.service.UserDetailServiceImp;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Proveedor de autenticación personalizado para EmailAuthenticationToken.
 *
 * Este proveedor maneja la autenticación basada en email y password,
 * validando las credenciales contra la base de datos.
 */
@Component
@RequiredArgsConstructor
public class EmailAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailServiceImp userDetailService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Realiza la autenticación del usuario verificando email y password.
     *
     * @param authentication Token de autenticación no autenticado con email y password
     * @return Token de autenticación autenticado con authorities
     * @throws AuthenticationException Si las credenciales son inválidas
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName(); // getPrincipal() como String
        String password = (String) authentication.getCredentials();

        UserDetails userDetails = userDetailService.loadUserByEmail(email);
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
        return new EmailAuthenticationToken(email, userDetails.getAuthorities());
    }

    /**
     * Indica que este proveedor soporta EmailAuthenticationToken.
     *
     * @param authentication Clase de autenticación a verificar
     * @return true si es EmailAuthenticationToken
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return EmailAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
