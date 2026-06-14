package com.pricepulse.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * ============================================================
 * SOLID PRINCIPLE: Single Responsibility Principle (SRP)
 * DESIGN PATTERN: Singleton Pattern
 * ============================================================
 *
 * WHAT IS SRP HERE?
 *   JwtUtil has ONE responsibility: everything related to JWT tokens.
 *   Just three operations:
 *     1. generateToken(email) → create a signed JWT for a user
 *     2. extractEmail(token)  → read the email claim from a token
 *     3. validateToken(token) → check if the token is valid/not expired
 *
 *   It does NOT load users from DB (UserDetailsService does that).
 *   It does NOT check if a user is authorized (SecurityConfig does that).
 *   It does NOT send tokens over HTTP (AuthService does that).
 *
 *   One reason to change: if the JWT library changes or token structure changes.
 *
 * WHAT IS THE SINGLETON PATTERN?
 *   Singleton ensures ONLY ONE instance of a class exists in the application.
 *
 *   Classical Singleton (manual — don't do this in Spring):
 *     private static JwtUtil instance;
 *     public static JwtUtil getInstance() {
 *       if (instance == null) instance = new JwtUtil();
 *       return instance;
 *     }
 *
 *   Spring Singleton (how it's done here — much cleaner):
 *     @Component ← tells Spring: create ONE JwtUtil and share it everywhere.
 *     Spring's IoC container manages the lifecycle. No manual getInstance().
 *     Every class that injects JwtUtil gets THE SAME instance.
 *
 *   WHY SINGLETON FOR JwtUtil?
 *     - The signing key and expiration config are loaded ONCE at startup.
 *     - Creating a new JwtUtil on every request would re-read config each time.
 *     - One shared instance = efficient and consistent behavior.
 * ============================================================
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // SRP: This is the ONLY place in the app where tokens are generated
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // SRP: This is the ONLY place in the app where email is extracted from a token
    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // SRP: This is the ONLY place in the app where token validity is checked
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
