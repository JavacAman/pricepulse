package com.pricepulse.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * ============================================================
 * DESIGN PATTERN: Builder Pattern
 * ============================================================
 * WHAT IS IT?
 *   The Builder Pattern separates object construction from its
 *   representation. It solves the "telescoping constructor" problem
 *   where constructors with many parameters become unreadable.
 *
 * WITHOUT Builder (bad):
 *   User u = new User(null, "Aman", "aman@gmail.com", "hashed123", Role.USER, LocalDateTime.now());
 *   ↑ Is the 4th argument the password or the role? Nobody can tell!
 *
 * WITH Builder (used in AuthService.register()):
 *   User u = User.builder()
 *               .name(request.getName())
 *               .email(request.getEmail())
 *               .password(passwordEncoder.encode(request.getPassword()))
 *               .build();  // role and createdAt use @Builder.Default values
 *   ↑ Crystal clear. Self-documenting. No room for argument order mistakes.
 *
 * @Builder.Default EXPLAINED:
 *   When you use .build() without setting 'role' or 'createdAt',
 *   Lombok's @Builder normally sets them to null (ignores initializers).
 *   @Builder.Default tells Lombok: "if not set by caller, use THIS value."
 *   So: role defaults to Role.USER, createdAt defaults to LocalDateTime.now().
 *
 * WHY @NoArgsConstructor + @AllArgsConstructor?
 *   JPA needs the no-arg constructor to hydrate objects from DB query results.
 *   Lombok's @Builder needs all-args constructor internally to build objects.
 *   Both annotations must be present alongside @Builder on @Entity classes.
 * ============================================================
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default  // If not specified in builder, role defaults to USER (not null)
    private Role role = Role.USER;

    @Column(name = "created_at")
    @Builder.Default  // If not specified in builder, createdAt defaults to now() (not null)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Role {
        USER, ADMIN
    }
}