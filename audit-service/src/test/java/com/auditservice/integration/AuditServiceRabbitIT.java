package com.auditservice.integration;

import com.auditservice.config.AuditAmqpCaseStatusConfig;
import com.govcaseflow.events.cases.CaseStatus;
import com.govcaseflow.events.cases.CaseStatusChangedEvent;
import com.auditservice.repository.AuditRepository;
import com.govcaseflow.infrastructure.tracing.CorrelationId;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.data.domain.Pageable;
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
@ExtendWith(OutputCaptureExtension.class)
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

    @BeforeEach
    void cleanAuditLog() {
        auditRepository.deleteAll();
    }

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
                AuditAmqpCaseStatusConfig.EXCHANGE,
                AuditAmqpCaseStatusConfig.ROUTING_KEY,
                event
        );

        // then
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() ->
                        assertThat(auditRepository.findAll()).hasSize(1)
                );
    }

    /**
     * Consumer half of SCRUM-7: case-service puts the X-Correlation-Id of the HTTP request
     * into the AMQP "correlationId" property (covered by CaseServiceRabbitIT); here that
     * property must end up in audit-service log lines and in the stored audit entry.
     */
    @Test
    void shouldLogAndStoreCorrelationIdFromMessage(CapturedOutput output) {
        String correlationId = UUID.randomUUID().toString();
        CaseStatusChangedEvent event =
                new CaseStatusChangedEvent(
                        UUID.randomUUID(),
                        CaseStatus.SUBMITTED,
                        CaseStatus.IN_REVIEW,
                        Instant.now(),
                        "TEST"
                );

        rabbitTemplate.convertAndSend(
                AuditAmqpCaseStatusConfig.EXCHANGE,
                AuditAmqpCaseStatusConfig.ROUTING_KEY,
                event,
                message -> {
                    message.getMessageProperties().setCorrelationId(correlationId);
                    return message;
                }
        );

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() ->
                        assertThat(auditRepository.findAllByTraceId(correlationId, Pageable.unpaged()))
                                .hasSize(1)
                );

        assertThat(output.getOut().lines()
                .filter(line -> line.contains("Received CaseStatusChangedEvent") || line.contains("audit.save")))
                .hasSize(2)
                .allSatisfy(line -> assertThat(line).contains("[" + correlationId + "]"));
    }
}
