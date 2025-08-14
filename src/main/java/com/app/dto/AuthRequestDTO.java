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
    @NotBlank(message = "{authRequest.email.notBlank}")
    @Email(message = "{authRequest.email.email}")
    private String email;

    @NotBlank(message = "{authRequest.password.notBlank}")
    @Size(min = 6, max = 255, message = "{authRequest.password.size}")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
}
