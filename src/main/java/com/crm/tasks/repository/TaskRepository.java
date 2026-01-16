package com.crm.tasks.repository;

import com.crm.tasks.model.Task;
import com.crm.tasks.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByCustomerId(Long customerId);

    List<Task> findByOfferId(Long offerId);

    List<Task> findByStatus(TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.dueDate < :now AND t.status != 'DONE'")
    List<Task> findOverdueTasks(@Param("now") LocalDateTime now);

    List<Task> findByCustomerIdAndStatus(Long customerId, TaskStatus status);
}