package fr.ensaetud.Booking_back.listing.repository;

import fr.ensaetud.Booking_back.listing.domain.ListingPicture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListingPictureRepository extends JpaRepository<ListingPicture, Long> {
}
