package com.crm.customers.dto;

import com.crm.customers.model.CustomerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;

/**
 * DTO for returning Customer data to clients.
 * Used in GET responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponseDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private CustomerStatus status;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}