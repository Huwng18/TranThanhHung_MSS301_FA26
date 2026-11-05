package com.mss301.account.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// [TODO-3.2] Signed, expiring access and refresh tokens use distinct type claims.
@Service
public class JwtService {
    private final SecretKey key;
    private final long expiration;
    private final long refreshExpiration;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration}") long expiration,
                      @Value("${jwt.refresh-expiration}") long refreshExpiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String generateAccessToken(String subject, Integer role) {
        Date now = new Date();
        return Jwts.builder().subject(subject).claim("role", role).claim("type", "access")
            .issuedAt(now).expiration(new Date(now.getTime() + expiration)).signWith(key).compact();
    }

    public String generateRefreshToken(String subject) {
        Date now = new Date();
        return Jwts.builder().subject(subject).claim("type", "refresh")
            .issuedAt(now).expiration(new Date(now.getTime() + refreshExpiration)).signWith(key).compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }
}
