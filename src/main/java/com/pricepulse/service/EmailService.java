package com.pricepulse.service;

import com.pricepulse.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * ============================================================
 * SOLID PRINCIPLE: Single Responsibility Principle (SRP)
 * SOLID PRINCIPLE: Dependency Inversion Principle (DIP)
 * DESIGN PATTERN: Singleton Pattern
 * ============================================================
 *
 * WHAT IS SRP?
 *   "A class should have only ONE reason to change."
 *   This class does ONE thing: send email notifications.
 *   It does NOT handle business logic, scheduling, or DB access.
 *   If the email template changes → only this class changes.
 *   If notification logic changes → a different class changes.
 *   One responsibility = one reason to change.
 *
 * WHAT IS DIP? (HOW THIS CLASS ENABLES IT)
 *   EmailService implements NotificationService (an interface).
 *   This means callers (like EmailPriceDropObserver) can depend on
 *   the NotificationService interface instead of this class directly.
 *
 *   WITHOUT DIP (tight coupling):
 *     EmailPriceDropObserver → EmailService (concrete class)
 *     Changing EmailService forces changes in EmailPriceDropObserver.
 *
 *   WITH DIP (loose coupling):
 *     EmailPriceDropObserver → NotificationService (interface)
 *                                      ↑ EmailService implements this
 *     You can replace EmailService with BetterEmailService tomorrow —
 *     EmailPriceDropObserver and NotificationFactory don't change at all.
 *
 * WHAT IS getType() FOR?
 *   Used by NotificationFactory (Factory Pattern) to map this bean
 *   to its type key "EMAIL". Factory calls getService("EMAIL") and
 *   gets back this bean. See NotificationFactory for full explanation.
 *
 * PATTERN: Singleton
 *   @Service tells Spring: create ONE EmailService instance and share
 *   it across the entire application. This is the Singleton Pattern
 *   implemented via Spring's IoC container.
 *   No need to write a manual getInstance() like classic Singleton —
 *   Spring handles it for you with @Service / @Component.
 * ============================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService implements NotificationService {

    private final JavaMailSender mailSender;

    // Unique type key → used by NotificationFactory to look up this bean
    @Override
    public String getType() {
        return "EMAIL";
    }

    @Override
    public void sendPriceDropAlert(String toEmail,
                                   String productName,
                                   Double targetPrice,
                                   Double currentPrice) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("PricePulse Alert: "
                    + productName + " price dropped!");
            message.setText(
                    "Hello!\n\n" +
                            "Great news! The price of " + productName +
                            " has dropped below your target price.\n\n" +
                            "Your Target Price: ₹" + targetPrice + "\n" +
                            "Current Price: ₹" + currentPrice + "\n\n" +
                            "Login to PricePulse to grab this deal!\n\n" +
                            "Happy Shopping!\n" +
                            "PricePulse Team"
            );
            mailSender.send(message);
            log.info("Email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}",
                    toEmail, e.getMessage());
        }
    }
}