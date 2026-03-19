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
}
