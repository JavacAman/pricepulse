package com.pricepulse.scheduler;

import com.pricepulse.entity.PriceAlert;
import com.pricepulse.entity.Product;
import com.pricepulse.observer.PriceDropObserver;
import com.pricepulse.repository.PriceAlertRepository;
import com.pricepulse.repository.ProductRepository;
import com.pricepulse.strategy.PriceUpdateStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * ============================================================
 * DESIGN PATTERN: Observer Pattern (Subject / Publisher)
 * DESIGN PATTERN: Strategy Pattern (Context)
 * SOLID PRINCIPLES: SRP + OCP + DIP
 * ============================================================
 *
 * THIS CLASS IS THE "SUBJECT" IN THE OBSERVER PATTERN:
 *   Observer Pattern has two roles:
 *     Subject   = knows when something happens, notifies all observers
 *     Observers = react to the event (send email, SMS, log, etc.)
 *
 *   PriceScheduler IS the Subject:
 *     - It detects price drops
 *     - It loops through ALL registered PriceDropObserver beans
 *     - It calls onPriceDrop() on each one
 *     - It does NOT care what each observer does
 *
 *   PriceScheduler does NOT know about EmailService, SMS, or push —
 *   it just says "hey, price dropped!" to whoever is listening.
 *
 *   To add SMS notification:
 *     1. Create: SmsPriceDropObserver implements PriceDropObserver { ... }
 *     2. Annotate with @Component
 *     3. Spring auto-adds it to the List<PriceDropObserver>
 *     4. PriceScheduler code = ZERO CHANGES
 *
 * THIS CLASS IS THE "CONTEXT" IN THE STRATEGY PATTERN:
 *   PriceScheduler holds a reference to PriceUpdateStrategy (interface).
 *   It calls priceUpdateStrategy.updatePrice(currentPrice).
 *   It does NOT know whether it's using RandomFluctuationStrategy
 *   or FixedDecreaseStrategy or ExternalApiPriceStrategy.
 *   Swap the strategy bean → PriceScheduler behavior changes with no code change.
 *
 * SOLID - SRP:
 *   PriceScheduler's ONE job: orchestrate the scheduled price check cycle.
 *   How prices change?   → Delegated to PriceUpdateStrategy.
 *   Who gets notified?  → Delegated to PriceDropObserver list.
 *   How to send email?  → Delegated to EmailPriceDropObserver → EmailService.
 *
 * SOLID - OCP:
 *   Open for extension (add observers/strategies), Closed for modification
 *   (PriceScheduler code never needs to change to support new behaviors).
 *
 * SOLID - DIP:
 *   Depends on PriceUpdateStrategy INTERFACE (not RandomFluctuationStrategy).
 *   Depends on PriceDropObserver INTERFACE (not EmailPriceDropObserver).
 *   High-level scheduling logic is decoupled from low-level implementations.
 * ============================================================
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PriceScheduler {

    private final PriceAlertRepository priceAlertRepository;
    private final ProductRepository productRepository;

    /**
     * Pattern-Strategy (Context):
     * Spring injects the active strategy implementation here.
     * Currently: RandomFluctuationStrategy.
     * To switch algorithms, change which @Component is active — no code change here.
     */
    private final PriceUpdateStrategy priceUpdateStrategy;

    /**
     * Pattern-Observer (Subject maintains observer list):
     * Spring auto-collects ALL @Component beans that implement PriceDropObserver
     * into this list. Currently contains: [EmailPriceDropObserver].
     * Add SmsPriceDropObserver @Component → Spring adds it here automatically.
     */
    private final List<PriceDropObserver> priceDropObservers;

    /**
     * ACID - ATOMICITY:
     *   Each product's price update AND alert state change are wrapped in
     *   ONE @Transactional transaction. If marking alert as triggered fails,
     *   the price update also rolls back. Consistent state is guaranteed.
     *
     * ACID - DURABILITY:
     *   Once the transaction commits successfully, both the new price
     *   and the triggered alert state are durably stored in MySQL.
     */
    @Scheduled(fixedRate = 100000)
    @Transactional
    public void checkPriceDrops() {
        log.info("Running price drop check...");

        List<Product> products = productRepository.findAll();

        for (Product product : products) {

            // Pattern-Strategy: Ask the strategy HOW to compute the new price.
            // PriceScheduler doesn't contain any pricing math — fully delegated.
            Double newPrice = priceUpdateStrategy.updatePrice(product.getCurrentPrice());
            product.setCurrentPrice(newPrice);
            productRepository.save(product);

            log.info("Product: {} | New Price: {}", product.getName(), newPrice);

            List<PriceAlert> alerts = priceAlertRepository
                    .findByProductIdAndIsTriggeredFalse(product.getId());

            for (PriceAlert alert : alerts) {
                if (newPrice <= alert.getTargetPrice()) {

                    // Pattern-Observer: Notify ALL registered observers.
                    // EmailPriceDropObserver → sends email
                    // (Future) SmsPriceDropObserver → sends SMS
                    // PriceScheduler doesn't know or care — it just notifies.
                    priceDropObservers.forEach(observer ->
                            observer.onPriceDrop(alert, product, newPrice));

                    // ACID-Atomicity: Mark as triggered in the SAME transaction.
                    // If this fails, the price update also rolls back.
                    alert.setIsTriggered(true);
                    priceAlertRepository.save(alert);
                }
            }
        }
        log.info("Price drop check completed.");
    }
}