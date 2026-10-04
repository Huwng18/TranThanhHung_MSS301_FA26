package com.fudn.customerservice.controller;

import com.fudn.customerservice.dto.AdminCustomerRequest;
import com.fudn.customerservice.dto.ChangePasswordRequest;
import com.fudn.customerservice.dto.CustomerResponse;
import com.fudn.customerservice.dto.RegisterRequest;
import com.fudn.customerservice.dto.UpdateProfileRequest;
import com.fudn.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
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

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> searchCustomers(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(customerService.searchCustomers(keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody AdminCustomerRequest request) {
        return new ResponseEntity<>(customerService.createCustomer(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody AdminCustomerRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
