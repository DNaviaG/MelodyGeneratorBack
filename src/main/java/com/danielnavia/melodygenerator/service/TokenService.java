package com.danielnavia.melodygenerator.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class TokenService {
    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(Integer userId, String email) {

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        Date expiresAt = new Date(System.currentTimeMillis() + 3_600_000L);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .expiration(expiresAt)
                .signWith(key)
                .compact();
    }
}
