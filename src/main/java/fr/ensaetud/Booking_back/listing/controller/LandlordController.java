package fr.ensaetud.Booking_back.listing.controller;

import com.auth0.exception.Auth0Exception;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ensaetud.Booking_back.listing.application.LandlordService;
import fr.ensaetud.Booking_back.listing.application.dto.CreatedListingDTO;
import fr.ensaetud.Booking_back.listing.application.dto.SaveListingDTO;
import fr.ensaetud.Booking_back.listing.application.dto.sub.PictureDTO;
import fr.ensaetud.Booking_back.user.domain.application.UserService;
import jakarta.validation.ConstraintViolation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Validator;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/landlord-listings")
public class LandlordController {
    private final LandlordService landlordService;
    private final Validator validator;
    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LandlordController(LandlordService landlordService, Validator validator, UserService userService) {
        this.landlordService = landlordService;
        this.validator = validator;
        this.userService = userService;
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreatedListingDTO> create(
            MultipartHttpServletRequest request,
            @RequestPart(name = "dto") String saveListingDTOString
    ) throws JsonProcessingException, Auth0Exception {
        List<PictureDTO> pictures=request.getFileMap().values().stream()
                .map(mapMultiPartFileToPictureDTO())
                .toList();

        SaveListingDTO saveListingDTO=objectMapper.readValue(saveListingDTOString,SaveListingDTO.class);
        saveListingDTO.setPictures(pictures);
        Set<ConstraintViolation<SaveListingDTO>> violations = validator.validate(saveListingDTO);
        if (!violations.isEmpty()) {
           String violationJoined= violations.stream()
                    .map(violation->violation.getPropertyPath()+" : "+violation.getMessage())
                    .collect(Collectors.joining());
            ProblemDetail problemDetail=ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,violationJoined);
            return ResponseEntity.of(problemDetail).build();
        }else {
            return ResponseEntity.ok(landlordService.create(saveListingDTO));
        }


    }

    private static Function<MultipartFile, PictureDTO> mapMultiPartFileToPictureDTO(){
        return multipartFile -> {
            try {
                return new PictureDTO(multipartFile.getBytes(),multipartFile.getContentType(),false);

            } catch (Exception e) {
                throw new RuntimeException(String.format("cannot parse file %s",multipartFile.getOriginalFilename()));
            }
        };
    }


}
