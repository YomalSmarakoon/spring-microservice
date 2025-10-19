package com.optimagrowth.license.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
/*
* While Spring Data “auto-magically” injects the configuration data for the database into a database connection object,
* all other custom properties can be injected using the @ConfigurationProperties annotation.
* */
@ConfigurationProperties(prefix = "example")
/*
* pulls all the example properties from the Spring Cloud Configuration Server and injects these into the property
* attribute on the ServiceConfig class.
* */
@Getter @Setter
public class ServiceConfig {

    private String property;

}
