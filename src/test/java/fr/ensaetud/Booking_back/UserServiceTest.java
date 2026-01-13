package fr.ensaetud.Booking_back;


import fr.ensaetud.Booking_back.user.application.UserService;
import fr.ensaetud.Booking_back.user.application.dto.ReadUserDTO;
import fr.ensaetud.Booking_back.user.domain.User;
import fr.ensaetud.Booking_back.user.mapper.UserMapper;
import fr.ensaetud.Booking_back.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private ReadUserDTO testReadUserDTO;
    private OAuth2User oAuth2User;
    private UUID testPublicId;

    @BeforeEach
    void setUp() {
        testPublicId = UUID.randomUUID();
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPublicId(testPublicId);
        testUser.setImageUrl("http://example.com/image.jpg");
        testUser.setLastModifiedDate(Instant.now());
        testUser.setAuthorities(Set.of());


        testReadUserDTO = new ReadUserDTO(
                testPublicId,
                "John",
                "Doe",
                "test@example.com",
                "http://example.com/image.jpg",
                Set.of("ROLE_USER")
        );

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "123456");
        attributes.put("email", "test@example.com");
        attributes.put("given_name", "John");
        attributes.put("family_name", "Doe");
        attributes.put("picture", "http://example.com/image.jpg");

        oAuth2User = new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "sub"
        );
    }

    @Test
    void getAuthenticatedUserFromSecurityContext_ShouldReturnUser_WhenUserExists() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(userRepository.findOneByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userMapper.readUserDTOToUser(testUser)).thenReturn(testReadUserDTO);
        ReadUserDTO result = userService.getAuthenticatedUserFromSecurityContext();

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("test@example.com");
        assertThat(result.firstName()).isEqualTo("John");
        assertThat(result.lastName()).isEqualTo("Doe");
        assertThat(result.publicId()).isEqualTo(testPublicId);
        assertThat(result.imageUrl()).isEqualTo("http://example.com/image.jpg");
        assertThat(result.authorities()).containsExactly("ROLE_USER");

        verify(userRepository).findOneByEmail("test@example.com");
        verify(userMapper).readUserDTOToUser(testUser);
    }

    @Test
    void getAuthenticatedUserFromSecurityContext_ShouldThrowException_WhenUserNotFound() {

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(userRepository.findOneByEmail(anyString())).thenReturn(Optional.empty());


        assertThatThrownBy(() -> userService.getAuthenticatedUserFromSecurityContext())
                .isInstanceOf(NoSuchElementException.class);

        verify(userRepository).findOneByEmail(anyString());
        verify(userMapper, never()).readUserDTOToUser(any());
    }

    @Test
    void getByEmail_ShouldReturnUser_WhenUserExists() {

        String email = "test@example.com";
        when(userRepository.findOneByEmail(email)).thenReturn(Optional.of(testUser));
        when(userMapper.readUserDTOToUser(testUser)).thenReturn(testReadUserDTO);


        Optional<ReadUserDTO> result = userService.getByEmail(email);
        assertThat(result).isPresent();
        assertThat(result.get().email()).isEqualTo(email);
        assertThat(result.get().firstName()).isEqualTo("John");
        assertThat(result.get().lastName()).isEqualTo("Doe");
        assertThat(result.get().publicId()).isEqualTo(testPublicId);

        verify(userRepository).findOneByEmail(email);
        verify(userMapper).readUserDTOToUser(testUser);
    }

    @Test
    void getByEmail_ShouldReturnEmpty_WhenUserNotFound() {

        String email = "notfound@example.com";
        when(userRepository.findOneByEmail(email)).thenReturn(Optional.empty());


        Optional<ReadUserDTO> result = userService.getByEmail(email);


        assertThat(result).isEmpty();
        verify(userRepository).findOneByEmail(email);
        verify(userMapper, never()).readUserDTOToUser(any());
    }

    @Test
    void getByPublicId_ShouldReturnUser_WhenUserExists() {

        UUID publicId = testPublicId;
        when(userRepository.findOneByPublicId(publicId)).thenReturn(Optional.of(testUser));
        when(userMapper.readUserDTOToUser(testUser)).thenReturn(testReadUserDTO);


        Optional<ReadUserDTO> result = userService.getByPublicId(publicId);


        assertThat(result).isPresent();
        assertThat(result.get().publicId()).isEqualTo(publicId);
        assertThat(result.get().email()).isEqualTo(testUser.getEmail());
        assertThat(result.get().firstName()).isEqualTo(testUser.getFirstName());
        assertThat(result.get().lastName()).isEqualTo(testUser.getLastName());

        verify(userRepository).findOneByPublicId(publicId);
        verify(userMapper).readUserDTOToUser(testUser);
    }

    @Test
    void getByPublicId_ShouldReturnEmpty_WhenUserNotFound() {

        UUID publicId = UUID.randomUUID();
        when(userRepository.findOneByPublicId(publicId)).thenReturn(Optional.empty());


        Optional<ReadUserDTO> result = userService.getByPublicId(publicId);


        assertThat(result).isEmpty();
        verify(userRepository).findOneByPublicId(publicId);
        verify(userMapper, never()).readUserDTOToUser(any());
    }

    @Test
    void syncWithIdp_ShouldCreateNewUser_WhenUserDoesNotExist() {

        when(userRepository.findOneByEmail(anyString())).thenReturn(Optional.empty());


        userService.syncWithIdp(oAuth2User, false);


        verify(userRepository).findOneByEmail(anyString());
        verify(userRepository).saveAndFlush(any(User.class));
    }

    @Test
    void syncWithIdp_ShouldUpdateUser_WhenIdpDateIsMoreRecent() {

        Instant oldDate = Instant.now().minusSeconds(3600);
        Instant newDate = Instant.now();

        testUser.setLastModifiedDate(oldDate);

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("updated_at", (int) newDate.getEpochSecond());

        OAuth2User updatedOAuth2User = new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "sub"
        );

        when(userRepository.findOneByEmail(anyString()))
                .thenReturn(Optional.of(testUser))
                .thenReturn(Optional.of(testUser));


        userService.syncWithIdp(updatedOAuth2User, false);


        verify(userRepository, times(2)).findOneByEmail(anyString());
        verify(userRepository).saveAndFlush(any(User.class));
    }

    @Test
    void syncWithIdp_ShouldNotUpdateUser_WhenIdpDateIsOlder() {

        Instant recentDate = Instant.now();
        Instant oldDate = Instant.now().minusSeconds(3600);

        testUser.setLastModifiedDate(recentDate);

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("updated_at", (int) oldDate.getEpochSecond());

        OAuth2User updatedOAuth2User = new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "sub"
        );

        when(userRepository.findOneByEmail(anyString())).thenReturn(Optional.of(testUser));


        userService.syncWithIdp(updatedOAuth2User, false);


        verify(userRepository).findOneByEmail(anyString());
        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    void syncWithIdp_ShouldUpdateUser_WhenForceResyncIsTrue() {

        Instant recentDate = Instant.now();
        Instant oldDate = Instant.now().minusSeconds(3600);

        testUser.setLastModifiedDate(recentDate);

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("updated_at", (int) oldDate.getEpochSecond());

        OAuth2User updatedOAuth2User = new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "sub"
        );

        when(userRepository.findOneByEmail(anyString()))
                .thenReturn(Optional.of(testUser))
                .thenReturn(Optional.of(testUser));


        userService.syncWithIdp(updatedOAuth2User, true);


        verify(userRepository, times(2)).findOneByEmail(anyString());
        verify(userRepository).saveAndFlush(any(User.class));
    }

    @Test
    void syncWithIdp_ShouldHandleInstantType_ForUpdatedAt() {

        Instant oldDate = Instant.now().minusSeconds(3600);
        Instant newDate = Instant.now();

        testUser.setLastModifiedDate(oldDate);

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("updated_at", newDate); // Instant au lieu d'Integer

        OAuth2User updatedOAuth2User = new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "sub"
        );

        when(userRepository.findOneByEmail(anyString()))
                .thenReturn(Optional.of(testUser))
                .thenReturn(Optional.of(testUser));


        userService.syncWithIdp(updatedOAuth2User, false);


        verify(userRepository, times(2)).findOneByEmail(anyString());
        verify(userRepository).saveAndFlush(any(User.class));
    }

    @Test
    void syncWithIdp_ShouldNotUpdateUser_WhenUpdatedAtIsNull() {

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.remove("updated_at");
        OAuth2User updatedOAuth2User = new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "sub"
        );

        when(userRepository.findOneByEmail(anyString())).thenReturn(Optional.of(testUser));


        userService.syncWithIdp(updatedOAuth2User, false);


        verify(userRepository).findOneByEmail(anyString());
        verify(userRepository, never()).saveAndFlush(any(User.class));
    }
}