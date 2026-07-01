package com.vistara.tourist_tracking_system.config;

import com.vistara.tourist_tracking_system.service.JwtService;
import com.vistara.tourist_tracking_system.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String requestURI = request.getRequestURI();

        log.debug("Processing request: {} with auth header: {}", requestURI, authHeader != null ? "Present" : "Missing");

        // Skip if no Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No Bearer token found for request: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        String userEmail = null;

        try {
            // Extract username from token
            userEmail = jwtService.extractUsername(jwt);
            log.debug("Extracted username from token: {}", userEmail);

        } catch (ExpiredJwtException e) {
            log.warn("⚠️ JWT token expired for request: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Token expired\"}");
            return;

        } catch (MalformedJwtException e) {
            log.warn("⚠️ Malformed JWT token for request: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Invalid token format\"}");
            return;

        } catch (SignatureException e) {
            log.warn("⚠️ Invalid JWT signature for request: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Invalid token signature\"}");
            return;

        } catch (Exception e) {
            log.error("❌ Error extracting username from JWT: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Invalid token\"}");
            return;
        }

        // Validate user and token
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // Load user details
                UserDetails userDetails = userService.loadUserByUsername(userEmail);
                log.debug("User details loaded for: {} with authorities: {}",
                        userEmail, userDetails.getAuthorities());

                // Validate the token
                if (jwtService.validateToken(jwt, userDetails)) {
                    // Create authentication token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.info("✅ JWT authentication successful for user: {} (Role: {})",
                            userEmail,
                            userDetails.getAuthorities().stream()
                                    .map(auth -> auth.getAuthority())
                                    .reduce((a, b) -> a + ", " + b)
                                    .orElse("No roles")
                    );

                } else {
                    log.warn("❌ JWT validation failed for user: {}", userEmail);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\": \"Invalid token\"}");
                    return;
                }

            } catch (UsernameNotFoundException e) {
                log.warn("❌ User not found: {}", userEmail);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"User not found\"}");
                return;

            } catch (Exception e) {
                log.error("❌ Error during authentication: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Authentication error\"}");
                return;
            }
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Skip JWT filter for public endpoints
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/auth/") ||
                path.startsWith("/public/") ||
                path.startsWith("/ws/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs/") ||
                path.startsWith("/payments/mpesa/stk-callback");
    }
}