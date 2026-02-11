package com.crm.offers.service.impl;

import com.crm.customers.exceptions.CustomerNotFoundException;
import com.crm.customers.model.Customer;
import com.crm.customers.repository.CustomerRepository;
import com.crm.offers.dto.OfferRequestDTO;
import com.crm.offers.dto.OfferResponseDTO;
import com.crm.offers.exceptions.OfferNotFoundException;
import com.crm.offers.mapper.OfferMapper;
import com.crm.offers.model.Offer;
import com.crm.offers.model.OfferStatus;
import com.crm.offers.repository.OfferRepository;
import com.crm.offers.service.OfferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;
    private final CustomerRepository customerRepository;
    private final OfferMapper offerMapper;

    @Override
    @CacheEvict(value = "offers", allEntries = true)
    public OfferResponseDTO createOffer(OfferRequestDTO requestDTO) {
        log.info("Creating new offer for customer id: {}", requestDTO.getCustomerId());

        Customer customer = customerRepository.findById(requestDTO.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(requestDTO.getCustomerId()));

        Offer offer = offerMapper.toEntity(requestDTO);
        offer.setCustomer(customer);

        Offer savedOffer = offerRepository.save(offer);

        log.info("Offer created successfully with id: {}", savedOffer.getId());
        return offerMapper.toResponseDTO(savedOffer);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "offers", key = "#id")
    public OfferResponseDTO getOfferById(Long id) {
        log.info("Fetching offer {} FROM DATABASE (not cached)", id);

        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException(id));

        return offerMapper.toResponseDTO(offer);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "offers", key = "'all'")
    public List<OfferResponseDTO> getAllOffers() {
        log.info("Fetching all offers FROM DATABASE (not cached)");

        return offerRepository.findAll().stream()
                .map(offerMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "offers", key = "#customerId")
    public List<OfferResponseDTO> getOffersByCustomerId(Long customerId) {
        log.info("Fetching offers for customer {} FROM DATABASE (not cached)", customerId);

        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }

        return offerRepository.findByCustomerId(customerId).stream()
                .map(offerMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "offers", allEntries = true)
    public OfferResponseDTO updateOffer(Long id, OfferRequestDTO requestDTO) {
        log.info("Updating offer with id: {}", id);

        Offer existing = offerRepository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException(id));

        if (!existing.getCustomer().getId().equals(requestDTO.getCustomerId())) {
            Customer newCustomer = customerRepository.findById(requestDTO.getCustomerId())
                    .orElseThrow(() -> new CustomerNotFoundException(requestDTO.getCustomerId()));
            existing.setCustomer(newCustomer);
        }

        existing.setTitle(requestDTO.getTitle());
        existing.setDescription(requestDTO.getDescription());
        existing.setPrice(requestDTO.getPrice());
        existing.setStatus(requestDTO.getStatus());

        Offer updatedOffer = offerRepository.save(existing);

        log.info("Offer updated successfully with id: {}", updatedOffer.getId());
        return offerMapper.toResponseDTO(updatedOffer);
    }

    @Override
    @CacheEvict(value = "offers", allEntries = true)
    public void deleteOffer(Long id) {
        log.info("Deleting offer with id: {}", id);

        if (!offerRepository.existsById(id)) {
            throw new OfferNotFoundException(id);
        }

        offerRepository.deleteById(id);
        log.info("Offer deleted successfully with id: {}", id);
    }

    @Override
    @CacheEvict(value = "offers", allEntries = true)
    public void changeOfferStatus(Long id, String status) {
        log.info("Changing status for offer id: {} to {}", id, status);

        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException(id));

        try {
            OfferStatus newStatus = OfferStatus.valueOf(status);
            offer.setStatus(newStatus);
            offerRepository.save(offer);
            log.info("Status changed successfully for offer {}", id);
        } catch (IllegalArgumentException e) {
            log.error("Invalid status: {}", status);
            throw new IllegalArgumentException("Nieprawidłowy status: " + status);
        }
    }
}