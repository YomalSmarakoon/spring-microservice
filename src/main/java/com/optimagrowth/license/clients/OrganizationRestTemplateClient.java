package com.optimagrowth.license.clients;

import com.optimagrowth.license.model.dto.orgnization.OrganizationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OrganizationRestTemplateClient {

    @Autowired
    RestTemplate restTemplate;

    public OrganizationResponse getOrganization(String organizationId) {
        ResponseEntity<OrganizationResponse> restExchange = restTemplate.exchange(
                "http://organization-service/v1/organization/{organizationId}", // Uses service ID, not host
                HttpMethod.GET, null, OrganizationResponse.class, organizationId);
        return restExchange.getBody();
    }

}
