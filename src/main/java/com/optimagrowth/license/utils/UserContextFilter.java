package com.optimagrowth.license.utils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Holds user-specific request context using {@link ThreadLocal}.
 *
 * <p>This implementation ensures that each thread has its own isolated
 * {@link UserContext}, making it safe to use in a typical synchronous
 * request-response lifecycle (e.g., Spring MVC).</p>
 *
 * <p><b>Important:</b> This context is bound to the current thread only.
 * It works correctly as long as the execution remains within the same thread.</p>
 *
 * <p>The context will <b>NOT</b> be automatically propagated to other threads in cases such as:</p>
 * <ul>
 *     <li>Methods annotated with {@code @Async}</li>
 *     <li>Usage of {@link java.util.concurrent.CompletableFuture}</li>
 *     <li>Parallel streams</li>
 *     <li>Reactive programming (e.g., Spring WebFlux)</li>
 * </ul>
 *
 * <p>This is because {@link ThreadLocal} does not share data across threads.
 * Each new thread will have its own separate context instance.</p>
 *
 * <p>Therefore, if your application uses asynchronous or multi-threaded
 * execution, you must manually propagate the context or use alternative
 * mechanisms such as:</p>
 * <ul>
 *     <li>Custom context propagation strategies</li>
 *     <li>{@code InheritableThreadLocal} (with caution)</li>
 *     <li>Framework-provided context propagation (e.g., Reactor Context)</li>
 * </ul>
 *
 * <p><b>Note:</b> Always clear the context after request completion
 * (e.g., in a filter) to prevent memory leaks due to thread reuse in pools.</p>
 */
@Component
public class UserContextFilter extends OncePerRequestFilter {

    /*
     * Why?  OncePerRequestFilter instead Filter
     *
     * Guarantees execution once per request
     * Cleaner integration with Spring Security
     * More commonly used in modern Spring Boot apps
     * */

    private static final Logger LOG = LoggerFactory.getLogger(UserContextFilter.class);

    private static final String HDR_CORRELATION_ID = "correlation-id";
    private static final String HDR_USER_ID = "user-id";
    private static final String HDR_AUTH_TOKEN = "auth-token";
    private static final String HDR_ORG_ID = "organization-id";

    /*
    * You must NOT trust these from headers; Because anyone can fake them:
    *   - user-id
    *   - organization-id
    *
    * Frontend can:
    *   - modify headers
    *   - modify request body
    *   - spoof userId
    *   - change orgId
    * Backend must:
    *   - validate everything
    *   - derive identity from secure source (JWT/session)
    * */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            UserContext context = UserContextHolder.getContext();

            // 1) Read headers
            String correlationId = headerOrNull(request, HDR_CORRELATION_ID);
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();

                LOG.debug("correlation-id generated in user context filter: {}.", correlationId);
            }

            context.setCorrelationId(correlationId);
            context.setUserId(headerOrNull(request, HDR_USER_ID));
            context.setAuthToken(headerOrNull(request, HDR_AUTH_TOKEN));
            context.setOrganizationId(headerOrNull(request, HDR_ORG_ID));

            // 2) Put into logging MDC so every log line can show correlationId
            MDC.put("correlationId", correlationId);
            if (context.getUserId() != null) MDC.put("userId", context.getUserId());
            if (context.getOrganizationId() != null) MDC.put("orgId", context.getOrganizationId());

            // 3) Continue request
            filterChain.doFilter(request, response);

        } finally {
            // Clean up no matter what (even if exception happens)
            MDC.clear();
            UserContextHolder.clear();
        }
    }

    private String headerOrNull(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    /*
    * More:
    * - how to automatically inject correlationId into logs (logback config)
    * - OR how this breaks in async (@Async) and how to fix it (very useful in real projects)
    * */
}
