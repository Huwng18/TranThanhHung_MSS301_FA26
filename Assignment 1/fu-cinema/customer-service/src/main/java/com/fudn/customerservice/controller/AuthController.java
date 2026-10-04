package com.fudn.customerservice.controller;

import com.fudn.customerservice.dto.LoginRequest;
import com.fudn.customerservice.dto.LoginResponse;
import com.fudn.customerservice.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final com.fudn.customerservice.service.CustomerService customerService;

    public AuthController(AuthService authService, com.fudn.customerservice.service.CustomerService customerService) {
        this.authService = authService;
        this.customerService = customerService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<com.fudn.customerservice.dto.CustomerResponse> register(@jakarta.validation.Valid @RequestBody com.fudn.customerservice.dto.RegisterRequest request) {
        return new ResponseEntity<>(customerService.register(request), org.springframework.http.HttpStatus.CREATED);
    }
}
