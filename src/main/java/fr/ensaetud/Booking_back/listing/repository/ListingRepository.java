package fr.ensaetud.Booking_back.listing.repository;


import fr.ensaetud.Booking_back.listing.domain.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, Long> {
    @Query("SELECT l FROM Listing l LEFT JOIN FETCH l.pictures picture WHERE l.landlordPublicId = :landlordPublicId AND picture.isCover = true")
    List<Listing> findAllByLandlordPublicIdFetchCoverPicture(UUID landlordPublicId);


    long deleteByPublicIdAndLandlordPublicId(UUID publicId, UUID landlordPublicId);
}
