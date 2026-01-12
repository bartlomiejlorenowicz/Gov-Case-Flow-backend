package com.caseservice.controller;

import com.caseservice.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal UserPrincipal principal,
                         Authentication authentication) {

        UUID userId = principal.userId();
        String username = principal.username();

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new MeResponse(userId, username, roles);
    }

    public record MeResponse(UUID userId, String username, List<String> roles) {}
}
