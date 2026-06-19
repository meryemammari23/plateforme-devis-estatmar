package com.quoteflow.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Génération et validation des tokens JWT.
 * Le token porte : subject = email, claims = userId + role.
 */
@Service
public class JwtService {

    private final SecretKey cle;
    private final long dureeValiditeMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms:86400000}") long dureeValiditeMs) {
        this.cle = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.dureeValiditeMs = dureeValiditeMs; // 86400000 = 24h
    }

    /** Crée un token pour un utilisateur. */
    public String genererToken(String email, Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        Date maintenant = new Date();
        Date expiration = new Date(maintenant.getTime() + dureeValiditeMs);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(maintenant)
                .setExpiration(expiration)
                .signWith(cle, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Email contenu dans le token. */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        Object v = extractAllClaims(token).get("userId");
        return v == null ? null : Long.valueOf(v.toString());
    }

    public String extractRole(String token) {
        Object v = extractAllClaims(token).get("role");
        return v == null ? null : v.toString();
    }

    /** Vérifie signature + expiration. */
    public boolean isTokenValid(String token) {
        try {
            return !extractAllClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(cle)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
