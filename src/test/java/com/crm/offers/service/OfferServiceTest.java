package com.crm.offers.service;

import com.crm.customers.exceptions.CustomerNotFoundException;
import com.crm.customers.model.Customer;
import com.crm.customers.model.CustomerStatus;
import com.crm.customers.repository.CustomerRepository;
import com.crm.offers.exceptions.OfferNotFoundException;
import com.crm.offers.model.Offer;
import com.crm.offers.model.OfferStatus;
import com.crm.offers.repository.OfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferServiceTest {

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private OfferService offerService;

    private Customer customer;
    private Offer offer;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .status(CustomerStatus.ACTIVE)
                .build();

        offer = Offer.builder()
                .id(1L)
                .title("Website Development")
                .description("Full-stack web application")
                .price(new BigDecimal("5000.00"))
                .status(OfferStatus.DRAFT)
                .customer(customer)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllOffers_Success() {
        // Given
        List<Offer> offers = Arrays.asList(offer);
        when(offerRepository.findAll()).thenReturn(offers);

        // When
        List<Offer> result = offerService.getAllOffers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Website Development", result.get(0).getTitle());
        verify(offerRepository, times(1)).findAll();
    }

    @Test
    void getOfferById_Success() {
        // Given
        when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));

        // When
        Offer result = offerService.getOfferById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Website Development", result.getTitle());
        verify(offerRepository, times(1)).findById(1L);
    }

    @Test
    void getOfferById_NotFound() {
        // Given
        when(offerRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OfferNotFoundException.class,
                () -> offerService.getOfferById(999L));
    }

    @Test
    void getOffersByCustomerId_Success() {
        // Given
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(offerRepository.findByCustomerId(1L)).thenReturn(Arrays.asList(offer));

        // When
        List<Offer> result = offerService.getOffersByCustomerId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getCustomer().getId());
        verify(offerRepository, times(1)).findByCustomerId(1L);
    }

    @Test
    void getOffersByCustomerId_CustomerNotFound() {
        // Given
        when(customerRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(CustomerNotFoundException.class,
                () -> offerService.getOffersByCustomerId(999L));
    }

    @Test
    void createOffer_Success() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(offerRepository.save(any(Offer.class))).thenReturn(offer);

        // When
        Offer result = offerService.createOffer(offer, 1L);

        // Then
        assertNotNull(result);
        assertEquals("Website Development", result.getTitle());
        assertEquals(customer, result.getCustomer());
        verify(offerRepository, times(1)).save(any(Offer.class));
    }

    @Test
    void createOffer_CustomerNotFound() {
        // Given
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CustomerNotFoundException.class,
                () -> offerService.createOffer(offer, 999L));
        verify(offerRepository, never()).save(any(Offer.class));
    }

    @Test
    void updateOffer_Success() {
        // Given
        Offer updatedData = Offer.builder()
                .title("Mobile App Development")
                .description("iOS and Android app")
                .price(new BigDecimal("8000.00"))
                .status(OfferStatus.SENT)
                .build();

        when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));
        when(offerRepository.save(any(Offer.class))).thenReturn(offer);

        // When
        Offer result = offerService.updateOffer(1L, updatedData, 1L);

        // Then
        assertNotNull(result);
        verify(offerRepository, times(1)).save(any(Offer.class)); // ✅ ВИПРАВЛЕНО: Offer.class замість Customer.class
    }

    @Test
    void updateOffer_NotFound() {
        // Given
        Offer updatedData = Offer.builder()
                .title("Mobile App Development")
                .build();

        when(offerRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OfferNotFoundException.class,
                () -> offerService.updateOffer(999L, updatedData, 1L));
    }

    @Test
    void updateOffer_NewCustomerNotFound() {
        // Given
        Offer updatedData = Offer.builder()
                .title("Mobile App Development")
                .build();

        Customer differentCustomer = Customer.builder()
                .id(2L)
                .build();

        offer.setCustomer(differentCustomer);

        when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CustomerNotFoundException.class,
                () -> offerService.updateOffer(1L, updatedData, 999L));
    }

    @Test
    void deleteOffer_Success() {
        // Given
        when(offerRepository.existsById(1L)).thenReturn(true);
        doNothing().when(offerRepository).deleteById(1L);

        // When
        offerService.deleteOffer(1L);

        // Then
        verify(offerRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteOffer_NotFound() {
        // Given
        when(offerRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(OfferNotFoundException.class,
                () -> offerService.deleteOffer(999L));
    }
}