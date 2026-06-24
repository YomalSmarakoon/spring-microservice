package com.optimagrowth.license.events.invalidator;

import com.optimagrowth.license.events.model.OrganizationChangeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * Removes stale organization entries from the Redis cache in response to
 * change events published by the organization service.
 * <p>
 * This class is a dedicated eviction component. Keeping eviction in its own
 * class rather than inside the event handler makes the @CacheEvict annotation
 * work correctly: Spring's AOP proxy intercepts calls to this bean's public
 * methods, and self-invocation (calling an @CacheEvict method from within the
 * same bean) would bypass the proxy and silently do nothing.
 * </p>
 * Called by OrganizationChangeHandler for every incoming event regardless of
 * action type. Evicting a key that does not exist in Redis is a safe no-op,
 * so CREATE events are handled correctly without special-casing.
 */
@Service
public class OrganizationCacheInvalidator {

    private static final Logger LOG = LoggerFactory.getLogger(OrganizationCacheInvalidator.class);

    /**
     * Removes the cached entry for the given organization from Redis.
     * <p>
     * The key expression "#event.organizationId" must match the key used when
     * the entry was stored (see OrganizationCacheClient.getOrganization), so
     * both sides use the raw organizationId string as the cache key.
     * </p>
     */
    @CacheEvict(cacheNames = "organizations", key = "#event.organizationId")
    public void invalidate(OrganizationChangeModel event) {
        LOG.info("Evicted organization from cache. action={}, organizationId={}",
                event.action(),
                event.organizationId());
    }
}
