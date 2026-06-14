package com.pricepulse.repository;

import com.pricepulse.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * ============================================================
 * SOLID PRINCIPLE: Dependency Inversion Principle (DIP)
 * SOLID PRINCIPLE: Interface Segregation Principle (ISP)
 * DESIGN PATTERN: Singleton Pattern
 * ============================================================
 *
 * DIP:
 *   ProductService depends on ProductRepository (this interface),
 *   not on any Hibernate class or JDBC driver.
 *   The actual SQL execution is handled by Spring Data's generated proxy.
 *   ProductService never knows or cares HOW data is stored — only WHAT to ask for.
 *
 * ISP (how JpaRepository applies):
 *   JpaRepository<Product, Long> provides many methods (save, findAll,
 *   findById, delete, count...). We only add what's specific to Product:
 *   findByCategory(). This keeps the interface focused on Product needs.
 *   Any unused JpaRepository methods exist but are never called —
 *   a mild ISP concern, but this is the standard Spring Data trade-off.
 *
 * PATTERN: Singleton
 *   Spring creates ONE proxy bean for ProductRepository, shared across
 *   ProductService, PriceScheduler, and any other injecting classes.
 * ============================================================
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Spring generates: SELECT * FROM products WHERE category = ?
    List<Product> findByCategory(String category);
}
