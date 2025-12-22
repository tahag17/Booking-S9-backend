package fr.ensaetud.Booking_back.listing.application.dto.vo;

import jakarta.validation.constraints.NotNull;

public record BedroomsVO(@NotNull(message = "Bedroom must be provided") int value) {
}
