package com.mss301.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// [TODO-3.3] Credentials passed to the authentication service.
public record LoginRequest(
    @NotBlank(message = "Username is required") @Size(max = 20) String username,
    @NotBlank(message = "Password is required") String password
) {}
