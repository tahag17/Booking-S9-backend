package fr.ensaetud.Booking_back.user.controller;

import fr.ensaetud.Booking_back.user.application.UserService;
import fr.ensaetud.Booking_back.user.application.dto.ReadUserDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.text.MessageFormat;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {


    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final ClientRegistration registration;

    public AuthController(UserService userService, ClientRegistrationRepository registrationRepository) {
        logger.info("=== AuthController Constructor Called ===");
        this.userService = userService;

        logger.info("Looking for OAuth2 client registration with ID: 'okta'");
        this.registration = registrationRepository.findByRegistrationId("okta");

        if (this.registration == null) {
            logger.error("❌ OAuth2 client registration 'okta' NOT FOUND!");

            // Try to find common registration IDs
            logger.info("Attempting to find other common registrations...");
            String[] commonIds = {"auth0", "okta", "google", "github"};
            for (String id : commonIds) {
                ClientRegistration reg = registrationRepository.findByRegistrationId(id);
                if (reg != null) {
                    logger.info("✓ Found registration: {}", id);
                } else {
                    logger.info("✗ Not found: {}", id);
                }
            }

            throw new IllegalStateException(
                    "OAuth2 client registration 'okta' not found. Check your application.yml configuration."
            );
        } else {
            logger.info("✅ Successfully found 'okta' registration");
            logger.info("Client ID: {}", this.registration.getClientId());
            logger.info("Issuer URI: {}", this.registration.getProviderDetails().getIssuerUri());
        }
    }

    @GetMapping("/get-authenticated-user")
    public ResponseEntity<ReadUserDTO> getAuthenticatedUser(
            @AuthenticationPrincipal OAuth2User user, @RequestParam boolean forceResync) {
        logger.info("=== GET /api/auth/get-authenticated-user called ===");
        logger.info("forceResync: {}", forceResync);
        logger.info("OAuth2User is null: {}", user == null);

        if(user == null) {
            logger.warn("User is null, returning INTERNAL_SERVER_ERROR");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            // Log all available attributes to see what we have
            logger.info("User attributes: {}", user.getAttributes());

            // Try different common email attribute names
            Object email = user.getAttribute("email");
            if (email == null) {
                email = user.getAttribute("preferred_username");
            }
            if (email == null) {
                email = user.getAttribute("name");
            }
            logger.info("User email/identifier: {}", email);

            logger.info("Syncing with IDP...");
            userService.syncWithIdp(user, forceResync);

            logger.info("Getting authenticated user from security context...");
            ReadUserDTO connectedUser = userService.getAuthenticatedUserFromSecurityContext();

            logger.info("Returning user: {}", connectedUser != null ? connectedUser.email() : "null");
            return new ResponseEntity<>(connectedUser, HttpStatus.OK);
        }
    }


    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        logger.info("=== POST /api/auth/logout called ===");

        try {
            String issuerUri = registration.getProviderDetails().getIssuerUri();
            logger.info("Issuer URI: {}", issuerUri);

            String originUrl = request.getHeader(HttpHeaders.ORIGIN);
            logger.info("Origin URL: {}", originUrl);

            Object[] params = {issuerUri, registration.getClientId(), originUrl};
            String logoutUrl = MessageFormat.format("{0}v2/logout?client_id={1}&returnTo={2}", params);
            logger.info("Logout URL: {}", logoutUrl);

            request.getSession().invalidate();
            logger.info("Session invalidated successfully");

            return ResponseEntity.ok().body(Map.of("logoutUrl", logoutUrl));
        } catch (Exception e) {
            logger.error("Error during logout", e);
            throw e;
        }
    }
}


