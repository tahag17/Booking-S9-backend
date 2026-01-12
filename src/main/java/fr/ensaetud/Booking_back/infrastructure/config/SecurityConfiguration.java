package fr.ensaetud.Booking_back.infrastructure.config;

import fr.ensaetud.Booking_back.user.controller.OAuth2SuccessHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfiguration.class);

    private final OAuth2SuccessHandler successHandler;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfiguration(OAuth2SuccessHandler successHandler,
                                 CorsConfigurationSource corsConfigurationSource) {
        this.successHandler = successHandler;
        this.corsConfigurationSource = corsConfigurationSource;
        logger.info("SecurityConfiguration initialized");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring SecurityFilterChain...");
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName(null);

        http.cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()) // adjust later for production
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(requestHandler)
                        .ignoringRequestMatchers("/api/**")
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(successHandler))
                //changed for mobile version
//                .oauth2ResourceServer(oauth2 -> oauth2
//                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
//                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .oauth2Client(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                );

        logger.info("SecurityFilterChain configured successfully");
        return http.build();
    }

    //added solely for mobile
//    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();

        // Tell Spring where roles are
        converter.setAuthoritiesClaimName(SecurityUtils.CLAIMS_NAMESPACE);
        converter.setAuthorityPrefix("");
        // because your roles already start with ROLE_

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(converter);

        return jwtConverter;
    }

    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return authorities -> {
            logger.info("Mapping user authorities: {}", authorities);
            Set<GrantedAuthority> grantedAuthorities = new HashSet<>();

            authorities.forEach(grantedAuthority -> {
                if (grantedAuthority instanceof OidcUserAuthority oidcUserAuthority) {
                    grantedAuthorities.addAll(
                            SecurityUtils.extractAuthorityFromClaims(
                                    oidcUserAuthority.getUserInfo().getClaims()
                            )
                    );
                }
            });

            logger.info("Mapped authorities: {}", grantedAuthorities);
            return grantedAuthorities;
        };
    }

    @Bean
    @Primary
    public NimbusJwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri("https://dev-k26quqx50xvr3vpw.us.auth0.com/.well-known/jwks.json")
                .build();

        decoder.setJwtValidator(jwt -> {
            System.out.println("DEBUG: JWT received: " + jwt.getTokenValue());
            System.out.println("DEBUG: JWT claims: " + jwt.getClaims());

            // ✅ Expect the API identifier, not the client ID
            String expectedAudience = "https://booking-backend-295607ecab74.herokuapp.com/";
            if (!jwt.getAudience().contains(expectedAudience)) {
                return OAuth2TokenValidatorResult.failure(
                        new org.springframework.security.oauth2.core.OAuth2Error("invalid_token",
                                "The required audience is missing", null));
            }

            return OAuth2TokenValidatorResult.success();
        });


        return decoder;
    }


}
