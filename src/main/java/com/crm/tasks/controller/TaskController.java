package com.crm.tasks.controller;

import com.crm.tasks.dto.TaskRequestDTO;
import com.crm.tasks.dto.TaskResponseDTO;
import com.crm.tasks.model.TaskStatus;
import com.crm.tasks.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(@Valid @RequestBody TaskRequestDTO requestDTO) {
        log.info("POST /api/tasks - Creating new task");
        TaskResponseDTO response = taskService.createTask(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getTaskById(@PathVariable Long id) {
        log.info("GET /api/tasks/{} - Fetching task", id);
        TaskResponseDTO response = taskService.getTaskById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getAllTasks() {
        log.info("GET /api/tasks - Fetching all tasks");
        List<TaskResponseDTO> response = taskService.getAllTasks();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByCustomer(@PathVariable Long customerId) {
        log.info("GET /api/tasks/customer/{} - Fetching tasks for customer", customerId);
        List<TaskResponseDTO> response = taskService.getTasksByCustomerId(customerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/offer/{offerId}")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByOffer(@PathVariable Long offerId) {
        log.info("GET /api/tasks/offer/{} - Fetching tasks for offer", offerId);
        List<TaskResponseDTO> response = taskService.getTasksByOfferId(offerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByStatus(@PathVariable TaskStatus status) {
        log.info("GET /api/tasks/status/{} - Fetching tasks with status", status);
        List<TaskResponseDTO> response = taskService.getTasksByStatus(status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<TaskResponseDTO>> getOverdueTasks() {
        log.info("GET /api/tasks/overdue - Fetching overdue tasks");
        List<TaskResponseDTO> response = taskService.getOverdueTasks();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequestDTO requestDTO) {
        log.info("PUT /api/tasks/{} - Updating task", id);
        TaskResponseDTO response = taskService.updateTask(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        log.info("DELETE /api/tasks/{} - Deleting task", id);
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}