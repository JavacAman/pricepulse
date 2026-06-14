package com.pricepulse.controller;

import com.pricepulse.entity.PriceAlert;
import com.pricepulse.service.PriceAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * ============================================================
 * SOLID PRINCIPLE: Single Responsibility Principle (SRP)
 * SOLID PRINCIPLE: Dependency Inversion Principle (DIP)
 * ============================================================
 *
 * SRP:
 *   PriceAlertController's ONLY job: handle HTTP routing for price alerts.
 *   Creating the alert, validating user/product, sending notifications —
 *   none of that happens here. It all lives in PriceAlertService and
 *   downstream components (separate, focused responsibilities).
 *
 * DIP:
 *   Depends on PriceAlertService (service layer), not on repositories
 *   or EmailService directly. The controller sits at the top of the
 *   dependency chain, relying on abstractions below it.
 * ============================================================
 */
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class PriceAlertController {

    private final PriceAlertService priceAlertService;

    @PostMapping
    public ResponseEntity<PriceAlert> createAlert(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam Double targetPrice) {
        return ResponseEntity.ok(
                priceAlertService.createAlert(userId, productId, targetPrice));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PriceAlert>> getUserAlerts(
            @PathVariable Long userId) {
        return ResponseEntity.ok(
                priceAlertService.getUserAlerts(userId));
    }

    @DeleteMapping("/{alertId}")
    public ResponseEntity<Void> deleteAlert(
            @PathVariable Long alertId) {
        priceAlertService.deleteAlert(alertId);
        return ResponseEntity.noContent().build();
    }
}
