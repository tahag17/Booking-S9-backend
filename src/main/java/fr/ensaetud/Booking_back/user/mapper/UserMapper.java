package fr.ensaetud.Booking_back.user.mapper;

import fr.ensaetud.Booking_back.user.domain.Authority;
import fr.ensaetud.Booking_back.user.domain.User;
import fr.ensaetud.Booking_back.user.domain.application.dto.ReadUserDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    ReadUserDTO readUserDTOToUser(User user);

    default String mapAuthoritiesToString(Authority authority) {
        return authority.getName();
    }


}
