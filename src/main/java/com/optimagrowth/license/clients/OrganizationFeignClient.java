package com.optimagrowth.license.clients;

import com.optimagrowth.license.model.dto.orgnization.OrganizationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient("organization-service")  // ❶ Service name in Eureka
public interface OrganizationFeignClient {

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/v1/organization/{organizationId}",  // ❷ API endpoint
            consumes = "application/json"
    )
    OrganizationResponse getOrganization(@PathVariable("organizationId") String organizationId);  // ❸ Path variable
}
