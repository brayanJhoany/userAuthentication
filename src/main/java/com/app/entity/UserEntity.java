package com.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @NotBlank
    @Size(min = 4, max = 255)
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    @Size(min = 4, max = 255)
    private String username;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @NotNull
    private Integer age;

    @Column(nullable = false)
    private boolean enabled = true;
}
