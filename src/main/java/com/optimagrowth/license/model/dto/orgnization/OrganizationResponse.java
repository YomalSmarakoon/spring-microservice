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

    // Must be public so Jackson can instantiate this class when deserializing
    // the JSON stored in Redis. A package-private or private constructor causes
    // a JsonMappingException at runtime when reading from the cache.
    public OrganizationResponse() {}
}
