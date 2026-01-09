package com.authservice.service;

import com.authservice.domain.RefreshToken;
import com.authservice.exception.InvalidTokenException;
import com.authservice.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken validate(String token) {

        RefreshToken rt = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (rt.isRevoked()) {
            throw new InvalidTokenException("Refresh token revoked");
        }

        if (rt.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("Refresh token expired");
        }

        return rt;
    }
}
