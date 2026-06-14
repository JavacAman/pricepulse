package com.pricepulse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * ============================================================
 * DESIGN PATTERN: Builder Pattern (on a DTO)
 * ============================================================
 * WHAT IS IT?
 *   DTOs (Data Transfer Objects) are often constructed in service
 *   methods and returned to controllers. Using the Builder pattern
 *   on DTOs makes construction explicit and readable.
 *
 * WHY @AllArgsConstructor IS KEPT:
 *   The existing code in AuthService uses:
 *     new AuthResponse(user.getId(), token, user.getEmail(), user.getName())
 *   @AllArgsConstructor keeps this working (backward compatible).
 *
 * ALTERNATIVE WITH Builder (equally valid, more readable):
 *   AuthResponse.builder()
 *       .userId(user.getId())
 *       .token(token)
 *       .email(user.getEmail())
 *       .name(user.getName())
 *       .build();
 *   ↑ Self-documenting. Adding a new field (e.g., role) doesn't require
 *     updating every call site — just add .role(user.getRole()) optionally.
 *
 * SOLID CONNECTION:
 *   → OCP: If you add a new optional field (e.g., expiresIn), existing
 *     build() calls still work — builder skips unset optional fields.
 *     With a constructor, every call site needs updating. Builder wins.
 * ============================================================
 */
@Data
@Builder
@AllArgsConstructor
public class AuthResponse {
    private Long userId;
    private String token;
    private String email;
    private String name;
}
