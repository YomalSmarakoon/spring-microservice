package com.optimagrowth.license.clients;

import com.optimagrowth.license.model.dto.orgnization.OrganizationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Cache-aware proxy for fetching organization data from the organization service.
 *
 * Why does this class exist instead of putting @Cacheable on OrganizationFeignClient?
 * Spring's caching AOP works by wrapping a Spring-managed bean in a proxy. Feign
 * already wraps OrganizationFeignClient in its own JDK dynamic proxy. The composition
 * of two proxy layers (Feign + Spring AOP caching) is unreliable and breaks silently
 * depending on proxy ordering, AOT compilation settings, and CGLIB mode. Placing
 * @Cacheable on a concrete @Service class avoids this problem entirely — Spring
 * proxies this class cleanly and the caching advice fires as expected.
 *
 * Request flow:
 *   Cache hit  → returns OrganizationResponse from Redis, no HTTP call made
 *   Cache miss → calls organization-service over HTTP, stores result in Redis
 *   Redis down → CacheConfig.errorHandler() logs and falls through to the HTTP call
 */
@Service
public class OrganizationCacheClient {

    private static final Logger LOG = LoggerFactory.getLogger(OrganizationCacheClient.class);

    private final OrganizationFeignClient feignClient;

    public OrganizationCacheClient(OrganizationFeignClient feignClient) {
        this.feignClient = feignClient;
    }

    /**
     * Returns from Redis on a cache hit. On a miss, calls the organization service
     * and populates the cache for subsequent requests.
     * <p>
     * Cache key is the organizationId string. This matches the key used by
     * OrganizationCacheInvalidator.invalidate(), so eviction targets the correct entry.
     * </p>
     * unless = "#result == null" prevents caching a null response. Without this guard,
     * a not-found organization would be stored as null and all future callers would
     * silently receive null for the full TTL duration.
     */
    @Cacheable(cacheNames = "organizations", key = "#organizationId", unless = "#result == null")
    public OrganizationResponse getOrganization(String organizationId) {
        LOG.info("Cache miss — calling organization-service. organizationId={}", organizationId);
        return feignClient.getOrganization(organizationId);
    }
}
