package com.pricepulse.notification;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ============================================================
 * DESIGN PATTERN: Factory Pattern
 * ============================================================
 * WHAT IS IT?
 *   The Factory Pattern provides a way to create objects without
 *   exposing the creation logic to the client. The client says
 *   "give me a notification service of type X" — it doesn't know
 *   how or which concrete class is returned.
 *
 * HOW IT'S APPLIED HERE:
 *   This factory holds a Map of all NotificationService implementations:
 *     "EMAIL" → EmailService
 *     "SMS"   → SmsService       (if you add one in future)
 *     "PUSH"  → PushService      (if you add one in future)
 *
 *   A caller does:
 *     notificationFactory.getService("EMAIL").sendPriceDropAlert(...)
 *   It never directly touches EmailService — the factory abstracts it.
 *
 * HOW SPRING MAKES THIS ELEGANT:
 *   Spring auto-collects ALL beans implementing NotificationService
 *   into the List<NotificationService> constructor argument.
 *   The factory maps them by their getType() key automatically.
 *   Adding SmsService? Just create the class with @Service —
 *   the factory picks it up with ZERO code change here.
 *
 * WHY IT MATTERS:
 *   WITHOUT Factory: Callers have scattered if-else blocks:
 *     if (type.equals("EMAIL")) { new EmailService().send(...) }
 *     else if (type.equals("SMS")) { new SmsService().send(...) }
 *   Every new channel requires finding and editing all those blocks.
 *
 *   WITH Factory: One place. One method. Completely extensible.
 *
 * SOLID CONNECTION:
 *   → OCP: New notification types added by creating a new bean,
 *     not by modifying this factory or its callers.
 *
 * PATTERN: Singleton
 *   @Component → Spring creates ONE NotificationFactory instance shared
 *   across the entire app (Singleton pattern via Spring IoC container).
 * ============================================================
 */
@Component
public class NotificationFactory {

    // Map: "EMAIL" → EmailService, "SMS" → SmsService, etc.
    private final Map<String, NotificationService> notificationServiceMap;

    /**
     * Spring injects every NotificationService bean into this list automatically.
     * We convert it to a Map keyed by getType() for O(1) lookup.
     */
    public NotificationFactory(List<NotificationService> services) {
        this.notificationServiceMap = services.stream()
                .collect(Collectors.toMap(NotificationService::getType, s -> s));
    }

    /**
     * Factory method — returns the correct NotificationService for the given type.
     * Caller never touches concrete classes directly.
     */
    public NotificationService getService(String type) {
        NotificationService service = notificationServiceMap.get(type);
        if (service == null) {
            throw new IllegalArgumentException("No notification service registered for type: " + type);
        }
        return service;
    }
}