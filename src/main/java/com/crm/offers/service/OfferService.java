package com.crm.offers.service;

import com.crm.offers.dto.OfferRequestDTO;
import com.crm.offers.dto.OfferResponseDTO;

import java.util.List;

public interface OfferService {

    OfferResponseDTO createOffer(OfferRequestDTO requestDTO);

    OfferResponseDTO getOfferById(Long id);

    List<OfferResponseDTO> getAllOffers();

    List<OfferResponseDTO> getOffersByCustomerId(Long customerId);

    OfferResponseDTO updateOffer(Long id, OfferRequestDTO requestDTO);

    void deleteOffer(Long id);

    void changeOfferStatus(Long id, String status);
}