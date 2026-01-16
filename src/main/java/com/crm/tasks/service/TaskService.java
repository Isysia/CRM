package com.crm.tasks.service;

import com.crm.tasks.dto.TaskRequestDTO;
import com.crm.tasks.dto.TaskResponseDTO;
import com.crm.tasks.model.TaskStatus;

import java.util.List;

public interface TaskService {

    TaskResponseDTO createTask(TaskRequestDTO requestDTO);

    TaskResponseDTO getTaskById(Long id);

    List<TaskResponseDTO> getAllTasks();

    List<TaskResponseDTO> getTasksByCustomerId(Long customerId);

    List<TaskResponseDTO> getTasksByOfferId(Long offerId);

    List<TaskResponseDTO> getTasksByStatus(TaskStatus status);

    List<TaskResponseDTO> getOverdueTasks();

    TaskResponseDTO updateTask(Long id, TaskRequestDTO requestDTO);

    void deleteTask(Long id);
}