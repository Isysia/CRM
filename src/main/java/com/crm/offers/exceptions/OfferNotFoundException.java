package com.crm.offers.exceptions;

/**
 * Exception thrown when an offer is not found.
 */
public class OfferNotFoundException extends RuntimeException {

    public OfferNotFoundException(Long id) {
        super("Offer not found with id: " + id);
    }
}