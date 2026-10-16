package com.optimagrowth.license.config;

import com.optimagrowth.license.security.BearerTokenProvider;
import com.optimagrowth.license.utils.UserContext;
import com.optimagrowth.license.utils.UserContextHolder;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

/*
 * This registers a custom Feign ErrorDecoder bean, so whenever a Feign client receives a non-2xx HTTP response,
 * Feign asks your decoder: “what Exception should I throw?”
 * */
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor bearerTokenRequestInterceptor(
            BearerTokenProvider bearerTokenProvider) {

        return requestTemplate -> {
            String token = bearerTokenProvider.getBearerToken();

            if (token != null) {
                requestTemplate.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            }
        };
    }

    // Mirrors UserContextInterceptor (used on the RestTemplate path) so the
    // Feign path also forwards correlation-id/user-id/organization-id downstream.
    @Bean
    public RequestInterceptor userContextRequestInterceptor() {
        return requestTemplate -> {
            UserContext context = UserContextHolder.getContext();

            if (context.getCorrelationId() != null) {
                requestTemplate.header("correlation-id", context.getCorrelationId());
            }
            if (context.getUserId() != null) {
                requestTemplate.header("user-id", context.getUserId());
            }
            if (context.getAuthToken() != null) {
                requestTemplate.header("auth-token", context.getAuthToken());
            }
            if (context.getOrganizationId() != null) {
                requestTemplate.header("organization-id", context.getOrganizationId());
            }
        };
    }

    @Bean
    public ErrorDecoder errorDecoder(MessageSource messageSource) {
        return new FeignErrorDecoder(messageSource);
    }
}
