package com.crm.offers.mapper;

import com.crm.customers.model.Customer;
import com.crm.offers.dto.OfferRequestDTO;
import com.crm.offers.dto.OfferResponseDTO;
import com.crm.offers.model.Offer;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Offer Entity and DTOs.
 */
@Component
public class OfferMapper {

    /**
     * Convert OfferRequestDTO → Offer Entity
     * Note: Customer must be set separately in Service
     */
    public Offer toEntity(OfferRequestDTO dto) {
        return Offer.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .status(dto.getStatus())
                .build();
    }

    /**
     * Convert Offer Entity → OfferResponseDTO
     */
    public OfferResponseDTO toResponseDTO(Offer entity) {
        Customer customer = entity.getCustomer();

        return OfferResponseDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .status(entity.getStatus())
                .customerId(customer.getId())
                .customerName(customer.getFirstName() + " " + customer.getLastName())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Update existing Offer entity from DTO
     */
    public void updateEntityFromDTO(Offer entity, OfferRequestDTO dto) {
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setStatus(dto.getStatus());
    }
}