package com.crm.customers.service.impl;

import com.crm.customers.dto.CustomerRequestDTO;
import com.crm.customers.dto.CustomerResponseDTO;
import com.crm.customers.exceptions.CustomerNotFoundException;
import com.crm.customers.exceptions.DuplicateResourceException;
import com.crm.customers.mapper.CustomerMapper;
import com.crm.customers.model.Customer;
import com.crm.customers.repository.CustomerRepository;
import com.crm.customers.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    @Override
    public CustomerResponseDTO createCustomer(CustomerRequestDTO requestDTO) {
        log.info("Creating new customer with email: {}", requestDTO.getEmail());

        // Check if email already exists
        if (repository.findByEmail(requestDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Customer", "email", requestDTO.getEmail());
        }

        // Convert DTO to Entity
        Customer customer = mapper.toEntity(requestDTO);

        // Save to database
        Customer savedCustomer = repository.save(customer);

        log.info("Customer created successfully with id: {}", savedCustomer.getId());

        // Convert Entity back to DTO
        return mapper.toResponseDTO(savedCustomer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponseDTO getCustomerById(Long id) {
        log.info("Fetching customer with id: {}", id);

        Customer customer = repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        return mapper.toResponseDTO(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponseDTO> getAllCustomers() {
        log.info("Fetching all customers");

        return repository.findAll().stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO requestDTO) {
        log.info("Updating customer with id: {}", id);

        // Find existing customer
        Customer existing = repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        // Check email uniqueness (if email is being changed)
        repository.findByEmail(requestDTO.getEmail())
                .filter(c -> !c.getId().equals(id))
                .ifPresent(c -> {
                    throw new DuplicateResourceException("Customer", "email", requestDTO.getEmail());
                });

        // Update fields
        existing.setFirstName(requestDTO.getFirstName());
        existing.setLastName(requestDTO.getLastName());
        existing.setEmail(requestDTO.getEmail());
        existing.setPhone(requestDTO.getPhone());
        existing.setStatus(requestDTO.getStatus());

        // Save updated customer
        Customer updatedCustomer = repository.save(existing);

        log.info("Customer updated successfully with id: {}", updatedCustomer.getId());

        return mapper.toResponseDTO(updatedCustomer);
    }

    @Override
    public void deleteCustomer(Long id) {
        log.info("Deleting customer with id: {}", id);

        if (!repository.existsById(id)) {
            throw new CustomerNotFoundException(id);
        }

        repository.deleteById(id);

        log.info("Customer deleted successfully with id: {}", id);
    }
}