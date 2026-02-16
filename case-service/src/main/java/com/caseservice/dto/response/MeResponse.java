package com.caseservice.dto.response;

import java.util.List;
import java.util.UUID;

public record MeResponse(UUID userId, String username, List<String> roles) {
}
