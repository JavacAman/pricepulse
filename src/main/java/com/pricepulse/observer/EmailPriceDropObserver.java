package com.pricepulse.observer;

import com.pricepulse.entity.PriceAlert;
import com.pricepulse.entity.Product;
import com.pricepulse.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ============================================================
 * DESIGN PATTERN: Observer Pattern (Concrete Observer)
 * ============================================================
 * WHAT IS IT?
 *   This is a CONCRETE OBSERVER — it implements the PriceDropObserver
 *   interface and defines WHAT HAPPENS when a price drop is detected.
 *   In this case: send an email alert to the user.
 *
 * HOW IT FITS INTO THE OBSERVER PATTERN:
 *
 *   [ PriceScheduler ]  ←— Subject/Publisher
 *         |
 *         | loops through List<PriceDropObserver>
 *         ↓
 *   [ PriceDropObserver ]  ←— Observer Interface (contract)
 *         ↑
 *   [ EmailPriceDropObserver ]  ←— YOU ARE HERE (Concrete Observer)
 *         |
 *         | calls
 *         ↓
 *   [ NotificationService ]  ←— sends the actual email
 *
 * WHY A SEPARATE CLASS INSTEAD OF EMAILING DIRECTLY FROM SCHEDULER?
 *   If PriceScheduler called EmailService directly:
 *     - Adding SMS = modify PriceScheduler (violation of OCP)
 *     - PriceScheduler becomes fat with notification concerns (violation of SRP)
 *   With this class:
 *     - PriceScheduler only knows "notify observers" (SRP maintained)
 *     - New notifications = new @Component class (OCP maintained)
 *
 * SOLID PRINCIPLES APPLIED:
 *   → SRP (Single Responsibility):
 *     This class has ONE job: react to a price drop by sending an email.
 *     It does not schedule, does not check prices, does not save to DB.
 *
 *   → DIP (Dependency Inversion):
 *     Depends on NotificationService (interface), NOT EmailService (class).
 *     If you replace EmailService with BetterEmailService tomorrow,
 *     this class doesn't need to change at all.
 *
 * PATTERN: Singleton
 *   @Component → Spring creates ONE instance and registers it as a
 *   PriceDropObserver bean. PriceScheduler's List<PriceDropObserver>
 *   automatically includes this bean via Spring dependency injection.
 * ============================================================
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailPriceDropObserver implements PriceDropObserver {

    // DIP: We depend on the NotificationService INTERFACE, not EmailService CLASS.
    // Tomorrow if you switch to a BetterEmailService, just swap the @Service bean —
    // this observer class stays untouched.
    private final NotificationService notificationService;

    @Override
    public void onPriceDrop(PriceAlert alert, Product product, Double newPrice) {
        log.info("Observer triggered → sending email to: {}", alert.getUser().getEmail());

        // Delegate actual sending to NotificationService (which is EmailService at runtime)
        notificationService.sendPriceDropAlert(
                alert.getUser().getEmail(),
                product.getName(),
                alert.getTargetPrice(),
                newPrice
        );
    }
}