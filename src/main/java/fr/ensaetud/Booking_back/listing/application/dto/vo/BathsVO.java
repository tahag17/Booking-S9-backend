package fr.ensaetud.Booking_back.listing.application.dto.vo;

import jakarta.validation.constraints.NotNull;

public record BathsVO(@NotNull(message = "Baths must be provided") int value) {
}
