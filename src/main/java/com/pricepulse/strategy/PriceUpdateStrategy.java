package com.pricepulse.strategy;

/**
 * ============================================================
 * DESIGN PATTERN: Strategy Pattern
 * ============================================================
 * WHAT IS IT?
 *   The Strategy Pattern defines a family of algorithms,
 *   encapsulates each one inside a class, and makes them
 *   interchangeable. The client (PriceScheduler) uses a
 *   reference to this interface — it doesn't know or care
 *   which specific algorithm is running behind the scenes.
 *
 * HOW IT'S APPLIED HERE:
 *   This interface is the "Strategy". It declares ONE method:
 *   updatePrice(). Any class that wants to be a "price update
 *   algorithm" must implement this interface.
 *
 *   Current strategies:
 *     → RandomFluctuationStrategy  (simulates market movement)
 *   You can add more:
 *     → FixedDecreaseStrategy      (always drops price by 5%)
 *     → ExternalApiPriceStrategy   (fetches real price from web)
 *
 * WHY IT MATTERS:
 *   WITHOUT this pattern, PriceScheduler would have if-else blocks:
 *     if (type == "random") { ...logic... }
 *     else if (type == "fixed") { ...logic... }
 *   Every new algorithm forces you to modify PriceScheduler — risky!
 *
 *   WITH this pattern, PriceScheduler just calls:
 *     priceUpdateStrategy.updatePrice(currentPrice);
 *   You swap the algorithm by changing Spring config — zero code change.
 *
 * SOLID CONNECTION:
 *   → OCP (Open/Closed): New algorithms are OPEN for extension (add new class),
 *     PriceScheduler is CLOSED for modification (never changes).
 *   → DIP (Dependency Inversion): PriceScheduler depends on THIS abstraction,
 *     not on any concrete strategy class.
 *   → LSP (Liskov Substitution): Any strategy implementation must be safely
 *     substitutable here — PriceScheduler must work correctly with any of them.
 * ============================================================
 */
public interface PriceUpdateStrategy {

    // The single algorithm entry point. Implementations define HOW the price changes.
    Double updatePrice(Double currentPrice);
}
