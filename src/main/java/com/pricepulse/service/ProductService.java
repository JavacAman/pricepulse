package com.pricepulse.service;

import com.pricepulse.entity.Product;
import com.pricepulse.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * ============================================================
 * SOLID PRINCIPLES: SRP + DIP + ISP
 * DESIGN PATTERN: Singleton Pattern
 * ============================================================
 *
 * WHAT IS SRP HERE?
 *   ProductService has ONE responsibility: manage product data
 *   access and its Redis cache lifecycle. Nothing else.
 *   No email, no alerts, no JWT — just products + caching.
 *
 * WHAT IS DIP HERE?
 *   "High-level modules (controllers) should depend on
 *   abstractions, not concrete classes."
 *
 *   ProductController does NOT inject ProductService directly.
 *   Instead it injects:
 *     ProductQueryService   → for GET endpoints
 *     ProductCommandService → for POST/PUT/DELETE endpoints
 *
 *   ProductService IMPLEMENTS both of those interfaces.
 *   Controllers never know ProductService exists — they only see
 *   the interfaces. This is DIP in action.
 *
 * WHAT IS ISP HERE?
 *   Instead of one big ProductService interface with 6 methods,
 *   we split into TWO focused interfaces:
 *     ProductQueryService   (read-only: get, getAll, getByCategory)
 *     ProductCommandService (write-only: add, updatePrice, delete)
 *
 *   A future service needing only reads depends on ProductQueryService —
 *   it never sees addProduct() or deleteProduct().
 *
 * PATTERN: Singleton
 *   @Service → Spring creates ONE ProductService instance.
 *   Both ProductQueryService and ProductCommandService injection points
 *   in ProductController receive THE SAME ProductService bean.
 * ============================================================
 */
@Service
@RequiredArgsConstructor
public class ProductService implements ProductQueryService, ProductCommandService {

    private final ProductRepository productRepository;

    /**
     * ACID - ATOMICITY: If the DB save fails, @Transactional rolls back.
     * ACID - CONSISTENCY: JPA validates @Column(nullable=false) before commit.
     * @CacheEvict: Invalidates the "products" list cache after a new product
     *   is added, so the next getAllProducts() fetches fresh data from DB.
     */
    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    /**
     * @Cacheable: On first call, fetches from DB and stores in Redis.
     * Subsequent calls return from Redis cache (no DB hit) until evicted.
     * ACID - CONSISTENCY: Only committed products are ever cached or returned.
     */
    @Override
    @Cacheable(value = "products")
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * @Cacheable(key = "#id"): Each product is cached individually by its ID.
     * ACID - CONSISTENCY: Returns only a committed, valid product.
     */
    @Override
    @Cacheable(value = "product", key = "#id")
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));
    }

    /**
     * @Cacheable(key = "#category"): Category-level cache.
     * ACID - CONSISTENCY: Returns only committed products for this category.
     */
    @Override
    @Cacheable(value = "products", key = "#category")
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    /**
     * ACID - ATOMICITY: The DB update and cache refresh are in one transaction.
     *   If the DB save fails, @Transactional rolls back → cache is not updated.
     *   No risk of cache having a price that the DB doesn't have.
     * ACID - DURABILITY: Once @Transactional commits, the price change is
     *   permanent and survives system restarts.
     * @CachePut: Updates the specific product's cache entry with the new price.
     * @CacheEvict: Invalidates the list cache so getAllProducts() re-fetches.
     */
    @Override
    @Transactional
    @CachePut(value = "product", key = "#id")
    @CacheEvict(value = "products", allEntries = true)
    public Product updatePrice(Long id, Double newPrice) {
        Product product = getProductById(id);
        product.setCurrentPrice(newPrice);
        return productRepository.save(product);
    }

    /**
     * ACID - ATOMICITY: Delete is atomic — product is either fully removed or not at all.
     * Both cache entries (individual + list) are evicted after successful delete.
     */
    @Override
    @Transactional
    @CacheEvict(value = {"product", "products"}, allEntries = true)
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}