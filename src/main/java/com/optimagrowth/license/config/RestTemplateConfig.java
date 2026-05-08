package com.optimagrowth.license.config;

import com.optimagrowth.license.security.BearerTokenProvider;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(
            RestTemplateBuilder builder,
            BearerTokenProvider bearerTokenProvider) {

        RestTemplate restTemplate = builder.build();

        restTemplate.getInterceptors().add((request, body, execution) -> {
            String token = bearerTokenProvider.getBearerToken();

            if (token != null) {
                request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            }

            return execution.execute(request, body);
        });

        return restTemplate;
    }
}
