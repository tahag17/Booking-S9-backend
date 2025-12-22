package fr.ensaetud.Booking_back.listing.application.dto.sub;

import fr.ensaetud.Booking_back.listing.application.dto.vo.DescriptionVO;
import fr.ensaetud.Booking_back.listing.application.dto.vo.TitleVO;
import jakarta.validation.constraints.NotNull;

public record DescriptionDTO(
        @NotNull DescriptionVO description,
        @NotNull TitleVO title
        ) {
}
