package com.pricepulse.controller;

import com.pricepulse.entity.PriceAlert;
import com.pricepulse.service.PriceAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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