package com.mss301.account.controller;

import com.mss301.account.dto.*;
import com.mss301.account.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

// [TODO-3.4] Public login, token refresh, and stateless logout endpoints.
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login and receive access and refresh tokens")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request), "Login successful");
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.success(authService.refresh(request.refreshToken()), "Token refreshed");
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout: the client must discard its tokens")
    public ApiResponse<Void> logout() {
        return ApiResponse.success(null, "Logged out successfully");
    }
}
