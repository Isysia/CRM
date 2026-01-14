package com.crm.offers.repository;

import com.crm.offers.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Offer entity.
 */
@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {

    /**
     * Find all offers for a specific customer.
     */
    List<Offer> findByCustomerId(Long customerId);
}