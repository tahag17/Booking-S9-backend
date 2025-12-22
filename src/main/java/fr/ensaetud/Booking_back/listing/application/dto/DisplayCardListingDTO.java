package fr.ensaetud.Booking_back.listing.application.dto;

import fr.ensaetud.Booking_back.listing.application.dto.sub.PictureDTO;
import fr.ensaetud.Booking_back.listing.application.dto.vo.PriceVO;
import fr.ensaetud.Booking_back.listing.domain.BookingCategory;

import java.util.UUID;

public record DisplayCardListingDTO(PriceVO price,
                                    String location,
                                    PictureDTO cover,
                                    BookingCategory bookingCategory,
                                    UUID publicId) {
}
