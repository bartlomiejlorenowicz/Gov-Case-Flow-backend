package com.caseservice.integration;

import com.caseservice.domain.CaseStatus;
import com.caseservice.domain.CaseEntity;
import com.caseservice.repository.CaseRepository;
import com.caseservice.repository.CaseStatusHistoryRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.http.MediaType;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CaseServiceRabbitIT {

    private static final String EXCHANGE = "case.events.exchange";
    private static final String ROUTING_KEY = "case.status.changed";

    private static final String AUDIT_QUEUE = "audit.case-status.queue";

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13-management");

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("case")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.liquibase.enabled", () -> true);
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    CaseRepository caseRepository;

    @Autowired
    CaseStatusHistoryRepository historyRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RabbitAdmin rabbitAdmin;

    @BeforeEach
    void setup() {
        historyRepository.deleteAll();
        caseRepository.deleteAll();

        var queue = new org.springframework.amqp.core.Queue(AUDIT_QUEUE, true);
        var exchange = new org.springframework.amqp.core.TopicExchange(EXCHANGE);

        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareExchange(exchange);

        rabbitAdmin.declareBinding(
                org.springframework.amqp.core.BindingBuilder
                        .bind(queue)
                        .to(exchange)
                        .with(ROUTING_KEY)
        );
    }

    @WithMockUser(username = "test@test.com", roles = "USER")
    @Test
    void shouldChangeStatusAndPublishEventToRabbit() throws Exception {
        // given
        CaseEntity entity = caseRepository.save(
                CaseEntity.builder()
                        .caseNumber("CASE-2026-IT-001")
                        .applicantPesel("90010112345")
                        .status(CaseStatus.SUBMITTED)
                        .build()
        );

        UUID caseId = entity.getId();

        String json = """
                { "newStatus": "IN_REVIEW" }
                """;

        // when
        mockMvc.perform(
                        patch("/api/cases/{caseId}/status", caseId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf())
                                .content(json)
                )
                .andExpect(status().isNoContent());

        // then
        CaseEntity updated = caseRepository.findById(caseId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(CaseStatus.IN_REVIEW);

        // then
        assertThat(historyRepository.findAll()).hasSize(1);

        // then 3
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Message message = rabbitTemplate.receive(AUDIT_QUEUE);
                    assertThat(message).isNotNull();
                });
    }
}