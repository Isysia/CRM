package com.crm.tasks.controller;

import com.crm.tasks.dto.TaskRequestDTO;
import com.crm.tasks.dto.TaskResponseDTO;
import com.crm.tasks.model.TaskStatus;
import com.crm.tasks.service.TaskService;
// ↓↓↓ ДОДАТИ ЦІ IMPORTS ↓↓↓
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
// ↑↑↑ КІНЕЦЬ НОВИХ IMPORTS ↑↑↑
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
@Tag(name = "Tasks", description = "Task management APIs")  // ← ДОДАТИ
@SecurityRequirement(name = "basicAuth")  // ← ДОДАТИ
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Create a new task", description = "Requires MANAGER or ADMIN role")  // ← ДОДАТИ
    @ApiResponses(value = {  // ← ДОДАТИ
            @ApiResponse(responseCode = "201", description = "Task created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Customer or Offer not found")
    })
    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(@Valid @RequestBody TaskRequestDTO requestDTO) {
        log.info("POST /api/tasks - Creating new task");
        TaskResponseDTO response = taskService.createTask(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get task by ID")  // ← ДОДАТИ
    @ApiResponses(value = {  // ← ДОДАТИ
            @ApiResponse(responseCode = "200", description = "Task found"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getTaskById(
            @Parameter(description = "Task ID") @PathVariable Long id) {  // ← ЗМІНИТИ
        log.info("GET /api/tasks/{} - Fetching task", id);
        TaskResponseDTO response = taskService.getTaskById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all tasks")  // ← ДОДАТИ
    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getAllTasks() {
        log.info("GET /api/tasks - Fetching all tasks");
        List<TaskResponseDTO> response = taskService.getAllTasks();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get tasks by customer ID")  // ← ДОДАТИ
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByCustomer(
            @Parameter(description = "Customer ID") @PathVariable Long customerId) {  // ← ЗМІНИТИ
        log.info("GET /api/tasks/customer/{} - Fetching tasks for customer", customerId);
        List<TaskResponseDTO> response = taskService.getTasksByCustomerId(customerId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get tasks by offer ID")  // ← ДОДАТИ
    @GetMapping("/offer/{offerId}")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByOffer(
            @Parameter(description = "Offer ID") @PathVariable Long offerId) {  // ← ЗМІНИТИ
        log.info("GET /api/tasks/offer/{} - Fetching tasks for offer", offerId);
        List<TaskResponseDTO> response = taskService.getTasksByOfferId(offerId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get tasks by status")  // ← ДОДАТИ
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByStatus(
            @Parameter(description = "Task status (TODO, IN_PROGRESS, DONE)") @PathVariable TaskStatus status) {  // ← ЗМІНИТИ
        log.info("GET /api/tasks/status/{} - Fetching tasks with status", status);
        List<TaskResponseDTO> response = taskService.getTasksByStatus(status);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get overdue tasks")  // ← ДОДАТИ
    @GetMapping("/overdue")
    public ResponseEntity<List<TaskResponseDTO>> getOverdueTasks() {
        log.info("GET /api/tasks/overdue - Fetching overdue tasks");
        List<TaskResponseDTO> response = taskService.getOverdueTasks();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update task", description = "Requires MANAGER or ADMIN role")  // ← ДОДАТИ
    @ApiResponses(value = {  // ← ДОДАТИ
            @ApiResponse(responseCode = "200", description = "Task updated"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> updateTask(
            @Parameter(description = "Task ID") @PathVariable Long id,  // ← ЗМІНИТИ
            @Valid @RequestBody TaskRequestDTO requestDTO) {
        log.info("PUT /api/tasks/{} - Updating task", id);
        TaskResponseDTO response = taskService.updateTask(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete task", description = "Requires ADMIN role")  // ← ДОДАТИ
    @ApiResponses(value = {  // ← ДОДАТИ
            @ApiResponse(responseCode = "204", description = "Task deleted"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "Task ID") @PathVariable Long id) {  // ← ЗМІНИТИ
        log.info("DELETE /api/tasks/{} - Deleting task", id);
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}