package com.crm.customers.repository;

import com.crm.customers.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Customer entities.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // При потребі можна додати методи на кшталт:
    // List<Customer> findByStatus(String status);
}
