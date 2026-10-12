package com.govcaseflow.infrastructure.tracing;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void shouldGenerateIdWhenHeaderMissing() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> idInChain = new AtomicReference<>();

        filter.doFilter(new MockHttpServletRequest(), response,
                (req, res) -> idInChain.set(MDC.get(CorrelationId.MDC_KEY)));

        assertThat(CorrelationId.isValid(idInChain.get())).isTrue();
        assertThat(response.getHeader(CorrelationId.HEADER)).isEqualTo(idInChain.get());
    }

    @Test
    void shouldKeepIncomingValidId() throws Exception {
        String incoming = "3f2b8c1e-6d4a-4f7e-9b2c-1a2b3c4d5e6f";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationId.HEADER, incoming);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> idInChain = new AtomicReference<>();

        filter.doFilter(request, response,
                (req, res) -> idInChain.set(MDC.get(CorrelationId.MDC_KEY)));

        assertThat(idInChain.get()).isEqualTo(incoming);
        assertThat(response.getHeader(CorrelationId.HEADER)).isEqualTo(incoming);
    }

    @Test
    void shouldReplaceIncomingNonUuidValue() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationId.HEADER, "abc\nFAKE LOG LINE");
        AtomicReference<String> idInChain = new AtomicReference<>();

        filter.doFilter(request, new MockHttpServletResponse(),
                (req, res) -> idInChain.set(MDC.get(CorrelationId.MDC_KEY)));

        assertThat(CorrelationId.isValid(idInChain.get())).isTrue();
    }

    @Test
    void shouldClearMdcAfterRequest() throws Exception {
        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), (req, res) -> {});

        assertThat(MDC.get(CorrelationId.MDC_KEY)).isNull();
    }

    @Test
    void shouldClearMdcWhenChainThrows() {
        assertThatThrownBy(() -> filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(),
                (req, res) -> { throw new ServletException("boom"); }))
                .isInstanceOf(ServletException.class);

        assertThat(MDC.get(CorrelationId.MDC_KEY)).isNull();
    }
}
