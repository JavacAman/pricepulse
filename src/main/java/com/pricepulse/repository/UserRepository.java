package com.pricepulse.repository;

import com.pricepulse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * ============================================================
 * SOLID PRINCIPLE: Dependency Inversion Principle (DIP)
 * DESIGN PATTERN: Singleton Pattern
 * ============================================================
 *
 * WHAT IS DIP HERE?
 *   DIP says: "High-level modules should depend on abstractions,
 *   not on concrete implementations."
 *
 *   UserRepository IS the abstraction (it's an interface).
 *   The concrete implementation (Hibernate's JPA proxy) is generated
 *   by Spring Data JPA at runtime — you never write it.
 *
 *   AuthService depends on UserRepository (interface), not on any
 *   specific DB driver or Hibernate class. You can swap MySQL for
 *   PostgreSQL without changing AuthService at all — only the
 *   DB config changes. That's DIP in action.
 *
 *   Dependency chain:
 *     AuthService → UserRepository (interface)  ← DIP applied here
 *                        ↑
 *                 Spring Data generates the Hibernate proxy at runtime
 *
 * WHAT IS JpaRepository?
 *   JpaRepository<User, Long> gives you FREE CRUD methods:
 *     save(), findById(), findAll(), deleteById(), existsById(), count()...
 *   You don't write any SQL for these — Spring generates them.
 *   The custom methods below use Spring's query derivation —
 *   Spring reads the method name and generates the SQL automatically.
 *
 * PATTERN: Singleton
 *   Spring creates ONE proxy bean implementing UserRepository.
 *   Every class that injects UserRepository gets THE SAME bean.
 * ============================================================
 */
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring generates: SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // Spring generates: SELECT COUNT(*) > 0 FROM users WHERE email = ?
    Boolean existsByEmail(String email);
}
