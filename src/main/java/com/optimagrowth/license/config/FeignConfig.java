package com.optimagrowth.license.config;

import feign.codec.ErrorDecoder;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
* This registers a custom Feign ErrorDecoder bean, so whenever a Feign client receives a non-2xx HTTP response,
* Feign asks your decoder: “what Exception should I throw?”
* */
@Configuration
public class FeignConfig {

    @Bean
    public ErrorDecoder errorDecoder(MessageSource messageSource) {
        return new FeignErrorDecoder(messageSource);
    }
}
