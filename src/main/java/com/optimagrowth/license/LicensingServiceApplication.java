package com.optimagrowth.license;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;
import java.util.ResourceBundle;

@SpringBootApplication
/*
* Spring Cloud Config Server always has the latest properties, but Spring Boot apps only load them at startup.
* To dynamically refresh properties without restarting, use @RefreshScope and the /refresh actuator endpoint.
*
* Not all properties are refreshed (e.g., DB configs managed by Spring Data won’t reload).
*/
@RefreshScope
public class LicensingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LicensingServiceApplication.class, args);
	}

	@Bean
	public LocaleResolver localeResolver() {
		SessionLocaleResolver localeResolver = new SessionLocaleResolver();
		localeResolver.setDefaultLocale(Locale.US);		// set US as the default locale
		return localeResolver;
	}

	@Bean
	public ResourceBundleMessageSource messageSource() {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		/*
		* Doesn't throw an error if a message isn't found,
		* instead it returns the message code
		*/
		messageSource.setUseCodeAsDefaultMessage(true);

		messageSource.setBasenames("messages");		// Sets the base name of the languages properties files
		return messageSource;
	}
}
