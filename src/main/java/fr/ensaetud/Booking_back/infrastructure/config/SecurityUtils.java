package fr.ensaetud.Booking_back.infrastructure.config;

import fr.ensaetud.Booking_back.user.domain.Authority;
import fr.ensaetud.Booking_back.user.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SecurityUtils {

    private static final Logger log = LoggerFactory.getLogger(SecurityUtils.class);

    public static final String ROLE_TENANT = "ROLE_TENANT";
    public static final String ROLE_LANDLORD = "ROLE_LANDLORD";
    public static final String CLAIMS_NAMESPACE = "https://www.ensas9.fr/roles";

    public static User mapOauth2AttributesToUser(Map<String, Object> attributes) {
        User user = new User();

        log.info("===== OAUTH2 USER ATTRIBUTES RECEIVED FROM AUTH0 =====");
        attributes.forEach((k, v) -> log.info("ATTR: {} = {}", k, v));
        log.info("======================================================");

        String sub = String.valueOf(attributes.get("sub"));
        String username = null;

        if (attributes.get("preferred_username") != null) {
            username = ((String) attributes.get("preferred_username")).toLowerCase();
        }

        if (attributes.get("given_name") != null) {
            user.setFirstName(((String) attributes.get("given_name")).toLowerCase());
        } else if (attributes.get("nickname") != null) {
            user.setFirstName(((String) attributes.get("nickname")).toLowerCase());
        }

        if (attributes.get("family_name") != null) {
            user.setLastName(((String) attributes.get("family_name")).toLowerCase());
        }

        if (attributes.get("email") != null) {
            user.setEmail(((String) attributes.get("email")).toLowerCase());
        } else if (sub.contains("|") && (username != null && username.contains("@"))) {
            user.setEmail(username);
        } else {
            user.setEmail(sub);
        }

        if (attributes.get("picture") != null) {
            user.setImageUrl(((String) attributes.get("picture")));
        }

        if (attributes.get(CLAIMS_NAMESPACE) != null) {
            List<String> authoritiesRaw = (List<String>) attributes.get(CLAIMS_NAMESPACE);
            Set<Authority> authorities = authoritiesRaw.stream()
                    .map(role -> {
                        Authority authorityObj = new Authority();
                        authorityObj.setName(role);
                        return authorityObj;
                    }).collect(Collectors.toSet());
            user.setAuthorities(authorities);
        }

        return user;
    }

    public static User mapJwtClaimsToUser(Map<String, Object> claims) {
        User user = new User();

        // Email from the custom claim we added in Auth0
        if (claims.get("https://www.ensas9.fr/email") != null) {
            user.setEmail((String) claims.get("https://www.ensas9.fr/email"));
        } else if (claims.get("email") != null) {
            user.setEmail((String) claims.get("email"));
        }

        // First/Last name
        if (claims.get("given_name") != null) {
            user.setFirstName(((String) claims.get("given_name")).toLowerCase());
        }

        if (claims.get("family_name") != null) {
            user.setLastName(((String) claims.get("family_name")).toLowerCase());
        }

        // Picture
        if (claims.get("picture") != null) {
            user.setImageUrl((String) claims.get("picture"));
        }

        // Roles
        if (claims.get(SecurityUtils.CLAIMS_NAMESPACE) != null) {
            List<String> authoritiesRaw = (List<String>) claims.get(SecurityUtils.CLAIMS_NAMESPACE);
            Set<Authority> authorities = authoritiesRaw.stream()
                    .map(role -> {
                        Authority authorityObj = new Authority();
                        authorityObj.setName(role);
                        return authorityObj;
                    }).collect(Collectors.toSet());
            user.setAuthorities(authorities);
        }

        return user;
    }


    public static List<SimpleGrantedAuthority> extractAuthorityFromClaims(Map<String, Object> claims) {

        log.info("===== JWT CLAIMS =====");
        claims.forEach((key, value) ->
                log.info("CLAIM: {} = {} ({})", key, value,
                        value != null ? value.getClass().getName() : "null")
        );
        log.info("======================");

        return mapRolesToGrantedAuthorities(getRolesFromClaims(claims));
    }

    private static Collection<String> getRolesFromClaims(Map<String, Object> claims) {
        Object roles = claims.get(CLAIMS_NAMESPACE);
        log.info("Roles claim [{}] = {}", CLAIMS_NAMESPACE, roles);
        return (List<String>) roles;
    }

    private static List<SimpleGrantedAuthority> mapRolesToGrantedAuthorities(Collection<String> roles) {

        if (roles == null) {
            log.warn("No roles found in token");
            return List.of();
        }

        return roles.stream()
                .filter(role -> role.startsWith("ROLE_"))
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    public static boolean hasCurrentUserAnyOfAuthorities(String... authorities) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication != null && getAuthorities(authentication)
                .anyMatch(authority -> Arrays.asList(authorities).contains(authority)));
    }

    private static Stream<String> getAuthorities(Authentication authentication) {

        // 🔥 HERE is where we log the RAW JWT
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();

            log.info("===== RAW JWT FROM AUTH0 =====");
            log.info("Token Value: {}", jwt.getTokenValue());
            log.info("JWT Headers: {}", jwt.getHeaders());
            log.info("JWT Claims: {}", jwt.getClaims());
            log.info("================================");
        }

        Collection<? extends GrantedAuthority> authorities =
                authentication instanceof JwtAuthenticationToken jwtAuthenticationToken ?
                        extractAuthorityFromClaims(jwtAuthenticationToken.getToken().getClaims())
                        : authentication.getAuthorities();

        return authorities.stream().map(GrantedAuthority::getAuthority);
    }

    public static SecurityContext buildSecurityContext(OAuth2User user) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        log.info("===== BUILDING SECURITY CONTEXT FROM OAUTH2 USER =====");
        user.getAttributes().forEach((k, v) -> log.info("ATTR: {} = {}", k, v));
        log.info("=====================================================");

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null,
                        extractAuthorityFromClaims(user.getAttributes()));

        context.setAuthentication(auth);
        return context;
    }
}
