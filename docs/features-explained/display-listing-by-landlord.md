# Display Listings by Landlord

This document describes the initial implementation of the **Display Listings by Landlord** feature.

The purpose of this feature is to allow the system to **retrieve and display listings created by a specific landlord**, in a format suitable for frontend display (listing cards).


---

##  Feature Goal

The goal of this feature is to:

- Fetch listings that belong to **one specific landlord**
- Retrieve only the **data required for display**
- Include the **cover picture** for each listing
- Return results in a **controlled, explicit application state**
- Keep the domain, application, and presentation layers clearly separated

This feature is **read-only** and focused on **display**, not modification.

---

##  Architectural Approach

The implementation follows a **clean / layered architecture**:

- **Domain Layer**  
  Contains core business entities (e.g. `Listing`)

- **Application Layer**  
  Defines use-case outputs and orchestrates the flow (`DTOs`, `State`)

- **Infrastructure Layer**  
  Handles data access (`JpaRepository`, custom queries)

Each layer has a single responsibility and does not leak concerns into other layers.

---

##  Components Implemented

---

### 1️. DisplayCardListingDTO

#### 📍 Location
```
fr.ensaetud.Booking_back.listing.application.dto.DisplayCardListingDTO
```

#### 📄 Code
```java
public record DisplayCardListingDTO(
    PriceVO price,
    String location,
    PictureDTO cover,
    BookingCategory bookingCategory,
    UUID publicId
) {}
```

####  Purpose
Represents the data required by the frontend to display a single listing card.

This DTO:

- Is immutable
- Is read-only
- Does not expose the full Listing domain entity
- Acts as an output model of application use cases

####  Fields

| Field | Description |
|-------|-------------|
| price | Price value object |
| location | Displayable location |
| cover | Cover picture DTO |
| bookingCategory | Listing category |
| publicId | Public identifier exposed to clients |

---

### 2️. Application State Handling (State)

#### 📍 Location
```
fr.ensaetud.Booking_back.sharedkernel.service
```

This feature uses a custom application-level state abstraction to represent the result of use case execution.

#### 2.1 StatusNotification

```java
public enum StatusNotification {
    OK,
    ERROR,
    UNAUTHORIZED
}
```

##### 🎯 Purpose
Represents the outcome of a use case in a high-level, transport-agnostic way.

This status is later translated by controllers into HTTP responses.

---

#### 2.2 State<T, V>

```java
public class State<T, V> {
    private T value;
    private V error;
    private StatusNotification status;
}
```

#####  Purpose
Encapsulates:

- A success value
- Or an error payload
- Along with the execution status

This avoids:

- Exception-driven control flow
- Ambiguous null returns
- Inconsistent controller logic

#####  Example
```java
State<List<DisplayCardListingDTO>, String>
```

---

#### 2.3 StateBuilder<T, V>

```java
public class StateBuilder<T, V> {
    public State<T, V> forSuccess(T value);
    public State<T, V> forError(V error);
    public State<T, V> forUnauthorized(V error);
}
```

##### 🎯 Purpose
Provides a fluent API to create valid State instances with clear intent.

---

### 3️. ListingRepository

#### 📍 Location
```
fr.ensaetud.Booking_back.listing.repository.ListingRepository
```

####  Code

```java
public interface ListingRepository extends JpaRepository<Listing, Long> {

    @Query("""
        SELECT l FROM Listing l
        LEFT JOIN FETCH l.pictures picture
        WHERE l.landlordPublicId = :landlordPublicId
          AND picture.isCover = true
    """)
    List<Listing> findAllByLandlordPublicIdFetchCoverPicture(UUID landlordPublicId);

    long deleteByPublicIdAndLandlordPublicId(UUID publicId, UUID landlordPublicId);
}
```

####  Purpose
Handles data access for listing-related operations.

For the display feature, this repository:

- Retrieves all listings belonging to a specific landlord
- Fetches the cover picture eagerly
- Prevents the N+1 select problem
- Ensures listings are filtered by ownership

####  Key Query Explained

```jpql
SELECT l FROM Listing l
LEFT JOIN FETCH l.pictures picture
WHERE l.landlordPublicId = :landlordPublicId
  AND picture.isCover = true
```

This query:

- Selects listings owned by a given landlord
- Fetches only the cover picture
- Ensures display-ready data without additional queries

---

### 4. ListingMapper

#### 📍 Location
```
fr.ensaetud.Booking_back.listing.mapper.ListingMapper
```

