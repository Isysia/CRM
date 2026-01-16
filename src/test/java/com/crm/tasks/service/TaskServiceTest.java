package com.crm.tasks.service;

import com.crm.customers.exceptions.CustomerNotFoundException;
import com.crm.customers.model.Customer;
import com.crm.customers.model.CustomerStatus;
import com.crm.customers.repository.CustomerRepository;
import com.crm.offers.exceptions.OfferNotFoundException;
import com.crm.offers.model.Offer;
import com.crm.offers.model.OfferStatus;
import com.crm.offers.repository.OfferRepository;
import com.crm.tasks.dto.TaskRequestDTO;
import com.crm.tasks.dto.TaskResponseDTO;
import com.crm.tasks.exceptions.TaskNotFoundException;
import com.crm.tasks.mapper.TaskMapper;
import com.crm.tasks.model.Task;
import com.crm.tasks.model.TaskPriority;
import com.crm.tasks.model.TaskStatus;
import com.crm.tasks.repository.TaskRepository;
import com.crm.tasks.service.impl.TaskServiceImpl;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Customer testCustomer;
    private Offer testOffer;
    private Task testTask;
    private TaskRequestDTO testRequestDTO;
    private TaskResponseDTO testResponseDTO;

    @BeforeEach
    void setUp() {
        // Create test customer
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setFirstName("John");
        testCustomer.setLastName("Doe");
        testCustomer.setEmail("john.doe@example.com");
        testCustomer.setPhone("+1234567890");
        testCustomer.setStatus(CustomerStatus.ACTIVE);

        // Create test offer
        testOffer = new Offer();
        testOffer.setId(1L);
        testOffer.setTitle("Premium Package");
        testOffer.setDescription("Premium service package");
        testOffer.setPrice(new BigDecimal("1000.00"));
        testOffer.setStatus(OfferStatus.SENT);
        testOffer.setCustomer(testCustomer);

        // Create test task
        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Follow up call");
        testTask.setDescription("Call customer to discuss offer");
        testTask.setDueDate(LocalDateTime.now().plusDays(3));
        testTask.setStatus(TaskStatus.TODO);
        testTask.setPriority(TaskPriority.HIGH);
        testTask.setCustomer(testCustomer);
        testTask.setOffer(testOffer);
        testTask.setCreatedAt(LocalDateTime.now());
        testTask.setUpdatedAt(LocalDateTime.now());

        // Create test DTO
        testRequestDTO = new TaskRequestDTO();
        testRequestDTO.setTitle("Follow up call");
        testRequestDTO.setDescription("Call customer to discuss offer");
        testRequestDTO.setDueDate(LocalDateTime.now().plusDays(3));
        testRequestDTO.setStatus(TaskStatus.TODO);
        testRequestDTO.setPriority(TaskPriority.HIGH);
        testRequestDTO.setCustomerId(1L);
        testRequestDTO.setOfferId(1L);

        testResponseDTO = TaskResponseDTO.builder()
                .id(1L)
                .title("Follow up call")
                .description("Call customer to discuss offer")
                .dueDate(testTask.getDueDate())
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .customerId(1L)
                .customerName("John Doe")
                .offerId(1L)
                .offerTitle("Premium Package")
                .createdAt(testTask.getCreatedAt())
                .updatedAt(testTask.getUpdatedAt())
                .build();
    }

    @Test
    @DisplayName("Should create task successfully")
    void shouldCreateTaskSuccessfully() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(offerRepository.findById(1L)).thenReturn(Optional.of(testOffer));
        when(taskMapper.toEntity(testRequestDTO, testCustomer, testOffer)).thenReturn(testTask);
        when(taskRepository.save(testTask)).thenReturn(testTask);
        when(taskMapper.toDTO(testTask)).thenReturn(testResponseDTO);

        // When
        TaskResponseDTO result = taskService.createTask(testRequestDTO);

        // Then
        assertNotNull(result);
        assertEquals("Follow up call", result.getTitle());
        assertEquals(TaskStatus.TODO, result.getStatus());
        assertEquals(TaskPriority.HIGH, result.getPriority());
        assertEquals("John Doe", result.getCustomerName());

        verify(customerRepository).findById(1L);
        verify(offerRepository).findById(1L);
        verify(taskRepository).save(testTask);
    }

    @Test
    @DisplayName("Should create task without offer")
    void shouldCreateTaskWithoutOffer() {
        // Given
        testRequestDTO.setOfferId(null);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(taskMapper.toEntity(testRequestDTO, testCustomer, null)).thenReturn(testTask);
        when(taskRepository.save(testTask)).thenReturn(testTask);
        when(taskMapper.toDTO(testTask)).thenReturn(testResponseDTO);

        // When
        TaskResponseDTO result = taskService.createTask(testRequestDTO);

        // Then
        assertNotNull(result);
        verify(customerRepository).findById(1L);
        verify(offerRepository, never()).findById(anyLong());
        verify(taskRepository).save(testTask);
    }

    @Test
    @DisplayName("Should throw exception when customer not found on create")
    void shouldThrowExceptionWhenCustomerNotFoundOnCreate() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CustomerNotFoundException.class, () -> {
            taskService.createTask(testRequestDTO);
        });

        verify(customerRepository).findById(1L);
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when offer not found on create")
    void shouldThrowExceptionWhenOfferNotFoundOnCreate() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(offerRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OfferNotFoundException.class, () -> {
            taskService.createTask(testRequestDTO);
        });

        verify(offerRepository).findById(1L);
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when offer belongs to different customer")
    void shouldThrowExceptionWhenOfferBelongsToDifferentCustomer() {
        // Given
        Customer anotherCustomer = new Customer();
        anotherCustomer.setId(2L);
        testOffer.setCustomer(anotherCustomer);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(offerRepository.findById(1L)).thenReturn(Optional.of(testOffer));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(testRequestDTO);
        });

        assertEquals("Offer does not belong to the specified customer", exception.getMessage());
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when due date is in the past")
    void shouldThrowExceptionWhenDueDateIsInThePast() {
        // Given
        testRequestDTO.setDueDate(LocalDateTime.now().minusDays(1));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(offerRepository.findById(1L)).thenReturn(Optional.of(testOffer));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(testRequestDTO);
        });

        assertEquals("Due date must be in the future", exception.getMessage());
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get task by id successfully")
    void shouldGetTaskByIdSuccessfully() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskMapper.toDTO(testTask)).thenReturn(testResponseDTO);

        // When
        TaskResponseDTO result = taskService.getTaskById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Follow up call", result.getTitle());

        verify(taskRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when task not found by id")
    void shouldThrowExceptionWhenTaskNotFoundById() {
        // Given
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TaskNotFoundException.class, () -> {
            taskService.getTaskById(999L);
        });

        verify(taskRepository).findById(999L);
    }

    @Test
    @DisplayName("Should get all tasks")
    void shouldGetAllTasks() {
        // Given
        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Second task");

        List<Task> tasks = Arrays.asList(testTask, task2);
        when(taskRepository.findAll()).thenReturn(tasks);
        when(taskMapper.toDTO(any(Task.class))).thenReturn(testResponseDTO);

        // When
        List<TaskResponseDTO> result = taskService.getAllTasks();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        verify(taskRepository).findAll();
        verify(taskMapper, times(2)).toDTO(any(Task.class));
    }

    @Test
    @DisplayName("Should get tasks by customer id")
    void shouldGetTasksByCustomerId() {
        // Given
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.findByCustomerId(1L)).thenReturn(Arrays.asList(testTask));
        when(taskMapper.toDTO(testTask)).thenReturn(testResponseDTO);

        // When
        List<TaskResponseDTO> result = taskService.getTasksByCustomerId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getCustomerName());

        verify(customerRepository).existsById(1L);
        verify(taskRepository).findByCustomerId(1L);
    }

    @Test
    @DisplayName("Should throw exception when customer not found for getTasksByCustomerId")
    void shouldThrowExceptionWhenCustomerNotFoundForGetTasks() {
        // Given
        when(customerRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(CustomerNotFoundException.class, () -> {
            taskService.getTasksByCustomerId(999L);
        });

        verify(customerRepository).existsById(999L);
        verify(taskRepository, never()).findByCustomerId(anyLong());
    }

    @Test
    @DisplayName("Should get tasks by offer id")
    void shouldGetTasksByOfferId() {
        // Given
        when(offerRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.findByOfferId(1L)).thenReturn(Arrays.asList(testTask));
        when(taskMapper.toDTO(testTask)).thenReturn(testResponseDTO);

        // When
        List<TaskResponseDTO> result = taskService.getTasksByOfferId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(offerRepository).existsById(1L);
        verify(taskRepository).findByOfferId(1L);
    }

    @Test
    @DisplayName("Should get tasks by status")
    void shouldGetTasksByStatus() {
        // Given
        when(taskRepository.findByStatus(TaskStatus.TODO)).thenReturn(Arrays.asList(testTask));
        when(taskMapper.toDTO(testTask)).thenReturn(testResponseDTO);

        // When
        List<TaskResponseDTO> result = taskService.getTasksByStatus(TaskStatus.TODO);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TaskStatus.TODO, result.get(0).getStatus());

        verify(taskRepository).findByStatus(TaskStatus.TODO);
    }

    @Test
    @DisplayName("Should get overdue tasks")
    void shouldGetOverdueTasks() {
        // Given
        when(taskRepository.findOverdueTasks(any(LocalDateTime.class))).thenReturn(Arrays.asList(testTask));
        when(taskMapper.toDTO(testTask)).thenReturn(testResponseDTO);

        // When
        List<TaskResponseDTO> result = taskService.getOverdueTasks();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(taskRepository).findOverdueTasks(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should update task successfully")
    void shouldUpdateTaskSuccessfully() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(offerRepository.findById(1L)).thenReturn(Optional.of(testOffer));
        when(taskRepository.save(testTask)).thenReturn(testTask);
        when(taskMapper.toDTO(testTask)).thenReturn(testResponseDTO);

        // When
        TaskResponseDTO result = taskService.updateTask(1L, testRequestDTO);

        // Then
        assertNotNull(result);
        verify(taskRepository).findById(1L);
        verify(taskMapper).updateEntity(testTask, testRequestDTO, testCustomer, testOffer);
        verify(taskRepository).save(testTask);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent task")
    void shouldThrowExceptionWhenUpdatingNonExistentTask() {
        // Given
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TaskNotFoundException.class, () -> {
            taskService.updateTask(999L, testRequestDTO);
        });

        verify(taskRepository).findById(999L);
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when invalid status transition")
    void shouldThrowExceptionWhenInvalidStatusTransition() {
        // Given
        testTask.setStatus(TaskStatus.TODO);
        testRequestDTO.setStatus(TaskStatus.DONE);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(offerRepository.findById(1L)).thenReturn(Optional.of(testOffer));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTask(1L, testRequestDTO);
        });

        assertTrue(exception.getMessage().contains("Cannot change status from TODO to DONE directly"));
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete task successfully")
    void shouldDeleteTaskSuccessfully() {
        // Given
        when(taskRepository.existsById(1L)).thenReturn(true);

        // When
        taskService.deleteTask(1L);

        // Then
        verify(taskRepository).existsById(1L);
        verify(taskRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent task")
    void shouldThrowExceptionWhenDeletingNonExistentTask() {
        // Given
        when(taskRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(TaskNotFoundException.class, () -> {
            taskService.deleteTask(999L);
        });

        verify(taskRepository).existsById(999L);
        verify(taskRepository, never()).deleteById(anyLong());
    }
}