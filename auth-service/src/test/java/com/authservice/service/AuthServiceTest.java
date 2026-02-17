package com.authservice.service;

import com.authservice.domain.Role;
import com.authservice.domain.User;
import com.authservice.dto.request.LoginRequest;
import com.authservice.dto.request.RegisterRequest;
import com.authservice.event.UserPromotedEvent;
import com.authservice.event.UserRegisteredEvent;
import com.authservice.exception.InvalidCredentialsException;
import com.authservice.exception.UserAlreadyExistsException;
import com.authservice.mapper.UserMapper;
import com.authservice.repository.UserRepository;
import com.authservice.security.JwtService;
import com.authservice.service.utils.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private AuthService authService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserMapper userMapper;


    private final Clock clock =
            Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setup() {
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                clock,
                jwtService,
                refreshTokenService,
                eventPublisher,
                currentUserProvider,
                userMapper
        );
    }

    @Test
    void shouldRegisterUserAndPublishEvent() {
        RegisterRequest request = new RegisterRequest("test@test.com", "Pass123!");

        when(userRepository.existsByUsernameIgnoreCase(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");

        authService.register(request);

        verify(userRepository).save(any(User.class));
        verify(eventPublisher).publishEvent(any(UserRegisteredEvent.class));
    }

    @Test
    void shouldThrowWhenUserAlreadyExists() {
        RegisterRequest request = new RegisterRequest("test@test.com", "Pass123!");

        when(userRepository.existsByUsernameIgnoreCase(any())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> authService.register(request));

        verify(userRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldThrowWhenPasswordInvalid() {
        LoginRequest request = new LoginRequest("test@test.com", "wrong");

        User user = User.builder()
                .username("test@test.com")
                .passwordHash("hashed")
                .build();

        when(userRepository.findByUsernameIgnoreCase(any()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(request));
    }


    @Test
    public void shouldPromoteUserToOfficerAndPublishEvent() {
        UUID adminId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        User user = User.builder()
                .id(targetId)
                .roles(new HashSet<>(Set.of(Role.USER)))
                .build();

        when(currentUserProvider.getUserId()).thenReturn(adminId);
        when(userRepository.findById(targetId)).thenReturn(Optional.of(user));

        authService.promoteToOfficer(targetId);

        assertThat(user.getRoles()).contains(Role.OFFICER);

        verify(eventPublisher).publishEvent(any(UserPromotedEvent.class));
    }
}