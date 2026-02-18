package com.optimagrowth.license.service;

import com.optimagrowth.license.clients.OrganizationDiscoveryClient;
import com.optimagrowth.license.clients.OrganizationFeignClient;
import com.optimagrowth.license.clients.OrganizationRestTemplateClient;
import com.optimagrowth.license.config.ServiceConfig;
import com.optimagrowth.license.exception.OrganizationNotFoundException;
import com.optimagrowth.license.model.License;
import com.optimagrowth.license.model.dto.license.LicenseResponse;
import com.optimagrowth.license.model.dto.orgnization.OrganizationResponse;
import com.optimagrowth.license.repository.LicenseRepository;
import com.optimagrowth.license.utils.UserContextHolder;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LicenseService {

    private static final Logger LOG = LoggerFactory.getLogger(LicenseService.class);

    @Autowired
    MessageSource messageSource;

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    ServiceConfig config;

    @Autowired
    private OrganizationDiscoveryClient wbClient;

    @Autowired
    private OrganizationRestTemplateClient restTemplateClient;

    @Autowired
    private OrganizationFeignClient organizationFeignClient;

    private final AtomicInteger counter = new AtomicInteger(0); // for testing retry

    public License getLicense(String licenseId, String organizationId) {
        License license = licenseRepository
                .findByOrganizationIdAndLicenseId(organizationId, licenseId);

        if (null == license) {
            throw new IllegalArgumentException(
                    String.format(messageSource.getMessage(
                                    "license.search.error.message", null, null),
                            licenseId, organizationId));
        }

        return license.withComment(config.getProperty());
    }

    public License createLicense(License license) {
        license.setLicenseId(UUID.randomUUID().toString());
        licenseRepository.save(license);
        return license.withComment(config.getProperty());
    }

    public License updateLicense(License license) {
        licenseRepository.save(license);
        return license.withComment(config.getProperty());
    }

    public String deleteLicense(String licenseId) {
        String responseMessage = null;

        License license = new License();
        license.setLicenseId(licenseId);
        licenseRepository.delete(license);
        responseMessage = String.format(messageSource.getMessage(
                "license.delete.message", null, null), licenseId);
        return responseMessage;
    }

    public LicenseResponse getLicense(String licenseId, String organizationId, String clientType) {
        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);
        if (null == license) {
            throw new IllegalArgumentException(String.format(
                    messageSource.getMessage("license.search.error.message", null, null),
                    licenseId, organizationId));
        }

        license.withComment(config.getProperty());

        LicenseResponse licenseResponse = new LicenseResponse(license);

        // retrieve org info based on clientType (Feign / RestTemplate / WebClient)
        OrganizationResponse organization = retrieveOrganizationInfo(organizationId,
                clientType);

        // populate organization fields into the license
        if (null != organization) {
            licenseResponse.setOrganizationName(organization.getName());
            licenseResponse.setContactName(organization.getContactName());
            licenseResponse.setContactEmail(organization.getContactEmail());
            licenseResponse.setContactPhone(organization.getContactPhone());
        }
        return licenseResponse;
    }


    private OrganizationResponse retrieveOrganizationInfo(String organizationId, String clientType) {
        switch (clientType) {
            /*
             * Netflix Feign Client – A declarative REST client integrated with Eureka for automatic load-balanced calls.
             * */
            case "feign":
                try {
                    return organizationFeignClient.getOrganization(organizationId);
                } catch (OrganizationNotFoundException ex) {
                    LOG.error("Organization not found: {}", organizationId);
                    return null;  // or throw the exception further
                }
                /*
                 * Spring Discovery Client–enabled RestTemplate – A RestTemplate enhanced to work with service discovery automatically.
                 * */
            case "rest":
                return restTemplateClient.getOrganization(organizationId);
            /*
             * Spring Discovery Client – Uses DiscoveryClient and a standard RestTemplate.
             * */
            case "webclient":
                return wbClient.getOrganization(organizationId);
            default:
                return null;
        }
    }

    /**
     * <ul>
     *     <li>Your sleep() intentionally throws a TimeoutException.</li>
     *     <li>After enough failures, the circuit breaker becomes OPEN.</li>
     *     <li>Once OPEN, Resilience4j throws CallNotPermittedException immediately for any further calls.</li>
     * </ul>
     *
     */
    @CircuitBreaker(name = "licenseService", fallbackMethod = "buildFallbackLicenseList")
    // @Bulkhead(name = "bulkheadLicenseService", type = Bulkhead.Type.THREADPOOL, fallbackMethod = "buildFallbackLicenseList")
    // @Retry(name = "retryLicenseService", fallbackMethod = "buildFallbackLicenseList")
    // @RateLimiter(name = "licenseService", fallbackMethod = "buildFallbackLicenseList")
    public List<License> getLicensesByOrganization(String organizationId) throws InterruptedException, TimeoutException {

        LOG.info("getLicensesByOrganization Correlation id: {}",
                UserContextHolder.getContext().getCorrelationId());

        /* for testing @Retry
        int attempt = counter.incrementAndGet();

        if (attempt <= 2) { // fail first 2 calls
            throw new TimeoutException("Simulated timeout");
        }*/

        // randomlyRunLong();
        return licenseRepository.findByOrganizationId(organizationId);
    }

    private List<License> buildFallbackLicenseList(
            String organizationId, Throwable t) {

        List<License> fallbackList = new ArrayList<>();

        License license = new License();
        license.setLicenseId("0000000-00-xxxxx");
        license.setOrganizationId(organizationId);
        license.setProductName(
                "Sorry no licensing information currently available"
        );

        fallbackList.add(license);
        return fallbackList;
    }

    /*
     * Purposely timing out a call to the licensing service database
     * */
    private void randomlyRunLong() throws InterruptedException, TimeoutException {
        /*Random rand = new Random();
        int randomNum = rand.nextInt(3) + 1;
        if (randomNum==3)*/
        sleep();
    }

    /*private void sleep(){
        try {
            Thread.sleep(5000);
            throw new java.util.concurrent.TimeoutException();
        } catch (InterruptedException | TimeoutException e) {
            LOG.error(e.getMessage());
        }
    }*/
    private void sleep() throws TimeoutException, InterruptedException {
        Thread.sleep(5000);
        throw new TimeoutException("Simulated timeout");
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *                                                  FROM CHAPTER 3
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * */

    /*public License getLicense(String licenseId, String organizationId) {
        License license = new License();

        license.setId(new Random().nextInt(1000));
        license.setLicenseId(licenseId);
        license.setOrganizationId(organizationId);
        license.setDescription("Software Product");
        license.setProductName("Ostock");
        license.setLicenseType("full");

        return license;
    }

    public String createLicense(License license, String organizationId, Locale locale) {    // Retrieve the Local as a method parameter
        String responseMessage = null;

        if (license != null) {
            license.setOrganizationId(organizationId);
            responseMessage = String.format(messageSource.getMessage(
                    "license.create.message", null, locale), license.toString());   // Sets the received locale to retrieve the specific message
        }

        return responseMessage;
    }

    public String updateLicense(License license, String organizationId) {
        String responseMessage = null;

        if (license != null) {
            license.setOrganizationId(organizationId);
            responseMessage = String.format(messageSource.getMessage(
                    "license.update.message", null, null), license.toString());   // Sends a null locale to retrieve the specific message
        }

        return responseMessage;
    }

    public String deleteLicense(String licenseId, String organizationId) {
        String responseMessage = null;

        responseMessage = String.format("Deleting license with id %s for the organization %s", licenseId, organizationId);

        return responseMessage;
    }*/
}
