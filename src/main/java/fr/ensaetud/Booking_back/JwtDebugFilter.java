package fr.ensaetud.Booking_back;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtDebugFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            System.out.println("\n===== DEBUG: Current Authentication =====");
            System.out.println("Authentication class: " + auth.getClass().getName());
            System.out.println("Authorities: " + auth.getAuthorities());

            // If this is a JWT-authenticated request
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();

                System.out.println("----- JWT RAW TOKEN -----");
                System.out.println(jwt.getTokenValue());

                System.out.println("----- JWT HEADERS -----");
                System.out.println(jwt.getHeaders());

                System.out.println("----- JWT CLAIMS -----");
                System.out.println(jwt.getClaims());
            } else {
                System.out.println("Principal: " + auth.getPrincipal());
            }

            System.out.println("=========================================\n");
        }

        filterChain.doFilter(request, response);
    }
}
