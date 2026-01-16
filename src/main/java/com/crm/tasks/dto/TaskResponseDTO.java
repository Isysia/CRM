package com.crm.tasks.dto;

import com.crm.tasks.model.TaskPriority;
import com.crm.tasks.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDTO {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private TaskStatus status;
    private TaskPriority priority;

    // Related entities info
    private Long customerId;
    private String customerName;

    private Long offerId;
    private String offerTitle;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Helper methods
    public boolean isOverdue() {
        return dueDate.isBefore(LocalDateTime.now()) && status != TaskStatus.DONE;
    }
}