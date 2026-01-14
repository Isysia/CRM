package com.crm.offers.controller;

import com.crm.offers.dto.OfferRequestDTO;
import com.crm.offers.dto.OfferResponseDTO;
import com.crm.offers.mapper.OfferMapper;
import com.crm.offers.model.Offer;
import com.crm.offers.service.OfferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Offer operations.
 */
@RestController
@RequestMapping("/api/offers")
public class OfferController {

    private final OfferService service;
    private final OfferMapper mapper;

    public OfferController(OfferService service, OfferMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    /**
     * GET /api/offers
     * Returns all offers
     */
    @GetMapping
    public ResponseEntity<List<OfferResponseDTO>> getAllOffers() {
        List<OfferResponseDTO> offers = service.getAllOffers()
                .stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(offers);
    }

    /**
     * GET /api/offers/{id}
     * Returns a single offer by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<OfferResponseDTO> getOfferById(@PathVariable Long id) {
        Offer offer = service.getOfferById(id);
        return ResponseEntity.ok(mapper.toResponseDTO(offer));
    }

    /**
     * GET /api/customers/{customerId}/offers
     * Returns all offers for a specific customer
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OfferResponseDTO>> getOffersByCustomerId(@PathVariable Long customerId) {
        List<OfferResponseDTO> offers = service.getOffersByCustomerId(customerId)
                .stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(offers);
    }

    /**
     * POST /api/offers
     * Creates a new offer
     */
    @PostMapping
    public ResponseEntity<OfferResponseDTO> createOffer(@Valid @RequestBody OfferRequestDTO requestDTO) {
        Offer offer = mapper.toEntity(requestDTO);
        Offer created = service.createOffer(offer, requestDTO.getCustomerId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapper.toResponseDTO(created));
    }

    /**
     * PUT /api/offers/{id}
     * Updates an existing offer
     */
    @PutMapping("/{id}")
    public ResponseEntity<OfferResponseDTO> updateOffer(
            @PathVariable Long id,
            @Valid @RequestBody OfferRequestDTO requestDTO
    ) {
        Offer offerData = mapper.toEntity(requestDTO);
        Offer updated = service.updateOffer(id, offerData, requestDTO.getCustomerId());

        return ResponseEntity.ok(mapper.toResponseDTO(updated));
    }

    /**
     * DELETE /api/offers/{id}
     * Deletes an offer
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        service.deleteOffer(id);
        return ResponseEntity.noContent().build();
    }
}