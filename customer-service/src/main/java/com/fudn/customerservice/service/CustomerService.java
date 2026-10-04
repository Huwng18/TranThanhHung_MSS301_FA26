package com.fudn.customerservice.service;

import com.fudn.customerservice.dto.ChangePasswordRequest;
import com.fudn.customerservice.dto.CustomerResponse;
import com.fudn.customerservice.dto.RegisterRequest;
import com.fudn.customerservice.dto.UpdateProfileRequest;
import com.fudn.customerservice.exception.ApiException;
import com.fudn.customerservice.model.Customer;
import com.fudn.customerservice.model.CustomerStatus;
import com.fudn.customerservice.repository.CustomerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CustomerResponse register(RegisterRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already in use");
        }
        
        Customer customer = Customer.builder()
                .customerName(request.getCustomerName())
                .telephone(request.getTelephone())
                .email(request.getEmail())
                .customerBirthday(request.getCustomerBirthday())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(CustomerStatus.ACTIVE)
                .build();
                
        customer = customerRepository.save(customer);
        return mapToResponse(customer);
    }

    public CustomerResponse getProfile(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Customer not found"));
        return mapToResponse(customer);
    }

    @Transactional
    public CustomerResponse updateProfile(Long customerId, UpdateProfileRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Customer not found"));
                
        customer.setCustomerName(request.getCustomerName());
        customer.setTelephone(request.getTelephone());
        customer.setCustomerBirthday(request.getCustomerBirthday());
        
        customer = customerRepository.save(customer);
        return mapToResponse(customer);
    }

    @Transactional
    public void changePassword(Long customerId, ChangePasswordRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Customer not found"));
                
        if (!passwordEncoder.matches(request.getOldPassword(), customer.getPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Incorrect old password");
        }
        
        customer.setPassword(passwordEncoder.encode(request.getNewPassword()));
        customerRepository.save(customer);
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .customerName(customer.getCustomerName())
                .email(customer.getEmail())
                .telephone(customer.getTelephone())
                .customerBirthday(customer.getCustomerBirthday())
                .status(customer.getStatus())
                .build();
    }
}
