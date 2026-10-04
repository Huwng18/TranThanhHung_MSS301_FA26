package com.fudn.customerservice.dto;

import com.fudn.customerservice.model.CustomerStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class CustomerResponse {
    private Long id;
    private String customerName;
    private String email;
    private String telephone;
    private LocalDate customerBirthday;
    private CustomerStatus status;
}
