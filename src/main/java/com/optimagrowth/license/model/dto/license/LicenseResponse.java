package com.optimagrowth.license.model.dto.license;

import com.optimagrowth.license.model.License;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LicenseResponse {

    private String licenseId;
    private String description;
    private String organizationId;
    private String productName;
    private String licenseType;
    private String comment;

    // Organization enriched fields
    private String organizationName;
    private String contactName;
    private String contactEmail;
    private String contactPhone;


    public LicenseResponse(License license) {
        this.licenseId = license.getLicenseId();
        this.description = license.getDescription();
        this.organizationId = license.getOrganizationId();
        this.productName = license.getProductName();
        this.licenseType = license.getLicenseType();
        this.comment = license.getComment();
    }
}
