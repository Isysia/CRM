package com.crm.offers.controller;

import com.crm.customers.model.Customer;
import com.crm.customers.model.CustomerStatus;
import com.crm.customers.repository.CustomerRepository;
import com.crm.offers.dto.OfferRequestDTO;
import com.crm.offers.model.Offer;
import com.crm.offers.model.OfferStatus;
import com.crm.offers.repository.OfferRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OfferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer;

    @BeforeEach
    void setUp() {
        offerRepository.deleteAll();
        customerRepository.deleteAll();

        // Create a customer first (offers need customers)
        customer = Customer.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .status(CustomerStatus.ACTIVE)
                .build();
        customer = customerRepository.save(customer);
    }

    @Test
    void getAllOffers_ReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/offers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAllOffers_ReturnsListOfOffers() throws Exception {
        // Given
        Offer offer = Offer.builder()
                .title("Website Development")
                .description("Full-stack web application")
                .price(new BigDecimal("5000.00"))
                .status(OfferStatus.DRAFT)
                .customer(customer)
                .build();
        offerRepository.save(offer);

        // When & Then
        mockMvc.perform(get("/api/offers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Website Development"))
                .andExpect(jsonPath("$[0].price").value(5000.00))
                .andExpect(jsonPath("$[0].customerName").value("John Doe"));
    }

    @Test
    void getOfferById_ExistingId_ReturnsOffer() throws Exception {
        // Given
        Offer offer = Offer.builder()
                .title("Website Development")
                .description("Full-stack web application")
                .price(new BigDecimal("5000.00"))
                .status(OfferStatus.DRAFT)
                .customer(customer)
                .build();
        Offer saved = offerRepository.save(offer);

        // When & Then
        mockMvc.perform(get("/api/offers/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.title").value("Website Development"))
                .andExpect(jsonPath("$.customerId").value(customer.getId()));
    }

    @Test
    void getOfferById_NonExistingId_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/offers/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Offer not found with id: 999"));
    }

    @Test
    void getOffersByCustomerId_ReturnsOffers() throws Exception {
        // Given
        Offer offer1 = Offer.builder()
                .title("Offer 1")
                .description("Description 1")
                .price(new BigDecimal("1000.00"))
                .status(OfferStatus.DRAFT)
                .customer(customer)
                .build();

        Offer offer2 = Offer.builder()
                .title("Offer 2")
                .description("Description 2")
                .price(new BigDecimal("2000.00"))
                .status(OfferStatus.SENT)
                .customer(customer)
                .build();

        offerRepository.save(offer1);
        offerRepository.save(offer2);

        // When & Then
        mockMvc.perform(get("/api/offers/customer/" + customer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].customerId").value(customer.getId()))
                .andExpect(jsonPath("$[1].customerId").value(customer.getId()));
    }

    @Test
    void getOffersByCustomerId_CustomerNotFound_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/offers/customer/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createOffer_ValidInput_ReturnsCreated() throws Exception {
        // Given
        OfferRequestDTO requestDTO = OfferRequestDTO.builder()
                .title("Website Development")
                .description("Full-stack web application")
                .price(new BigDecimal("5000.00"))
                .status(OfferStatus.DRAFT)
                .customerId(customer.getId())
                .build();

        // When & Then
        mockMvc.perform(post("/api/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Website Development"))
                .andExpect(jsonPath("$.price").value(5000.00))
                .andExpect(jsonPath("$.customerId").value(customer.getId()));
    }

    @Test
    void createOffer_InvalidTitle_ReturnsBadRequest() throws Exception {
        // Given
        OfferRequestDTO invalidDTO = OfferRequestDTO.builder()
                .title("AB") // Too short
                .description("Description")
                .price(new BigDecimal("5000.00"))
                .status(OfferStatus.DRAFT)
                .customerId(customer.getId())
                .build();

        // When & Then
        mockMvc.perform(post("/api/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void createOffer_NegativePrice_ReturnsBadRequest() throws Exception {
        // Given
        OfferRequestDTO invalidDTO = OfferRequestDTO.builder()
                .title("Website Development")
                .description("Description")
                .price(new BigDecimal("-100.00")) // Negative price
                .status(OfferStatus.DRAFT)
                .customerId(customer.getId())
                .build();

        // When & Then
        mockMvc.perform(post("/api/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOffer_CustomerNotFound_ReturnsNotFound() throws Exception {
        // Given
        OfferRequestDTO requestDTO = OfferRequestDTO.builder()
                .title("Website Development")
                .description("Description")
                .price(new BigDecimal("5000.00"))
                .status(OfferStatus.DRAFT)
                .customerId(999L) // Non-existent customer
                .build();

        // When & Then
        mockMvc.perform(post("/api/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found with id: 999"));
    }

    @Test
    void updateOffer_ValidInput_ReturnsUpdated() throws Exception {
        // Given - existing offer
        Offer existing = Offer.builder()
                .title("Old Title")
                .description("Old Description")
                .price(new BigDecimal("3000.00"))
                .status(OfferStatus.DRAFT)
                .customer(customer)
                .build();
        Offer saved = offerRepository.save(existing);

        OfferRequestDTO updateDTO = OfferRequestDTO.builder()
                .title("Updated Title")
                .description("Updated Description")
                .price(new BigDecimal("6000.00"))
                .status(OfferStatus.SENT)
                .customerId(customer.getId())
                .build();

        // When & Then
        mockMvc.perform(put("/api/offers/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.price").value(6000.00))
                .andExpect(jsonPath("$.status").value("SENT"));
    }

    @Test
    void updateOffer_NonExistingId_ReturnsNotFound() throws Exception {
        // Given
        OfferRequestDTO updateDTO = OfferRequestDTO.builder()
                .title("Updated Title")
                .description("Updated Description")
                .price(new BigDecimal("6000.00"))
                .status(OfferStatus.SENT)
                .customerId(customer.getId())
                .build();

        // When & Then
        mockMvc.perform(put("/api/offers/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteOffer_ExistingId_ReturnsNoContent() throws Exception {
        // Given
        Offer offer = Offer.builder()
                .title("Website Development")
                .description("Description")
                .price(new BigDecimal("5000.00"))
                .status(OfferStatus.DRAFT)
                .customer(customer)
                .build();
        Offer saved = offerRepository.save(offer);

        // When & Then
        mockMvc.perform(delete("/api/offers/" + saved.getId()))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/offers/" + saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteOffer_NonExistingId_ReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/offers/999"))
                .andExpect(status().isNotFound());
    }
}