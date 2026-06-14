package com.pricepulse.service;

import com.pricepulse.dto.AuthResponse;
import com.pricepulse.dto.LoginRequest;
import com.pricepulse.dto.RegisterRequest;
import com.pricepulse.entity.User;
import com.pricepulse.repository.UserRepository;
import com.pricepulse.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 *
 * Why unit tests (not @SpringBootTest)?
 *   @SpringBootTest starts the full Spring context — slow and unnecessary here.
 *   With Mockito we isolate AuthService from its DB/JWT/Security dependencies.
 *   Each test runs in milliseconds and tests ONE thing clearly.
 */
@ExtendWith(MockitoExtension.class) // JUnit5: enables Mockito annotations (@Mock, @InjectMocks)
class AuthServiceTest {

    // Mockito: @Mock creates a fake (stub) of each dependency.
    // AuthService will never touch a real DB, real encoder, or real JWT library.
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;

    // Mockito: @InjectMocks creates an instance of AuthService and automatically
    // injects the four @Mock fields above via constructor injection.
    @InjectMocks private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        // Shared test data reused across all tests in this class
        registerRequest = new RegisterRequest();
        registerRequest.setName("Aman");
        registerRequest.setEmail("aman@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("aman@example.com");
        loginRequest.setPassword("password123");

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("Aman");
        mockUser.setEmail("aman@example.com");
        mockUser.setPassword("encoded-password");
        mockUser.setRole(User.Role.USER);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // register() tests
    // ─────────────────────────────────────────────────────────────────────────

    // JUnit5: testing register() - happy path
    // Mockito: mocking existsByEmail=false, passwordEncoder, save (JPA id simulation), jwtUtil
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("register() - should create user and return JWT token when email is not already taken")
    void register_newEmail_createsUserAndReturnsToken() {
        // ── Arrange ──────────────────────────────────────────────────────────
        when(userRepository.existsByEmail("aman@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");

        // JPA normally sets the ID on the entity object IN PLACE after save().
        // doAnswer simulates this behavior so user.getId() returns 1L afterwards.
        doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        }).when(userRepository).save(any(User.class));

        when(jwtUtil.generateToken("aman@example.com")).thenReturn("mocked-jwt-token");

        // ── Act ──────────────────────────────────────────────────────────────
        AuthResponse response = authService.register(registerRequest);

        // ── Assert ───────────────────────────────────────────────────────────
        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("aman@example.com", response.getEmail());
        assertEquals("Aman", response.getName());
        assertEquals("mocked-jwt-token", response.getToken());

        // Verify interactions: save and encode must each be called exactly once
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
        verify(jwtUtil).generateToken("aman@example.com");
    }

    // JUnit5: testing register() - duplicate email edge case
    // Mockito: mocking existsByEmail=true to simulate taken email
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("register() - should throw RuntimeException when email is already registered")
    void register_duplicateEmail_throwsException() {
        // ── Arrange ──────────────────────────────────────────────────────────
        when(userRepository.existsByEmail("aman@example.com")).thenReturn(true);

        // ── Act & Assert ──────────────────────────────────────────────────────
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(registerRequest));

        assertEquals("Email already registered", ex.getMessage());

        // Critical: save() and encode() must NEVER be called if email is taken
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // login() tests
    // ─────────────────────────────────────────────────────────────────────────

    // JUnit5: testing login() - happy path
    // Mockito: mocking AuthenticationManager success, UserRepository, JwtUtil
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("login() - should return JWT token for valid credentials")
    void login_validCredentials_returnsToken() {
        // ── Arrange ──────────────────────────────────────────────────────────
        // authenticate() returns an Authentication object; returning null is fine
        // for the test since AuthService doesn't use the return value
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("aman@example.com")).thenReturn(Optional.of(mockUser));
        when(jwtUtil.generateToken("aman@example.com")).thenReturn("mocked-jwt-token");

        // ── Act ──────────────────────────────────────────────────────────────
        AuthResponse response = authService.login(loginRequest);

        // ── Assert ───────────────────────────────────────────────────────────
        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("aman@example.com", response.getEmail());
        assertEquals("Aman", response.getName());
        assertEquals("mocked-jwt-token", response.getToken());
    }

    // JUnit5: testing login() - user not found after authentication
    // Mockito: AuthenticationManager passes but findByEmail returns empty
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("login() - should throw RuntimeException when user not found after authentication")
    void login_userNotFound_throwsException() {
        // ── Arrange ──────────────────────────────────────────────────────────
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("aman@example.com")).thenReturn(Optional.empty());

        // ── Act & Assert ──────────────────────────────────────────────────────
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(loginRequest));

        assertEquals("User not found", ex.getMessage());
        // Token must NOT be issued if user record doesn't exist
        verify(jwtUtil, never()).generateToken(anyString());
    }

    // JUnit5: testing login() - wrong password / bad credentials
    // Mockito: AuthenticationManager throws BadCredentialsException
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("login() - should propagate exception when credentials are invalid")
    void login_badCredentials_throwsException() {
        // ── Arrange ──────────────────────────────────────────────────────────
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // ── Act & Assert ──────────────────────────────────────────────────────
        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));

        // DB and JWT must never be touched after auth failure
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtUtil, never()).generateToken(anyString());
    }
}
