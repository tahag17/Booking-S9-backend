package fr.ensaetud.Booking_back;

import fr.ensaetud.Booking_back.listing.application.TenantService;
import org.springframework.test.context.ActiveProfiles;

import fr.ensaetud.Booking_back.booking.application.BookingService;
import fr.ensaetud.Booking_back.booking.application.dto.BookedDateDTO;
import fr.ensaetud.Booking_back.listing.application.dto.DisplayCardListingDTO;
import fr.ensaetud.Booking_back.listing.application.dto.DisplayListingDTO;
import fr.ensaetud.Booking_back.listing.application.dto.SearchDTO;
import fr.ensaetud.Booking_back.listing.application.dto.sub.DescriptionDTO;
import fr.ensaetud.Booking_back.listing.application.dto.sub.ListingInfoDTO;
import fr.ensaetud.Booking_back.listing.application.dto.sub.PictureDTO;
import fr.ensaetud.Booking_back.listing.application.dto.vo.*;
import fr.ensaetud.Booking_back.listing.domain.BookingCategory;
import fr.ensaetud.Booking_back.listing.domain.Listing;
import fr.ensaetud.Booking_back.listing.mapper.ListingMapper;
import fr.ensaetud.Booking_back.listing.repository.ListingRepository;
import fr.ensaetud.Booking_back.sharedkernel.service.State;
import fr.ensaetud.Booking_back.sharedkernel.service.StatusNotification;
import fr.ensaetud.Booking_back.user.application.UserService;
import fr.ensaetud.Booking_back.user.application.dto.ReadUserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class TenantServiceTest {

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private ListingMapper listingMapper;

    @Mock
    private UserService userService;

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private TenantService tenantService;

    private UUID listingPublicId;
    private UUID landlordPublicId;
    private Listing listing;
    private DisplayCardListingDTO displayCardListingDTO;
    private DisplayListingDTO displayListingDTO;
    private ReadUserDTO landlordUser;
    private Pageable pageable;
    private PictureDTO pictureDTO;

    @BeforeEach
    void setUp() {
        listingPublicId = UUID.randomUUID();
        landlordPublicId = UUID.randomUUID();
        pageable = PageRequest.of(0, 10, Sort.by("createdDate").descending());

        pictureDTO = new PictureDTO(
                new byte[]{1, 2, 3},
                "image/jpeg",
                true
        );

        listing = new Listing();
        listing.setPublicId(listingPublicId);
        listing.setTitle("Beautiful Apartment");
        listing.setDescription("A beautiful apartment in Paris");
        listing.setLocation("Paris, France");
        listing.setPrice(100);
        listing.setGuests(4);
        listing.setBedrooms(2);
        listing.setBeds(2);
        listing.setBathrooms(1);
        listing.setBookingCategory(BookingCategory.ALL);
        listing.setLandlordPublicId(landlordPublicId);

        displayCardListingDTO = new DisplayCardListingDTO(
                new PriceVO(100),
                "Paris, France",
                pictureDTO,
                BookingCategory.ALL,
                listingPublicId
        );

        displayListingDTO = new DisplayListingDTO();
        displayListingDTO.setDescription(new DescriptionDTO(
                new DescriptionVO("A beautiful apartment in Paris"),
                new TitleVO("Beautiful Apartment")
        ));
        displayListingDTO.setLocation("Paris, France");
        displayListingDTO.setPrice(new PriceVO(100));
        displayListingDTO.setCategory(BookingCategory.ALL);
        displayListingDTO.setInfos(new ListingInfoDTO(
                new GuestsVO(4),
                new BedsVO(2),
                new BedroomsVO(2),
                new BathsVO(1)
        ));
        displayListingDTO.setPictures(List.of(pictureDTO));

        landlordUser = new ReadUserDTO(
                landlordPublicId,
                "Jane",
                "Smith",
                "jane.smith@example.com",
                "http://image.url",
                Set.of("ROLE_LANDLORD")
        );
    }

    @Test
    void getAllByCategory_shouldReturnAllListings_whenCategoryIsAll() {

        List<Listing> listings = Arrays.asList(listing);
        Page<Listing> listingPage = new PageImpl<>(listings, pageable, listings.size());

        when(listingRepository.findAllWithCoverOnly(pageable)).thenReturn(listingPage);
        when(listingMapper.listingToDisplayCardListingDTO(listing)).thenReturn(displayCardListingDTO);


        Page<DisplayCardListingDTO> result = tenantService.getAllByCategory(pageable, BookingCategory.ALL);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(displayCardListingDTO);
        assertThat(result.getContent().get(0).publicId()).isEqualTo(listingPublicId);
        assertThat(result.getContent().get(0).location()).isEqualTo("Paris, France");

        verify(listingRepository).findAllWithCoverOnly(pageable);
        verify(listingRepository, never()).findAllByBookingCategoryWithCoverOnly(any(), any());
        verify(listingMapper).listingToDisplayCardListingDTO(listing);
    }

    @Test
    void getAllByCategory_shouldReturnFilteredListings_whenSpecificCategoryProvided() {

        BookingCategory category = BookingCategory.BEACH;
        listing.setBookingCategory(category);

        DisplayCardListingDTO beachListing = new DisplayCardListingDTO(
                new PriceVO(200),
                "Nice, France",
                pictureDTO,
                BookingCategory.BEACH,
                listingPublicId
        );

        List<Listing> listings = Arrays.asList(listing);
        Page<Listing> listingPage = new PageImpl<>(listings, pageable, listings.size());

        when(listingRepository.findAllByBookingCategoryWithCoverOnly(pageable, category))
                .thenReturn(listingPage);
        when(listingMapper.listingToDisplayCardListingDTO(listing)).thenReturn(beachListing);


        Page<DisplayCardListingDTO> result = tenantService.getAllByCategory(pageable, category);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).bookingCategory()).isEqualTo(BookingCategory.BEACH);

        verify(listingRepository).findAllByBookingCategoryWithCoverOnly(pageable, category);
        verify(listingRepository, never()).findAllWithCoverOnly(any());
    }

    @Test
    void getAllByCategory_shouldReturnEmptyPage_whenNoListingsFound() {
        Page<Listing> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(listingRepository.findAllWithCoverOnly(pageable)).thenReturn(emptyPage);

        Page<DisplayCardListingDTO> result = tenantService.getAllByCategory(pageable, BookingCategory.ALL);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        verify(listingRepository).findAllWithCoverOnly(pageable);
        verify(listingMapper, never()).listingToDisplayCardListingDTO(any());
    }

    @Test
    void getOne_shouldReturnListingWithLandlord_whenListingExists() {

        when(listingRepository.findByPublicId(listingPublicId)).thenReturn(Optional.of(listing));
        when(listingMapper.listingToDisplayListingDTO(listing)).thenReturn(displayListingDTO);
        when(userService.getByPublicId(landlordPublicId)).thenReturn(Optional.of(landlordUser));


        State<DisplayListingDTO, String> result = tenantService.getOne(listingPublicId);


        assertThat(result.getStatus()).isEqualTo(StatusNotification.OK);
        assertThat(result.getValue()).isNotNull();
        assertThat(result.getValue().getLocation()).isEqualTo("Paris, France");
        assertThat(result.getValue().getLandlord()).isNotNull();
        assertThat(result.getValue().getLandlord().firstname()).isEqualTo("Jane");
        assertThat(result.getValue().getLandlord().imageUrl()).isEqualTo("http://image.url");
        assertThat(result.getError()).isNull();

        verify(listingRepository).findByPublicId(listingPublicId);
        verify(listingMapper).listingToDisplayListingDTO(listing);
        verify(userService).getByPublicId(landlordPublicId);
    }

    @Test
    void getOne_shouldReturnError_whenListingDoesNotExist() {

        when(listingRepository.findByPublicId(listingPublicId)).thenReturn(Optional.empty());


        State<DisplayListingDTO, String> result = tenantService.getOne(listingPublicId);


        assertThat(result.getStatus()).isEqualTo(StatusNotification.ERROR);
        assertThat(result.getValue()).isNull();
        assertThat(result.getError()).isEqualTo(
                String.format("Listing doesn't exist for publicId: %s", listingPublicId)
        );

        verify(listingRepository).findByPublicId(listingPublicId);
        verify(listingMapper, never()).listingToDisplayListingDTO(any());
        verify(userService, never()).getByPublicId(any());
    }

    @Test
    void search_shouldReturnAvailableListings_excludingBookedOnes() {

        UUID listing2PublicId = UUID.randomUUID();
        UUID listing3PublicId = UUID.randomUUID();

        Listing listing2 = new Listing();
        listing2.setPublicId(listing2PublicId);
        listing2.setLocation("Paris, France");
        listing2.setGuests(4);
        listing2.setBedrooms(2);
        listing2.setBeds(2);
        listing2.setBathrooms(1);

        Listing listing3 = new Listing();
        listing3.setPublicId(listing3PublicId);
        listing3.setLocation("Paris, France");
        listing3.setGuests(4);
        listing3.setBedrooms(2);
        listing3.setBeds(2);
        listing3.setBathrooms(1);

        List<Listing> allListings = Arrays.asList(listing, listing2, listing3);
        Page<Listing> listingPage = new PageImpl<>(allListings, pageable, allListings.size());

        OffsetDateTime startDate = OffsetDateTime.now().plusDays(1);
        OffsetDateTime endDate = OffsetDateTime.now().plusDays(5);
        BookedDateDTO bookedDateDTO = new BookedDateDTO(startDate, endDate);

        SearchDTO searchDTO = new SearchDTO(
                bookedDateDTO,
                new ListingInfoDTO(
                        new GuestsVO(4),
                        new BedsVO(2),
                        new BedroomsVO(2),
                        new BathsVO(1)
                ),
                "Paris, France"
        );

        // listing2 is already booked
        List<UUID> bookedListingIds = Arrays.asList(listing2PublicId);

        DisplayCardListingDTO displayCard1 = new DisplayCardListingDTO(
                new PriceVO(100),
                "Paris, France",
                pictureDTO,
                BookingCategory.ALL,
                listingPublicId
        );

        DisplayCardListingDTO displayCard3 = new DisplayCardListingDTO(
                new PriceVO(150),
                "Paris, France",
                pictureDTO,
                BookingCategory.ALL,
                listing3PublicId
        );

        when(listingRepository.findAllByLocationAndBathroomsAndBedroomsAndGuestsAndBeds(
                pageable, "Paris, France", 1, 2, 4, 2
        )).thenReturn(listingPage);

        when(bookingService.getBookingMatchByListingIdsAndBookedDate(anyList(), eq(bookedDateDTO)))
                .thenReturn(bookedListingIds);

        when(listingMapper.listingToDisplayCardListingDTO(listing)).thenReturn(displayCard1);
        when(listingMapper.listingToDisplayCardListingDTO(listing3)).thenReturn(displayCard3);


        Page<DisplayCardListingDTO> result = tenantService.search(pageable, searchDTO);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(DisplayCardListingDTO::publicId)
                .containsExactlyInAnyOrder(listingPublicId, listing3PublicId)
                .doesNotContain(listing2PublicId);

        verify(listingRepository).findAllByLocationAndBathroomsAndBedroomsAndGuestsAndBeds(
                pageable, "Paris, France", 1, 2, 4, 2
        );
        verify(bookingService).getBookingMatchByListingIdsAndBookedDate(
                argThat(list -> list.size() == 3 && list.containsAll(Arrays.asList(listingPublicId, listing2PublicId, listing3PublicId))),
                eq(bookedDateDTO)
        );
        verify(listingMapper, times(2)).listingToDisplayCardListingDTO(any());
    }

    @Test
    void search_shouldReturnAllListings_whenNoneAreBooked() {

        List<Listing> allListings = Arrays.asList(listing);
        Page<Listing> listingPage = new PageImpl<>(allListings, pageable, allListings.size());

        OffsetDateTime startDate = OffsetDateTime.now().plusDays(1);
        OffsetDateTime endDate = OffsetDateTime.now().plusDays(5);
        BookedDateDTO bookedDateDTO = new BookedDateDTO(startDate, endDate);

        SearchDTO searchDTO = new SearchDTO(
                bookedDateDTO,
                new ListingInfoDTO(
                        new GuestsVO(4),
                        new BedsVO(2),
                        new BedroomsVO(2),
                        new BathsVO(1)
                ),
                "Paris, France"
        );

        when(listingRepository.findAllByLocationAndBathroomsAndBedroomsAndGuestsAndBeds(
                pageable, "Paris, France", 1, 2, 4, 2
        )).thenReturn(listingPage);

        when(bookingService.getBookingMatchByListingIdsAndBookedDate(anyList(), eq(bookedDateDTO)))
                .thenReturn(Collections.emptyList());

        when(listingMapper.listingToDisplayCardListingDTO(listing)).thenReturn(displayCardListingDTO);


        Page<DisplayCardListingDTO> result = tenantService.search(pageable, searchDTO);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).publicId()).isEqualTo(listingPublicId);

        verify(bookingService).getBookingMatchByListingIdsAndBookedDate(anyList(), eq(bookedDateDTO));
    }

    @Test
    void search_shouldReturnEmptyPage_whenAllListingsAreBooked() {

        List<Listing> allListings = Arrays.asList(listing);
        Page<Listing> listingPage = new PageImpl<>(allListings, pageable, allListings.size());

        OffsetDateTime startDate = OffsetDateTime.now().plusDays(1);
        OffsetDateTime endDate = OffsetDateTime.now().plusDays(5);
        BookedDateDTO bookedDateDTO = new BookedDateDTO(startDate, endDate);

        SearchDTO searchDTO = new SearchDTO(
                bookedDateDTO,
                new ListingInfoDTO(
                        new GuestsVO(4),
                        new BedsVO(2),
                        new BedroomsVO(2),
                        new BathsVO(1)
                ),
                "Paris, France"
        );


        List<UUID> bookedListingIds = Arrays.asList(listingPublicId);

        when(listingRepository.findAllByLocationAndBathroomsAndBedroomsAndGuestsAndBeds(
                pageable, "Paris, France", 1, 2, 4, 2
        )).thenReturn(listingPage);

        when(bookingService.getBookingMatchByListingIdsAndBookedDate(anyList(), eq(bookedDateDTO)))
                .thenReturn(bookedListingIds);


        Page<DisplayCardListingDTO> result = tenantService.search(pageable, searchDTO);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        verify(listingMapper, never()).listingToDisplayCardListingDTO(any());
    }

    @Test
    void search_shouldReturnEmptyPage_whenNoListingsMatchCriteria() {

        Page<Listing> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        OffsetDateTime startDate = OffsetDateTime.now().plusDays(1);
        OffsetDateTime endDate = OffsetDateTime.now().plusDays(5);
        BookedDateDTO bookedDateDTO = new BookedDateDTO(startDate, endDate);

        SearchDTO searchDTO = new SearchDTO(
                bookedDateDTO,
                new ListingInfoDTO(
                        new GuestsVO(10),
                        new BedsVO(5),
                        new BedroomsVO(5),
                        new BathsVO(3)
                ),
                "Tokyo, Japan"
        );

        when(listingRepository.findAllByLocationAndBathroomsAndBedroomsAndGuestsAndBeds(
                pageable, "Tokyo, Japan", 3, 5, 10, 5
        )).thenReturn(emptyPage);


        Page<DisplayCardListingDTO> result = tenantService.search(pageable, searchDTO);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        verify(listingMapper, never()).listingToDisplayCardListingDTO(any());
    }

    @Test
    void getAllByCategory_shouldHandleMultiplePages() {
        List<Listing> listings = Arrays.asList(listing);
        Page<Listing> listingPage = new PageImpl<>(listings, pageable, 50); // Total 50 elements

        when(listingRepository.findAllWithCoverOnly(pageable)).thenReturn(listingPage);
        when(listingMapper.listingToDisplayCardListingDTO(listing)).thenReturn(displayCardListingDTO);


        Page<DisplayCardListingDTO> result = tenantService.getAllByCategory(pageable, BookingCategory.ALL);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(50);
        assertThat(result.getTotalPages()).isEqualTo(5); // 50/10 pages

        verify(listingRepository).findAllWithCoverOnly(pageable);
    }

    @Test
    void search_shouldHandleDifferentSearchCriteria() {
        Listing luxuryListing = new Listing();
        luxuryListing.setPublicId(UUID.randomUUID());
        luxuryListing.setLocation("Monaco");
        luxuryListing.setGuests(8);
        luxuryListing.setBedrooms(4);
        luxuryListing.setBeds(6);
        luxuryListing.setBathrooms(3);
        luxuryListing.setPrice(500);

        List<Listing> listings = Arrays.asList(luxuryListing);
        Page<Listing> listingPage = new PageImpl<>(listings, pageable, listings.size());

        OffsetDateTime startDate = OffsetDateTime.now().plusDays(10);
        OffsetDateTime endDate = OffsetDateTime.now().plusDays(17);
        BookedDateDTO bookedDateDTO = new BookedDateDTO(startDate, endDate);

        SearchDTO searchDTO = new SearchDTO(
                bookedDateDTO,
                new ListingInfoDTO(
                        new GuestsVO(8),
                        new BedsVO(6),
                        new BedroomsVO(4),
                        new BathsVO(3)
                ),
                "Monaco"
        );

        DisplayCardListingDTO luxuryCard = new DisplayCardListingDTO(
                new PriceVO(500),
                "Monaco",
                pictureDTO,
                BookingCategory.LUXES,
                luxuryListing.getPublicId()
        );

        when(listingRepository.findAllByLocationAndBathroomsAndBedroomsAndGuestsAndBeds(
                pageable, "Monaco", 3, 4, 8, 6
        )).thenReturn(listingPage);

        when(bookingService.getBookingMatchByListingIdsAndBookedDate(anyList(), eq(bookedDateDTO)))
                .thenReturn(Collections.emptyList());

        when(listingMapper.listingToDisplayCardListingDTO(luxuryListing)).thenReturn(luxuryCard);


        Page<DisplayCardListingDTO> result = tenantService.search(pageable, searchDTO);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).location()).isEqualTo("Monaco");
        assertThat(result.getContent().get(0).price().value()).isEqualTo(500);

        verify(listingRepository).findAllByLocationAndBathroomsAndBedroomsAndGuestsAndBeds(
                pageable, "Monaco", 3, 4, 8, 6
        );
    }
}
