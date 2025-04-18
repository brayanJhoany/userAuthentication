package com.app.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

/**
 * Clase utilitaria para la generación, validación y análisis de tokens JWT.
 */
@Component
@Slf4j
public class JwtUtils {

    @Value("${jwt.secret_key}")
    private String secretKey;

    @Value("${jwt.expiration.time}")
    private String timeExpiration;

    /**
     * Genera un token JWT para el usuario proporcionado.
     *
     * @param username nombre del usuario autenticado
     * @return token JWT firmado con clave secreta y expiración configurada
     */
    public String generateAccessToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + Long.parseLong(timeExpiration)))
                .signWith(getSignatureKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Valida si un token JWT es válido y no ha expirado.
     *
     * @param token token JWT a validar
     * @return true si el token es válido, false en caso contrario
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignatureKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return true;
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extrae el nombre de usuario (subject) desde un token JWT.
     *
     * @param token token JWT
     * @return nombre de usuario contenido en el token
     */
    public String getUsernameFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    /**
     * Extrae un valor específico (claim) del token JWT usando una función lambda.
     *
     * @param token           token JWT
     * @param claimsResolver  función para obtener un claim específico
     * @param <T>             tipo del claim devuelto
     * @return valor del claim solicitado
     */
    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todos los claims del token JWT.
     *
     * @param token token JWT
     * @return objeto Claims con toda la información contenida en el token
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignatureKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Decodifica la clave secreta en formato Base64 y genera la clave de firma.
     *
     * @return clave criptográfica HMAC-SHA usada para firmar tokens
     */
    public Key getSignatureKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
