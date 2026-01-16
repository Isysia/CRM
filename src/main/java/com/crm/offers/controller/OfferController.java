package com.crm.offers.controller;

import com.crm.offers.dto.OfferRequestDTO;
import com.crm.offers.dto.OfferResponseDTO;
import com.crm.offers.mapper.OfferMapper;
import com.crm.offers.model.Offer;
import com.crm.offers.service.OfferService;
// ↓↓↓ ДОДАТИ ЦІ IMPORTS ↓↓↓
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
// ↑↑↑ КІНЕЦЬ НОВИХ IMPORTS ↑↑↑
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/offers")
@Tag(name = "Offers", description = "Commercial offers management APIs")  // ← ДОДАТИ
@SecurityRequirement(name = "basicAuth")  // ← ДОДАТИ
public class OfferController {

    private final OfferService service;
    private final OfferMapper mapper;

    public OfferController(OfferService service, OfferMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(summary = "Get all offers")  // ← ДОДАТИ
    @GetMapping
    public ResponseEntity<List<OfferResponseDTO>> getAllOffers() {
        List<OfferResponseDTO> offers = service.getAllOffers()
                .stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(offers);
    }

    @Operation(summary = "Get offer by ID")  // ← ДОДАТИ
    @ApiResponses(value = {  // ← ДОДАТИ
            @ApiResponse(responseCode = "200", description = "Offer found"),
            @ApiResponse(responseCode = "404", description = "Offer not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OfferResponseDTO> getOfferById(
            @Parameter(description = "Offer ID") @PathVariable Long id) {  // ← ЗМІНИТИ
        Offer offer = service.getOfferById(id);
        return ResponseEntity.ok(mapper.toResponseDTO(offer));
    }

    @Operation(summary = "Get offers by customer ID")  // ← ДОДАТИ
    @ApiResponses(value = {  // ← ДОДАТИ
            @ApiResponse(responseCode = "200", description = "Offers found"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OfferResponseDTO>> getOffersByCustomerId(
            @Parameter(description = "Customer ID") @PathVariable Long customerId) {  // ← ЗМІНИТИ
        List<OfferResponseDTO> offers = service.getOffersByCustomerId(customerId)
                .stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(offers);
    }

    @Operation(summary = "Create a new offer", description = "Requires MANAGER or ADMIN role")  // ← ДОДАТИ
    @ApiResponses(value = {  // ← ДОДАТИ
            @ApiResponse(responseCode = "201", description = "Offer created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PostMapping
    public ResponseEntity<OfferResponseDTO> createOffer(@Valid @RequestBody OfferRequestDTO requestDTO) {
        Offer offer = mapper.toEntity(requestDTO);
        Offer created = service.createOffer(offer, requestDTO.getCustomerId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapper.toResponseDTO(created));
    }

    @Operation(summary = "Update offer", description = "Requires MANAGER or ADMIN role")  // ← ДОДАТИ
    @ApiResponses(value = {  // ← ДОДАТИ
            @ApiResponse(responseCode = "200", description = "Offer updated"),
            @ApiResponse(responseCode = "404", description = "Offer or Customer not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<OfferResponseDTO> updateOffer(
            @Parameter(description = "Offer ID") @PathVariable Long id,  // ← ЗМІНИТИ
            @Valid @RequestBody OfferRequestDTO requestDTO
    ) {
        Offer offerData = mapper.toEntity(requestDTO);
        Offer updated = service.updateOffer(id, offerData, requestDTO.getCustomerId());

        return ResponseEntity.ok(mapper.toResponseDTO(updated));
    }

    @Operation(summary = "Delete offer", description = "Requires ADMIN role")  // ← ДОДАТИ
    @ApiResponses(value = {  // ← ДОДАТИ
            @ApiResponse(responseCode = "204", description = "Offer deleted"),
            @ApiResponse(responseCode = "404", description = "Offer not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(
            @Parameter(description = "Offer ID") @PathVariable Long id) {  // ← ЗМІНИТИ
        service.deleteOffer(id);
        return ResponseEntity.noContent().build();
    }
}