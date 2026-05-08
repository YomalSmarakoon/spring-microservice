package com.optimagrowth.license.config;

import com.optimagrowth.license.security.BearerTokenProvider;
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

    @Bean
    public ErrorDecoder errorDecoder(MessageSource messageSource) {
        return new FeignErrorDecoder(messageSource);
    }
}
