package com.danielnavia.melodygenerator.service;

import com.danielnavia.melodygenerator.model.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class TokenService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${app.jwt.expires-in}")
    private long expiresIn;

    public String generateToken(Integer userId, String email, List<Role> roles) {

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        Date expiresAt = new Date(System.currentTimeMillis() +  expiresIn * 1000);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .expiration(expiresAt)
                .signWith(key)
                .claim("roles", roles)
                .compact();
    }
}
