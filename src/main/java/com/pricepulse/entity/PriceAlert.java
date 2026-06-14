package com.pricepulse.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * ============================================================
 * DESIGN PATTERN: Builder Pattern
 * ============================================================
 * WHAT IS IT?
 *   Builder Pattern constructs objects step-by-step using a
 *   fluent API. Each setter in the chain is named after its
 *   field, making construction self-documenting and safe.
 *
 * HOW IT'S USED IN PriceAlertService.createAlert():
 *
 *   OLD WAY (setter-based — easy to forget a field):
 *     PriceAlert alert = new PriceAlert();
 *     alert.setUser(user);
 *     alert.setProduct(product);
 *     alert.setTargetPrice(targetPrice);
 *     alert.setIsTriggered(false);  // easy to forget this line!
 *
 *   NEW WAY (builder — explicit, clean, interview-ready):
 *     PriceAlert alert = PriceAlert.builder()
 *                           .user(user)
 *                           .product(product)
 *                           .targetPrice(targetPrice)
 *                           .build();
 *     // isTriggered defaults to false via @Builder.Default ✓
 *     // createdAt defaults to now() via @Builder.Default ✓
 *
 * KEY BENEFIT:
 *   You cannot accidentally skip required fields — the builder
 *   makes the construction intent visible at a glance.
 *   Also useful when adding new optional fields — existing build()
 *   call sites don't need to change (unlike constructors).
 *
 * SOLID CONNECTION:
 *   → OCP: Adding a new optional field to PriceAlert won't break
 *     any existing PriceAlert.builder()...build() call sites.
 *     With a constructor, you'd need to update every call site.
 * ============================================================
 */
@Entity
@Table(name = "price_alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Double targetPrice;

    @Column(nullable = false)
    @Builder.Default  // Ensures isTriggered = false even if not set in builder chain
    private Boolean isTriggered = false;

    @Column(name = "created_at")
    @Builder.Default  // Ensures createdAt = now() even if not set in builder chain
    private LocalDateTime createdAt = LocalDateTime.now();
}