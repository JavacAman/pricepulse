package com.pricepulse.service;

import com.pricepulse.entity.Product;

/**
 * ============================================================
 * SOLID PRINCIPLE: Interface Segregation Principle (ISP)
 * ============================================================
 * WHAT IS ISP?
 *   "Clients should not be forced to depend on interfaces
 *   they don't use."
 *
 * HOW IT'S APPLIED HERE:
 *   This interface contains ONLY write/mutation operations.
 *   Any client that only needs to modify products (add, update, delete)
 *   depends on this interface — it never sees getAllProducts() or
 *   getProductById(), which it doesn't need.
 *
 * PAIR WITH ProductQueryService:
 *   Together, ProductQueryService + ProductCommandService split
 *   what would otherwise be one overloaded interface into two
 *   focused, client-appropriate contracts.
 *
 *   This also maps to the CQRS concept (Command Query Responsibility
 *   Segregation) — reads and writes are separated at the interface level.
 *
 * SOLID CONNECTION:
 *   → DIP: ProductController's write endpoints depend on this interface,
 *     not the concrete ProductService.
 *   → OCP: New command operations (bulkDelete, discountAll) extend this
 *     interface without affecting ProductQueryService or its callers.
 * ============================================================
 */
public interface ProductCommandService {

    Product addProduct(Product product);

    Product updatePrice(Long id, Double newPrice);

    void deleteProduct(Long id);
}