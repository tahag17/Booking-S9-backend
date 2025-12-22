package fr.ensaetud.Booking_back.listing.application.dto;

import fr.ensaetud.Booking_back.listing.application.dto.sub.DescriptionDTO;
import fr.ensaetud.Booking_back.listing.application.dto.sub.ListingInfoDTO;
import fr.ensaetud.Booking_back.listing.application.dto.sub.PictureDTO;
import fr.ensaetud.Booking_back.listing.application.dto.vo.PriceVO;
import fr.ensaetud.Booking_back.listing.domain.BookingCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class SaveListingDTO {
    @NotNull
    BookingCategory category;
    @NotNull
    String location;
    @NotNull @Valid
    ListingInfoDTO info;
    @NotNull @Valid
    PriceVO price;
    @NotNull
    List<PictureDTO> pictures;
    @NotNull @Valid
    DescriptionDTO description;

    public BookingCategory getCategory() {
        return category;
    }

    public void setCategory(BookingCategory category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ListingInfoDTO getInfo() {
        return info;
    }

    public void setInfo(ListingInfoDTO info) {
        this.info = info;
    }

    public PriceVO getPrice() {
        return price;
    }

    public void setPrice(PriceVO price) {
        this.price = price;
    }

    public List<PictureDTO> getPictures() {
        return pictures;
    }

    public void setPictures(List<PictureDTO> pictures) {
        this.pictures = pictures;
    }

    public DescriptionDTO getDescription() {
        return description;
    }

    public void setDescription(DescriptionDTO description) {
        this.description = description;
    }
}
