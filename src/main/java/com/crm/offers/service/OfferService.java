package com.crm.offers.service;

import com.crm.customers.exceptions.CustomerNotFoundException;
import com.crm.customers.model.Customer;
import com.crm.customers.repository.CustomerRepository;
import com.crm.offers.exceptions.OfferNotFoundException;
import com.crm.offers.model.Offer;
import com.crm.offers.repository.OfferRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class OfferService {

    private final OfferRepository offerRepository;
    private final CustomerRepository customerRepository;

    public OfferService(OfferRepository offerRepository, CustomerRepository customerRepository) {
        this.offerRepository = offerRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * Get all offers.
     */
    public List<Offer> getAllOffers() {
        return offerRepository.findAll();
    }

    /**
     * Get offer by ID.
     */
    public Offer getOfferById(Long id) {
        return offerRepository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException(id));
    }

    /**
     * Get all offers for a specific customer.
     */
    public List<Offer> getOffersByCustomerId(Long customerId) {
        // Verify customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }
        return offerRepository.findByCustomerId(customerId);
    }

    /**
     * Create new offer.
     * Business rule: Offer must have a valid customer.
     */
    public Offer createOffer(Offer offer, Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        offer.setCustomer(customer);
        return offerRepository.save(offer);
    }

    /**
     * Update existing offer.
     */
    public Offer updateOffer(Long id, Offer newData, Long customerId) {
        Offer existing = getOfferById(id);

        // Verify new customer exists if customer is being changed
        if (!existing.getCustomer().getId().equals(customerId)) {
            Customer newCustomer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new CustomerNotFoundException(customerId));
            existing.setCustomer(newCustomer);
        }

        existing.setTitle(newData.getTitle());
        existing.setDescription(newData.getDescription());
        existing.setPrice(newData.getPrice());
        existing.setStatus(newData.getStatus());

        return offerRepository.save(existing);
    }

    /**
     * Delete offer by ID.
     */
    public void deleteOffer(Long id) {
        if (!offerRepository.existsById(id)) {
            throw new OfferNotFoundException(id);
        }
        offerRepository.deleteById(id);
    }
}