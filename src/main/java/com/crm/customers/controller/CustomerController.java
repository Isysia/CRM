package com.crm.customers.controller;

import com.crm.customers.dto.CustomerRequestDTO;
import com.crm.customers.dto.CustomerResponseDTO;
import com.crm.customers.mapper.CustomerMapper;
import com.crm.customers.model.Customer;
import com.crm.customers.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Customer operations.
 * Now uses DTOs instead of exposing Entity directly.
 */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService service;
    private final CustomerMapper mapper;

    public CustomerController(CustomerService service, CustomerMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    /**
     * GET /api/customers
     * Returns all customers as DTOs
     */
    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> getAllCustomers() {
        List<CustomerResponseDTO> customers = service.getAllCustomers()
                .stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(customers);
    }

    /**
     * GET /api/customers/{id}
     * Returns a single customer by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomerById(@PathVariable Long id) {
        Customer customer = service.getCustomerById(id);
        return ResponseEntity.ok(mapper.toResponseDTO(customer));
    }

    /**
     * POST /api/customers
     * Creates a new customer
     * @Valid triggers Bean Validation
     */
    @PostMapping
    public ResponseEntity<CustomerResponseDTO> createCustomer(
            @Valid @RequestBody CustomerRequestDTO requestDTO
    ) {
        Customer customer = mapper.toEntity(requestDTO);
        Customer created = service.createCustomer(customer);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapper.toResponseDTO(created));
    }

    /**
     * PUT /api/customers/{id}
     * Updates an existing customer
     */
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequestDTO requestDTO
    ) {
        Customer customerData = mapper.toEntity(requestDTO);
        Customer updated = service.updateCustomer(id, customerData);

        return ResponseEntity.ok(mapper.toResponseDTO(updated));
    }

    /**
     * DELETE /api/customers/{id}
     * Deletes a customer
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        service.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}