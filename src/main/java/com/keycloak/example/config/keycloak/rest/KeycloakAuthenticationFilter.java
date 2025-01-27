package com.keycloak.example.config.keycloak.rest;

import jakarta.servlet.*;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

@Slf4j
public class KeycloakAuthenticationFilter implements Filter {

    private final IdentityService identityService;
    private final String userNameAttribute;

    public KeycloakAuthenticationFilter(
            IdentityService identityService,
            OAuth2AuthorizedClientService clientService,
            String userNameAttribute) {
        if (identityService == null || clientService == null || !StringUtils.hasLength(userNameAttribute)) {
            throw new IllegalArgumentException("Required dependencies are missing or invalid");
        }
        this.identityService = identityService;
        this.userNameAttribute = userNameAttribute;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            log.warn("No authentication found in security context");
            throw new AccessDeniedException("No authentication token provided");
        }

        String userId = extractUserId(authentication);
        log.debug("Extracted userId: {}", userId);

        if (!StringUtils.hasLength(userId)) {
            log.error("User ID extraction failed");
            throw new AccessDeniedException("Unable to extract user ID from token");
        }

        try {
            identityService.setAuthentication(userId, getUserGroups(userId));
            chain.doFilter(request, response);
        } finally {
            identityService.clearAuthentication();
        }
    }

    private String extractUserId(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getTokenAttributes().get(userNameAttribute).toString();
        } else if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
            return oidcUser.getName();
        } else {
            log.warn("Unsupported authentication type: {}", authentication.getClass().getName());
            throw new AccessDeniedException("Invalid authentication request token");
        }
    }

    private List<String> getUserGroups(String userId) {
        return identityService.createGroupQuery()
                .groupMember(userId)
                .list()
                .stream()
                .map(group -> {
                    log.debug("User [{}] belongs to group [{}]", userId, group.getId());
                    return group.getId();
                })
                .toList();
    }
}