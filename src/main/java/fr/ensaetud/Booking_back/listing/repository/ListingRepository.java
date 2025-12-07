package fr.ensaetud.Booking_back.listing.repository;


import fr.ensaetud.Booking_back.listing.domain.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListingRepository extends JpaRepository<Listing, Long> {


}
