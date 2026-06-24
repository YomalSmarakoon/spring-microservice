package com.optimagrowth.license.events.handler;

import com.optimagrowth.license.events.invalidator.OrganizationCacheInvalidator;
import com.optimagrowth.license.events.model.OrganizationChangeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Spring Cloud Stream consumer for organization change events published by
 * the organization service via Kafka.
 * <p>
 * Uses the functional-style Consumer<T> API introduced in Spring Cloud Stream 3.x.
 * This replaces the now-deprecated @EnableBinding, @Input, @Output, Source and
 * Sink annotations used in the book's original implementation.
 * </p>
 * Binding convention: the bean name "organizationChangeConsumer" maps to the
 * Spring Cloud Stream binding "organizationChangeConsumer-in-0", which must be
 * configured in application.yml with the correct Kafka destination and consumer group.
 */
@Component
public class OrganizationChangeHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OrganizationChangeHandler.class);

    // Injected to evict stale organization entries from Redis when a change event arrives.
    private final OrganizationCacheInvalidator cacheInvalidator;

    public OrganizationChangeHandler(OrganizationCacheInvalidator cacheInvalidator) {
        this.cacheInvalidator = cacheInvalidator;
    }

    @Bean
    public Consumer<OrganizationChangeModel> organizationChangeConsumer() {
        return event -> {
            LOG.info("Received organization change event. action={}, organizationId={}",
                    event.action(), event.organizationId());

            // Evict the cached entry for this organization so the next read fetches
            // fresh data from the organization service.
            // For CREATE: this is a no-op because nothing is cached yet.
            // For UPDATE and DELETE: the stale entry is removed from Redis.
            // We intentionally evict rather than update because the event payload
            // only carries the organizationId — not the full updated object. Evicting
            // and re-fetching on the next request keeps the two services decoupled.
            cacheInvalidator.invalidate(event);
        };
    }
}
