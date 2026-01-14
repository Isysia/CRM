package com.crm.customers.controller;

import com.crm.customers.dto.CustomerRequestDTO;
import com.crm.customers.model.Customer;
import com.crm.customers.model.CustomerStatus;
import com.crm.customers.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
    }

    @Test
    void getAllCustomers_ReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAllCustomers_ReturnsListOfCustomers() throws Exception {
        // Given
        Customer customer = Customer.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .status(CustomerStatus.ACTIVE)
                .build();
        customerRepository.save(customer);

        // When & Then
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].email").value("john@example.com"));
    }

    @Test
    void getCustomerById_ExistingId_ReturnsCustomer() throws Exception {
        // Given
        Customer customer = Customer.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .status(CustomerStatus.ACTIVE)
                .build();
        Customer saved = customerRepository.save(customer);

        // When & Then
        mockMvc.perform(get("/api/customers/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void getCustomerById_NonExistingId_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/customers/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found with id: 999"));
    }

    @Test
    void createCustomer_ValidInput_ReturnsCreated() throws Exception {
        // Given
        CustomerRequestDTO requestDTO = CustomerRequestDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .status(CustomerStatus.ACTIVE)
                .build();

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void createCustomer_InvalidEmail_ReturnsBadRequest() throws Exception {
        // Given
        CustomerRequestDTO invalidDTO = CustomerRequestDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("invalid-email")
                .phone("+1234567890")
                .status(CustomerStatus.ACTIVE)
                .build();

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void createCustomer_MissingFirstName_ReturnsBadRequest() throws Exception {
        // Given
        CustomerRequestDTO invalidDTO = CustomerRequestDTO.builder()
                .lastName("Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .status(CustomerStatus.ACTIVE)
                .build();

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCustomer_DuplicateEmail_ReturnsConflict() throws Exception {
        // Given - existing customer
        Customer existing = Customer.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("john@example.com")
                .phone("+9876543210")
                .status(CustomerStatus.ACTIVE)
                .build();
        customerRepository.save(existing);

        CustomerRequestDTO requestDTO = CustomerRequestDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com") // duplicate
                .phone("+1234567890")
                .status(CustomerStatus.ACTIVE)
                .build();

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void updateCustomer_ValidInput_ReturnsUpdated() throws Exception {
        // Given - existing customer
        Customer existing = Customer.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .status(CustomerStatus.ACTIVE)
                .build();
        Customer saved = customerRepository.save(existing);

        CustomerRequestDTO updateDTO = CustomerRequestDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .phone("+9876543210")
                .status(CustomerStatus.INACTIVE)
                .build();

        // When & Then
        mockMvc.perform(put("/api/customers/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void updateCustomer_NonExistingId_ReturnsNotFound() throws Exception {
        // Given
        CustomerRequestDTO updateDTO = CustomerRequestDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .phone("+9876543210")
                .status(CustomerStatus.ACTIVE)
                .build();

        // When & Then
        mockMvc.perform(put("/api/customers/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCustomer_ExistingId_ReturnsNoContent() throws Exception {
        // Given
        Customer customer = Customer.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .status(CustomerStatus.ACTIVE)
                .build();
        Customer saved = customerRepository.save(customer);

        // When & Then
        mockMvc.perform(delete("/api/customers/" + saved.getId()))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/customers/" + saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCustomer_NonExistingId_ReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/customers/999"))
                .andExpect(status().isNotFound());
    }
}