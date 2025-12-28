package fr.ensaetud.Booking_back.user.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log =
            LoggerFactory.getLogger(OAuth2SuccessHandler.class);

    @Value("${application.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        log.info("✅ OAuth2 SUCCESS HANDLER TRIGGERED");
        log.info("➡ Redirecting user to frontend URL: {}", frontendUrl);
        log.info("➡ Authenticated principal: {}", authentication.getName());
        log.info("➡ Session ID: {}", request.getSession().getId());

        response.sendRedirect(frontendUrl);
    }

}

