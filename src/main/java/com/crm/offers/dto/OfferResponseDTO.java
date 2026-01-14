package com.crm.offers.dto;

import com.crm.offers.model.OfferStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for returning Offer data to clients.
 * Used in GET responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferResponseDTO {

    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private OfferStatus status;
    private Long customerId;
    private String customerName; // firstName + lastName
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}