package fr.ensaetud.Booking_back.infrastructure.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class AuthHeaderLoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(AuthHeaderLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String auth = req.getHeader("Authorization");

        logger.info("➡️ Incoming request: {} {}", req.getMethod(), req.getRequestURI());
        logger.info("➡️ Authorization header present: {}", auth != null);
        if (auth != null) {
            logger.info("➡️ Authorization header starts with: {}", auth.substring(0, Math.min(auth.length(), 30)));
        }

        chain.doFilter(request, response);
    }
}
