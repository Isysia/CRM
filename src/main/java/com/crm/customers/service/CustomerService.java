package com.crm.customers.service;

import com.crm.customers.model.Customer;
import com.crm.customers.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Service layer for managing Customer entities.
 */
@Service
public class CustomerService {

    private final CustomerRepository repository;

    @Autowired
    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    /**
     * Pobiera wszystkich klientów.
     *
     * @return lista wszystkich Customer
     */
    public List<Customer> getAllCustomers() {
        return repository.findAll();
    }

    /**
     * Pobiera klienta po jego identyfikatorze.
     *
     * @param id identyfikator klienta
     * @return znaleziony Customer
     * @throws RuntimeException jeśli klient o danym ID nie istnieje
     */
    public Customer getCustomerById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id " + id));
    }

    /**
     * Tworzy nowego klienta lub aktualizuje istniejącego.
     *
     * @param customer obiekt Customer do zapisania
     * @return zapisany obiekt Customer
     */
    public Customer saveCustomer(Customer customer) {
        return repository.save(customer);
    }

    /**
     * Aktualizuje istniejącego klienta.
     *
     * @param id       identyfikator klienta do aktualizacji
     * @param newData  dane do aktualizacji
     * @return zaktualizowany Customer
     * @throws RuntimeException jeśli klient o danym ID nie istnieje
     */
    public Customer updateCustomer(Long id, Customer newData) {
        Customer existing = getCustomerById(id);
        existing.setName(newData.getName());
        existing.setEmail(newData.getEmail());
        existing.setPhone(newData.getPhone());
        existing.setStatus(newData.getStatus());
        return repository.save(existing);
    }

    /**
     * Usuwa klienta o podanym ID.
     *
     * @param id identyfikator klienta do usunięcia
     * @throws RuntimeException jeśli klient o danym ID nie istnieje
     */
    public void deleteCustomer(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Cannot delete. Customer not found with id " + id);
        }
        repository.deleteById(id);
    }
}
