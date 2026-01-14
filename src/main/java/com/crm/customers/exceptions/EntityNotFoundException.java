package com.crm.customers.exceptions;

public class EntityNotFoundException extends DomainException {

    public EntityNotFoundException(String entityName, Object id) {
        super(entityName + " not found with id: " + id);
    }
}
