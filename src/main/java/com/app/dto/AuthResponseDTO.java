package com.app.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponseDTO {
    private final String token;
    private final UserResponseDTO user;
}
