package ru.story1click.jwt.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.story1click.jwt.service.JwtService;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {

    @Value(value = "${jwt.secret}")
    public String SECRET;

    public String extractRole(String jwt) {
        Map<String, Object> claims = extractAllClaims(jwt);
        return claims.get("role").toString(); // просто строка
    }

    @Override
    public boolean validate(String jwt, String originalUri) {
        try {
            String role = extractRole(jwt);

            boolean hasRole;

            if (originalUri.startsWith("/api/v1/users") || originalUri.startsWith("/api/v1/orders")) {
                hasRole = role.equals("ROLE_USER") || role.equals("ROLE_ADMIN");
            } else {
                hasRole = role.equals("ROLE_ADMIN");
            }

            Claims claims = extractAllClaims(jwt);
            boolean notExpired = claims.getExpiration().after(new Date());

            return hasRole && notExpired;
        } catch (JwtException e) {
            return false;
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
