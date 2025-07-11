package com.app.dto;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserDTO {
    @Email
    @NotBlank
    @Size(min = 4, max = 255)
    private String email;

    @NotBlank
    @Size(min = 4, max = 255)
    private String username;

    @NotBlank
    @Size(min = 6)
    private String password;

    @NotNull(message = "La edad es obligatoria")
    @Min(1)
    @Max(130)
    private Integer age;

}
