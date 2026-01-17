package com.crm.offers.service;

import com.crm.customers.exceptions.CustomerNotFoundException;
import com.crm.customers.model.Customer;
import com.crm.customers.model.CustomerStatus;
import com.crm.customers.repository.CustomerRepository;
import com.crm.offers.dto.OfferRequestDTO;
import com.crm.offers.dto.OfferResponseDTO;
import com.crm.offers.exceptions.OfferNotFoundException;
import com.crm.offers.mapper.OfferMapper;
import com.crm.offers.model.Offer;
import com.crm.offers.model.OfferStatus;
import com.crm.offers.repository.OfferRepository;
import com.crm.offers.service.impl.OfferServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    @Mock
    private OfferMapper offerMapper;

    @InjectMocks
    private OfferServiceImpl offerService;

    private Customer customer;
    private Offer offer;
    private OfferRequestDTO requestDTO;
    private OfferResponseDTO responseDTO;

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

        requestDTO = new OfferRequestDTO();
        requestDTO.setTitle("Website Development");
        requestDTO.setDescription("Full-stack web application");
        requestDTO.setPrice(new BigDecimal("5000.00"));
        requestDTO.setStatus(OfferStatus.DRAFT);
        requestDTO.setCustomerId(1L);

        responseDTO = OfferResponseDTO.builder()
                .id(1L)
                .title("Website Development")
                .description("Full-stack web application")
                .price(new BigDecimal("5000.00"))
                .status(OfferStatus.DRAFT)
                .customerId(1L)
                .customerName("John Doe")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should get all offers successfully")
    void getAllOffers_Success() {
        // Given
        when(offerRepository.findAll()).thenReturn(Arrays.asList(offer));
        when(offerMapper.toResponseDTO(offer)).thenReturn(responseDTO);

        // When
        List<OfferResponseDTO> result = offerService.getAllOffers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Website Development", result.get(0).getTitle());
        verify(offerRepository, times(1)).findAll();
        verify(offerMapper, times(1)).toResponseDTO(offer);
    }

    @Test
    @DisplayName("Should get offer by id successfully")
    void getOfferById_Success() {
        // Given
        when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));
        when(offerMapper.toResponseDTO(offer)).thenReturn(responseDTO);

        // When
        OfferResponseDTO result = offerService.getOfferById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Website Development", result.getTitle());
        verify(offerRepository, times(1)).findById(1L);
        verify(offerMapper, times(1)).toResponseDTO(offer);
    }

    @Test
    @DisplayName("Should throw exception when offer not found by id")
    void getOfferById_NotFound() {
        // Given
        when(offerRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OfferNotFoundException.class,
                () -> offerService.getOfferById(999L));
        verify(offerMapper, never()).toResponseDTO(any());
    }

    @Test
    @DisplayName("Should get offers by customer id successfully")
    void getOffersByCustomerId_Success() {
        // Given
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(offerRepository.findByCustomerId(1L)).thenReturn(Arrays.asList(offer));
        when(offerMapper.toResponseDTO(offer)).thenReturn(responseDTO);

        // When
        List<OfferResponseDTO> result = offerService.getOffersByCustomerId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getCustomerId());
        verify(offerRepository, times(1)).findByCustomerId(1L);
    }

    @Test
    @DisplayName("Should throw exception when customer not found")
    void getOffersByCustomerId_CustomerNotFound() {
        // Given
        when(customerRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(CustomerNotFoundException.class,
                () -> offerService.getOffersByCustomerId(999L));
        verify(offerRepository, never()).findByCustomerId(any());
    }

    @Test
    @DisplayName("Should create offer successfully")
    void createOffer_Success() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(offerMapper.toEntity(requestDTO)).thenReturn(offer);
        when(offerRepository.save(any(Offer.class))).thenReturn(offer);
        when(offerMapper.toResponseDTO(offer)).thenReturn(responseDTO);

        // When
        OfferResponseDTO result = offerService.createOffer(requestDTO);

        // Then
        assertNotNull(result);
        assertEquals("Website Development", result.getTitle());
        verify(customerRepository, times(1)).findById(1L);
        verify(offerRepository, times(1)).save(any(Offer.class));
        verify(offerMapper, times(1)).toResponseDTO(offer);
    }

    @Test
    @DisplayName("Should throw exception when customer not found on create")
    void createOffer_CustomerNotFound() {
        // Given
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());
        requestDTO.setCustomerId(999L);

        // When & Then
        assertThrows(CustomerNotFoundException.class,
                () -> offerService.createOffer(requestDTO));
        verify(offerRepository, never()).save(any(Offer.class));
    }

    @Test
    @DisplayName("Should update offer successfully")
    void updateOffer_Success() {
        // Given
        OfferRequestDTO updateDTO = new OfferRequestDTO();
        updateDTO.setTitle("Mobile App Development");
        updateDTO.setDescription("iOS and Android app");
        updateDTO.setPrice(new BigDecimal("8000.00"));
        updateDTO.setStatus(OfferStatus.SENT);
        updateDTO.setCustomerId(1L);

        when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));
        when(offerRepository.save(any(Offer.class))).thenReturn(offer);
        when(offerMapper.toResponseDTO(offer)).thenReturn(responseDTO);

        // When
        OfferResponseDTO result = offerService.updateOffer(1L, updateDTO);

        // Then
        assertNotNull(result);
        verify(offerRepository, times(1)).save(any(Offer.class));
        verify(offerMapper, times(1)).toResponseDTO(offer);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent offer")
    void updateOffer_NotFound() {
        // Given
        OfferRequestDTO updateDTO = new OfferRequestDTO();
        updateDTO.setTitle("Mobile App Development");
        updateDTO.setCustomerId(1L);

        when(offerRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OfferNotFoundException.class,
                () -> offerService.updateOffer(999L, updateDTO));
        verify(offerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when new customer not found")
    void updateOffer_NewCustomerNotFound() {
        // Given
        OfferRequestDTO updateDTO = new OfferRequestDTO();
        updateDTO.setTitle("Mobile App Development");
        updateDTO.setCustomerId(999L);

        Customer differentCustomer = Customer.builder()
                .id(2L)
                .build();
        offer.setCustomer(differentCustomer);

        when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CustomerNotFoundException.class,
                () -> offerService.updateOffer(1L, updateDTO));
        verify(offerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete offer successfully")
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
    @DisplayName("Should throw exception when deleting non-existent offer")
    void deleteOffer_NotFound() {
        // Given
        when(offerRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(OfferNotFoundException.class,
                () -> offerService.deleteOffer(999L));
        verify(offerRepository, never()).deleteById(any());
    }
}