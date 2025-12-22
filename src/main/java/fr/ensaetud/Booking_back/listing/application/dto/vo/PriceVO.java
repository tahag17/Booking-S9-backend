package fr.ensaetud.Booking_back.listing.application.dto.vo;

import jakarta.validation.constraints.NotNull;

public record PriceVO(@NotNull(message = "Price must be provided") int value) {
}
