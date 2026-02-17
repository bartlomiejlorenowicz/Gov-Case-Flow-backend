package com.authservice.controller;

import com.authservice.dto.UserDto;
import com.authservice.repository.UserRepository;
import com.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuthController {

    private final AuthService authService;

    @PostMapping("/{id}/promote/officer")
    public void promote(@PathVariable UUID id) {
        authService.promoteToOfficer(id);
    }

    @GetMapping("/users/count")
    public long countUsers() {
        return authService.countUsers();
    }

    @GetMapping("/users")
    public Page<UserDto> getUsers(Pageable pageable) {
        return authService.getUsers(pageable);
    }

}
