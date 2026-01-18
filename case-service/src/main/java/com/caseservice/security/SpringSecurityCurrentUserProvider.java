package com.caseservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class SpringSecurityCurrentUserProvider implements CurrentUserProvider {

    @Override
    public CurrentUser getCurrentUser() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("Unauthenticated request");
        }

        if (!(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new IllegalStateException("Unsupported principal type: " + auth.getPrincipal().getClass());
        }

        UUID userId = principal.userId();
        String username = principal.username();

        Set<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .collect(Collectors.toUnmodifiableSet());

        return new CurrentUser(userId, username, roles);
    }
}