package com.pricepulse.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ============================================================
 * DESIGN PATTERN: Builder Pattern
 * ============================================================
 * WHAT IS IT?
 *   The Builder Pattern provides a step-by-step way to construct
 *   complex objects. Instead of passing all fields through a
 *   constructor (hard to read, easy to mix up order), you use a
 *   fluent builder API where each field is explicitly named.
 *
 * WITHOUT Builder (telescoping constructor — hard to read):
 *   Product p = new Product(null, "iPhone", "Electronics", 999.99, "img.jpg", LocalDateTime.now());
 *   ↑ What does null mean here? Is 999.99 the price or something else? Confusing!
 *
 * WITH Builder (clear and readable):
 *   Product p = Product.builder()
 *                 .name("iPhone")
 *                 .category("Electronics")
 *                 .currentPrice(999.99)
 *                 .imageUrl("img.jpg")
 *                 .build();
 *   ↑ Each field is named. Order doesn't matter. No confusion.
 *
 * HOW LOMBOK GENERATES THE BUILDER:
 *   @Builder generates a static inner class 'ProductBuilder' with
 *   methods for each field. @Builder.Default ensures fields with
 *   initializers (like createdAt) keep their default values even
 *   when not set in the builder chain.
 *
 * WHY @NoArgsConstructor + @AllArgsConstructor?
 *   JPA requires a no-arg constructor to instantiate entities from DB rows.
 *   @Builder requires an all-args constructor internally.
 *   Both are needed together with @Builder on JPA entities.
 *
 * PATTERN: Singleton (at repository level)
 *   Product instances are managed by JPA's EntityManager.
 *   The ProductRepository @Bean itself is a Singleton — one shared
 *   instance handles all DB access for Product throughout the app.
 * ============================================================
 */
@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Double currentPrice;

    private String imageUrl;

    @Column(name = "created_at")
    @Builder.Default  // Without this, builder would set createdAt = null instead of now()
    private LocalDateTime createdAt = LocalDateTime.now();
}