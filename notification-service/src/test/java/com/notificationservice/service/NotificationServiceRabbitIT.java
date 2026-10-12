package com.notificationservice.service;

import com.govcaseflow.events.cases.CaseStatusChangedEvent;
import com.notificationservice.config.AuthNotificationAmqpConfig;
import com.notificationservice.config.NotificationAmqpConfig;
import com.govcaseflow.events.auth.UserRegisteredEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.govcaseflow.events.cases.CaseStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.waitAtMost;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class NotificationServiceRabbitIT {

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbit =
            new RabbitMQContainer("rabbitmq:4.0.0-alpine");

    @Autowired
    private TestListener testListener;

    @BeforeEach
    void setup() {
        testListener.clear();
    }

    @Test
    void shouldConsumeCaseStatusChangedEvent() {
        // given
        CaseStatusChangedEvent event = new CaseStatusChangedEvent(
                UUID.randomUUID(),
                CaseStatus.SUBMITTED,
                CaseStatus.IN_REVIEW,
                Instant.now(),
                "SYSTEM"
        );

        // when
        testListener.publishCase(event);

        // then
        waitAtMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(testListener.caseEvents).hasSize(1);

            CaseStatusChangedEvent consumed = testListener.caseEvents.get(0);
            assertThat(consumed.caseId()).isEqualTo(event.caseId());
            assertThat(consumed.oldStatus()).isEqualTo(event.oldStatus());
            assertThat(consumed.newStatus()).isEqualTo(event.newStatus());
            assertThat(consumed.changedBy()).isEqualTo(event.changedBy());
            assertThat(consumed.changedAt()).isNotNull();
        });
    }

    @Test
    void shouldConsumeUserRegisteredEvent() {
        // given
        UserRegisteredEvent event = new UserRegisteredEvent(
                UUID.randomUUID(),
                "bartek@test.com",
                Instant.now()
        );

        // when
        testListener.publishUser(event);

        // then
        waitAtMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(testListener.userEvents).hasSize(1);

            UserRegisteredEvent consumed = testListener.userEvents.get(0);
            assertThat(consumed.userId()).isEqualTo(event.userId());
            assertThat(consumed.email()).isEqualTo(event.email());
            assertThat(consumed.registeredAt()).isNotNull();
        });
    }

    @TestConfiguration
    static class Config {

        @Bean
        TestListener testListener(RabbitTemplate rabbitTemplate) {
            return new TestListener(rabbitTemplate);
        }
    }

    static class TestListener {

        private final RabbitTemplate rabbitTemplate;

        private final List<CaseStatusChangedEvent> caseEvents = new ArrayList<>();
        private final List<UserRegisteredEvent> userEvents = new ArrayList<>();

        TestListener(RabbitTemplate rabbitTemplate) {
            this.rabbitTemplate = rabbitTemplate;
        }

        void publishCase(CaseStatusChangedEvent event) {
            rabbitTemplate.convertAndSend(
                    NotificationAmqpConfig.EXCHANGE,
                    NotificationAmqpConfig.ROUTING_KEY,
                    event
            );
        }

        void publishUser(UserRegisteredEvent event) {
            rabbitTemplate.convertAndSend(
                    AuthNotificationAmqpConfig.EXCHANGE,
                    AuthNotificationAmqpConfig.ROUTING_KEY,
                    event
            );
        }

        @RabbitListener(queues = NotificationAmqpConfig.QUEUE)
        void listenCase(CaseStatusChangedEvent event) {
            caseEvents.add(event);
        }

        @RabbitListener(queues = AuthNotificationAmqpConfig.QUEUE)
        void listenUser(UserRegisteredEvent event) {
            userEvents.add(event);
        }

        void clear() {
            caseEvents.clear();
            userEvents.clear();
        }
    }
}