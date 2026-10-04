package com.fudn.customerservice.controller;

import com.fudn.customerservice.dto.ChangePasswordRequest;
import com.fudn.customerservice.dto.CustomerResponse;
import com.fudn.customerservice.dto.RegisterRequest;
import com.fudn.customerservice.dto.UpdateProfileRequest;
import com.fudn.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/register")
    public ResponseEntity<CustomerResponse> register(@Valid @RequestBody RegisterRequest request) {
        return new ResponseEntity<>(customerService.register(request), HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> getProfile(@RequestHeader("X-User-Id") Long customerId) {
        return ResponseEntity.ok(customerService.getProfile(customerId));
    }

    @PutMapping("/me")
    public ResponseEntity<CustomerResponse> updateProfile(
            @RequestHeader("X-User-Id") Long customerId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(customerService.updateProfile(customerId, request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @RequestHeader("X-User-Id") Long customerId,
            @Valid @RequestBody ChangePasswordRequest request) {
        customerService.changePassword(customerId, request);
        return ResponseEntity.ok().build();
    }
}
