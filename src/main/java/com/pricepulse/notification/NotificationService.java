package com.pricepulse.notification;

/**
 * ============================================================
 * SOLID PRINCIPLE: Interface Segregation Principle (ISP)
 * SOLID PRINCIPLE: Dependency Inversion Principle (DIP)
 * SOLID PRINCIPLE: Open/Closed Principle (OCP)
 * ============================================================
 * WHAT IS ISP?
 *   "Clients should not be forced to depend on methods they don't use."
 *   Instead of one big interface with 10 methods, create small focused
 *   interfaces. Each client uses only what it needs.
 *
 * HOW IT'S APPLIED HERE:
 *   This interface has ONLY what price-drop notification callers need.
 *   No sendWelcomeEmail(), no sendOtp() — just the price drop alert.
 *   EmailPriceDropObserver depends on THIS, not on a giant interface.
 *
 * WHAT IS DIP?
 *   "High-level modules should depend on abstractions, not concretions."
 *   HIGH-LEVEL  → PriceScheduler, EmailPriceDropObserver (business logic)
 *   ABSTRACTION → This interface (NotificationService)
 *   LOW-LEVEL   → EmailService (the actual email sending code)
 *
 *   Without DIP: EmailPriceDropObserver → EmailService (concrete class)
 *   With DIP:    EmailPriceDropObserver → NotificationService (interface)
 *                                                ↑ EmailService implements this
 *   Benefit: Swap EmailService for SmsService without touching observers.
 *
 * WHAT IS OCP?
 *   "Open for extension, Closed for modification."
 *   To add SMS alerts: create SmsNotificationService implements NotificationService.
 *   Existing code (factory, observers) needs ZERO modification.
 * ============================================================
 */
public interface NotificationService {

    /**
     * Sends a price drop alert to the user.
     * Implementations decide the channel: email, SMS, push notification, etc.
     */
    void sendPriceDropAlert(String recipient, String productName,
                            Double targetPrice, Double currentPrice);

    /**
     * Returns a unique type key for this implementation.
     * Used by NotificationFactory to look up the correct service.
     * Example: "EMAIL", "SMS", "PUSH"
     */
    String getType();
}