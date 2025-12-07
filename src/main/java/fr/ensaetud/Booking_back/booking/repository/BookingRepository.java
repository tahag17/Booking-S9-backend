package fr.ensaetud.Booking_back.booking.repository;

import fr.ensaetud.Booking_back.booking.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

}
