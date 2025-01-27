package com.keycloak.example.config.keycloak.rest;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Slf4j
@Configuration
@ConditionalOnProperty(name = RestApiSecurityConfig.REST_SECURITY_ENABLED, havingValue = RestApiSecurityConfig.TRUE,
        matchIfMissing = true)
@Profile("!local")
public class RestApiSecurityConfig {

    public static final String REST_SECURITY_ENABLED = "rest.security.enabled";
    public static final String TRUE = "true";
    
    private static final String PROVIDER_PREFIX = "spring.security.oauth2.client.provider.";
    private static final String JWK_SET_URI_SUFFIX = ".jwk-set-uri";
    private static final String ISSUER_URI_SUFFIX = ".issuer-uri";
    private static final String USER_NAME_ATTRIBUTE_SUFFIX = ".user-name-attribute";
    private static final String ENGINE_REST = "/engine-rest/**";

    private final RestApiSecurityConfigurationProperties configProps;
    private final IdentityService identityService;
    private final OAuth2AuthorizedClientService clientService;
    private final ApplicationContext applicationContext;

    @Inject
    public RestApiSecurityConfig(RestApiSecurityConfigurationProperties configProps,
                                 IdentityService identityService,
                                 OAuth2AuthorizedClientService clientService,
                                 ApplicationContext applicationContext) {
        this.configProps = configProps;
        this.identityService = identityService;
        this.clientService = clientService;
        this.applicationContext = applicationContext;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain httpSecurityRest(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        String jwkSetUri = getRequiredProperty(PROVIDER_PREFIX + configProps.getProvider() + JWK_SET_URI_SUFFIX);

        log.info("Configuring HTTP security with JWK Set URI: {}", jwkSetUri);

        return http.securityMatcher(antMatcher(ENGINE_REST))
                .csrf(csrf -> csrf.ignoringRequestMatchers(antMatcher(ENGINE_REST)))
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
                        .jwt(jwt -> jwt.decoder(jwtDecoder).jwkSetUri(jwkSetUri)))
                .addFilterBefore(keycloakAuthenticationFilter(), AuthorizationFilter.class).build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        String issuerUri = getRequiredProperty(PROVIDER_PREFIX + configProps.getProvider() + ISSUER_URI_SUFFIX);
        log.info("Configuring JwtDecoder with Issuer URI: {}", issuerUri);

        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromOidcIssuerLocation(issuerUri);

        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(configProps.getRequiredAudience());
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(withAudience);

        return jwtDecoder;
    }

    private String getRequiredProperty(String propertyName) {
        try {
            return applicationContext.getEnvironment().getRequiredProperty(propertyName);
        } catch (IllegalStateException e) {
            log.error("Missing required property: {}", propertyName);
            throw e;
        }
    }

    public KeycloakAuthenticationFilter keycloakAuthenticationFilter() {
        String userNameAttribute = getRequiredProperty(PROVIDER_PREFIX + configProps.getProvider() + USER_NAME_ATTRIBUTE_SUFFIX);
        return new KeycloakAuthenticationFilter(identityService, clientService, userNameAttribute);
    }
}