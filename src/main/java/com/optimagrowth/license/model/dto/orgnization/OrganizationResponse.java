package com.optimagrowth.license.model.dto.orgnization;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OrganizationResponse {

    private String id;
    private String name;
    private String contactName;
    private String contactEmail;
    private String contactPhone;

    OrganizationResponse() {}
}
