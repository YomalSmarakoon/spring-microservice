package com.optimagrowth.license.events.handler;

import com.optimagrowth.license.events.model.OrganizationChangeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class OrganizationChangeHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OrganizationChangeHandler.class);

    @Bean
    public Consumer<OrganizationChangeModel> organizationChangeConsumer() {
        return event -> {
            LOG.info(
                    "Received organization change event. action={}, organizationId={}",
                    event.action(),
                    event.organizationId()
            );

            // Later: evict/update Redis cache here
        };
    }
}
