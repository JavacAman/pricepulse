package com.pricepulse.service;

import com.pricepulse.entity.PriceAlert;
import com.pricepulse.entity.Product;
import com.pricepulse.entity.User;
import com.pricepulse.repository.PriceAlertRepository;
import com.pricepulse.repository.ProductRepository;
import com.pricepulse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * ============================================================
 * SOLID PRINCIPLE: Single Responsibility Principle (SRP)
 * DESIGN PATTERN: Singleton Pattern
 * ============================================================
 * WHAT IS SRP?
 *   This class manages the price alert lifecycle ONLY:
 *   create, query, and delete price alerts.
 *   It does NOT send emails (EmailService does that).
 *   It does NOT schedule price checks (PriceScheduler does that).
 *   It does NOT handle JWT or authentication (AuthService does that).
 *
 *   ONE class, ONE responsibility, ONE reason to change.
 *
 * PATTERN: Singleton
 *   @Service → Spring creates ONE PriceAlertService instance,
 *   shared across all controllers and components that need it.
 * ============================================================
 */
@Service
@RequiredArgsConstructor
public class PriceAlertService {

    private final PriceAlertRepository priceAlertRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * ============================================================
     * DESIGN PATTERN: Builder Pattern (used for PriceAlert construction)
     * ACID PROPERTIES: Atomicity + Consistency + Isolation
     * ============================================================
     *
     * BUILDER PATTERN HERE:
     *   OLD WAY (setter-based — verbose and mutation-heavy):
     *     PriceAlert alert = new PriceAlert();
     *     alert.setUser(user);
     *     alert.setProduct(product);
     *     alert.setTargetPrice(targetPrice);
     *     alert.setIsTriggered(false);
     *
     *   NEW WAY (builder — clean and readable):
     *     PriceAlert alert = PriceAlert.builder()
     *                           .user(user)
     *                           .product(product)
     *                           .targetPrice(targetPrice)
     *                           .build();
     *     // isTriggered = false by @Builder.Default ✓
     *     // createdAt = now() by @Builder.Default ✓
     *
     * ACID - ATOMICITY:
     *   @Transactional wraps ALL three DB operations into ONE transaction:
     *   (1) userRepository.findById()
     *   (2) productRepository.findById()
     *   (3) priceAlertRepository.save()
     *   If any step throws an exception, ALL steps are rolled back.
     *   No orphaned alert is ever saved if the user or product lookup fails.
     *
     * ACID - CONSISTENCY:
     *   "Referential integrity must be maintained."
     *   Both user and product MUST exist before an alert is created.
     *   The @ManyToOne FK constraints in PriceAlert enforce this at the
     *   DB level too — double protection (app + DB).
     *
     * ACID - ISOLATION:
     *   @Transactional prevents dirty reads — if another transaction is
     *   deleting this user while we're creating an alert, isolation
     *   ensures we either see the user (and succeed) or don't (and fail cleanly).
     * ============================================================
     */
    @Transactional
    public PriceAlert createAlert(Long userId,
                                  Long productId,
                                  Double targetPrice) {
        // ACID-Consistency: Validate user exists BEFORE creating alert (referential integrity)
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // ACID-Consistency: Validate product exists BEFORE creating alert
        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        // Pattern-Builder: Explicit, clean construction — defaults handled by @Builder.Default
        PriceAlert alert = PriceAlert.builder()
                .user(user)
                .product(product)
                .targetPrice(targetPrice)
                .build();

        // ACID-Atomicity: This save is part of the transaction started by @Transactional
        return priceAlertRepository.save(alert);
    }

    // ACID-Consistency: Returns only committed, valid alerts for the given user
    public List<PriceAlert> getUserAlerts(Long userId) {
        return priceAlertRepository.findByUserId(userId);
    }

    // ACID-Atomicity: Delete is atomic — the alert is either fully removed or not at all
    @Transactional
    public void deleteAlert(Long alertId) {
        priceAlertRepository.deleteById(alertId);
    }
}