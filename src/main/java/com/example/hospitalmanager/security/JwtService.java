package com.example.hospitalmanager.security;

import com.example.hospitalmanager.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtService(JwtProperties jwtProperties) {
        String encodedSecret = jwtProperties.secret();
        long expirationMillis = jwtProperties.expirationMs();
        if (encodedSecret == null || encodedSecret.isBlank()) {
            throw new IllegalStateException("Set JWT_SECRET to a Base64-encoded key of at least 32 bytes.");
        }
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(encodedSecret);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("JWT_SECRET must be valid Base64.", exception);
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must decode to at least 32 bytes for HS256.");
        }
        if (expirationMillis <= 0) {
            throw new IllegalStateException("JWT expiration must be a positive number of milliseconds.");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(UserDetails userDetails) {
        Instant issuedAt = Instant.now();
        String role = null;
        for (GrantedAuthority authority : userDetails.getAuthorities()) {
            if (authority != null) {
                role = authority.getAuthority();
                break;
            }
        }
        if (role == null) {
            throw new IllegalArgumentException("Authenticated user has no authority.");
        }
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("role", role)
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(issuedAt.plusMillis(expirationMillis)))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getBody().getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        Claims claims = parseClaims(token).getBody();
        return userDetails.getUsername().equals(claims.getSubject())
                && claims.getExpiration() != null
                && claims.getExpiration().after(new Date());
    }

    public long getExpirationSeconds() {
        return expirationMillis / 1000;
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token);
    }
}
