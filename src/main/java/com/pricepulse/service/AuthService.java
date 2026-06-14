package com.pricepulse.service;

import com.pricepulse.dto.AuthResponse;
import com.pricepulse.dto.LoginRequest;
import com.pricepulse.dto.RegisterRequest;
import com.pricepulse.entity.User;
import com.pricepulse.repository.UserRepository;
import com.pricepulse.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ============================================================
 * SOLID PRINCIPLE: Single Responsibility Principle (SRP)
 * DESIGN PATTERN: Singleton Pattern
 * ============================================================
 * WHAT IS SRP?
 *   "A class should have only ONE reason to change."
 *   AuthService manages user authentication: register and login.
 *   Nothing else. It does NOT send emails, does NOT handle products,
 *   does NOT validate JWT tokens — those are other classes' jobs.
 *
 *   If the registration flow changes → only AuthService changes.
 *   If email sending changes → only EmailService changes.
 *   Each class has its OWN reason to change. That's SRP.
 *
 * PATTERN: Singleton
 *   @Service → Spring creates ONE AuthService instance shared
 *   across the whole application (Singleton via Spring IoC).
 * ============================================================
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    /**
     * ============================================================
     * DESIGN PATTERN: Builder Pattern (used for User construction)
     * ACID PROPERTIES: Atomicity + Consistency + Isolation
     * ============================================================
     *
     * BUILDER PATTERN HERE:
     *   User.builder()
     *       .name(request.getName())
     *       .email(request.getEmail())
     *       .password(passwordEncoder.encode(request.getPassword()))
     *       .build();
     *   ↑ Each field is explicitly named. Role and createdAt use
     *     @Builder.Default values (USER and now()). Clean and safe.
     *
     * ACID - ATOMICITY:
     *   "All steps succeed together, or none of them do."
     *   @Transactional wraps this method in a DB transaction.
     *   Steps: (1) check email → (2) save user → (3) return token
     *   If step 2 (save) fails for any reason (DB error, constraint),
     *   Spring rolls back everything — no partial/ghost user is created.
     *
     * ACID - CONSISTENCY:
     *   "The DB must go from one valid state to another valid state."
     *   We check existsByEmail() BEFORE saving, ensuring the 'unique'
     *   constraint on email is never violated by application logic.
     *   The DB schema also enforces this via a unique index (double safety).
     *
     * ACID - ISOLATION:
     *   "Concurrent transactions don't interfere with each other."
     *   If two users register with the same email simultaneously,
     *   the unique constraint ensures only one succeeds.
     *   @Transactional provides the isolation level for the read-check.
     * ============================================================
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // ACID-Consistency: Validate email uniqueness BEFORE any write
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Pattern-Builder: Fluent construction — role defaults to USER, createdAt to now()
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        // ACID-Atomicity: If this save fails, @Transactional rolls back — no partial data
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(user.getId(), token, user.getEmail(), user.getName());
    }

    /**
     * ============================================================
     * ACID - CONSISTENCY: Login
     * ============================================================
     * authenticationManager.authenticate() throws an exception if
     * credentials are invalid — no token is ever issued for invalid users.
     * The DB state is never modified here, maintaining full consistency.
     * ============================================================
     */
    public AuthResponse login(LoginRequest request) {
        // ACID-Consistency: Authenticate credentials before issuing token
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(user.getId(), token, user.getEmail(), user.getName());
    }
}