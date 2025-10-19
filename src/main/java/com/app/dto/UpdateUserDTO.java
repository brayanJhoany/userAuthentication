package com.app.dto;

import com.app.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString(exclude = "password")
@EqualsAndHashCode(exclude = "password")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserDTO {
    @Email(message = "{email.invalid}")
    @Size(max = 254, message = "{email.size}")
    private String email;

    @Size(min = 2, max = 50, message = "{username.size}")
    private String username;

    @Min(value = 18, message = "{age.min}")
    @Max(value = 120, message = "{age.max}")
    private Integer age;

    @StrongPassword
    @Size(min = 8, max = 72, message = "{password.size}")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
}
