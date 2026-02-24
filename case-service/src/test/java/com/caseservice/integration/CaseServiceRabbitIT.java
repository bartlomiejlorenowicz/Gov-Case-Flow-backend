package com.caseservice.integration;

import com.caseservice.configuration.CaseAmqpConfig;
import com.caseservice.domain.CaseStatus;
import com.caseservice.domain.CaseEntity;
import com.caseservice.repository.CaseRepository;
import com.caseservice.repository.CaseStatusHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.govcaseflow.events.cases.CaseStatusChangedEvent;
import io.jsonwebtoken.Jwts;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.http.MediaType;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.rabbitmq.listener.simple.auto-startup=false"
})
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CaseServiceRabbitIT {

    private static final String EXCHANGE = CaseAmqpConfig.EXCHANGE;
    private static final String ROUTING_KEY = CaseAmqpConfig.STATUS_CHANGED_KEY;
    private static final String TEST_QUEUE = "test.case.events.queue";

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
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    TestJwtFactory jwtFactory;

    @BeforeEach
    void setup() {
        historyRepository.deleteAll();
        caseRepository.deleteAll();

        Queue queue = new Queue(TEST_QUEUE, false);
        TopicExchange exchange = new TopicExchange(EXCHANGE, true, false);

        rabbitAdmin.declareExchange(exchange);
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareBinding(
                BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY)
        );

        rabbitAdmin.purgeQueue(TEST_QUEUE, true);
    }

    @Test
    void shouldChangeStatusAndPublishEventToRabbit() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        String token = Jwts.builder()
                .setSubject("test@test.com")
                .claim("userId", userId.toString())
                .claim("roles", java.util.List.of("ADMIN"))
                .setIssuedAt(new java.util.Date())
                .setExpiration(Date.from(java.time.Instant.now().plusSeconds(3600)))
                .signWith(
                        io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                                "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF".getBytes()
                        )
                )
                .compact();

        CaseEntity entity = caseRepository.save(
                CaseEntity.builder()
                        .caseNumber("CASE-2026-IT-001")
                        .applicantPesel("90010112345")
                        .status(CaseStatus.SUBMITTED)
                        .createdByUserId(userId)
                        .build()
        );

        UUID caseId = entity.getId();

        String json = """
            { "newStatus": "IN_REVIEW" }
            """;

        // when
        mockMvc.perform(
                        patch("/api/officer/cases/{caseId}/status", caseId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf())
                                .header("Authorization", "Bearer " + token)
                                .content(json)
                )
                .andExpect(status().isNoContent());

        // then
        CaseEntity updated = caseRepository.findById(caseId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(CaseStatus.IN_REVIEW);

        assertThat(historyRepository.findAll()).hasSize(1);

        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Message message = rabbitTemplate.receive(TEST_QUEUE);
                    assertThat(message).isNotNull();

                    CaseStatusChangedEvent event =
                            objectMapper.readValue(message.getBody(), CaseStatusChangedEvent.class);

                    assertThat(event.caseId()).isEqualTo(caseId);
                    assertThat(event.oldStatus()).isEqualTo(com.govcaseflow.events.cases.CaseStatus.SUBMITTED);
                    assertThat(event.newStatus()).isEqualTo(com.govcaseflow.events.cases.CaseStatus.IN_REVIEW);
                    assertThat(event.changedBy()).isNotBlank();
                });
    }
}