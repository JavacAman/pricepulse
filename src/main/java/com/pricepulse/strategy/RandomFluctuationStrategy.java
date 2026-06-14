package com.pricepulse.strategy;

import org.springframework.stereotype.Component;

/**
 * ============================================================
 * DESIGN PATTERN: Strategy Pattern (Concrete Strategy)
 * ============================================================
 * WHAT IS IT?
 *   A Concrete Strategy is a specific implementation of the
 *   Strategy interface. It contains the actual algorithm.
 *   The client (PriceScheduler) doesn't know this class exists —
 *   it only knows the PriceUpdateStrategy interface.
 *
 * HOW IT'S APPLIED HERE:
 *   This class IS the algorithm: it randomly fluctuates the price
 *   between -20% and +20% to simulate real market movement.
 *
 *   PriceScheduler gets this injected by Spring automatically:
 *     private final PriceUpdateStrategy priceUpdateStrategy;
 *            ↑ interface type             ↑ Spring injects THIS bean
 *
 * WHY IT MATTERS:
 *   If you want to switch to a "fixed 5% decrease" strategy:
 *   1. Create: FixedDecreaseStrategy implements PriceUpdateStrategy
 *   2. Mark the old one @Primary or use @Qualifier
 *   PriceScheduler code stays UNCHANGED — that's the power of Strategy.
 *
 * SOLID CONNECTION:
 *   → LSP (Liskov Substitution Principle):
 *     This class can replace PriceUpdateStrategy anywhere without
 *     breaking the caller. It accepts a Double, returns a valid
 *     Double — the contract is fully respected.
 *
 * PATTERN: Singleton
 *   @Component tells Spring to create ONE instance of this class
 *   and share it everywhere. This is the Singleton pattern —
 *   we don't create objects with 'new', Spring manages the lifecycle.
 * ============================================================
 */
@Component
public class RandomFluctuationStrategy implements PriceUpdateStrategy {

    @Override
    public Double updatePrice(Double currentPrice) {
        // Random fluctuation between -20% (-0.2) and +20% (+0.2)
        double fluctuation = (Math.random() * 0.4) - 0.2;
        double newPrice = currentPrice * (1 + fluctuation);
        // Round to 2 decimal places for clean currency values
        return Math.round(newPrice * 100.0) / 100.0;
    }
}