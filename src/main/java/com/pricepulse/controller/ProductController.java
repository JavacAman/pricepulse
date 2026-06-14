package com.pricepulse.controller;

import com.pricepulse.entity.Product;
import com.pricepulse.service.ProductCommandService;
import com.pricepulse.service.ProductQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * ============================================================
 * SOLID PRINCIPLES: SRP + DIP + ISP
 * ============================================================
 *
 * WHAT IS SRP HERE?
 *   ProductController's ONLY job: receive HTTP requests for products
 *   and return HTTP responses. All business logic is in the services.
 *   No DB calls, no email, no JWT — just routing.
 *
 * WHAT IS DIP HERE?
 *   "High-level modules (this controller) should NOT depend on
 *   low-level modules (ProductService). Both should depend on abstractions."
 *
 *   WITHOUT DIP (tight coupling — bad):
 *     private final ProductService productService; // concrete class!
 *     If ProductService changes its class signature → controller breaks.
 *
 *   WITH DIP (loose coupling — what we do):
 *     private final ProductQueryService productQueryService;   // interface
 *     private final ProductCommandService productCommandService; // interface
 *     ProductService can change internally → controller is unaffected.
 *
 * WHAT IS ISP HERE?
 *   "Clients should not depend on methods they don't use."
 *   Instead of one ProductService with ALL methods (6 total), we inject:
 *
 *     ProductQueryService   → getProductById, getAllProducts, getProductsByCategory
 *       ↑ used by GET endpoints only
 *
 *     ProductCommandService → addProduct, updatePrice, deleteProduct
 *       ↑ used by POST/PUT/DELETE endpoints only
 *
 *   A future read-only controller would ONLY need ProductQueryService —
 *   it's never exposed to addProduct() or deleteProduct(). That's ISP.
 *
 * NOTE ON SPRING WIRING:
 *   ProductService implements BOTH interfaces.
 *   When Spring sees "inject ProductQueryService", it finds ProductService
 *   (the only bean implementing it) and injects it.
 *   Same for ProductCommandService. Both fields point to THE SAME bean.
 * ============================================================
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    // ISP + DIP: Read endpoints only need the query interface
    private final ProductQueryService productQueryService;

    // ISP + DIP: Write endpoints only need the command interface
    private final ProductCommandService productCommandService;

    @PostMapping
    public ResponseEntity<Product> addProduct(
            @RequestBody Product product) {
        return ResponseEntity.ok(productCommandService.addProduct(product));
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productQueryService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(
            @PathVariable Long id) {
        return ResponseEntity.ok(productQueryService.getProductById(id));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getByCategory(
            @PathVariable String category) {
        return ResponseEntity
                .ok(productQueryService.getProductsByCategory(category));
    }

    @PutMapping("/{id}/price")
    public ResponseEntity<Product> updatePrice(
            @PathVariable Long id,
            @RequestParam Double newPrice) {
        return ResponseEntity
                .ok(productCommandService.updatePrice(id, newPrice));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productCommandService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
