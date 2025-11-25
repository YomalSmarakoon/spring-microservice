package com.optimagrowth.license.config;

import feign.codec.ErrorDecoder;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public ErrorDecoder errorDecoder(MessageSource messageSource) {
        return new FeignErrorDecoder(messageSource);
    }
}
