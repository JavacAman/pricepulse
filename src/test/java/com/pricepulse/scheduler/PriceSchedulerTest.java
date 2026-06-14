package com.pricepulse.scheduler;

import com.pricepulse.entity.PriceAlert;
import com.pricepulse.entity.Product;
import com.pricepulse.entity.User;
import com.pricepulse.observer.PriceDropObserver;
import com.pricepulse.repository.PriceAlertRepository;
import com.pricepulse.repository.ProductRepository;
import com.pricepulse.strategy.PriceUpdateStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PriceScheduler.
 *
 * WHY NO @InjectMocks HERE?
 *   PriceScheduler has a List<PriceDropObserver> field.
 *   Mockito's @InjectMocks cannot handle generic List<Interface> injection.
 *   Solution: manually construct PriceScheduler in @BeforeEach,
 *   passing a real List containing the mocked observer.
 *   This is cleaner and more explicit for collection dependencies.
 *
 * NOTE ON @Transactional and @Scheduled:
 *   These Spring annotations are AOP-proxied and do NOT activate
 *   in pure Mockito tests. checkPriceDrops() is called directly here
 *   without scheduling or transaction wrappers — we test the logic only.
 */
@ExtendWith(MockitoExtension.class) // JUnit5: enables Mockito annotations
class PriceSchedulerTest {

    // Mockito: mocking repository and strategy dependencies
    @Mock private PriceAlertRepository priceAlertRepository;
    @Mock private ProductRepository productRepository;
    @Mock private PriceUpdateStrategy priceUpdateStrategy;

    // Mockito: mocking a single PriceDropObserver to verify it gets notified
    @Mock private PriceDropObserver mockObserver;

    // Manually constructed — not @InjectMocks (see class javadoc above)
    private PriceScheduler priceScheduler;

    private User mockUser;
    private Product mockProduct;

    @BeforeEach
    void setUp() {
        // Manually build PriceScheduler with the mocked observer in the list.
        // List.of() wraps the single mock observer — simulates Spring injecting
        // all PriceDropObserver beans into the list.
        priceScheduler = new PriceScheduler(
                priceAlertRepository,
                productRepository,
                priceUpdateStrategy,
                List.of(mockObserver)
        );

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("aman@example.com");

        mockProduct = new Product();
        mockProduct.setId(10L);
        mockProduct.setName("iPhone 15");
        mockProduct.setCurrentPrice(1000.0);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // checkPriceDrops() tests
    // ─────────────────────────────────────────────────────────────────────────

    // JUnit5: testing checkPriceDrops() - no products in DB
    // Mockito: mocking productRepository.findAll to return empty list
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("checkPriceDrops() - should do nothing when there are no products")
    void checkPriceDrops_noProducts_noObserversNotified() {
        // ── Arrange ──────────────────────────────────────────────────────────
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        // ── Act ──────────────────────────────────────────────────────────────
        priceScheduler.checkPriceDrops();

        // ── Assert ───────────────────────────────────────────────────────────
        // With no products, strategy should never be called and no observer notified
        verify(priceUpdateStrategy, never()).updatePrice(any());
        verify(mockObserver, never()).onPriceDrop(any(), any(), any());
        verify(priceAlertRepository, never()).save(any());
    }

    // JUnit5: testing checkPriceDrops() - price drops below target
    // Mockito: strategy returns 800.0, target is 900.0 → alert triggers, observer notified
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("checkPriceDrops() - should notify observer and mark alert triggered when price drops below target")
    void checkPriceDrops_priceDropsBelowTarget_notifiesObserverAndTriggersAlert() {
        // ── Arrange ──────────────────────────────────────────────────────────
        PriceAlert alert = new PriceAlert();
        alert.setId(100L);
        alert.setUser(mockUser);
        alert.setProduct(mockProduct);
        alert.setTargetPrice(900.0);   // user wants alert when price ≤ 900
        alert.setIsTriggered(false);

        when(productRepository.findAll()).thenReturn(List.of(mockProduct));
        when(priceUpdateStrategy.updatePrice(1000.0)).thenReturn(800.0); // drops to 800!
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);
        when(priceAlertRepository.findByProductIdAndIsTriggeredFalse(10L))
                .thenReturn(List.of(alert));

        // ── Act ──────────────────────────────────────────────────────────────
        priceScheduler.checkPriceDrops();

        // ── Assert ───────────────────────────────────────────────────────────
        // Observer MUST be notified — 800.0 <= 900.0 (target price hit)
        verify(mockObserver).onPriceDrop(alert, mockProduct, 800.0);

        // Alert MUST be saved with isTriggered=true so user isn't notified again
        verify(priceAlertRepository).save(alert);
    }

