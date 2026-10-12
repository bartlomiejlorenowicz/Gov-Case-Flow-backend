package com.govcaseflow.infrastructure.config;

import com.govcaseflow.infrastructure.tracing.CorrelationIdListenerAdvice;
import com.govcaseflow.infrastructure.tracing.CorrelationIdMessagePostProcessor;
import org.aopalliance.aop.Advice;
import org.springframework.amqp.rabbit.config.BaseRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@AutoConfiguration
@ConditionalOnClass(RabbitTemplate.class)
public class CorrelationIdAmqpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CorrelationIdMessagePostProcessor correlationIdMessagePostProcessor() {
        return new CorrelationIdMessagePostProcessor();
    }

    /**
     * Applies to the RabbitTemplate auto-configured by Spring Boot.
     * Services defining their own template must add the post-processor themselves.
     */
    @Bean
    public RabbitTemplateCustomizer correlationIdRabbitTemplateCustomizer(
            CorrelationIdMessagePostProcessor correlationIdMessagePostProcessor) {
        return template -> template.addBeforePublishPostProcessors(correlationIdMessagePostProcessor);
    }

    /**
     * Prepends the correlation advice to every listener container factory instead of
     * replacing Boot's factory, so the retry interceptor configured from
     * spring.rabbitmq.listener.* stays in place and runs inside the MDC scope.
     */
    @Bean
    public static BeanPostProcessor correlationIdListenerAdvicePostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof BaseRabbitListenerContainerFactory<?> factory) {
                    factory.setAdviceChain(withCorrelationAdvice(factory.getAdviceChain()));
                }
                return bean;
            }
        };
    }

    private static Advice[] withCorrelationAdvice(Advice[] existing) {
        if (existing == null || existing.length == 0) {
            return new Advice[] {new CorrelationIdListenerAdvice()};
        }
        if (Arrays.stream(existing).anyMatch(CorrelationIdListenerAdvice.class::isInstance)) {
            return existing;
        }
        Advice[] chain = new Advice[existing.length + 1];
        chain[0] = new CorrelationIdListenerAdvice();
        System.arraycopy(existing, 0, chain, 1, existing.length);
        return chain;
    }
}
