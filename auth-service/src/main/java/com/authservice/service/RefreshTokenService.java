package com.authservice.service;

import com.authservice.domain.RefreshToken;
import com.authservice.exception.InvalidTokenException;
import com.authservice.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final Clock clock;

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
}