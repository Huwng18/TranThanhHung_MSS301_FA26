package com.fudn.customerservice.service;

import com.fudn.customerservice.dto.LoginRequest;
import com.fudn.customerservice.dto.LoginResponse;
import com.fudn.customerservice.exception.ApiException;
import com.fudn.customerservice.model.Customer;
import com.fudn.customerservice.model.CustomerStatus;
import com.fudn.customerservice.repository.CustomerRepository;
import com.fudn.customerservice.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        if (adminEmail.equals(request.getEmail())) {
            if (adminPassword.equals(request.getPassword())) {
                String token = jwtService.generateToken(0L, adminEmail, "ROLE_ADMIN");
                return new LoginResponse(token);
            }
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid admin credentials");
        }

        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        if (customer.getStatus() == CustomerStatus.INACTIVE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Customer is inactive");
        }

        String token = jwtService.generateToken(customer.getId(), customer.getEmail(), "ROLE_CUSTOMER");
        return new LoginResponse(token);
    }
}
