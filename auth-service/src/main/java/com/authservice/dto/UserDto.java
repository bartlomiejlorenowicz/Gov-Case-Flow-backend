package com.authservice.dto;

import com.authservice.domain.Role;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserDto(
        UUID id,
        String username,
        Set<Role> roles,
        boolean enabled,
        Instant createdAt
) {}
