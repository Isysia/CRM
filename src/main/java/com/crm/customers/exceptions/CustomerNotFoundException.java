package com.crm.customers.exceptions;

public class CustomerNotFoundException extends EntityNotFoundException {

    public CustomerNotFoundException(Long id) {
        super("Customer", id);
    }
}
