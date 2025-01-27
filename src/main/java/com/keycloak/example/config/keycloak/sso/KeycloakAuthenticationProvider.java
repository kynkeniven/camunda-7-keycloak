package com.keycloak.example.config.keycloak.sso;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.security.auth.AuthenticationResult;
import org.camunda.bpm.engine.rest.security.auth.impl.ContainerBasedAuthenticationProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
public class KeycloakAuthenticationProvider extends ContainerBasedAuthenticationProvider {


    @Override
    public AuthenticationResult extractAuthenticatedUser(HttpServletRequest request, ProcessEngine engine) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof OAuth2AuthenticationToken) ||
                !(authentication.getPrincipal() instanceof OidcUser oidcUser)) {
            log.warn("Authentication is not an OAuth2AuthenticationToken");
            return AuthenticationResult.unsuccessful();
        }

        String userId = oidcUser.getName();
        if (!StringUtils.hasLength(userId)) {
            log.warn("User ID is not available or empty");
            return AuthenticationResult.unsuccessful();
        }

        log.info("Successfully authenticated user: {}", userId);
        AuthenticationResult authenticationResult = new AuthenticationResult(userId, true);
        authenticationResult.setGroups(getUserGroups(userId, engine));

        return authenticationResult;
    }

    private List<String> getUserGroups(String userId, ProcessEngine engine) {
        return engine.getIdentityService().createGroupQuery().groupMember(userId).list()
                .stream()
                .map(group -> {
                    log.debug("User belongs to group: {}", group.getId());
                    return group.getId();
                })
                .toList();
    }
}