    // JUnit5: testing checkPriceDrops() - price stays above target
    // Mockito: strategy returns 950.0, target is 900.0 → alert NOT triggered
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("checkPriceDrops() - should NOT notify observer when price is still above target")
    void checkPriceDrops_priceAboveTarget_observerNotNotified() {
        // ── Arrange ──────────────────────────────────────────────────────────
        PriceAlert alert = new PriceAlert();
        alert.setId(100L);
        alert.setUser(mockUser);
        alert.setProduct(mockProduct);
        alert.setTargetPrice(900.0);  // user wants alert when price ≤ 900
        alert.setIsTriggered(false);

        when(productRepository.findAll()).thenReturn(List.of(mockProduct));
        when(priceUpdateStrategy.updatePrice(1000.0)).thenReturn(950.0); // still above 900!
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);
        when(priceAlertRepository.findByProductIdAndIsTriggeredFalse(10L))
                .thenReturn(List.of(alert));

        // ── Act ──────────────────────────────────────────────────────────────
        priceScheduler.checkPriceDrops();

        // ── Assert ───────────────────────────────────────────────────────────
        // 950.0 > 900.0, so observer must NOT be called and alert must NOT be saved
        verify(mockObserver, never()).onPriceDrop(any(), any(), any());
        verify(priceAlertRepository, never()).save(any());
    }

    // JUnit5: testing checkPriceDrops() - multiple products, mixed alert outcomes
    // Mockito: product1 drops (triggers), product2 doesn't drop (no trigger)
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("checkPriceDrops() - should only trigger alerts for products whose price actually dropped")
    void checkPriceDrops_multipleProducts_onlyTriggersEligibleAlerts() {
        // ── Arrange ──────────────────────────────────────────────────────────
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("iPhone");
        product1.setCurrentPrice(1000.0);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("MacBook");
        product2.setCurrentPrice(2000.0);

        PriceAlert alertForProduct1 = new PriceAlert();
        alertForProduct1.setId(1L);
        alertForProduct1.setUser(mockUser);
        alertForProduct1.setProduct(product1);
        alertForProduct1.setTargetPrice(900.0);
        alertForProduct1.setIsTriggered(false);

        PriceAlert alertForProduct2 = new PriceAlert();
        alertForProduct2.setId(2L);
        alertForProduct2.setUser(mockUser);
        alertForProduct2.setProduct(product2);
        alertForProduct2.setTargetPrice(1800.0);
        alertForProduct2.setIsTriggered(false);

        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));
        when(priceUpdateStrategy.updatePrice(1000.0)).thenReturn(850.0); // product1 drops!
        when(priceUpdateStrategy.updatePrice(2000.0)).thenReturn(1900.0); // product2 stays above
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        when(priceAlertRepository.findByProductIdAndIsTriggeredFalse(1L))
                .thenReturn(List.of(alertForProduct1));
        when(priceAlertRepository.findByProductIdAndIsTriggeredFalse(2L))
                .thenReturn(List.of(alertForProduct2));

        // ── Act ──────────────────────────────────────────────────────────────
        priceScheduler.checkPriceDrops();

        // ── Assert ───────────────────────────────────────────────────────────
        // product1 alert: 850 <= 900 → observer notified, alert triggered
        verify(mockObserver).onPriceDrop(alertForProduct1, product1, 850.0);
        verify(priceAlertRepository).save(alertForProduct1);

        // product2 alert: 1900 > 1800 → observer NOT notified, alert NOT triggered
        verify(mockObserver, never()).onPriceDrop(alertForProduct2, product2, 1900.0);
        verify(priceAlertRepository, never()).save(alertForProduct2);
    }

    // JUnit5: testing checkPriceDrops() - price equals exact target (boundary)
    // Mockito: newPrice == targetPrice exactly → alert SHOULD trigger (condition is <=)
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("checkPriceDrops() - should trigger alert when price equals the exact target price")
    void checkPriceDrops_priceEqualsTarget_triggersAlert() {
        // ── Arrange ──────────────────────────────────────────────────────────
        PriceAlert alert = new PriceAlert();
        alert.setId(100L);
        alert.setUser(mockUser);
        alert.setProduct(mockProduct);
        alert.setTargetPrice(900.0);  // exact boundary
        alert.setIsTriggered(false);

        when(productRepository.findAll()).thenReturn(List.of(mockProduct));
        when(priceUpdateStrategy.updatePrice(1000.0)).thenReturn(900.0); // exactly hits target
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);
        when(priceAlertRepository.findByProductIdAndIsTriggeredFalse(10L))
                .thenReturn(List.of(alert));

        // ── Act ──────────────────────────────────────────────────────────────
        priceScheduler.checkPriceDrops();

        // ── Assert ───────────────────────────────────────────────────────────
        // 900.0 <= 900.0 is TRUE — boundary case must trigger the observer
        verify(mockObserver).onPriceDrop(alert, mockProduct, 900.0);
    }
}
