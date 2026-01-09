package com.authservice.dto.request;

public record LogoutRequest(
        String refreshToken
) {}