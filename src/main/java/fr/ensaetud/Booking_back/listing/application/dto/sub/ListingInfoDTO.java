package fr.ensaetud.Booking_back.listing.application.dto.sub;

import fr.ensaetud.Booking_back.listing.application.dto.vo.BathsVO;
import fr.ensaetud.Booking_back.listing.application.dto.vo.BedroomsVO;
import fr.ensaetud.Booking_back.listing.application.dto.vo.BedsVO;
import fr.ensaetud.Booking_back.listing.application.dto.vo.GuestsVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ListingInfoDTO(@NotNull @Valid GuestsVO guests,
                             @NotNull @Valid BedsVO beds,
                             @NotNull @Valid BedroomsVO bedrooms,
                             @NotNull @Valid BathsVO baths) {
}
