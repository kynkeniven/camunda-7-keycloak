package com.keycloak.example.config.keycloak.plugin;

import org.camunda.bpm.extension.keycloak.plugin.KeycloakIdentityProviderPlugin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = KeycloakIdentityProvider.PLUGIN_IDENTITY_KEYCLOAK)
@Profile("!local")
public class KeycloakIdentityProvider extends KeycloakIdentityProviderPlugin {

    protected static final String PLUGIN_IDENTITY_KEYCLOAK = "plugin.identity.keycloak";
}