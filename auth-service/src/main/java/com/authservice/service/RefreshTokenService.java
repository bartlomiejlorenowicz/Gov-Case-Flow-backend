package com.authservice.service;

import com.authservice.domain.RefreshToken;
import com.authservice.domain.User;
import com.authservice.exception.InvalidTokenException;
import com.authservice.repository.RefreshTokenRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Getter
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final Clock clock;

    @Transactional
    public RefreshToken createForUser(User user) {
        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setToken(UUID.randomUUID().toString());
        rt.setCreatedAt(Instant.now(clock));
        rt.setExpiresAt(Instant.now(clock).plus(Duration.ofDays(30)));
        rt.setRevoked(false);
        return repository.save(rt);
    }

    public RefreshToken validate(String token) {

        RefreshToken rt = repository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (rt.isRevoked()) {
            throw new InvalidTokenException("Refresh token revoked");
        }

        if (rt.getExpiresAt().isBefore(Instant.now(clock))) {
            throw new InvalidTokenException("Refresh token expired");
        }

        return rt;
    }

    @Transactional
    public void revoke(String token) {

        RefreshToken refreshToken = repository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        refreshToken.setRevoked(true);
    }
}