package com.pricepulse.service;

import com.pricepulse.entity.PriceAlert;
import com.pricepulse.entity.Product;
import com.pricepulse.entity.User;
import com.pricepulse.repository.PriceAlertRepository;
import com.pricepulse.repository.ProductRepository;
import com.pricepulse.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // JUnit5: enables Mockito annotations
class PriceAlertServiceTest {

    // Mockito: mocking all three repository dependencies
    @Mock private PriceAlertRepository priceAlertRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;

    // Mockito: @InjectMocks creates PriceAlertService and injects all @Mock repositories
    @InjectMocks private PriceAlertService priceAlertService;

    private User mockUser;
    private Product mockProduct;
    private PriceAlert mockAlert;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("Aman");
        mockUser.setEmail("aman@example.com");

        mockProduct = new Product();
        mockProduct.setId(10L);
        mockProduct.setName("iPhone 15");
        mockProduct.setCategory("Electronics");
        mockProduct.setCurrentPrice(999.99);

        mockAlert = new PriceAlert();
        mockAlert.setId(100L);
        mockAlert.setUser(mockUser);
        mockAlert.setProduct(mockProduct);
        mockAlert.setTargetPrice(850.0);
        mockAlert.setIsTriggered(false);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createAlert() tests
    // ─────────────────────────────────────────────────────────────────────────

    // JUnit5: testing createAlert() - happy path
    // Mockito: mocking UserRepository.findById, ProductRepository.findById, PriceAlertRepository.save
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("createAlert() - should create and save alert when user and product both exist")
    void createAlert_userAndProductExist_returnsCreatedAlert() {
        // ── Arrange ──────────────────────────────────────────────────────────
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(productRepository.findById(10L)).thenReturn(Optional.of(mockProduct));
        when(priceAlertRepository.save(any(PriceAlert.class))).thenReturn(mockAlert);

        // ── Act ──────────────────────────────────────────────────────────────
        PriceAlert result = priceAlertService.createAlert(1L, 10L, 850.0);

        // ── Assert ───────────────────────────────────────────────────────────
        assertNotNull(result);
        assertEquals(mockUser, result.getUser());
        assertEquals(mockProduct, result.getProduct());
        assertEquals(850.0, result.getTargetPrice());
        assertFalse(result.getIsTriggered());
        verify(priceAlertRepository).save(any(PriceAlert.class));
    }

    // JUnit5: testing createAlert() - user not found edge case
    // Mockito: mocking UserRepository.findById to return empty Optional
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("createAlert() - should throw RuntimeException when user ID does not exist")
    void createAlert_userNotFound_throwsException() {
        // ── Arrange ──────────────────────────────────────────────────────────
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // ── Act & Assert ──────────────────────────────────────────────────────
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> priceAlertService.createAlert(99L, 10L, 850.0));

        assertEquals("User not found", ex.getMessage());

        // Product lookup and save must NEVER run if user doesn't exist
        verify(productRepository, never()).findById(any());
        verify(priceAlertRepository, never()).save(any());
    }

    // JUnit5: testing createAlert() - product not found edge case
    // Mockito: user found, but ProductRepository.findById returns empty
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("createAlert() - should throw RuntimeException when product ID does not exist")
    void createAlert_productNotFound_throwsException() {
        // ── Arrange ──────────────────────────────────────────────────────────
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // ── Act & Assert ──────────────────────────────────────────────────────
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> priceAlertService.createAlert(1L, 99L, 850.0));

        assertEquals("Product not found", ex.getMessage());
        // Alert must NOT be saved if product doesn't exist (referential integrity)
        verify(priceAlertRepository, never()).save(any());
    }

    // JUnit5: testing createAlert() - verifies alert is built with correct field values
    // Mockito: capture the actual PriceAlert passed to save() using ArgumentCaptor
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("createAlert() - should save alert with isTriggered=false by default")
    void createAlert_newAlert_savedWithIsTriggeredFalse() {
        // ── Arrange ──────────────────────────────────────────────────────────
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(productRepository.findById(10L)).thenReturn(Optional.of(mockProduct));
        when(priceAlertRepository.save(any(PriceAlert.class))).thenAnswer(invocation -> {
            // Return the actual PriceAlert passed to save() so we can verify its fields
            return invocation.getArgument(0);
        });

        // ── Act ──────────────────────────────────────────────────────────────
        PriceAlert result = priceAlertService.createAlert(1L, 10L, 750.0);

        // ── Assert ───────────────────────────────────────────────────────────
        assertFalse(result.getIsTriggered(), "New alert should not be triggered");
        assertEquals(750.0, result.getTargetPrice());
        assertEquals(mockUser, result.getUser());
        assertEquals(mockProduct, result.getProduct());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getUserAlerts() tests
    // ─────────────────────────────────────────────────────────────────────────

    // JUnit5: testing getUserAlerts() - user has multiple alerts
    // Mockito: mocking PriceAlertRepository.findByUserId to return list
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("getUserAlerts() - should return all alerts belonging to the given user")
    void getUserAlerts_userHasAlerts_returnsList() {
        // ── Arrange ──────────────────────────────────────────────────────────
        PriceAlert alert2 = new PriceAlert();
        alert2.setId(101L);
        when(priceAlertRepository.findByUserId(1L)).thenReturn(Arrays.asList(mockAlert, alert2));

        // ── Act ──────────────────────────────────────────────────────────────
        List<PriceAlert> result = priceAlertService.getUserAlerts(1L);

        // ── Assert ───────────────────────────────────────────────────────────
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(priceAlertRepository).findByUserId(1L);
    }

    // JUnit5: testing getUserAlerts() - user has zero alerts
    // Mockito: mocking findByUserId to return empty list
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("getUserAlerts() - should return empty list when user has no alerts")
    void getUserAlerts_noAlerts_returnsEmptyList() {
        // ── Arrange ──────────────────────────────────────────────────────────
        when(priceAlertRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        // ── Act ──────────────────────────────────────────────────────────────
        List<PriceAlert> result = priceAlertService.getUserAlerts(1L);

        // ── Assert ───────────────────────────────────────────────────────────
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteAlert() tests
    // ─────────────────────────────────────────────────────────────────────────

    // JUnit5: testing deleteAlert() - happy path
    // Mockito: mocking PriceAlertRepository.deleteById
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("deleteAlert() - should delete alert by ID without throwing")
    void deleteAlert_validId_deletesSuccessfully() {
        // ── Arrange ──────────────────────────────────────────────────────────
        doNothing().when(priceAlertRepository).deleteById(100L);

        // ── Act ──────────────────────────────────────────────────────────────
        assertDoesNotThrow(() -> priceAlertService.deleteAlert(100L));

        // ── Assert ───────────────────────────────────────────────────────────
        verify(priceAlertRepository).deleteById(100L);
    }
}
