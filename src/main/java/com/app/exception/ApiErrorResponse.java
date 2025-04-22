package com.app.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiErrorResponse {
    private String code;        // → "USER_NOT_FOUND"
    private String message;     // → "Usuario no encontrado"
    private int    status;      // → 404
    private String error;       // → "Not Found"
    private String timestamp;
}