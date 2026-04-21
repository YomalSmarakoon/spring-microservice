package com.optimagrowth.license.utils;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Intercepts outgoing HTTP client requests and propagates values stored in
 * {@link UserContextHolder} as request headers.
 *
 * <p>This is used in inter-service communication so that contextual
 * information such as correlation ID can be forwarded from the current
 * request to downstream microservices.</p>
 *
 * <p>The interceptor reads the current thread-bound {@link UserContext}
 * and adds its values to the outbound HTTP headers before the request
 * is executed.</p>
 *
 * <p>This helps preserve tracing and request-scoped metadata across
 * service boundaries.</p>
 */
public class UserContextInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {

        UserContext context = UserContextHolder.getContext();

        if (context.getCorrelationId() != null) {
            request.getHeaders().add("correlation-id", context.getCorrelationId());
        }
        if (context.getUserId() != null) {
            request.getHeaders().add("user-id", context.getUserId());
        }
        if (context.getAuthToken() != null) {
            request.getHeaders().add("auth-token", context.getAuthToken());
        }
        if (context.getOrganizationId() != null) {
            request.getHeaders().add("organization-id", context.getOrganizationId());
        }

        return execution.execute(request, body);
    }
}
