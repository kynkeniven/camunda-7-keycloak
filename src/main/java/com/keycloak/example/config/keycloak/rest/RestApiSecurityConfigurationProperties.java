package com.keycloak.example.config.keycloak.rest;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = RestApiSecurityConfigurationProperties.REST_SECURITY)
@Validated
@Profile("!local")
public class RestApiSecurityConfigurationProperties {

    public static final String REST_SECURITY = "rest.security";
    private Boolean enabled = true;

    @NotEmpty
    private String provider;

    @NotEmpty
    private String requiredAudience;

}