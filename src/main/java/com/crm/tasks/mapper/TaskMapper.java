package com.crm.tasks.mapper;

import com.crm.customers.model.Customer;
import com.crm.offers.model.Offer;
import com.crm.tasks.dto.TaskRequestDTO;
import com.crm.tasks.dto.TaskResponseDTO;
import com.crm.tasks.model.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public Task toEntity(TaskRequestDTO dto, Customer customer, Offer offer) {
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setDueDate(dto.getDueDate());
        task.setStatus(dto.getStatus());
        task.setPriority(dto.getPriority());
        task.setCustomer(customer);
        task.setOffer(offer);
        return task;
    }

    public TaskResponseDTO toDTO(Task task) {
        return TaskResponseDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .dueDate(task.getDueDate())
                .status(task.getStatus())
                .priority(task.getPriority())
                .customerId(task.getCustomer().getId())
                .customerName(task.getCustomer().getFirstName() + " " + task.getCustomer().getLastName())
                .offerId(task.getOffer() != null ? task.getOffer().getId() : null)
                .offerTitle(task.getOffer() != null ? task.getOffer().getTitle() : null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    public void updateEntity(Task task, TaskRequestDTO dto, Customer customer, Offer offer) {
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setDueDate(dto.getDueDate());
        task.setStatus(dto.getStatus());
        task.setPriority(dto.getPriority());
        task.setCustomer(customer);
        task.setOffer(offer);
    }
}