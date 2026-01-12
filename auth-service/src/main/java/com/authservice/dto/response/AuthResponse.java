package com.authservice.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken) {
}
