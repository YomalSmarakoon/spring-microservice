package com.optimagrowth.license.controller;

import com.optimagrowth.license.service.LicenseService;
import com.optimagrowth.license.model.License;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

/*
 * Tells Spring Boot that this is a REST based service &
 * it will automatically serialize/deserialize service requests/responses via JSON
 */
@RestController
/*
 * Expose all the HTTP endpoints in this class with a prefix of below
 */
@RequestMapping(value = "v1/organization/{organizationId}/license")
public class LicenseController {

    @Autowired
    private LicenseService licenseService;

    @GetMapping(value = "/{licenceId}")
    /*
    * Also can use:
    *       @RequestMapping(value = "/{licenceId}", method = RequestMethod.GET)
    */
    public ResponseEntity<License> getLicense(
            @PathVariable("organizationId") String organizationId,
            @PathVariable("licenceId") String licenceId
    ) {

        License license = licenseService.getLicense(licenceId, organizationId);
        return ResponseEntity.ok(license);
    }

    @PostMapping
    public ResponseEntity<String> createLicense(
            @PathVariable("organizationId") String organizationId,
            @RequestBody License request,
            @RequestHeader(value = "Accept-Language", required = false) Locale locale
            ) {
        return ResponseEntity.ok(licenseService.createLicense(request, organizationId, locale));
    }

    @PutMapping
    public ResponseEntity<String> updateLicense(
            @PathVariable("organizationId") String organizationId,
            @RequestBody License request
    ) {
        return ResponseEntity.ok(licenseService.updateLicense(request, organizationId));
    }

    @DeleteMapping(value = "/{licenceId}")
    public ResponseEntity<String> deleteLicense(
            @PathVariable("organizationId") String organizationId,
            @PathVariable("licenceId") String licenceId
    ) {
        return ResponseEntity.ok(licenseService.deleteLicense(licenceId, organizationId));
    }
}
