package com.keycloak.example.config.keycloak.rest;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    public static final String INVALID_TOKEN = "invalid_token";
    public static final String THE_REQUIRED_AUDIENCE_IS_MISSING = "The required audience is missing";
    private final String audience;

    public AudienceValidator(String audience) {
        this.audience = audience;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        if (jwt.getAudience().contains(audience)) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult
                .failure(new OAuth2Error(INVALID_TOKEN, THE_REQUIRED_AUDIENCE_IS_MISSING, null));
    }
}