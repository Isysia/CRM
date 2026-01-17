package com.crm.offers.controller;

import com.crm.offers.dto.OfferRequestDTO;
import com.crm.offers.dto.OfferResponseDTO;
import com.crm.offers.service.OfferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
public class OfferController {

    private final OfferService service;

    public OfferController(OfferService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<OfferResponseDTO>> getAllOffers() {
        return ResponseEntity.ok(service.getAllOffers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfferResponseDTO> getOfferById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getOfferById(id));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OfferResponseDTO>> getOffersByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(service.getOffersByCustomerId(customerId));
    }

    @PostMapping
    public ResponseEntity<OfferResponseDTO> createOffer(@Valid @RequestBody OfferRequestDTO requestDTO) {
        OfferResponseDTO created = service.createOffer(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OfferResponseDTO> updateOffer(
            @PathVariable Long id,
            @Valid @RequestBody OfferRequestDTO requestDTO) {
        return ResponseEntity.ok(service.updateOffer(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        service.deleteOffer(id);
        return ResponseEntity.noContent().build();
    }
}