package com.crm.tasks.controller;

import com.crm.customers.model.Customer;
import com.crm.customers.model.CustomerStatus;
import com.crm.customers.repository.CustomerRepository;
import com.crm.offers.model.Offer;
import com.crm.offers.model.OfferStatus;
import com.crm.offers.repository.OfferRepository;
import com.crm.tasks.dto.TaskRequestDTO;
import com.crm.tasks.model.Task;
import com.crm.tasks.model.TaskPriority;
import com.crm.tasks.model.TaskStatus;
import com.crm.tasks.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OfferRepository offerRepository;

    private Customer testCustomer;
    private Offer testOffer;
    private Task testTask;

    @BeforeEach
    void setUp() {
        // Clear database
        taskRepository.deleteAll();
        offerRepository.deleteAll();
        customerRepository.deleteAll();

        // Create test customer
        testCustomer = new Customer();
        testCustomer.setFirstName("John");
        testCustomer.setLastName("Doe");
        testCustomer.setEmail("john.doe@example.com");
        testCustomer.setPhone("+1234567890");
        testCustomer.setStatus(CustomerStatus.ACTIVE);
        testCustomer = customerRepository.save(testCustomer);

        // Create test offer
        testOffer = new Offer();
        testOffer.setTitle("Premium Package");
        testOffer.setDescription("Premium service package");
        testOffer.setPrice(new BigDecimal("1000.00"));
        testOffer.setStatus(OfferStatus.SENT);
        testOffer.setCustomer(testCustomer);
        testOffer = offerRepository.save(testOffer);

        // Create test task
        testTask = new Task();
        testTask.setTitle("Follow up call");
        testTask.setDescription("Call customer to discuss offer");
        testTask.setDueDate(LocalDateTime.now().plusDays(3));
        testTask.setStatus(TaskStatus.TODO);
        testTask.setPriority(TaskPriority.HIGH);
        testTask.setCustomer(testCustomer);
        testTask.setOffer(testOffer);
        testTask = taskRepository.save(testTask);
    }

    @Test
    @DisplayName("POST /api/tasks - Should create task successfully")
    void shouldCreateTaskSuccessfully() throws Exception {
        // Given
        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("New urgent task");
        requestDTO.setDescription("Handle customer complaint");
        requestDTO.setDueDate(LocalDateTime.now().plusDays(1));
        requestDTO.setStatus(TaskStatus.TODO);
        requestDTO.setPriority(TaskPriority.HIGH);
        requestDTO.setCustomerId(testCustomer.getId());
        requestDTO.setOfferId(testOffer.getId());

        // When & Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("New urgent task"))
                .andExpect(jsonPath("$.description").value("Handle customer complaint"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.customerId").value(testCustomer.getId()))
                .andExpect(jsonPath("$.customerName").value("John Doe"))
                .andExpect(jsonPath("$.offerId").value(testOffer.getId()))
                .andExpect(jsonPath("$.offerTitle").value("Premium Package"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("POST /api/tasks - Should create task without offer")
    void shouldCreateTaskWithoutOffer() throws Exception {
        // Given
        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("General follow-up");
        requestDTO.setDescription("Check customer status");
        requestDTO.setDueDate(LocalDateTime.now().plusDays(2));
        requestDTO.setStatus(TaskStatus.TODO);
        requestDTO.setPriority(TaskPriority.MEDIUM);
        requestDTO.setCustomerId(testCustomer.getId());
        requestDTO.setOfferId(null); // No offer

        // When & Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("General follow-up"))
                .andExpect(jsonPath("$.customerId").value(testCustomer.getId()))
                .andExpect(jsonPath("$.offerId").doesNotExist())
                .andExpect(jsonPath("$.offerTitle").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/tasks - Should return 400 when title is blank")
    void shouldReturn400WhenTitleIsBlank() throws Exception {
        // Given
        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("");
        requestDTO.setDescription("Some description");
        requestDTO.setDueDate(LocalDateTime.now().plusDays(1));
        requestDTO.setStatus(TaskStatus.TODO);
        requestDTO.setPriority(TaskPriority.LOW);
        requestDTO.setCustomerId(testCustomer.getId());

        // When & Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors.title").exists());
    }

    @Test
    @DisplayName("POST /api/tasks - Should return 400 when title is too short")
    void shouldReturn400WhenTitleIsTooShort() throws Exception {
        // Given
        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("AB");
        requestDTO.setDescription("Some description");
        requestDTO.setDueDate(LocalDateTime.now().plusDays(1));
        requestDTO.setStatus(TaskStatus.TODO);
        requestDTO.setPriority(TaskPriority.LOW);
        requestDTO.setCustomerId(testCustomer.getId());

        // When & Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.title").exists());
    }

    @Test
    @DisplayName("POST /api/tasks - Should return 400 when due date is null")
    void shouldReturn400WhenDueDateIsNull() throws Exception {
        // Given
        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("Valid title");
        requestDTO.setDescription("Some description");
        requestDTO.setDueDate(null);
        requestDTO.setStatus(TaskStatus.TODO);
        requestDTO.setPriority(TaskPriority.LOW);
        requestDTO.setCustomerId(testCustomer.getId());

        // When & Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.dueDate").exists());
    }

    @Test
    @DisplayName("POST /api/tasks - Should return 400 when due date is in the past")
    void shouldReturn400WhenDueDateIsInThePast() throws Exception {
        // Given
        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("Valid title");
        requestDTO.setDescription("Some description");
        requestDTO.setDueDate(LocalDateTime.now().minusDays(1));
        requestDTO.setStatus(TaskStatus.TODO);
        requestDTO.setPriority(TaskPriority.LOW);
        requestDTO.setCustomerId(testCustomer.getId());

        // When & Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.dueDate").exists());
    }

    @Test
    @DisplayName("POST /api/tasks - Should return 404 when customer not found")
    void shouldReturn404WhenCustomerNotFound() throws Exception {
        // Given
        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("Valid title");
        requestDTO.setDescription("Some description");
        requestDTO.setDueDate(LocalDateTime.now().plusDays(1));
        requestDTO.setStatus(TaskStatus.TODO);
        requestDTO.setPriority(TaskPriority.LOW);
        requestDTO.setCustomerId(999L); // Non-existent customer

        // When & Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Customer not found with id: 999"));
    }

    @Test
    @DisplayName("POST /api/tasks - Should return 404 when offer not found")
    void shouldReturn404WhenOfferNotFound() throws Exception {
        // Given
        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("Valid title");
        requestDTO.setDescription("Some description");
        requestDTO.setDueDate(LocalDateTime.now().plusDays(1));
        requestDTO.setStatus(TaskStatus.TODO);
        requestDTO.setPriority(TaskPriority.LOW);
        requestDTO.setCustomerId(testCustomer.getId());
        requestDTO.setOfferId(999L); // Non-existent offer

        // When & Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Offer not found with id: 999"));
    }

    @Test
    @DisplayName("GET /api/tasks/{id} - Should return task by id")
    void shouldReturnTaskById() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tasks/{id}", testTask.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTask.getId()))
                .andExpect(jsonPath("$.title").value("Follow up call"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.customerName").value("John Doe"))
                .andExpect(jsonPath("$.offerTitle").value("Premium Package"));
    }

    @Test
    @DisplayName("GET /api/tasks/{id} - Should return 404 when task not found")
    void shouldReturn404WhenTaskNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tasks/{id}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found with id: 999"));
    }

    @Test
    @DisplayName("GET /api/tasks - Should return all tasks")
    void shouldReturnAllTasks() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tasks"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].title").exists())
                .andExpect(jsonPath("$[0].customerName").exists());
    }

    @Test
    @DisplayName("GET /api/tasks/customer/{customerId} - Should return tasks by customer")
    void shouldReturnTasksByCustomer() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tasks/customer/{customerId}", testCustomer.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].customerId").value(testCustomer.getId()))
                .andExpect(jsonPath("$[0].customerName").value("John Doe"));
    }

    @Test
    @DisplayName("GET /api/tasks/customer/{customerId} - Should return 404 when customer not found")
    void shouldReturn404WhenCustomerNotFoundForGetTasks() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tasks/customer/{customerId}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found with id: 999"));
    }

    @Test
    @DisplayName("GET /api/tasks/offer/{offerId} - Should return tasks by offer")
    void shouldReturnTasksByOffer() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tasks/offer/{offerId}", testOffer.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].offerId").value(testOffer.getId()))
                .andExpect(jsonPath("$[0].offerTitle").value("Premium Package"));
    }

    @Test
    @DisplayName("GET /api/tasks/status/{status} - Should return tasks by status")
    void shouldReturnTasksByStatus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tasks/status/{status}", "TODO"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].status").value("TODO"));
    }

    @Test
    @DisplayName("GET /api/tasks/overdue - Should return overdue tasks")
    void shouldReturnOverdueTasks() throws Exception {
        // Given - Create overdue task
        Task overdueTask = new Task();
        overdueTask.setTitle("Overdue task");
        overdueTask.setDescription("This task is overdue");
        overdueTask.setDueDate(LocalDateTime.now().minusDays(1));
        overdueTask.setStatus(TaskStatus.TODO);
        overdueTask.setPriority(TaskPriority.HIGH);
        overdueTask.setCustomer(testCustomer);
        taskRepository.save(overdueTask);

        // When & Then
        mockMvc.perform(get("/api/tasks/overdue"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("PUT /api/tasks/{id} - Should update task successfully")
    void shouldUpdateTaskSuccessfully() throws Exception {
        // Given
        TaskRequestDTO updateDTO = new TaskRequestDTO();
        updateDTO.setTitle("Updated task title");
        updateDTO.setDescription("Updated description");
        updateDTO.setDueDate(LocalDateTime.now().plusDays(5));
        updateDTO.setStatus(TaskStatus.IN_PROGRESS);
        updateDTO.setPriority(TaskPriority.MEDIUM);
        updateDTO.setCustomerId(testCustomer.getId());
        updateDTO.setOfferId(testOffer.getId());

        // When & Then
        mockMvc.perform(put("/api/tasks/{id}", testTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTask.getId()))
                .andExpect(jsonPath("$.title").value("Updated task title"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));
    }

    @Test
    @DisplayName("PUT /api/tasks/{id} - Should return 404 when task not found")
    void shouldReturn404WhenUpdatingNonExistentTask() throws Exception {
        // Given
        TaskRequestDTO updateDTO = new TaskRequestDTO();
        updateDTO.setTitle("Updated title");
        updateDTO.setDescription("Updated description");
        updateDTO.setDueDate(LocalDateTime.now().plusDays(5));
        updateDTO.setStatus(TaskStatus.TODO);
        updateDTO.setPriority(TaskPriority.LOW);
        updateDTO.setCustomerId(testCustomer.getId());

        // When & Then
        mockMvc.perform(put("/api/tasks/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found with id: 999"));
    }

    @Test
    @DisplayName("DELETE /api/tasks/{id} - Should delete task successfully")
    void shouldDeleteTaskSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/tasks/{id}", testTask.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verify task is deleted
        mockMvc.perform(get("/api/tasks/{id}", testTask.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/tasks/{id} - Should return 404 when task not found")
    void shouldReturn404WhenDeletingNonExistentTask() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/tasks/{id}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found with id: 999"));
    }
}