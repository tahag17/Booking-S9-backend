package fr.ensaetud.Booking_back.listing.mapper;

import fr.ensaetud.Booking_back.listing.application.dto.CreatedListingDTO;
import fr.ensaetud.Booking_back.listing.application.dto.SaveListingDTO;
import fr.ensaetud.Booking_back.listing.domain.Listing;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ListingPictureMapper.class})
public interface ListingMapper {
    @Mapping(target = "landlordPublicId",ignore = true)
    @Mapping(target = "publicId",ignore = true)
    @Mapping(target = "lastModifiedDate",ignore = true)
    @Mapping(target = "id",ignore = true)
    @Mapping(target = "createdDate",ignore = true)
    @Mapping(target = "pictures",ignore = true)
    @Mapping(target = "title",source = "description.title.value")
    @Mapping(target = "description",source = "description.description.value")
    @Mapping(target = "bedrooms",source = "info.bedrooms.value")
    @Mapping(target = "bathrooms",source = "info.baths.value")
    @Mapping(target = "beds",source = "info.beds.value")
    @Mapping(target = "guests",source = "info.guests.value")
    @Mapping(target = "price",source = "price.value")
    @Mapping(target = "bookingCategory",source = "category")

    Listing saveListingDTOToListing(SaveListingDTO saveListingDTO);


    CreatedListingDTO listingToCreatedListingDTO(Listing listing);

}
