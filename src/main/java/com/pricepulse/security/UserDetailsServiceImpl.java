package com.pricepulse.security;

import com.pricepulse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * ============================================================
 * SOLID PRINCIPLES: SRP + DIP + LSP
 * DESIGN PATTERN: Singleton Pattern
 * ============================================================
 *
 * WHAT IS SRP HERE?
 *   This class has ONE job: load a user from the database for
 *   Spring Security's authentication process.
 *
 *   It does NOT validate passwords (AuthenticationManager does that).
 *   It does NOT generate tokens (JwtUtil does that).
 *   It does NOT send HTTP responses (controllers do that).
 *
 *   One job → one reason to change.
 *
 * WHAT IS DIP HERE?
 *   "High-level modules should not depend on low-level modules.
 *   Both should depend on abstractions."
 *
 *   JwtFilter does NOT import UserDetailsServiceImpl directly.
 *   It depends on UserDetailsService (Spring's interface):
 *     private final UserDetailsService userDetailsService;
 *
 *   At runtime, Spring injects THIS class (UserDetailsServiceImpl)
 *   into JwtFilter. If you ever swap the implementation
 *   (e.g., load users from LDAP instead of MySQL), JwtFilter
 *   doesn't change at all — it still just knows UserDetailsService.
 *
 * WHAT IS LSP HERE?
 *   Liskov Substitution Principle: "A subtype must be substitutable
 *   for its parent type without breaking the program."
 *
 *   UserDetailsServiceImpl IS-A UserDetailsService.
 *   Spring Security's AuthenticationManager expects UserDetailsService.
 *   Our implementation can replace the interface anywhere it's used —
 *   the caller (AuthenticationManager, JwtFilter) works identically.
 *   That's LSP: the child class honors the parent's contract fully.
 *
 * PATTERN: Singleton
 *   @Service → ONE UserDetailsServiceImpl instance for the whole app.
 *   Spring ensures the same instance is injected into JwtFilter and
 *   AuthenticationManager everywhere they need it.
 * ============================================================
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    // DIP: This class is the "low-level module" that Spring Security's
    // "high-level" auth components (JwtFilter, AuthenticationManager)
    // depend on via the UserDetailsService abstraction.
    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.getEmail())
                        .password(user.getPassword())
                        .roles(user.getRole().name())
                        .build())
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + email));
    }
}
