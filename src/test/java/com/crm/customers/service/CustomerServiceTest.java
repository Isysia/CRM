package com.crm.customers.service;

import com.crm.customers.dto.CustomerRequestDTO;
import com.crm.customers.dto.CustomerResponseDTO;
import com.crm.customers.exceptions.CustomerNotFoundException;
import com.crm.customers.exceptions.DuplicateResourceException;
import com.crm.customers.mapper.CustomerMapper;
import com.crm.customers.model.Customer;
import com.crm.customers.model.CustomerStatus;
import com.crm.customers.repository.CustomerRepository;
import com.crm.customers.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer customer;
    private CustomerRequestDTO requestDTO;
    private CustomerResponseDTO responseDTO;

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

        requestDTO = new CustomerRequestDTO();
        requestDTO.setFirstName("John");
        requestDTO.setLastName("Doe");
        requestDTO.setEmail("john@example.com");
        requestDTO.setPhone("+1234567890");
        requestDTO.setStatus(CustomerStatus.ACTIVE);

        responseDTO = CustomerResponseDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .status(CustomerStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Should create customer successfully")
    void createCustomer_Success() {
        // Given
        when(customerRepository.findByEmail(requestDTO.getEmail())).thenReturn(Optional.empty());
        when(customerMapper.toEntity(requestDTO)).thenReturn(customer);
        when(customerRepository.save(customer)).thenReturn(customer);
        when(customerMapper.toResponseDTO(customer)).thenReturn(responseDTO);

        // When
        CustomerResponseDTO result = customerService.createCustomer(requestDTO);

        // Then
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("john@example.com", result.getEmail());
        verify(customerRepository, times(1)).save(customer);
        verify(customerMapper, times(1)).toEntity(requestDTO);
        verify(customerMapper, times(1)).toResponseDTO(customer);
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void createCustomer_DuplicateEmail_ThrowsException() {
        // Given
        when(customerRepository.findByEmail(requestDTO.getEmail()))
                .thenReturn(Optional.of(customer));

        // When & Then
        assertThrows(DuplicateResourceException.class,
                () -> customerService.createCustomer(requestDTO));

        verify(customerRepository, never()).save(any(Customer.class));
        verify(customerMapper, never()).toEntity(any());
    }

    @Test
    @DisplayName("Should get customer by id successfully")
    void getCustomerById_Success() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerMapper.toResponseDTO(customer)).thenReturn(responseDTO);

        // When
        CustomerResponseDTO result = customerService.getCustomerById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getFirstName());
        verify(customerRepository, times(1)).findById(1L);
        verify(customerMapper, times(1)).toResponseDTO(customer);
    }

    @Test
    @DisplayName("Should throw exception when customer not found by id")
    void getCustomerById_NotFound() {
        // Given
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CustomerNotFoundException.class,
                () -> customerService.getCustomerById(999L));

        verify(customerMapper, never()).toResponseDTO(any());
    }

    @Test
    @DisplayName("Should get all customers")
    void getAllCustomers_Success() {
        // Given
        Customer customer2 = Customer.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .phone("+9876543210")
                .status(CustomerStatus.ACTIVE)
                .build();

        List<Customer> customers = Arrays.asList(customer, customer2);

        when(customerRepository.findAll()).thenReturn(customers);
        when(customerMapper.toResponseDTO(any(Customer.class))).thenReturn(responseDTO);

        // When
        List<CustomerResponseDTO> result = customerService.getAllCustomers();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(customerRepository, times(1)).findAll();
        verify(customerMapper, times(2)).toResponseDTO(any(Customer.class));
    }

    @Test
    @DisplayName("Should update customer successfully")
    void updateCustomer_Success() {
        // Given
        CustomerRequestDTO updateDTO = new CustomerRequestDTO();
        updateDTO.setFirstName("Jane");
        updateDTO.setLastName("Smith");
        updateDTO.setEmail("jane@example.com");
        updateDTO.setPhone("+9876543210");
        updateDTO.setStatus(CustomerStatus.ACTIVE);

        Customer updatedCustomer = Customer.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .phone("+9876543210")
                .status(CustomerStatus.ACTIVE)
                .build();

        CustomerResponseDTO updatedResponseDTO = CustomerResponseDTO.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .phone("+9876543210")
                .status(CustomerStatus.ACTIVE)
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.findByEmail("jane@example.com")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(updatedCustomer);
        when(customerMapper.toResponseDTO(updatedCustomer)).thenReturn(updatedResponseDTO);

        // When
        CustomerResponseDTO result = customerService.updateCustomer(1L, updateDTO);

        // Then
        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("jane@example.com", result.getEmail());
        verify(customerRepository, times(1)).save(any(Customer.class));
        verify(customerMapper, times(1)).toResponseDTO(updatedCustomer);
    }

    @Test
    @DisplayName("Should throw exception when updating with duplicate email")
    void updateCustomer_DuplicateEmail_ThrowsException() {
        // Given
        Customer anotherCustomer = Customer.builder()
                .id(2L)
                .email("jane@example.com")
                .build();

        CustomerRequestDTO updateDTO = new CustomerRequestDTO();
        updateDTO.setFirstName("Jane");
        updateDTO.setLastName("Smith");
        updateDTO.setEmail("jane@example.com");
        updateDTO.setPhone("+9876543210");
        updateDTO.setStatus(CustomerStatus.ACTIVE);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.findByEmail("jane@example.com"))
                .thenReturn(Optional.of(anotherCustomer));

        // When & Then
        assertThrows(DuplicateResourceException.class,
                () -> customerService.updateCustomer(1L, updateDTO));

        verify(customerRepository, never()).save(any());
        verify(customerMapper, never()).toResponseDTO(any());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent customer")
    void updateCustomer_CustomerNotFound_ThrowsException() {
        // Given
        CustomerRequestDTO updateDTO = new CustomerRequestDTO();
        updateDTO.setFirstName("Jane");
        updateDTO.setLastName("Smith");
        updateDTO.setEmail("jane@example.com");
        updateDTO.setPhone("+9876543210");
        updateDTO.setStatus(CustomerStatus.ACTIVE);

        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CustomerNotFoundException.class,
                () -> customerService.updateCustomer(999L, updateDTO));

        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete customer successfully")
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
    @DisplayName("Should throw exception when deleting non-existent customer")
    void deleteCustomer_NotFound() {
        // Given
        when(customerRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(CustomerNotFoundException.class,
                () -> customerService.deleteCustomer(999L));

        verify(customerRepository, never()).deleteById(any());
    }
}