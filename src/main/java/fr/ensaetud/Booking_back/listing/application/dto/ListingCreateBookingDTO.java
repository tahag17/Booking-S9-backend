package fr.ensaetud.Booking_back.listing.application.dto;

import fr.ensaetud.Booking_back.listing.application.dto.vo.PriceVO;

import java.util.UUID;

public record ListingCreateBookingDTO(
        UUID listingPublicId, PriceVO price) {
}
