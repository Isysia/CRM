package com.crm.tasks.service.impl;

import com.crm.customers.exceptions.CustomerNotFoundException;
import com.crm.customers.model.Customer;
import com.crm.customers.repository.CustomerRepository;
import com.crm.offers.exceptions.OfferNotFoundException;
import com.crm.offers.model.Offer;
import com.crm.offers.repository.OfferRepository;
import com.crm.tasks.dto.TaskRequestDTO;
import com.crm.tasks.dto.TaskResponseDTO;
import com.crm.tasks.exceptions.TaskNotFoundException;
import com.crm.tasks.mapper.TaskMapper;
import com.crm.tasks.model.Task;
import com.crm.tasks.model.TaskStatus;
import com.crm.tasks.repository.TaskRepository;
import com.crm.tasks.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final CustomerRepository customerRepository;
    private final OfferRepository offerRepository;
    private final TaskMapper taskMapper;

    @Override
    public TaskResponseDTO createTask(TaskRequestDTO requestDTO) {
        log.info("Creating new task with title: {}", requestDTO.getTitle());

        // Validate customer exists
        Customer customer = customerRepository.findById(requestDTO.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(requestDTO.getCustomerId()));

        // Validate offer exists if provided
        Offer offer = null;
        if (requestDTO.getOfferId() != null) {
            offer = offerRepository.findById(requestDTO.getOfferId())
                    .orElseThrow(() -> new OfferNotFoundException(requestDTO.getOfferId()));

            // Validate that offer belongs to the same customer
            if (!offer.getCustomer().getId().equals(customer.getId())) {
                throw new IllegalArgumentException(
                        "Offer does not belong to the specified customer"
                );
            }
        }

        // Validate due date is in the future (additional check)
        if (requestDTO.getDueDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Due date must be in the future");
        }

        Task task = taskMapper.toEntity(requestDTO, customer, offer);
        Task savedTask = taskRepository.save(task);

        log.info("Task created successfully with id: {}", savedTask.getId());
        return taskMapper.toDTO(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponseDTO getTaskById(Long id) {
        log.info("Fetching task with id: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        return taskMapper.toDTO(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getAllTasks() {
        log.info("Fetching all tasks");

        return taskRepository.findAll().stream()
                .map(taskMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksByCustomerId(Long customerId) {
        log.info("Fetching tasks for customer id: {}", customerId);

        // Validate customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }

        return taskRepository.findByCustomerId(customerId).stream()
                .map(taskMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksByOfferId(Long offerId) {
        log.info("Fetching tasks for offer id: {}", offerId);

        // Validate offer exists
        if (!offerRepository.existsById(offerId)) {
            throw new OfferNotFoundException(offerId);
        }

        return taskRepository.findByOfferId(offerId).stream()
                .map(taskMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksByStatus(TaskStatus status) {
        log.info("Fetching tasks with status: {}", status);

        return taskRepository.findByStatus(status).stream()
                .map(taskMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getOverdueTasks() {
        log.info("Fetching overdue tasks");

        return taskRepository.findOverdueTasks(LocalDateTime.now()).stream()
                .map(taskMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TaskResponseDTO updateTask(Long id, TaskRequestDTO requestDTO) {
        log.info("Updating task with id: {}", id);

        // Find existing task
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        // Validate customer exists
        Customer customer = customerRepository.findById(requestDTO.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(requestDTO.getCustomerId()));

        // Validate offer exists if provided
        Offer offer = null;
        if (requestDTO.getOfferId() != null) {
            offer = offerRepository.findById(requestDTO.getOfferId())
                    .orElseThrow(() -> new OfferNotFoundException(requestDTO.getOfferId()));

            // Validate that offer belongs to the same customer
            if (!offer.getCustomer().getId().equals(customer.getId())) {
                throw new IllegalArgumentException(
                        "Offer does not belong to the specified customer"
                );
            }
        }

        // Validate status transition
        validateStatusTransition(existingTask.getStatus(), requestDTO.getStatus());

        // Update entity
        taskMapper.updateEntity(existingTask, requestDTO, customer, offer);
        Task updatedTask = taskRepository.save(existingTask);

        log.info("Task updated successfully with id: {}", updatedTask.getId());
        return taskMapper.toDTO(updatedTask);
    }

    @Override
    public void deleteTask(Long id) {
        log.info("Deleting task with id: {}", id);

        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }

        taskRepository.deleteById(id);
        log.info("Task deleted successfully with id: {}", id);
    }

    /**
     * Validates task status transitions
     * Valid transitions:
     * TODO -> IN_PROGRESS -> DONE
     * Any status -> TODO (can revert)
     */
    private void validateStatusTransition(TaskStatus currentStatus, TaskStatus newStatus) {
        if (currentStatus == newStatus) {
            return; // No change
        }
    }

    @Override
    public TaskResponseDTO updateTaskStatus(Long id, TaskStatus newStatus) {
        log.info("Updating status for task id: {} to {}", id, newStatus);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        // Тут можна додати логіку: Юзер не може перевести з DONE назад в TODO, якщо це потрібно
        // if (task.getStatus() == TaskStatus.DONE && newStatus == TaskStatus.TODO) { ... }

        task.setStatus(newStatus);
        Task updatedTask = taskRepository.save(task);

        return taskMapper.toDTO(updatedTask);
    }
}

