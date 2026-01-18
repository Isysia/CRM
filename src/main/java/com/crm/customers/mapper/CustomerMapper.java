package com.crm.customers.mapper;

import com.crm.customers.dto.CustomerRequestDTO;
import com.crm.customers.dto.CustomerResponseDTO;
import com.crm.customers.model.Customer;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Mapper for converting between Customer Entity and DTOs.
 * Manual mapping (no MapStruct) for simplicity.
 */
@Component
public class CustomerMapper {

    /**
     * Convert CustomerRequestDTO → Customer Entity
     */
    public Customer toEntity(CustomerRequestDTO dto) {
        return Customer.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .status(dto.getStatus())
                .build();
    }

    /**
     * Convert Customer Entity → CustomerResponseDTO
     */
    public CustomerResponseDTO toResponseDTO(Customer entity) {
        return CustomerResponseDTO.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Update existing Customer entity from DTO
     */
    public void updateEntityFromDTO(Customer entity, CustomerRequestDTO dto) {
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setStatus(dto.getStatus());
    }
}