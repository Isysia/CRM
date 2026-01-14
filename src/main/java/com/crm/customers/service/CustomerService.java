package com.crm.customers.service;

import com.crm.customers.exceptions.CustomerNotFoundException;
import com.crm.customers.exceptions.DuplicateResourceException;
import com.crm.customers.model.Customer;
import com.crm.customers.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    /**
     * Get all customers.
     */
    public List<Customer> getAllCustomers() {
        return repository.findAll();
    }

    /**
     * Get customer by ID.
     */
    public Customer getCustomerById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    /**
     * Create new customer.
     */
    public Customer createCustomer(Customer customer) {
        if (repository.findByEmail(customer.getEmail()).isPresent()) {
            throw new DuplicateResourceException(
                    "Customer",
                    "email",
                    customer.getEmail()
            );
        }
        return repository.save(customer);
    }

    /**
     * Update existing customer.
     */
    public Customer updateCustomer(Long id, Customer newData) {
        Customer existing = getCustomerById(id);

        // email uniqueness check
        repository.findByEmail(newData.getEmail())
                .filter(c -> !c.getId().equals(id))
                .ifPresent(c -> {
                    throw new DuplicateResourceException(
                            "Customer",
                            "email",
                            newData.getEmail()
                    );
                });

        existing.setFirstName(newData.getFirstName());
        existing.setLastName(newData.getLastName());
        existing.setEmail(newData.getEmail());
        existing.setPhone(newData.getPhone());
        existing.setStatus(newData.getStatus());

        return repository.save(existing);
    }

    /**
     * Delete customer by ID.
     */
    public void deleteCustomer(Long id) {
        if (!repository.existsById(id)) {
            throw new CustomerNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
