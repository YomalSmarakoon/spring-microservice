package com.optimagrowth.license.config;

import com.optimagrowth.license.exception.OrganizationNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

/*
* So if organization-service returns 404, you throw your domain exception OrganizationNotFoundException (with an i18n message).
* Those exceptions then bubble into Spring MVC, where your @RestControllerAdvice can convert them to JSON.
* */
@Component
public class FeignErrorDecoder implements ErrorDecoder {

    private final MessageSource messageSource;

    public FeignErrorDecoder(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        switch (response.status()) {
            case 404:
                String notFoundMsg =
                        messageSource.getMessage("organization.not.found",
                                null, "Organization not found", Locale.getDefault());
                return new OrganizationNotFoundException(notFoundMsg);

            case 400:
                return new IllegalArgumentException("Bad request made to organization-service");

            case 500:
                return new RuntimeException("organization-service internal error");

            default:
                return new Exception("Unexpected error calling organization-service");
        }
    }
}
