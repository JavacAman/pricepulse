package com.pricepulse.service;

import com.pricepulse.entity.Product;
import java.util.List;

/**
 * ============================================================
 * SOLID PRINCIPLE: Interface Segregation Principle (ISP)
 * ============================================================
 * WHAT IS ISP?
 *   "A client should never be forced to implement or depend on
 *   methods it does not use."
 *
 *   In simpler words: don't create one giant interface with 10 methods
 *   when different clients only need 2-3 of those methods each.
 *   Split the interface into smaller, focused ones.
 *
 * WITHOUT ISP (bad design):
 *   interface ProductService {
 *     Product addProduct(...);        // write
 *     Product updatePrice(...);       // write
 *     void deleteProduct(...);        // write
 *     List<Product> getAll();         // read
 *     Product getById(...);           // read
 *     List<Product> getByCategory();  // read
 *   }
 *   A service that only needs to READ products is still forced to depend
 *   on addProduct(), updatePrice(), deleteProduct() — methods it never uses.
 *
 * WITH ISP (this design):
 *   ProductQueryService  → only READ methods (this interface)
 *   ProductCommandService → only WRITE methods (separate interface)
 *
 *   PriceAlertService needs product reads? → depends on ProductQueryService only.
 *   ProductController POST endpoint? → depends on ProductCommandService only.
 *
 * SOLID CONNECTION:
 *   → DIP (Dependency Inversion): Callers depend on this abstraction,
 *     not on the concrete ProductService class.
 *   → OCP (Open/Closed): New query methods extend this interface
 *     without affecting the write-side interface or its callers.
 * ============================================================
 */
public interface ProductQueryService {

    Product getProductById(Long id);

    List<Product> getAllProducts();

    List<Product> getProductsByCategory(String category);
}