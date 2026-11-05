package com.mss301.account.dto;

import jakarta.validation.constraints.NotBlank;

// [TODO-3.4] Missing refresh tokens are validation errors rather than server errors.
public record RefreshRequest(@NotBlank(message = "Refresh token is required") String refreshToken) {}
