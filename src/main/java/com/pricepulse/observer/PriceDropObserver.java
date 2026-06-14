package com.pricepulse.observer;

import com.pricepulse.entity.PriceAlert;
import com.pricepulse.entity.Product;

/**
 * ============================================================
 * DESIGN PATTERN: Observer Pattern (Observer / Subscriber interface)
 * ============================================================
 * WHAT IS IT?
 *   The Observer Pattern defines a one-to-many relationship between
 *   objects. When the SUBJECT (publisher) changes state, it
 *   automatically notifies all registered OBSERVERS (subscribers).
 *
 *   Think of it like a YouTube subscription:
 *     YouTube Channel = Subject   (PriceScheduler)
 *     Subscriber      = Observer  (EmailPriceDropObserver)
 *   When the channel uploads → all subscribers get notified.
 *
 * ROLES IN THIS PROJECT:
 *   Subject   → PriceScheduler
 *                 Detects price drops, loops through all observers,
 *                 calls observer.onPriceDrop() on each one.
 *
 *   Observer  → This interface (PriceDropObserver)
 *                 Defines the notification contract.
 *
 *   Concrete  → EmailPriceDropObserver
 *   Observer      Sends an email when onPriceDrop() is called.
 *               → SmsDropObserver (future — add without touching Scheduler)
 *
 * HOW IT'S APPLIED HERE:
 *   PriceScheduler holds:  List<PriceDropObserver> priceDropObservers;
 *   Spring auto-injects every class that implements this interface.
 *   When price drops:
 *     priceDropObservers.forEach(o -> o.onPriceDrop(alert, product, newPrice));
 *   All observers get called — email, SMS, push, logs — all at once.
 *
 * WHY IT MATTERS:
 *   WITHOUT Observer: PriceScheduler has hardcoded calls:
 *     emailService.send(...);
 *     smsService.send(...);    // had to modify scheduler to add this
 *     pushService.send(...);   // had to modify again
 *   Every new notification type requires editing PriceScheduler — risky!
 *
 *   WITH Observer: PriceScheduler never changes. Just add a new
 *   @Component class that implements PriceDropObserver. Done.
 *
 * SOLID CONNECTION:
 *   → OCP: PriceScheduler is CLOSED for modification, OPEN for extension.
 *   → DIP: PriceScheduler depends on THIS abstraction, not EmailService.
 * ============================================================
 */
public interface PriceDropObserver {

    /**
     * Called by PriceScheduler (the Subject) whenever a product's
     * current price drops to or below a user's target price.
     *
     * @param alert    the user's price alert that was triggered
     * @param product  the product whose price dropped
     * @param newPrice the new (lower) price that triggered this alert
     */
    void onPriceDrop(PriceAlert alert, Product product, Double newPrice);
}