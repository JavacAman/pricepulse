package com.pricepulse.service;

import com.pricepulse.entity.Product;
import com.pricepulse.repository.ProductRepository;
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

/**
 * Unit tests for ProductService.
 *
 * IMPORTANT NOTE ON CACHING:
 *   @Cacheable, @CachePut, @CacheEvict are Spring AOP annotations.
 *   They only activate when the bean is managed by Spring's proxy container.
 *   With @InjectMocks, ProductService is created directly (no AOP proxy),
 *   so cache logic is NOT active here — that belongs in integration tests.
 *   These tests verify the BUSINESS LOGIC only (DB calls, error handling).
 */
@ExtendWith(MockitoExtension.class) // JUnit5: enables Mockito annotations
class ProductServiceTest {

    // Mockito: mocking ProductRepository — no real DB interaction in these tests
    @Mock private ProductRepository productRepository;

    // Mockito: @InjectMocks creates ProductService and injects the mocked repository
    @InjectMocks private ProductService productService;

    private Product mockProduct;

    @BeforeEach
    void setUp() {
        mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setName("iPhone 15");
        mockProduct.setCategory("Electronics");
        mockProduct.setCurrentPrice(999.99);
        mockProduct.setImageUrl("https://example.com/iphone.jpg");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // addProduct() tests
    // ─────────────────────────────────────────────────────────────────────────

    // JUnit5: testing addProduct() - happy path
    // Mockito: mocking ProductRepository.save
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("addProduct() - should save product and return the saved entity")
    void addProduct_validProduct_returnsSavedProduct() {
        // ── Arrange ──────────────────────────────────────────────────────────
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

        // ── Act ──────────────────────────────────────────────────────────────
        Product result = productService.addProduct(mockProduct);

        // ── Assert ───────────────────────────────────────────────────────────
        assertNotNull(result);
        assertEquals("iPhone 15", result.getName());
        assertEquals(999.99, result.getCurrentPrice());
        verify(productRepository).save(mockProduct);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAllProducts() tests
    // ─────────────────────────────────────────────────────────────────────────

    // JUnit5: testing getAllProducts() - products exist in repository
    // Mockito: mocking ProductRepository.findAll to return list
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("getAllProducts() - should return all products from the repository")
    void getAllProducts_productsExist_returnsList() {
        // ── Arrange ──────────────────────────────────────────────────────────
        Product secondProduct = new Product();
        secondProduct.setId(2L);
        secondProduct.setName("Samsung S24");
        secondProduct.setCurrentPrice(799.99);

        when(productRepository.findAll()).thenReturn(Arrays.asList(mockProduct, secondProduct));

        // ── Act ──────────────────────────────────────────────────────────────
        List<Product> result = productService.getAllProducts();

        // ── Assert ───────────────────────────────────────────────────────────
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository).findAll();
    }

    // JUnit5: testing getAllProducts() - empty repository
    // Mockito: mocking findAll to return empty list
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("getAllProducts() - should return empty list when no products exist")
    void getAllProducts_noProducts_returnsEmptyList() {
        // ── Arrange ──────────────────────────────────────────────────────────
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        // ── Act ──────────────────────────────────────────────────────────────
        List<Product> result = productService.getAllProducts();

        // ── Assert ───────────────────────────────────────────────────────────
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getProductById() tests
    // ─────────────────────────────────────────────────────────────────────────

    // JUnit5: testing getProductById() - product exists
    // Mockito: mocking ProductRepository.findById to return the product
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("getProductById() - should return the product when ID exists")
    void getProductById_productExists_returnsProduct() {
        // ── Arrange ──────────────────────────────────────────────────────────
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));

        // ── Act ──────────────────────────────────────────────────────────────
        Product result = productService.getProductById(1L);

        // ── Assert ───────────────────────────────────────────────────────────
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("iPhone 15", result.getName());
        verify(productRepository).findById(1L);
    }

    // JUnit5: testing getProductById() - product does not exist
    // Mockito: mocking findById to return empty Optional (simulates 404 scenario)
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("getProductById() - should throw RuntimeException when product ID does not exist")
    void getProductById_notFound_throwsRuntimeException() {
        // ── Arrange ──────────────────────────────────────────────────────────
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // ── Act & Assert ──────────────────────────────────────────────────────
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productService.getProductById(99L));

        assertEquals("Product not found", ex.getMessage());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getProductsByCategory() tests
    // ─────────────────────────────────────────────────────────────────────────

    // JUnit5: testing getProductsByCategory() - matching products found
    // Mockito: mocking ProductRepository.findByCategory
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("getProductsByCategory() - should return products matching the given category")
    void getProductsByCategory_matchingProducts_returnsList() {
        // ── Arrange ──────────────────────────────────────────────────────────
        when(productRepository.findByCategory("Electronics"))
                .thenReturn(List.of(mockProduct));

        // ── Act ──────────────────────────────────────────────────────────────
        List<Product> result = productService.getProductsByCategory("Electronics");

        // ── Assert ───────────────────────────────────────────────────────────
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).getCategory());
        verify(productRepository).findByCategory("Electronics");
    }

    // JUnit5: testing getProductsByCategory() - no products in category
    // Mockito: mocking findByCategory to return empty list
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("getProductsByCategory() - should return empty list when no products in category")
    void getProductsByCategory_noMatch_returnsEmptyList() {
        // ── Arrange ──────────────────────────────────────────────────────────
        when(productRepository.findByCategory("Furniture")).thenReturn(Collections.emptyList());

        // ── Act ──────────────────────────────────────────────────────────────
        List<Product> result = productService.getProductsByCategory("Furniture");

        // ── Assert ───────────────────────────────────────────────────────────
        assertTrue(result.isEmpty());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updatePrice() tests
    // ─────────────────────────────────────────────────────────────────────────

    // JUnit5: testing updatePrice() - price updated and saved
    // Mockito: mocking findById and save; verifying new price is persisted
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("updatePrice() - should update currentPrice and save the product")
    void updatePrice_validId_updatesAndSavesProduct() {
        // ── Arrange ──────────────────────────────────────────────────────────
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ── Act ──────────────────────────────────────────────────────────────
        Product result = productService.updatePrice(1L, 799.99);

        // ── Assert ───────────────────────────────────────────────────────────
        assertNotNull(result);
        assertEquals(799.99, result.getCurrentPrice());
        verify(productRepository).save(mockProduct);
    }

    // JUnit5: testing updatePrice() - product not found edge case
    // Mockito: findById returns empty; save must never be called
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("updatePrice() - should throw RuntimeException when product does not exist")
    void updatePrice_productNotFound_throwsException() {
        // ── Arrange ──────────────────────────────────────────────────────────
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // ── Act & Assert ──────────────────────────────────────────────────────
        assertThrows(RuntimeException.class,
                () -> productService.updatePrice(99L, 500.0));

        verify(productRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteProduct() tests
    // ─────────────────────────────────────────────────────────────────────────

    // JUnit5: testing deleteProduct() - happy path
    // Mockito: mocking ProductRepository.deleteById
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("deleteProduct() - should call deleteById on repository without throwing")
    void deleteProduct_validId_deletesSuccessfully() {
        // ── Arrange ──────────────────────────────────────────────────────────
        doNothing().when(productRepository).deleteById(1L);

        // ── Act ──────────────────────────────────────────────────────────────
        assertDoesNotThrow(() -> productService.deleteProduct(1L));

        // ── Assert ───────────────────────────────────────────────────────────
        verify(productRepository).deleteById(1L);
    }
}
