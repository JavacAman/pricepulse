package com.pricepulse.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * ============================================================
 * DESIGN PATTERN: Decorator Pattern
 * DESIGN PATTERN: Singleton Pattern
 * SOLID PRINCIPLE: SRP + OCP
 * ============================================================
 *
 * WHAT IS THE DECORATOR PATTERN?
 *   The Decorator Pattern attaches additional responsibilities to an
 *   object DYNAMICALLY by wrapping it in decorator objects.
 *   Each decorator adds ONE layer of behavior, then passes the
 *   call to the next decorator in the chain.
 *
 *   Classic example: Java InputStream
 *     InputStream → BufferedInputStream → DataInputStream
 *     Each wraps the previous one, adding a layer of capability.
 *
 * HOW SPRING SECURITY USES THE DECORATOR PATTERN:
 *   Every HTTP request passes through a CHAIN of filters.
 *   Each filter is a DECORATOR — it adds one security concern,
 *   then hands the request to the next filter in the chain.
 *
 *   Request flow through decorators:
 *
 *   Incoming Request
 *       ↓
 *   [CORS Filter]         → adds cross-origin permission headers
 *       ↓
 *   [CSRF Disabled]       → skips CSRF for stateless REST APIs
 *       ↓
 *   [SessionManagement]   → enforces STATELESS (no server sessions)
 *       ↓
 *   [JwtFilter]           → extracts JWT, sets authentication in context
 *       ↓
 *   [AuthorizationFilter] → checks if the authenticated user can access the route
 *       ↓
 *   Your @Controller      → actual business logic runs here
 *
 *   Each filter DECORATES the request with one security concern.
 *   None of them interfere with each other — clean separation.
 *
 * HOW addFilterBefore() WORKS (Decorator Registration):
 *   .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
 *   This inserts JwtFilter BEFORE Spring's default auth filter in the chain.
 *   JwtFilter is our custom decorator for JWT-based authentication.
 *
 * SOLID - SRP:
 *   SecurityConfig's ONE job: wire up the security filter chain.
 *   JWT logic → JwtFilter.
 *   User loading → UserDetailsServiceImpl.
 *   Token validation → JwtUtil.
 *   All SRP-compliant.
 *
 * SOLID - OCP:
 *   To add OAuth2 login or rate limiting: add another filter with
 *   addFilterBefore/addFilterAfter. No existing filter changes.
 *
 * PATTERN: Singleton
 *   @Configuration → Spring creates ONE SecurityConfig instance.
 *   All @Bean methods return singleton beans shared app-wide.
 * ============================================================
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // JwtFilter is our custom Decorator that adds JWT auth to the chain
    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Decorator Layer 1: CORS — allows Angular (localhost:4200) to call this API
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                    corsConfig.setAllowedOrigins(java.util.List.of(
                            "http://localhost:4200",
                            "https://rococo-narwhal-5e63c6.netlify.app"
                    ));
                    corsConfig.setAllowedMethods(java.util.List.of("GET","POST","PUT","DELETE","OPTIONS"));
                    corsConfig.setAllowedHeaders(java.util.List.of("*"));
                    corsConfig.setAllowCredentials(true);
                    return corsConfig;
                }))
                // Decorator Layer 2: CSRF disabled — REST APIs use JWT tokens, not CSRF cookies
                .csrf(csrf -> csrf.disable())
                // Decorator Layer 3: Session management — STATELESS means no server-side sessions
                //   Every request must carry a JWT token — server stores nothing between calls
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Decorator Layer 4: Authorization rules — /api/auth/** is public, all else needs JWT
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated())
                // Decorator Layer 5: JwtFilter — our custom decorator, runs BEFORE Spring's auth filter
                //   Reads "Authorization: Bearer <token>" header and sets SecurityContext
                .addFilterBefore(jwtFilter,
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // Singleton: ONE BCryptPasswordEncoder instance shared across the app
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Singleton: ONE AuthenticationManager instance shared across the app
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}