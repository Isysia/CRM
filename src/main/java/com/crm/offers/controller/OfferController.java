package com.crm.offers.controller;

import com.crm.offers.dto.OfferRequestDTO;
import com.crm.offers.dto.OfferResponseDTO;
import com.crm.offers.service.OfferService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/offers")
@Slf4j
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
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<OfferResponseDTO> createOffer(@Valid @RequestBody OfferRequestDTO requestDTO) {
        OfferResponseDTO created = service.createOffer(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<OfferResponseDTO> updateOffer(
            @PathVariable Long id,
            @Valid @RequestBody OfferRequestDTO requestDTO) {
        return ResponseEntity.ok(service.updateOffer(id, requestDTO));
    }

    // ✅ НОВИЙ ENDPOINT: Зміна статусу (без окремого DTO класу)
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Void> changeOfferStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String status = request.get("status");
        log.info("PATCH /api/offers/{}/status - Changing status to {}", id, status);
        service.changeOfferStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        service.deleteOffer(id);
        return ResponseEntity.noContent().build();
    }
}