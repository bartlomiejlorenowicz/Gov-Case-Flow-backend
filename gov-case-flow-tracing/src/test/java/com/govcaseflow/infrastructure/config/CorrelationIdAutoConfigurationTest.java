package com.govcaseflow.infrastructure.config;

import com.govcaseflow.infrastructure.tracing.CorrelationIdFilter;
import com.govcaseflow.infrastructure.tracing.CorrelationIdListenerAdvice;
import org.aopalliance.aop.Advice;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdAutoConfigurationTest {

    private final ApplicationContextRunner amqpContext = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    RabbitAutoConfiguration.class,
                    CorrelationIdAmqpAutoConfiguration.class));

    @Test
    void shouldPrependAdviceAndKeepBootRetryInterceptor() {
        amqpContext
                .withPropertyValues("spring.rabbitmq.listener.simple.retry.enabled=true")
                .run(context -> {
                    Advice[] chain = context.getBean(SimpleRabbitListenerContainerFactory.class).getAdviceChain();

                    assertThat(chain).hasSize(2);
                    assertThat(chain[0]).isInstanceOf(CorrelationIdListenerAdvice.class);
                    assertThat(chain[1]).isInstanceOf(RetryOperationsInterceptor.class);
                });
    }

    @Test
    void shouldAddAdviceWhenNoRetryConfigured() {
        amqpContext.run(context -> {
            Advice[] chain = context.getBean(SimpleRabbitListenerContainerFactory.class).getAdviceChain();

            assertThat(chain).singleElement().isInstanceOf(CorrelationIdListenerAdvice.class);
        });
    }

    @Test
    void shouldRegisterFilterBeforeSecurityInServletApp() {
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(CorrelationIdWebAutoConfiguration.class))
                .run(context -> {
                    FilterRegistrationBean<?> registration = context.getBean(FilterRegistrationBean.class);

                    assertThat(registration.getFilter()).isInstanceOf(CorrelationIdFilter.class);
                    assertThat(registration.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
                });
    }

    @Test
    void shouldNotRegisterFilterInNonWebApp() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(CorrelationIdWebAutoConfiguration.class))
                .run(context -> assertThat(context).doesNotHaveBean(FilterRegistrationBean.class));
    }
}
