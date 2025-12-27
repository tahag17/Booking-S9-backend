package fr.ensaetud.Booking_back.listing.application.dto;

import fr.ensaetud.Booking_back.booking.application.dto.BookedDateDTO;
import fr.ensaetud.Booking_back.listing.application.dto.sub.ListingInfoDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record SearchDTO(@Valid BookedDateDTO dates,
                        @Valid ListingInfoDTO infos,
                        @NotEmpty String location) {
}
