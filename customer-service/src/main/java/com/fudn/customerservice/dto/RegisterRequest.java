package com.fudn.customerservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.Data;
import java.time.LocalDate;

@Data
public class RegisterRequest {
    @NotBlank(message = "Name is required")
    private String customerName;

    @NotBlank(message = "Telephone is required")
    private String telephone;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Past(message = "Birthday must be in the past")
    private LocalDate customerBirthday;

    @NotBlank(message = "Password is required")
    private String password;
}
