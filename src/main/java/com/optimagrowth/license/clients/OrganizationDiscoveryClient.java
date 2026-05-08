package com.optimagrowth.license.clients;

import com.optimagrowth.license.model.dto.orgnization.OrganizationResponse;
import com.optimagrowth.license.security.BearerTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class OrganizationDiscoveryClient {

    @Autowired
    private DiscoveryClient discoveryClient;    // ❶ Access Eureka

    @Autowired
    private BearerTokenProvider bearerTokenProvider;

    public OrganizationResponse getOrganization(String organizationId) {
        RestTemplate restTemplate = new RestTemplate();

        List<ServiceInstance> instances = discoveryClient.getInstances("organization-service"); // ❷ Get service instances

        if (instances.isEmpty()) return null;

        String serviceUri = String.format("%s/v1/organization/%s",
                instances.get(0).getUri().toString(), organizationId);  // ❸ Build URL

        HttpHeaders headers = new HttpHeaders();

        String token = bearerTokenProvider.getBearerToken();
        if (token != null) {
            headers.setBearerAuth(token);
        }

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<OrganizationResponse> restExchange = restTemplate.exchange(
                serviceUri, HttpMethod.GET, requestEntity, OrganizationResponse.class);  // ❹ REST call

        return restExchange.getBody();
    }

}
