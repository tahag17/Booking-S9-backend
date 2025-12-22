package fr.ensaetud.Booking_back.listing.application.dto.vo;

import jakarta.validation.constraints.NotNull;

public record TitleVO(@NotNull(message = "Title must be provided") int value) {
}
