package com.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class UpdateUserDTO {
    @Email
    private String email;
    @Size(min=2, max=50)
    private String username;
    private Integer age;
    private String password;
}
