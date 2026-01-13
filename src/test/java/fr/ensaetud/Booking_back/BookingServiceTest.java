package fr.ensaetud.Booking_back;



import fr.ensaetud.Booking_back.booking.application.BookingService;
import fr.ensaetud.Booking_back.booking.application.dto.BookedDateDTO;
import fr.ensaetud.Booking_back.booking.application.dto.BookedListingDTO;
import fr.ensaetud.Booking_back.booking.application.dto.NewBookingDTO;
import fr.ensaetud.Booking_back.booking.domain.Booking;
import fr.ensaetud.Booking_back.booking.mapper.BookingMapper;
import fr.ensaetud.Booking_back.booking.repository.BookingRepository;
import fr.ensaetud.Booking_back.infrastructure.config.SecurityUtils;
import fr.ensaetud.Booking_back.listing.application.LandlordService;
import fr.ensaetud.Booking_back.listing.application.dto.DisplayCardListingDTO;
import fr.ensaetud.Booking_back.listing.application.dto.ListingCreateBookingDTO;
import fr.ensaetud.Booking_back.listing.application.dto.sub.PictureDTO;
import fr.ensaetud.Booking_back.listing.application.dto.vo.PriceVO;
import fr.ensaetud.Booking_back.listing.domain.BookingCategory;
import fr.ensaetud.Booking_back.sharedkernel.service.State;
import fr.ensaetud.Booking_back.sharedkernel.service.StatusNotification;
import fr.ensaetud.Booking_back.user.application.UserService;
import fr.ensaetud.Booking_back.user.application.dto.ReadUserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private UserService userService;

    @Mock
    private LandlordService landlordService;

    @InjectMocks
    private BookingService bookingService;

    private UUID tenantPublicId;
    private UUID listingPublicId;
    private UUID bookingPublicId;
    private ReadUserDTO connectedUser;
    private NewBookingDTO newBookingDTO;
    private Booking booking;
    private ListingCreateBookingDTO listingCreateBookingDTO;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;

    @BeforeEach
    void setUp() {
        tenantPublicId = UUID.randomUUID();
        listingPublicId = UUID.randomUUID();
        bookingPublicId = UUID.randomUUID();
        startDate = OffsetDateTime.now().plusDays(1);
        endDate = OffsetDateTime.now().plusDays(5);

        connectedUser = new ReadUserDTO(
                tenantPublicId,
                "John",
                "Doe",
                "john.doe@example.com",
                "http://image.url",
                Set.of("ROLE_TENANT")
        );

        newBookingDTO = new NewBookingDTO(startDate, endDate, listingPublicId);

        booking = new Booking();
        booking.setPublicId(bookingPublicId);
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        booking.setFkTenant(tenantPublicId);
        booking.setFkListing(listingPublicId);
        booking.setNumberOfTravelers(1);

        listingCreateBookingDTO = new ListingCreateBookingDTO(
                listingPublicId,
                new PriceVO(100)
        );
    }

    @Test
    void create_shouldSucceed_whenValidBooking() {

        when(bookingMapper.newBookingToBooking(newBookingDTO)).thenReturn(booking);
        when(landlordService.getByListingPublicId(listingPublicId))
                .thenReturn(Optional.of(listingCreateBookingDTO));
        when(bookingRepository.bookingExistsAtInterval(startDate, endDate, listingPublicId))
                .thenReturn(false);
        when(userService.getAuthenticatedUserFromSecurityContext()).thenReturn(connectedUser);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);


        State<Void, String> result = bookingService.create(newBookingDTO);

        assertThat(result.getStatus()).isEqualTo(StatusNotification.OK);
        assertThat(result.getError()).isNull();

        verify(bookingRepository).save(argThat(b ->
                b.getFkListing().equals(listingPublicId) &&
                        b.getFkTenant().equals(tenantPublicId) &&
                        b.getNumberOfTravelers() == 1 &&
                        b.getTotalPrice() == 400
        ));
    }

    @Test
    void create_shouldFail_whenListingNotFound() {

        when(bookingMapper.newBookingToBooking(newBookingDTO)).thenReturn(booking);
        when(landlordService.getByListingPublicId(listingPublicId)).thenReturn(Optional.empty());

        State<Void, String> result = bookingService.create(newBookingDTO);


        assertThat(result.getStatus()).isEqualTo(StatusNotification.ERROR);
        assertThat(result.getError()).isEqualTo("Landlord public id not found");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_shouldFail_whenBookingAlreadyExists() {

        when(bookingMapper.newBookingToBooking(newBookingDTO)).thenReturn(booking);
        when(landlordService.getByListingPublicId(listingPublicId))
                .thenReturn(Optional.of(listingCreateBookingDTO));
        when(bookingRepository.bookingExistsAtInterval(startDate, endDate, listingPublicId))
                .thenReturn(true);


        State<Void, String> result = bookingService.create(newBookingDTO);


        assertThat(result.getStatus()).isEqualTo(StatusNotification.ERROR);
        assertThat(result.getError()).isEqualTo("One booking already exists");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void checkAvailability_shouldReturnBookedDates() {

        List<Booking> bookings = Arrays.asList(booking);
        BookedDateDTO bookedDateDTO = new BookedDateDTO(startDate, endDate);

        when(bookingRepository.findAllByFkListing(listingPublicId)).thenReturn(bookings);
        when(bookingMapper.bookingToCheckAvailability(booking)).thenReturn(bookedDateDTO);


        List<BookedDateDTO> result = bookingService.checkAvailability(listingPublicId);


        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(bookedDateDTO);

        verify(bookingRepository).findAllByFkListing(listingPublicId);
    }

    @Test
    void getBookedListing_shouldReturnBookedListings() {

        UUID listing2PublicId = UUID.randomUUID();

        Booking booking2 = new Booking();
        booking2.setPublicId(UUID.randomUUID());
        booking2.setFkListing(listing2PublicId);
        booking2.setTotalPrice(300);

        List<Booking> bookings = Arrays.asList(booking, booking2);

        PictureDTO pictureDTO = new PictureDTO(
                new byte[]{1, 2, 3},
                "image/jpeg",
                true
        );

        DisplayCardListingDTO listing1 = new DisplayCardListingDTO(
                new PriceVO(100),
                "Paris, France",
                pictureDTO,
                BookingCategory.ALL,
                listingPublicId
        );

        DisplayCardListingDTO listing2 = new DisplayCardListingDTO(
                new PriceVO(150),
                "Lyon, France",
                pictureDTO,
                BookingCategory.ALL,
                listing2PublicId
        );

        List<DisplayCardListingDTO> listings = Arrays.asList(listing1, listing2);

        BookedDateDTO bookedDateDTO = new BookedDateDTO(startDate, endDate);

        when(userService.getAuthenticatedUserFromSecurityContext()).thenReturn(connectedUser);
        when(bookingRepository.findAllByFkTenant(tenantPublicId)).thenReturn(bookings);
        when(landlordService.getCardDisplayByListingPublicId(anyList())).thenReturn(listings);
        when(bookingMapper.bookingToCheckAvailability(any(Booking.class))).thenReturn(bookedDateDTO);


        List<BookedListingDTO> result = bookingService.getBookedListing();


        assertThat(result).hasSize(2);
        assertThat(result.get(0).listingPublicId()).isEqualTo(listingPublicId);
        assertThat(result.get(0).location()).isEqualTo("Paris, France");
        assertThat(result.get(1).listingPublicId()).isEqualTo(listing2PublicId);

        verify(bookingRepository).findAllByFkTenant(tenantPublicId);
        verify(landlordService).getCardDisplayByListingPublicId(anyList());
    }

    @Test
    void cancel_shouldSucceed_whenTenantCancelsOwnBooking() {

        when(userService.getAuthenticatedUserFromSecurityContext()).thenReturn(connectedUser);
        when(bookingRepository.deleteBookingByFkTenantAndPublicId(tenantPublicId, bookingPublicId))
                .thenReturn(1);


        State<UUID, String> result = bookingService.cancel(bookingPublicId, listingPublicId, false);


        assertThat(result.getStatus()).isEqualTo(StatusNotification.OK);
        assertThat(result.getValue()).isEqualTo(bookingPublicId);
        assertThat(result.getError()).isNull();

        verify(bookingRepository).deleteBookingByFkTenantAndPublicId(tenantPublicId, bookingPublicId);
    }

    @Test
    void cancel_shouldFail_whenBookingNotFound() {

        when(userService.getAuthenticatedUserFromSecurityContext()).thenReturn(connectedUser);
        when(bookingRepository.deleteBookingByFkTenantAndPublicId(tenantPublicId, bookingPublicId))
                .thenReturn(0);


        State<UUID, String> result = bookingService.cancel(bookingPublicId, listingPublicId, false);


        assertThat(result.getStatus()).isEqualTo(StatusNotification.ERROR);
        assertThat(result.getError()).isEqualTo("Booking not found");
        assertThat(result.getValue()).isNull();
    }

    @Test
    void cancel_shouldSucceed_whenLandlordCancelsBooking() {

        ReadUserDTO landlordUser = new ReadUserDTO(
                UUID.randomUUID(),
                "Jane",
                "Smith",
                "jane.smith@example.com",
                "http://image.url",
                Set.of("ROLE_LANDLORD")
        );

        PictureDTO pictureDTO = new PictureDTO(
                new byte[]{1, 2, 3},
                "image/jpeg",
                true
        );

        DisplayCardListingDTO displayCardListingDTO = new DisplayCardListingDTO(
                new PriceVO(100),
                "Paris, France",
                pictureDTO,
                BookingCategory.ALL,
                listingPublicId
        );

        when(userService.getAuthenticatedUserFromSecurityContext()).thenReturn(landlordUser);
        when(landlordService.getByPublicIdAndLandlordPublicId(listingPublicId, landlordUser.publicId()))
                .thenReturn(Optional.of(displayCardListingDTO));
        when(bookingRepository.deleteBookingByPublicIdAndFkListing(bookingPublicId, listingPublicId))
                .thenReturn(1);

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(() ->
                            SecurityUtils.hasCurrentUserAnyOfAuthorities(SecurityUtils.ROLE_LANDLORD))
                    .thenReturn(true);


            State<UUID, String> result = bookingService.cancel(bookingPublicId, listingPublicId, true);


            assertThat(result.getStatus()).isEqualTo(StatusNotification.OK);
            assertThat(result.getValue()).isEqualTo(bookingPublicId);

            verify(landlordService).getByPublicIdAndLandlordPublicId(listingPublicId, landlordUser.publicId());
            verify(bookingRepository).deleteBookingByPublicIdAndFkListing(bookingPublicId, listingPublicId);
        }
    }

    @Test
    void cancel_shouldFail_whenLandlordDoesNotOwnListing() {

        ReadUserDTO landlordUser = new ReadUserDTO(
                UUID.randomUUID(),
                "Jane",
                "Smith",
                "jane.smith@example.com",
                "http://image.url",
                Set.of("ROLE_LANDLORD")
        );

        when(userService.getAuthenticatedUserFromSecurityContext()).thenReturn(landlordUser);
        when(landlordService.getByPublicIdAndLandlordPublicId(listingPublicId, landlordUser.publicId()))
                .thenReturn(Optional.empty());

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(() ->
                            SecurityUtils.hasCurrentUserAnyOfAuthorities(SecurityUtils.ROLE_LANDLORD))
                    .thenReturn(true);


            State<UUID, String> result = bookingService.cancel(bookingPublicId, listingPublicId, true);


            assertThat(result.getStatus()).isEqualTo(StatusNotification.ERROR);
            assertThat(result.getError()).isEqualTo("Booking not found");

            verify(bookingRepository, never()).deleteBookingByPublicIdAndFkListing(any(), any());
        }
    }

    @Test
    void getBookedListingForLandlord_shouldReturnAllBookingsForLandlordProperties() {

        PictureDTO pictureDTO = new PictureDTO(
                new byte[]{1, 2, 3},
                "image/jpeg",
                true
        );

        DisplayCardListingDTO listing1 = new DisplayCardListingDTO(
                new PriceVO(100),
                "Paris, France",
                pictureDTO,
                BookingCategory.ALL,
                listingPublicId
        );

        List<DisplayCardListingDTO> allProperties = Arrays.asList(listing1);
        List<Booking> allBookings = Arrays.asList(booking);
        BookedDateDTO bookedDateDTO = new BookedDateDTO(startDate, endDate);

        when(userService.getAuthenticatedUserFromSecurityContext()).thenReturn(connectedUser);
        when(landlordService.getAllProperties(connectedUser)).thenReturn(allProperties);
        when(bookingRepository.findAllByFkListingIn(anyList())).thenReturn(allBookings);
        when(bookingMapper.bookingToCheckAvailability(booking)).thenReturn(bookedDateDTO);


        List<BookedListingDTO> result = bookingService.getBookedListingForLandlord();


        assertThat(result).hasSize(1);
        assertThat(result.get(0).listingPublicId()).isEqualTo(listingPublicId);
        assertThat(result.get(0).location()).isEqualTo("Paris, France");

        verify(landlordService).getAllProperties(connectedUser);
        verify(bookingRepository).findAllByFkListingIn(anyList());
    }

    @Test
    void getBookingMatchByListingIdsAndBookedDate_shouldReturnMatchingListingIds() {

        UUID listing2PublicId = UUID.randomUUID();
        List<UUID> listingIds = Arrays.asList(listingPublicId, listing2PublicId);

        Booking booking2 = new Booking();
        booking2.setFkListing(listing2PublicId);

        List<Booking> matchingBookings = Arrays.asList(booking, booking2);
        BookedDateDTO bookedDateDTO = new BookedDateDTO(startDate, endDate);

        when(bookingRepository.findAllMatchWithDate(listingIds, startDate, endDate))
                .thenReturn(matchingBookings);


        List<UUID> result = bookingService.getBookingMatchByListingIdsAndBookedDate(
                listingIds,
                bookedDateDTO
        );


        assertThat(result).hasSize(2);
        assertThat(result).contains(listingPublicId, listing2PublicId);

        verify(bookingRepository).findAllMatchWithDate(listingIds, startDate, endDate);
    }

    @Test
    void create_shouldCalculateTotalPriceCorrectly_forMultipleDays() {

        OffsetDateTime start = OffsetDateTime.now().plusDays(1);
        OffsetDateTime end = OffsetDateTime.now().plusDays(8); // 7 nights
        NewBookingDTO bookingDTO = new NewBookingDTO(start, end, listingPublicId);

        Booking newBooking = new Booking();
        newBooking.setStartDate(start);
        newBooking.setEndDate(end);

        when(bookingMapper.newBookingToBooking(bookingDTO)).thenReturn(newBooking);
        when(landlordService.getByListingPublicId(listingPublicId))
                .thenReturn(Optional.of(listingCreateBookingDTO));
        when(bookingRepository.bookingExistsAtInterval(start, end, listingPublicId))
                .thenReturn(false);
        when(userService.getAuthenticatedUserFromSecurityContext()).thenReturn(connectedUser);
        when(bookingRepository.save(any(Booking.class))).thenReturn(newBooking);


        State<Void, String> result = bookingService.create(bookingDTO);


        assertThat(result.getStatus()).isEqualTo(StatusNotification.OK);

        verify(bookingRepository).save(argThat(b ->
                b.getTotalPrice() == 700
        ));
    }
}
