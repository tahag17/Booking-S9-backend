package fr.ensaetud.Booking_back.user.application;

import fr.ensaetud.Booking_back.infrastructure.config.SecurityUtils;
import fr.ensaetud.Booking_back.user.domain.User;
import fr.ensaetud.Booking_back.user.application.dto.ReadUserDTO;
import fr.ensaetud.Booking_back.user.mapper.UserMapper;
import fr.ensaetud.Booking_back.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private static final String UPDATED_AT_KEY = "updated_at";
    private final UserRepository userRepository;
    private final UserMapper userMapper;


    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

//    @Transactional(readOnly = true)
//    public ReadUserDTO getAuthenticatedUserFromSecurityContext() {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        Object principal = auth.getPrincipal();
//        User user;
//
//        if (principal instanceof OAuth2User oauth2User) {
//            // Case: OAuth2 login (like with a client)
//            user = SecurityUtils.mapOauth2AttributesToUser(oauth2User.getAttributes());
//        } else if (principal instanceof Jwt jwt) {
//            // Case: JWT resource server
//            Map<String, Object> claims = jwt.getClaims();
//            String email = (String) claims.get("https://www.ensas9.fr/email");
//            user = getByEmail(email).orElseThrow(() -> new RuntimeException("User not found for email: " + email));
//        } else {
//            throw new RuntimeException("Unsupported principal type: " + principal.getClass());
//        }
//
//        return getByEmail(user.getEmail())
//                .orElseThrow(() -> new RuntimeException("User not found for email: " + user.getEmail()));
//    }

    @Transactional(readOnly = true)
    public ReadUserDTO getAuthenticatedUserFromSecurityContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        User user;

        if (principal instanceof OAuth2User oauth2User) {
            user = SecurityUtils.mapOauth2AttributesToUser(oauth2User.getAttributes());
        } else if (principal instanceof Jwt jwt) {
            user = SecurityUtils.mapJwtClaimsToUser(jwt.getClaims());
        } else {
            throw new RuntimeException("Unsupported principal type: " + principal.getClass());
        }

        // Return DTO from user object
        return userMapper.readUserDTOToUser(user);
    }


    @Transactional(readOnly = true)
    public Optional<ReadUserDTO> getByEmail(String email) {
        Optional<User> oneByEmail = userRepository.findOneByEmail(email);
        return oneByEmail.map(userMapper::readUserDTOToUser);
    }

    public void syncWithIdp(OAuth2User oAuth2User, boolean forceResync) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        User user = SecurityUtils.mapOauth2AttributesToUser(attributes);
        Optional<User> existingUser = userRepository.findOneByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            if (attributes.get(UPDATED_AT_KEY) != null) {
                Instant lastModifiedDate = existingUser.orElseThrow().getLastModifiedDate();
                Instant idpModifiedDate;
                if (attributes.get(UPDATED_AT_KEY) instanceof Instant instant) {
                    idpModifiedDate = instant;
                } else {
                    idpModifiedDate = Instant.ofEpochSecond((Integer) attributes.get(UPDATED_AT_KEY));
                }
                if (idpModifiedDate.isAfter(lastModifiedDate) || forceResync) {
                    updateUser(user);
                }
            }
        } else {
            userRepository.saveAndFlush(user);
        }

    }

    private void updateUser(User user) {
        Optional<User> userToUpdateOpt = userRepository.findOneByEmail(user.getEmail());
        if (userToUpdateOpt.isPresent()) {
            User userToUpdate = userToUpdateOpt.get();
            userToUpdate.setEmail(user.getEmail());
            userToUpdate.setFirstName(user.getFirstName());
            userToUpdate.setLastName(user.getLastName());
            userToUpdate.setAuthorities(user.getAuthorities());
            userToUpdate.setImageUrl(user.getImageUrl());
            userRepository.saveAndFlush(userToUpdate);
        }
    }

    public Optional<ReadUserDTO> getByPublicId(UUID publicId) {
        Optional<User> oneByPublicId = userRepository.findOneByPublicId(publicId);
        return oneByPublicId.map(userMapper::readUserDTOToUser);
    }
    

}
