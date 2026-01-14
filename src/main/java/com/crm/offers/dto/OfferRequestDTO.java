package com.crm.offers.dto;

import com.crm.offers.model.OfferStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for creating or updating an Offer.
 * Used in POST /api/offers and PUT /api/offers/{id}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferRequestDTO {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Status is required")
    private OfferStatus status;

    @NotNull(message = "Customer ID is required")
    private Long customerId;
}