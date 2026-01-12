package fr.ensaetud.Booking_back.infrastructure.config;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class JwtSuccessLogger {

    private static final Logger logger = LoggerFactory.getLogger(JwtSuccessLogger.class);

    @EventListener
    public void onAuthSuccess(AuthenticationSuccessEvent event) {
        if (event.getAuthentication() instanceof JwtAuthenticationToken jwtAuth) {
            logger.info("✅ JWT authenticated successfully");
            logger.info("Issuer: {}", jwtAuth.getToken().getIssuer());
            logger.info("Audience: {}", jwtAuth.getToken().getAudience());
            logger.info("Subject: {}", jwtAuth.getToken().getSubject());
            logger.info("All Claims: {}", jwtAuth.getToken().getClaims());
        }
    }
}

