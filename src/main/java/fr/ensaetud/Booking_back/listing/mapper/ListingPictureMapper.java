package fr.ensaetud.Booking_back.listing.mapper;

import fr.ensaetud.Booking_back.listing.application.dto.sub.PictureDTO;
import fr.ensaetud.Booking_back.listing.domain.ListingPicture;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface ListingPictureMapper {

    @Mapping(target = "listingId",ignore = true)
    @Mapping(target = "listing",ignore = true)

    Set<ListingPicture> pictureDTOsToListingPictures(List<PictureDTO> pictureDTOs);

    List<PictureDTO> listingPicturesToPictureDTOs(Set<ListingPicture> listingPictures);
}
