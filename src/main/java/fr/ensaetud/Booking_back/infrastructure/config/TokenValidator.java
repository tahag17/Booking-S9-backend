package fr.ensaetud.Booking_back.infrastructure.config;

import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class TokenValidator {

    public OAuth2User validateAndGetUser(String token) {
        // TODO: validate the token with your provider (e.g., call Auth0 introspection endpoint)
        // For now, we assume the token is valid and contains claims
        // Example: decode manually or get claims from your Auth0 SDK

        Map<String, Object> claims = decodeToken(token);
        if (claims == null) return null;

        // Extract roles
        List<SimpleGrantedAuthority> authorities = SecurityUtils.extractAuthorityFromClaims(claims);

        // Create OAuth2User
        return new DefaultOAuth2User(authorities, claims, "email"); // "email" is the key used as username
    }

    private Map<String, Object> decodeToken(String token) {
        // Implement your token validation here
        // e.g., call Auth0 /introspect endpoint or decode JWT with library
        return Map.of(
                "email", "demo@example.com",
                "given_name", "Demo",
                "family_name", "User"
        );
    }
}
