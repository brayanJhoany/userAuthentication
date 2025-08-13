package com.app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthRequestDTO {
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be well-formed")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 255, message = "Password must be between 6 and 255 characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
}