####  Code

```java
@Mapper(componentModel = "spring", uses = {ListingPictureMapper.class})
public interface ListingMapper {

    CreatedListingDTO listingToCreatedListingDTO(Listing listing);

    @Mapping(target = "cover", source = "pictures")
    List<DisplayCardListingDTO> listingToDisplayCardListingDTOs(List<Listing> listings);

    @Mapping(target = "cover", source = "pictures", qualifiedByName = "extract-cover")
    DisplayCardListingDTO listingToDisplayCardListingDTO(Listing listing);

    default PriceVO mapPriceToPriceVO(int price) {
        return new PriceVO(price);
    }
}
```

####  Purpose
Handles the mapping between domain entities and DTOs using MapStruct.

For the display feature:

- **`listingToDisplayCardListingDTOs`**: Converts a list of `Listing` entities to display-ready DTOs
- **`listingToDisplayCardListingDTO`**: Converts a single listing, extracting the cover picture using a qualified method
- **`mapPriceToPriceVO`**: Transforms primitive price values into value objects

The mapper delegates picture-specific mappings to `ListingPictureMapper`.

---

### 5. LandlordService

#### 📍 Location
```
fr.ensaetud.Booking_back.listing.application.LandlordService
```

####  Relevant Methods

```java
@Transactional(readOnly = true)
public List<DisplayCardListingDTO> getAllProperties(ReadUserDTO landlord) {
    List<Listing> properties = listingRepository.findAllByLandlordPublicIdFetchCoverPicture(landlord.publicId());
    return listingMapper.listingToDisplayCardListingDTOs(properties);
}

@Transactional
public State<UUID, String> delete(UUID publicId, ReadUserDTO landlord) {
    long deletedSuccessfully = listingRepository.deleteByPublicIdAndLandlordPublicId(publicId, landlord.publicId());
    if (deletedSuccessfully > 0) {
        return State.<UUID, String>builder().forSuccess(publicId);
    } else {
        return State.<UUID, String>builder().forUnauthorized("User not authorized to delete this listing");
    }
}
```

####  Purpose

**`getAllProperties`**  
Retrieves all listings owned by a specific landlord and transforms them into display-ready DTOs. This method is read-only and orchestrates the repository query and mapping logic.

**`delete`**  
Deletes a listing by its public ID, ensuring the authenticated landlord owns it. Returns a `State` indicating success or authorization failure based on the number of records deleted.

---

### 6️. LandlordController

#### 📍 Location
```
fr.ensaetud.Booking_back.listing.controller.LandlordController
```

####  Relevant Endpoints

```java
@GetMapping(value = "/get-all")
@PreAuthorize("hasAnyRole('" + SecurityUtils.ROLE_LANDLORD + "')")
public ResponseEntity<List<DisplayCardListingDTO>> getAll() {
    ReadUserDTO connectedUser = userService.getAuthenticatedUserFromSecurityContext();
    List<DisplayCardListingDTO> allProperties = landlordService.getAllProperties(connectedUser);
    return ResponseEntity.ok(allProperties);
}

@DeleteMapping("/delete")
@PreAuthorize("hasAnyRole('" + SecurityUtils.ROLE_LANDLORD + "')")
public ResponseEntity<UUID> delete(@RequestParam UUID publicId) {
    ReadUserDTO connectedUser = userService.getAuthenticatedUserFromSecurityContext();
    State<UUID, String> deleteState = landlordService.delete(publicId, connectedUser);
    if (deleteState.getStatus().equals(StatusNotification.OK)) {
        return ResponseEntity.ok(deleteState.getValue());
    } else if (deleteState.getStatus().equals(StatusNotification.UNAUTHORIZED)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
}
```

####  Purpose

**`GET /api/landlord-listings/get-all`**  
Exposes an endpoint to retrieve all listings belonging to the authenticated landlord. Requires `ROLE_LANDLORD` authorization. Returns a list of display-ready listing cards.

**`DELETE /api/landlord-listings/delete`**  
Allows a landlord to delete one of their listings by providing its public ID. The endpoint translates the `State` result into appropriate HTTP responses (200 OK, 401 Unauthorized, or 500 Internal Server Error).

---

##  Current Flow

1. A landlord identifier (landlordPublicId) is provided
2. The repository fetches matching listings and their cover pictures
3. Listings are mapped (next step) to DisplayCardListingDTO
4. Results are wrapped in a State
5. Controllers expose the data to the frontend

---
