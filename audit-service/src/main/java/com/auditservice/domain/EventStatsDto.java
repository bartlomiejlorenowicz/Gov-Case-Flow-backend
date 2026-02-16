package com.auditservice.domain;

public record EventStatsDto(
        String eventType,
        long count
) {}
