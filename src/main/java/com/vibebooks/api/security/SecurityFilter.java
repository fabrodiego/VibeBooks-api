package com.vibebooks.api.security;

import com.vibebooks.api.repository.UserRepository;
import com.vibebooks.api.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);
    private static final String AUTH_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        logger.debug("Security filter triggered for route: {}", request.getRequestURI());

        String jwtToken = recoverToken(request);

        if (jwtToken != null) {
            try {
                String subject = tokenService.getSubject(jwtToken);
                logger.debug("Subject (User ID) extracted from token: {}", subject);

                UUID userId = UUID.fromString(subject);

                UserDetails user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("Token user not found in the database"));

                logger.info("User '{}' found. Authenticating...", user.getUsername());

                var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.info("User '{}' successfully authenticated and set in the Security Context.", user.getUsername());

            } catch (Exception e) {
                // It is good practice to clear the security context in case of a token validation error.
                SecurityContextHolder.clearContext();
                logger.error("Failed to validate JWT token: {} - {}", e.getClass().getSimpleName(), e.getMessage());

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or expired JWT Token.");
                return;
            }
        } else {
            logger.trace("No JWT Token found in Authorization header. Proceeding with filter chain for anonymous access.");
        }

        filterChain.doFilter(request, response);
        logger.debug("Security filter finished for route: {}", request.getRequestURI());
    }

    private String recoverToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTH_HEADER);
        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)) {
            return authorizationHeader.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
