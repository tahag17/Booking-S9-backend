package fr.ensaetud.Booking_back.infrastructure.config;

import fr.ensaetud.Booking_back.user.controller.OAuth2SuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {


    private final OAuth2SuccessHandler successHandler;
    private final CorsConfigurationSource corsConfigurationSource;  // Add this

    public SecurityConfiguration(OAuth2SuccessHandler successHandler,
                                 CorsConfigurationSource corsConfigurationSource) {
        this.successHandler = successHandler;
        this.corsConfigurationSource = corsConfigurationSource;  // Add this

    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName(null);
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource));
        http
//                .authorizeHttpRequests(authorize -> authorize
//                        .requestMatchers(HttpMethod.GET, "api/tenant-listing/get-all-by-category").permitAll()
//                        .requestMatchers(HttpMethod.GET, "api/tenant-listing/get-one").permitAll()
//                        .requestMatchers(HttpMethod.POST, "api/tenant-listing/search").permitAll()
//                        .requestMatchers(HttpMethod.GET, "api/booking/check-availability").permitAll()
//                        .requestMatchers(HttpMethod.GET, "assets/*").permitAll()
//                        .anyRequest()
//                        .authenticated())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(requestHandler))
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(successHandler)
                )                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .oauth2Client(Customizer.withDefaults());

        return http.build();

    }

    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return authorities -> {
            Set<GrantedAuthority> grantedAuthorities = new HashSet<>();

            authorities.forEach(grantedAuthority -> {
                if (grantedAuthority instanceof OidcUserAuthority oidcUserAuthority) {
                    grantedAuthorities
                            .addAll(SecurityUtils.extractAuthorityFromClaims(oidcUserAuthority.getUserInfo().getClaims()));
                }
            });
            return grantedAuthorities;
        };
    }

}


