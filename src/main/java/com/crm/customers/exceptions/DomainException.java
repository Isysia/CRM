package com.crm.customers.exceptions;

/**
 * Base class for all domain-level exceptions.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
