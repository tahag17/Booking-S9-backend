package fr.ensaetud.Booking_back.listing.application.dto.vo;

import jakarta.validation.constraints.NotNull;

public record GuestsVO(@NotNull(message = "Guests must be provided") int value) {
}
