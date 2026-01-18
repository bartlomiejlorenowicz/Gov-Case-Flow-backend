package com.authservice.service;

import com.authservice.config.AuthAmqpConfig;
import com.authservice.dto.request.RegisterRequest;
import com.authservice.event.UserRegisteredEvent;
import com.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.awaitility.Awaitility.waitAtMost;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class AuthServiceRabbitIT {

    static final String TEST_QUEUE = "test.auth.user-registered.queue";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("auth_db")
                    .withUsername("test")
                    .withPassword("test");

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbitmq =
            new RabbitMQContainer("rabbitmq:4.0.0-alpine");

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestListener testListener;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        testListener.clear();
    }

    @Test
    void shouldPublishUserRegisteredEventAfterRegister() {
        // given
        RegisterRequest request = new RegisterRequest("test@test.com", "PassPass123!");

        // when
        authService.register(request);

        // then
        waitAtMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(testListener.events).hasSize(1);
            UserRegisteredEvent event = testListener.events.get(0);
            assertThat(event.email()).isEqualTo("test@test.com");
        });
    }

    @Test
    void shouldNotPublishEventWhenTransactionRollsBack() {
        // given
        RegisterRequest req1 = new RegisterRequest("wot@test.com", "PassPass123!");
        RegisterRequest req2 = new RegisterRequest("wot@test.com", "PassPass123!");

        authService.register(req1);

        // when
        try {
            authService.register(req2);
        } catch (Exception ignored) {}

        // then
        waitAtMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(testListener.events).hasSize(1);
        });
    }

    @TestConfiguration
    static class Config {

        @Bean
        Queue testQueue() {
            return new Queue(TEST_QUEUE, true);
        }

        @Bean
        Binding testBinding(Queue testQueue, TopicExchange authExchange) {
            return BindingBuilder
                    .bind(testQueue)
                    .to(authExchange)
                    .with(AuthAmqpConfig.ROUTING_KEY);
        }

        @Bean
        TestListener testListener() {
            return new TestListener();
        }
    }

    static class TestListener {

        private final List<UserRegisteredEvent> events = new ArrayList<>();

        @RabbitListener(queues = TEST_QUEUE)
        void listen(UserRegisteredEvent event) {
            this.events.add(event);
        }

        void clear() {
            this.events.clear();
        }
    }
}
