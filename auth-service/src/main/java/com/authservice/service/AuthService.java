package com.authservice.service;

import com.authservice.domain.Role;
import com.authservice.domain.User;
import com.authservice.dto.request.LoginRequest;
import com.authservice.dto.request.RegisterRequest;
import com.authservice.exception.InvalidCredentialsException;
import com.authservice.exception.UserAlreadyExistsException;
import com.authservice.repository.UserRepository;
import com.authservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final JwtService jwtService;

    @Transactional
    public void register(RegisterRequest request) {

        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new UserAlreadyExistsException("User with email already exists");
        }

        User user = User.builder()
                .username(request.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .roles(Set.of(Role.USER))
                .enabled(true)
                .createdAt(Instant.now(clock))
                .build();

        userRepository.save(user);
    }

    public String login(LoginRequest request) {

        User user = userRepository.findByUsernameIgnoreCase(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return jwtService.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRoles()
                        .stream()
                        .map(Enum::name)
                        .toList()
        );
    }
}