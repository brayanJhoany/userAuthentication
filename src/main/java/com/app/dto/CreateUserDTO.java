package com.app.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password")
public class CreateUserDTO {
    @Email(message = "{createUser.email.email}")
    @NotBlank(message = "{createUser.email.notBlank}")
    @Size(min = 4, max = 255, message = "{createUser.email.size}")
    private String email;

    @NotBlank(message = "{createUser.username.notBlank}")
    @Size(min = 4, max = 255, message = "{createUser.username.size}")
    private String username;

    @NotBlank(message = "{createUser.password.notBlank}")
    @Size(min = 6, message = "{createUser.password.size}")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotNull(message = "{createUser.age.notNull}")
    @Min(value = 1, message = "{createUser.age.min}")
    @Max(value = 130, message = "{createUser.age.max}")
    private Integer age;

}
