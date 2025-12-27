package fr.ensaetud.Booking_back.booking.mapper;

import fr.ensaetud.Booking_back.booking.application.dto.BookedDateDTO;
import fr.ensaetud.Booking_back.booking.application.dto.NewBookingDTO;
import fr.ensaetud.Booking_back.booking.domain.Booking;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    Booking newBookingToBooking(NewBookingDTO newBookingDTO);

    BookedDateDTO bookingToCheckAvailability(Booking booking);

}
