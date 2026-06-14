package com.pricepulse.security;

import com.pricepulse.entity.User;
import com.pricepulse.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserDetailsServiceImpl.
 *
 * This covers the "UserService" requirement — PricePulse has no dedicated
 * UserService; UserDetailsServiceImpl is the Spring Security user-loading
 * component that bridges UserRepository with the auth framework.
 */
@ExtendWith(MockitoExtension.class) // JUnit5: enables Mockito annotations
class UserDetailsServiceImplTest {

    // Mockito: mocking UserRepository — no real DB needed
    @Mock private UserRepository userRepository;

    // Mockito: @InjectMocks creates UserDetailsServiceImpl and injects the mock repository
    @InjectMocks private UserDetailsServiceImpl userDetailsService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("Aman");
        mockUser.setEmail("aman@example.com");
        mockUser.setPassword("encoded-password");
        mockUser.setRole(User.Role.USER);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // loadUserByUsername() tests
    // ─────────────────────────────────────────────────────────────────────────

    // JUnit5: testing loadUserByUsername() - user found in repository
    // Mockito: mocking UserRepository.findByEmail to return the user
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("loadUserByUsername() - should return UserDetails when user exists in the DB")
    void loadUserByUsername_userExists_returnsUserDetails() {
        // ── Arrange ──────────────────────────────────────────────────────────
        when(userRepository.findByEmail("aman@example.com")).thenReturn(Optional.of(mockUser));

        // ── Act ──────────────────────────────────────────────────────────────
        UserDetails userDetails = userDetailsService.loadUserByUsername("aman@example.com");

        // ── Assert ───────────────────────────────────────────────────────────
        assertNotNull(userDetails);
        assertEquals("aman@example.com", userDetails.getUsername());
        assertEquals("encoded-password", userDetails.getPassword());

        // Verify that the ROLE_USER authority was mapped from Role.USER
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")),
                "UserDetails should have ROLE_USER authority");

        verify(userRepository).findByEmail("aman@example.com");
    }

    // JUnit5: testing loadUserByUsername() - user NOT found in repository
    // Mockito: mocking findByEmail to return empty Optional
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("loadUserByUsername() - should throw UsernameNotFoundException when user does not exist")
    void loadUserByUsername_userNotFound_throwsUsernameNotFoundException() {
        // ── Arrange ──────────────────────────────────────────────────────────
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // ── Act & Assert ──────────────────────────────────────────────────────
        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("unknown@example.com"));

        assertTrue(ex.getMessage().contains("unknown@example.com"),
                "Exception message should include the email that was not found");
        verify(userRepository).findByEmail("unknown@example.com");
    }

    // JUnit5: testing loadUserByUsername() - verifies admin role is correctly mapped
    // Mockito: mocking repository to return a user with ADMIN role
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("loadUserByUsername() - should map ADMIN role to ROLE_ADMIN authority")
    void loadUserByUsername_adminUser_hasAdminAuthority() {
        // ── Arrange ──────────────────────────────────────────────────────────
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("admin-encoded-password");
        adminUser.setRole(User.Role.ADMIN);

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));

        // ── Act ──────────────────────────────────────────────────────────────
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin@example.com");

        // ── Assert ───────────────────────────────────────────────────────────
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")),
                "Admin user should have ROLE_ADMIN authority");
    }
}
