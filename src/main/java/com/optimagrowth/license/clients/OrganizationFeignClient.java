package com.optimagrowth.license.clients;

import com.optimagrowth.license.config.FeignConfig;
import com.optimagrowth.license.model.dto.orgnization.OrganizationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Declarative HTTP client for the organization-service.
 * <p>
 * This interface is a pure HTTP transport layer — it does not participate in caching.
 * Caching is handled by OrganizationCacheClient, which wraps this client and applies
 * @Cacheable on a concrete Spring bean where AOP proxying is reliable.
 * </p>
 * FeignConfig attaches the Bearer token interceptor and a custom error decoder
 * that maps HTTP 404 responses to OrganizationNotFoundException.
 */
@FeignClient(
        name = "organization-service",   // ❶ Service name in Eureka
        configuration = FeignConfig.class
)
public interface OrganizationFeignClient {

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/v1/organization/{organizationId}",  // ❷ API endpoint
            consumes = "application/json"
    )
    OrganizationResponse getOrganization(@PathVariable("organizationId") String organizationId);  // ❸ Path variable
}
