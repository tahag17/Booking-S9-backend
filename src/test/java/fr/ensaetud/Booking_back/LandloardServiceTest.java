package fr.ensaetud.Booking_back;

import com.auth0.exception.Auth0Exception;
import fr.ensaetud.Booking_back.listing.application.LandlordService;
import fr.ensaetud.Booking_back.listing.application.PictureService;
import fr.ensaetud.Booking_back.listing.application.dto.CreatedListingDTO;
import fr.ensaetud.Booking_back.listing.application.dto.DisplayCardListingDTO;
import fr.ensaetud.Booking_back.listing.application.dto.ListingCreateBookingDTO;
import fr.ensaetud.Booking_back.listing.application.dto.SaveListingDTO;
import fr.ensaetud.Booking_back.listing.application.dto.sub.PictureDTO;
import fr.ensaetud.Booking_back.listing.application.dto.vo.PriceVO;
import fr.ensaetud.Booking_back.listing.domain.BookingCategory;
import fr.ensaetud.Booking_back.listing.domain.Listing;
import fr.ensaetud.Booking_back.listing.mapper.ListingMapper;
import fr.ensaetud.Booking_back.listing.repository.ListingRepository;
import fr.ensaetud.Booking_back.sharedkernel.service.State;
import fr.ensaetud.Booking_back.sharedkernel.service.StatusNotification;
import fr.ensaetud.Booking_back.user.application.Auth0Service;
import fr.ensaetud.Booking_back.user.application.UserService;
import fr.ensaetud.Booking_back.user.application.dto.ReadUserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class LandlordServiceTest {

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private ListingMapper listingMapper;

    @Mock
    private UserService userService;

    @Mock
    private Auth0Service auth0Service;

    @Mock
    private PictureService pictureService;

    @InjectMocks
    private LandlordService landlordService;

    private ReadUserDTO testLandlord;
    private Listing testListing;
    private SaveListingDTO testSaveListingDTO;
    private CreatedListingDTO testCreatedListingDTO;
    private DisplayCardListingDTO testDisplayCardListingDTO;
    private UUID testListingPublicId;
    private UUID testLandlordPublicId;


    @BeforeEach
    void setUp() {
        testLandlordPublicId = UUID.randomUUID();
        testListingPublicId = UUID.randomUUID();


        // Création du landlord
        testLandlord = new ReadUserDTO(
                testLandlordPublicId,
                "John",
                "Doe",
                "john.doe@example.com",
                "http://example.com/image.jpg",
                Set.of("ROLE_LANDLORD")
        );

        // Création du listing
        testListing = new Listing();
        testListing.setPublicId(testListingPublicId);
        testListing.setLandlordPublicId(testLandlordPublicId);
        testListing.setTitle("Beautiful Apartment");

        // Création du SaveListingDTO avec PictureDTO
        PictureDTO testPicture = new PictureDTO(
                "test-image".getBytes(),
                "image/jpeg",
                true
        );
        testSaveListingDTO = new SaveListingDTO();
        testSaveListingDTO.setPictures(List.of(testPicture));

        // Création du CreatedListingDTO
        testCreatedListingDTO = new CreatedListingDTO(testListingPublicId.toString());

        // Création du DisplayCardListingDTO avec la vraie structure
        PictureDTO coverPicture = new PictureDTO(
                "cover-image".getBytes(),
                "image/jpeg",
                true
        );
        PriceVO price = new PriceVO( 200);
        testDisplayCardListingDTO = new DisplayCardListingDTO(
                price,
                "Paris, France",
                coverPicture,
                BookingCategory.ROOMS,
                testListingPublicId
        );
    }

    @Test
    void create_ShouldCreateListing_WhenValidData() throws Auth0Exception {
        // Given
        when(listingMapper.saveListingDTOToListing(testSaveListingDTO)).thenReturn(testListing);
        when(userService.getAuthenticatedUserFromSecurityContext()).thenReturn(testLandlord);
        when(listingRepository.saveAndFlush(any(Listing.class))).thenReturn(testListing);
        when(listingMapper.listingToCreatedListingDTO(testListing)).thenReturn(testCreatedListingDTO);
        when(pictureService.saveAll(anyList(), any(Listing.class)))
                .thenReturn(Collections.emptyList());

        doNothing().when(auth0Service).addLandlordRoleToUser(testLandlord);

        // When
        CreatedListingDTO result = landlordService.create(testSaveListingDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.publicId()).isEqualTo(testListingPublicId.toString());

        verify(listingMapper).saveListingDTOToListing(testSaveListingDTO);
        verify(userService).getAuthenticatedUserFromSecurityContext();
        verify(listingRepository).saveAndFlush(any(Listing.class));
        verify(pictureService).saveAll(testSaveListingDTO.getPictures(), testListing);
        verify(auth0Service).addLandlordRoleToUser(testLandlord);
        verify(listingMapper).listingToCreatedListingDTO(testListing);
    }

    @Test
    void create_ShouldSetLandlordPublicId_WhenCreatingListing() throws Auth0Exception {
        // Given
        Listing newListing = new Listing();
        when(listingMapper.saveListingDTOToListing(testSaveListingDTO)).thenReturn(newListing);
        when(userService.getAuthenticatedUserFromSecurityContext()).thenReturn(testLandlord);
        when(listingRepository.saveAndFlush(any(Listing.class))).thenReturn(testListing);
        when(listingMapper.listingToCreatedListingDTO(any())).thenReturn(testCreatedListingDTO);

        // When
        landlordService.create(testSaveListingDTO);

        // Then
        assertThat(newListing.getLandlordPublicId()).isEqualTo(testLandlordPublicId);
        verify(listingRepository).saveAndFlush(newListing);
    }

    @Test
    void getAllProperties_ShouldReturnListOfProperties_WhenLandlordHasProperties() {
        // Given
        List<Listing> listings = List.of(testListing, testListing);
        List<DisplayCardListingDTO> expectedDTOs = List.of(testDisplayCardListingDTO, testDisplayCardListingDTO);

        when(listingRepository.findAllByLandlordPublicIdFetchCoverPicture(testLandlordPublicId))
                .thenReturn(listings);
        when(listingMapper.listingToDisplayCardListingDTOs(listings)).thenReturn(expectedDTOs);

        // When
        List<DisplayCardListingDTO> result = landlordService.getAllProperties(testLandlord);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(expectedDTOs);

        verify(listingRepository).findAllByLandlordPublicIdFetchCoverPicture(testLandlordPublicId);
        verify(listingMapper).listingToDisplayCardListingDTOs(listings);
    }

    @Test
    void getAllProperties_ShouldReturnEmptyList_WhenLandlordHasNoProperties() {
        // Given
        when(listingRepository.findAllByLandlordPublicIdFetchCoverPicture(testLandlordPublicId))
                .thenReturn(Collections.emptyList());
        when(listingMapper.listingToDisplayCardListingDTOs(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        // When
        List<DisplayCardListingDTO> result = landlordService.getAllProperties(testLandlord);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(listingRepository).findAllByLandlordPublicIdFetchCoverPicture(testLandlordPublicId);
    }

    @Test
    void delete_ShouldReturnSuccess_WhenDeletionIsSuccessful() {
        // Given
        when(listingRepository.deleteByPublicIdAndLandlordPublicId(testListingPublicId, testLandlordPublicId))
                .thenReturn(1L);

        // When
        State<UUID, String> result = landlordService.delete(testListingPublicId, testLandlord);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(StatusNotification.OK);
        assertThat(result.getValue()).isEqualTo(testListingPublicId);

        verify(listingRepository).deleteByPublicIdAndLandlordPublicId(testListingPublicId, testLandlordPublicId);
    }

    @Test
    void delete_ShouldReturnUnauthorized_WhenUserIsNotOwner() {
        // Given
        when(listingRepository.deleteByPublicIdAndLandlordPublicId(testListingPublicId, testLandlordPublicId))
                .thenReturn(0L);

        // When
        State<UUID, String> result = landlordService.delete(testListingPublicId, testLandlord);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(StatusNotification.UNAUTHORIZED);
        assertThat(result.getError()).isEqualTo("User not authorized to delete this listing");

        verify(listingRepository).deleteByPublicIdAndLandlordPublicId(testListingPublicId, testLandlordPublicId);
    }

    @Test
    void delete_ShouldReturnUnauthorized_WhenListingDoesNotExist() {
        // Given
        UUID nonExistentListingId = UUID.randomUUID();
        when(listingRepository.deleteByPublicIdAndLandlordPublicId(nonExistentListingId, testLandlordPublicId))
                .thenReturn(0L);

        // When
        State<UUID, String> result = landlordService.delete(nonExistentListingId, testLandlord);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(StatusNotification.UNAUTHORIZED);
        assertThat(result.getError()).isEqualTo("User not authorized to delete this listing");
    }

    @Test
    void getByListingPublicId_ShouldReturnListing_WhenListingExists() {
        // Given
        testListingPublicId = UUID.randomUUID();
        ListingCreateBookingDTO bookingDTO = new ListingCreateBookingDTO(testListingPublicId, new PriceVO(150));
        when(listingRepository.findByPublicId(testListingPublicId)).thenReturn(Optional.of(testListing));
        when(listingMapper.mapListingToListingCreateBookingDTO(testListing)).thenReturn(bookingDTO);

        // When
        Optional<ListingCreateBookingDTO> result = landlordService.getByListingPublicId(testListingPublicId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(bookingDTO);

        verify(listingRepository).findByPublicId(testListingPublicId);
        verify(listingMapper).mapListingToListingCreateBookingDTO(testListing);
    }

    @Test
    void getByListingPublicId_ShouldReturnEmpty_WhenListingDoesNotExist() {
        // Given
        UUID nonExistentListingId = UUID.randomUUID();
        when(listingRepository.findByPublicId(nonExistentListingId)).thenReturn(Optional.empty());

        // When
        Optional<ListingCreateBookingDTO> result = landlordService.getByListingPublicId(nonExistentListingId);

        // Then
        assertThat(result).isEmpty();

        verify(listingRepository).findByPublicId(nonExistentListingId);
        verify(listingMapper, never()).mapListingToListingCreateBookingDTO(any());
    }

    @Test
    void getCardDisplayByListingPublicId_ShouldReturnListings_WhenListingsExist() {
        // Given
        List<UUID> publicIds = List.of(testListingPublicId, UUID.randomUUID());
        List<Listing> listings = List.of(testListing, testListing);

        when(listingRepository.findAllByPublicIdIn(publicIds)).thenReturn(listings);
        when(listingMapper.listingToDisplayCardListingDTO(any(Listing.class)))
                .thenReturn(testDisplayCardListingDTO);

        // When
        List<DisplayCardListingDTO> result = landlordService.getCardDisplayByListingPublicId(publicIds);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(testDisplayCardListingDTO);

        verify(listingRepository).findAllByPublicIdIn(publicIds);
        verify(listingMapper, times(2)).listingToDisplayCardListingDTO(any(Listing.class));
    }

    @Test
    void getCardDisplayByListingPublicId_ShouldReturnEmptyList_WhenNoListingsFound() {
        // Given
        List<UUID> publicIds = List.of(UUID.randomUUID());
        when(listingRepository.findAllByPublicIdIn(publicIds)).thenReturn(Collections.emptyList());

        // When
        List<DisplayCardListingDTO> result = landlordService.getCardDisplayByListingPublicId(publicIds);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(listingRepository).findAllByPublicIdIn(publicIds);
        verify(listingMapper, never()).listingToDisplayCardListingDTO(any());
    }

    @Test
    void getByPublicIdAndLandlordPublicId_ShouldReturnListing_WhenListingExistsAndBelongsToLandlord() {
        // Given
        when(listingRepository.findOneByPublicIdAndLandlordPublicId(testListingPublicId, testLandlordPublicId))
                .thenReturn(Optional.of(testListing));
        when(listingMapper.listingToDisplayCardListingDTO(testListing))
                .thenReturn(testDisplayCardListingDTO);

        // When
        Optional<DisplayCardListingDTO> result = landlordService.getByPublicIdAndLandlordPublicId(
                testListingPublicId,
                testLandlordPublicId
        );

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testDisplayCardListingDTO);

        verify(listingRepository).findOneByPublicIdAndLandlordPublicId(testListingPublicId, testLandlordPublicId);
        verify(listingMapper).listingToDisplayCardListingDTO(testListing);
    }

    @Test
    void getByPublicIdAndLandlordPublicId_ShouldReturnEmpty_WhenListingDoesNotExist() {
        // Given
        UUID nonExistentListingId = UUID.randomUUID();
        when(listingRepository.findOneByPublicIdAndLandlordPublicId(nonExistentListingId, testLandlordPublicId))
                .thenReturn(Optional.empty());

        // When
        Optional<DisplayCardListingDTO> result = landlordService.getByPublicIdAndLandlordPublicId(
                nonExistentListingId,
                testLandlordPublicId
        );

        // Then
        assertThat(result).isEmpty();

        verify(listingRepository).findOneByPublicIdAndLandlordPublicId(nonExistentListingId, testLandlordPublicId);
        verify(listingMapper, never()).listingToDisplayCardListingDTO(any());
    }

    @Test
    void getByPublicIdAndLandlordPublicId_ShouldReturnEmpty_WhenListingBelongsToDifferentLandlord() {
        // Given
        UUID otherLandlordId = UUID.randomUUID();
        when(listingRepository.findOneByPublicIdAndLandlordPublicId(testListingPublicId, otherLandlordId))
                .thenReturn(Optional.empty());

        // When
        Optional<DisplayCardListingDTO> result = landlordService.getByPublicIdAndLandlordPublicId(
                testListingPublicId,
                otherLandlordId
        );

        // Then
        assertThat(result).isEmpty();

        verify(listingRepository).findOneByPublicIdAndLandlordPublicId(testListingPublicId, otherLandlordId);
        verify(listingMapper, never()).listingToDisplayCardListingDTO(any());
    }

    @Test
    void create_ShouldHandleAuth0Exception_WhenAddingLandlordRoleFails() throws Auth0Exception {
        // Given
        when(listingMapper.saveListingDTOToListing(testSaveListingDTO)).thenReturn(testListing);
        when(userService.getAuthenticatedUserFromSecurityContext()).thenReturn(testLandlord);
        when(listingRepository.saveAndFlush(any(Listing.class))).thenReturn(testListing);
        when(listingMapper.listingToCreatedListingDTO(testListing)).thenReturn(testCreatedListingDTO);
        doThrow(new RuntimeException("Auth0 error")).when(auth0Service).addLandlordRoleToUser(testLandlord);

        // When & Then
        CreatedListingDTO result = landlordService.create(testSaveListingDTO);

        // Then
        assertThat(result).isEqualTo(testCreatedListingDTO);

        verify(listingRepository).saveAndFlush(any(Listing.class));
        verify(pictureService).saveAll(testSaveListingDTO.getPictures(), testListing);
        verify(auth0Service).addLandlordRoleToUser(testLandlord);
    }
}
