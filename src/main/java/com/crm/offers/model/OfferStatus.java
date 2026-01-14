package com.crm.offers.model;

/**
 * Status of an offer in the sales pipeline.
 */
public enum OfferStatus {
    DRAFT,      // Чернетка (ще не надіслано)
    SENT,       // Надіслано клієнту
    ACCEPTED,   // Клієнт прийняв
    REJECTED,   // Клієнт відхилив
    EXPIRED     // Термін дії минув
}