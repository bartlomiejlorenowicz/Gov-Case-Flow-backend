package com.authservice.service;

import com.authservice.domain.RefreshToken;
import com.authservice.domain.Role;
import com.authservice.domain.User;
import com.authservice.dto.request.LoginRequest;
import com.authservice.dto.request.RegisterRequest;
import com.authservice.dto.response.AuthResponse;
import com.authservice.event.UserRegisteredEvent;
import com.authservice.exception.InvalidCredentialsException;
import com.authservice.exception.UserAlreadyExistsException;
import com.authservice.repository.UserRepository;
import com.authservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final RefreshTokenService refreshTokenService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void register(RegisterRequest request) {

        if (userRepository.existsByUsernameIgnoreCase(request.email().toLowerCase())) {
            throw new UserAlreadyExistsException("User with email already exists");
        }

        User user = User.builder()
                .username(request.email().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .roles(Set.of(Role.USER))
                .enabled(true)
                .createdAt(Instant.now(clock))
                .build();

        userRepository.save(user);

        eventPublisher.publishEvent(
                new UserRegisteredEvent(
                        user.getId(),
                        user.getUsername(),
                        user.getCreatedAt()
                ));
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByUsernameIgnoreCase(request.email().trim().toLowerCase())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String accessToken = jwtService.generateAccessToken(user);

        RefreshToken refreshToken = refreshTokenService.createForUser(user);

        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenValue) {

        RefreshToken refreshToken = refreshTokenService.validate(refreshTokenValue);

        User user = refreshToken.getUser();

        refreshToken.setRevoked(true);

        RefreshToken newRefreshToken = refreshTokenService.createForUser(user);

        String accessToken = jwtService.generateAccessToken(user);

        return new AuthResponse(accessToken, newRefreshToken.getToken());
    }
}