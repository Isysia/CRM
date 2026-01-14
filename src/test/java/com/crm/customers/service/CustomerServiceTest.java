package com.crm.customers.service;

import com.crm.customers.exceptions.CustomerNotFoundException;
import com.crm.customers.exceptions.DuplicateResourceException;
import com.crm.customers.model.Customer;
import com.crm.customers.model.CustomerStatus;
import com.crm.customers.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;

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
    }

    @Test
    void createCustomer_Success() {
        // Given
        when(customerRepository.findByEmail(customer.getEmail())).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // When
        Customer result = customerService.createCustomer(customer);

        // Then
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("john@example.com", result.getEmail());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void createCustomer_DuplicateEmail_ThrowsException() {
        // Given
        when(customerRepository.findByEmail(customer.getEmail()))
                .thenReturn(Optional.of(customer));

        // When & Then
        assertThrows(DuplicateResourceException.class,
                () -> customerService.createCustomer(customer));

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void getCustomerById_Success() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        // When
        Customer result = customerService.getCustomerById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getFirstName());
        verify(customerRepository, times(1)).findById(1L);
    }

    @Test
    void getCustomerById_NotFound() {
        // Given
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CustomerNotFoundException.class,
                () -> customerService.getCustomerById(999L));
    }

    @Test
    void updateCustomer_Success() {
        // Given
        Customer updatedData = Customer.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .phone("+9876543210")
                .status(CustomerStatus.ACTIVE)
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.findByEmail("jane@example.com")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // When
        Customer result = customerService.updateCustomer(1L, updatedData);

        // Then
        assertNotNull(result);
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void updateCustomer_DuplicateEmail_ThrowsException() {
        // Given
        Customer anotherCustomer = Customer.builder()
                .id(2L)
                .email("jane@example.com")
                .build();

        Customer updatedData = Customer.builder()
                .email("jane@example.com")
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.findByEmail("jane@example.com"))
                .thenReturn(Optional.of(anotherCustomer));

        // When & Then
        assertThrows(DuplicateResourceException.class,
                () -> customerService.updateCustomer(1L, updatedData));
    }

    @Test
    void deleteCustomer_Success() {
        // Given
        when(customerRepository.existsById(1L)).thenReturn(true);
        doNothing().when(customerRepository).deleteById(1L);

        // When
        customerService.deleteCustomer(1L);

        // Then
        verify(customerRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteCustomer_NotFound() {
        // Given
        when(customerRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(CustomerNotFoundException.class,
                () -> customerService.deleteCustomer(999L));
    }
}