
package fr.ensaetud.Booking_back.listing.application;

import com.auth0.exception.Auth0Exception;
import fr.ensaetud.Booking_back.listing.application.dto.CreatedListingDTO;
import fr.ensaetud.Booking_back.listing.application.dto.DisplayCardListingDTO;
import fr.ensaetud.Booking_back.listing.application.dto.ListingCreateBookingDTO;
import fr.ensaetud.Booking_back.listing.application.dto.SaveListingDTO;
import fr.ensaetud.Booking_back.listing.domain.Listing;
import fr.ensaetud.Booking_back.listing.mapper.ListingMapper;
import fr.ensaetud.Booking_back.listing.repository.ListingRepository;
import fr.ensaetud.Booking_back.sharedkernel.service.State;
import fr.ensaetud.Booking_back.user.application.Auth0Service;
import fr.ensaetud.Booking_back.user.application.UserService;
import fr.ensaetud.Booking_back.user.application.dto.ReadUserDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    public CreatedListingDTO create(SaveListingDTO saveListingDTO) {
        Listing newListing = listingMapper.saveListingDTOToListing(saveListingDTO);

        ReadUserDTO userConnected = userService.getAuthenticatedUserFromSecurityContext();
        newListing.setLandlordPublicId(userConnected.publicId());

        Listing savedListing = listingRepository.saveAndFlush(newListing);

        pictureService.saveAll(saveListingDTO.getPictures(), savedListing);

        auth0Service.addLandlordRoleToUser(userConnected);

        return listingMapper.listingToCreatedListingDTO(savedListing);
    }

    @Transactional(readOnly = true)
    public List<DisplayCardListingDTO> getAllProperties(ReadUserDTO landlord) {
        List<Listing> properties = listingRepository.findAllByLandlordPublicIdFetchCoverPicture(landlord.publicId());
        return listingMapper.listingToDisplayCardListingDTOs(properties);
    }

    @Transactional
    public State<UUID, String> delete(UUID publicId, ReadUserDTO landlord) {
        long deletedSuccessfuly = listingRepository.deleteByPublicIdAndLandlordPublicId(publicId, landlord.publicId());
        if (deletedSuccessfuly > 0) {
            return State.<UUID, String>builder().forSuccess(publicId);
        } else {
            return State.<UUID, String>builder().forUnauthorized("User not authorized to delete this listing");
        }
    }

    public Optional<ListingCreateBookingDTO> getByListingPublicId(UUID publicId) {
        return listingRepository.findByPublicId(publicId).map(listingMapper::mapListingToListingCreateBookingDTO);
    }

    public List<DisplayCardListingDTO> getCardDisplayByListingPublicId(List<UUID> allListingPublicIDs) {
        return listingRepository.findAllByPublicIdIn(allListingPublicIDs)
                .stream()
                .map(listingMapper::listingToDisplayCardListingDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<DisplayCardListingDTO> getByPublicIdAndLandlordPublicId(UUID listingPublicId, UUID landlordPublicId) {
        return listingRepository.findOneByPublicIdAndLandlordPublicId(listingPublicId, landlordPublicId)
                .map(listingMapper::listingToDisplayCardListingDTO);
    }
}