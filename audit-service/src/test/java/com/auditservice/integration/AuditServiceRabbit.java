package com.auditservice.integration;

import com.auditservice.config.AuditAmqpConfig;
import com.auditservice.domain.CaseStatus;
import com.auditservice.event.CaseStatusChangedEvent;
import com.auditservice.repository.AuditRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class AuditServiceRabbitIT {

    @Container
    static RabbitMQContainer rabbit =
            new RabbitMQContainer("rabbitmq:3.13-management");

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("audit")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AuditRepository auditRepository;

    @Test
    void shouldConsumeEventAndPersistAuditEntry() {
        // given
        CaseStatusChangedEvent event =
                new CaseStatusChangedEvent(
                        UUID.randomUUID(),
                        CaseStatus.IN_REVIEW,
                        CaseStatus.APPROVED,
                        Instant.now(),
                        "TEST"
                );

        // when
        rabbitTemplate.convertAndSend(
                AuditAmqpConfig.EXCHANGE,
                AuditAmqpConfig.ROUTING_KEY,
                event
        );

        // then
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() ->
                        assertThat(auditRepository.findAll()).hasSize(1)
                );
    }
}
