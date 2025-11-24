package com.optimagrowth.license.clients;

import com.optimagrowth.license.model.dto.orgnization.OrganizationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class OrganizationDiscoveryClient {

    @Autowired
    private DiscoveryClient discoveryClient;    // ❶ Access Eureka


    public OrganizationResponse getOrganization(String organizationId) {
        RestTemplate restTemplate = new RestTemplate();

        List<ServiceInstance> instances = discoveryClient.getInstances("organization-service"); // ❷ Get service instances

        if (instances.isEmpty()) return null;

        String serviceUri = String.format("%s/v1/organization/%s",
                instances.get(0).getUri().toString(), organizationId);  // ❸ Build URL

        ResponseEntity<OrganizationResponse> restExchange = restTemplate.exchange(
                serviceUri, HttpMethod.GET, null, OrganizationResponse.class, organizationId);  // ❹ REST call

        return restExchange.getBody();
    }

}
