package com.app.dto;


import com.app.validation.StrongPassword;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password")
public class CreateUserDTO {
    @Email
    @NotBlank
    @Size(min = 4, max = 255)
    private String email;

    @NotBlank
    @Size(min = 4, max = 255)
    private String username;

    @NotBlank
    @StrongPassword
    @Size(min = 8, max = 255)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotNull
    @Min(1)
    @Max(130)
    private Integer age;

}
