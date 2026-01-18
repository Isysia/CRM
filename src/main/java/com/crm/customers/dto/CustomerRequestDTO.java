package com.crm.customers.dto;

import com.crm.customers.model.CustomerStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a Customer.
 * Used in POST /api/customers and PUT /api/customers/{id}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRequestDTO {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @Pattern(
            regexp = "^\\+?[0-9\\s]{10,20}$",
            message = "Phone must be 10-20 characters (digits and spaces), optionally starting with +"
    )
    private String phone;

    @NotNull(message = "Status is required")
    private CustomerStatus status;
}