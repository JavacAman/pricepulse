package com.pricepulse.repository;

import com.pricepulse.entity.PriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * ============================================================
 * SOLID PRINCIPLE: Dependency Inversion Principle (DIP)
 * DESIGN PATTERN: Singleton Pattern
 * ============================================================
 *
 * DIP:
 *   PriceAlertService and PriceScheduler depend on this interface,
 *   not on any concrete persistence implementation.
 *   Spring Data generates the actual SQL queries at startup via
 *   its proxy mechanism — no SQL written by us, no tight coupling.
 *
 * HOW SPRING DERIVES QUERIES FROM METHOD NAMES:
 *   findByUserId(Long userId)
 *     → SELECT * FROM price_alerts WHERE user_id = ?
 *
 *   findByProductIdAndIsTriggeredFalse(Long productId)
 *     → SELECT * FROM price_alerts WHERE product_id = ? AND is_triggered = false
 *
 *   Spring reads the method name, parses the field names (UserId,
 *   ProductId, IsTriggered), and generates the JPQL/SQL automatically.
 *   This is Spring Data's "query derivation" — zero SQL needed.
 *
 * PATTERN: Singleton
 *   Spring creates ONE PriceAlertRepository proxy bean shared across
 *   PriceAlertService and PriceScheduler.
 * ============================================================
 */
public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {

    // Finds all alerts belonging to a specific user (for "my alerts" page)
    List<PriceAlert> findByUserId(Long userId);

    // Finds untriggered alerts for a product — used by PriceScheduler to
    // check which users to notify when this product's price drops
    List<PriceAlert> findByProductIdAndIsTriggeredFalse(Long productId);
}
