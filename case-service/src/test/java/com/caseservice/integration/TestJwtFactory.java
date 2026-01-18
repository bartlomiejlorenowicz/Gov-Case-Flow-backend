package com.caseservice.integration;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class TestJwtFactory {

    private final String jwtSecret;
    private final long expirationMs;

    public TestJwtFactory(
            @Value("${security.jwt.secret}") String jwtSecret,
            @Value("${security.jwt.expiration-ms}") long expirationMs
    ) {
        this.jwtSecret = jwtSecret;
        this.expirationMs = expirationMs;
    }

    public String token(UUID userId, String username, List<String> roles) {

        var key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId.toString())
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plusMillis(expirationMs)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
