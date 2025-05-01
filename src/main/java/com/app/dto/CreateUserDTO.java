package com.app.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    private String password;

    @NotNull(message = "La edad es obligatoria")
    private Integer age;

}
