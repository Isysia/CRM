package com.crm.customers.exceptions;

public class DuplicateResourceException extends DomainException {

    public DuplicateResourceException(String resourceName, String field, String value) {
        super(resourceName + " with " + field + " '" + value + "' already exists");
    }
}
