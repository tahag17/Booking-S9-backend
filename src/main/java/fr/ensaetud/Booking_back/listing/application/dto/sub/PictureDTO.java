package fr.ensaetud.Booking_back.listing.application.dto.sub;

import jakarta.validation.constraints.NotNull;

public record PictureDTO(@NotNull byte[] file,
                         @NotNull String fileContentType,
                         @NotNull boolean isCover) {
}
