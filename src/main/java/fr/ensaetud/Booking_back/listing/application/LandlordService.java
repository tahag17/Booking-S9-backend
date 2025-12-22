
package fr.ensaetud.Booking_back.listing.application;

import com.auth0.exception.Auth0Exception;
import fr.ensaetud.Booking_back.listing.application.dto.CreatedListingDTO;
import fr.ensaetud.Booking_back.listing.application.dto.SaveListingDTO;
import fr.ensaetud.Booking_back.listing.domain.Listing;
import fr.ensaetud.Booking_back.listing.mapper.ListingMapper;
import fr.ensaetud.Booking_back.listing.repository.ListingRepository;
import fr.ensaetud.Booking_back.user.domain.application.Auth0Service;
import fr.ensaetud.Booking_back.user.domain.application.UserService;
import fr.ensaetud.Booking_back.user.domain.application.dto.ReadUserDTO;
import org.apache.commons.collections4.Put;
import org.springframework.stereotype.Service;

@Service
public class LandlordService {
    private final ListingRepository listingRepository;
    private final ListingMapper listingMapper;
    private final UserService userService;
    private final Auth0Service auth0Service;
    private final PictureService pictureService;

    public LandlordService(ListingRepository listingRepository, ListingMapper listingMapper, UserService userService, Auth0Service auth0Service, PictureService pictureService) {
        this.listingRepository = listingRepository;
        this.listingMapper = listingMapper;
        this.userService = userService;
        this.auth0Service = auth0Service;
        this.pictureService = pictureService;
    }

    public CreatedListingDTO create(SaveListingDTO saveListingDTO) throws Auth0Exception {
        Listing newListing=listingMapper.saveListingDTOToListing(saveListingDTO);
        ReadUserDTO userConnected=userService.getAuthenticatedUserFromSecurityContext();
        newListing.setLandlordPublicId(userConnected.publicId());
        Listing savedListing=listingRepository.saveAndFlush(newListing);
        pictureService.saveAll(saveListingDTO.getPictures(),savedListing);
        auth0Service.addLandlordRoleToUser(userConnected);
        return listingMapper.listingToCreatedListingDTO(savedListing);

    }
}

