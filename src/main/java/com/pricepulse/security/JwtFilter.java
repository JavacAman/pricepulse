package com.pricepulse.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * ============================================================
 * DESIGN PATTERN: Decorator Pattern (Concrete Decorator)
 * SOLID PRINCIPLES: SRP + DIP
 * ============================================================
 *
 * DECORATOR PATTERN — THIS CLASS IS ONE FILTER/DECORATOR IN THE CHAIN:
 *   Recall from SecurityConfig: Spring Security's filter chain is a
 *   series of Decorator layers. This class IS one of those layers —
 *   specifically the "JWT Authentication" decorator.
 *
 *   What this decorator ADDS to each request:
 *     1. Reads the "Authorization: Bearer <token>" header
 *     2. Validates the token using JwtUtil
 *     3. Extracts the user's email from the token
 *     4. Loads the UserDetails from DB
 *     5. Sets the authentication in Spring's SecurityContext
 *     6. Calls filterChain.doFilter() → passes to the NEXT decorator
 *
 *   filterChain.doFilter(request, response) is the KEY LINE.
 *   It's how decorators "chain" — each one does its work,
 *   then hands the request to the next decorator in line.
 *   This is identical to how Java's InputStream decorators work:
 *     bufferedStream.read() → adds buffering, then calls underlying stream.
 *
 * WHY extends OncePerRequestFilter?
 *   OncePerRequestFilter is the BASE DECORATOR provided by Spring.
 *   It guarantees this filter runs EXACTLY ONCE per request
 *   (not twice due to forwarding/includes). We override doFilterInternal()
 *   to add our specific decoration: JWT authentication.
 *
 * SOLID - SRP:
 *   This class has ONE job: extract + validate JWT and set SecurityContext.
 *   It does NOT generate tokens (JwtUtil does that).
 *   It does NOT load users from DB itself (UserDetailsService does that).
 *   It does NOT decide access rules (SecurityConfig does that).
 *
 * SOLID - DIP:
 *   Depends on UserDetailsService INTERFACE (not UserDetailsServiceImpl).
 *   Spring injects UserDetailsServiceImpl at runtime — this class
 *   never imports or knows UserDetailsServiceImpl exists.
 *
 * PATTERN: Singleton
 *   @Component → ONE JwtFilter instance for the entire application.
 * ============================================================
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    // DIP: Depends on interface, not UserDetailsServiceImpl directly
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Only process requests that carry a Bearer token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.extractEmail(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // Set authentication in Spring's SecurityContext so downstream
                // filters and controllers know WHO is making this request
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Decorator Pattern: Pass the (now-decorated) request to the next filter in the chain
        filterChain.doFilter(request, response);
    }
}
