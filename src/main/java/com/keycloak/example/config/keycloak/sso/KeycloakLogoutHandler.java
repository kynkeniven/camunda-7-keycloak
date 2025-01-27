package com.keycloak.example.config.keycloak.sso;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class KeycloakLogoutHandler implements LogoutSuccessHandler {

    public static final String OPENID_CONNECT_AUTH = "openid-connect/auth";
    public static final String OPENID_CONNECT_LOGOUT = "openid-connect/logout";
    public static final String APP = "/app";
    public static final String POST_LOGOUT_REDIRECT_URI_S_ID_TOKEN_HINT_S = "%s?post_logout_redirect_uri=%s&id_token_hint=%s";
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    private final String oauth2UserLogoutUri;

    public KeycloakLogoutHandler(
            @Value("${spring.security.oauth2.client.provider.keycloak.authorization-uri:}") String oauth2UserAuthorizationUri) {
        this.oauth2UserLogoutUri = StringUtils.hasText(oauth2UserAuthorizationUri) ?
                oauth2UserAuthorizationUri.replace(OPENID_CONNECT_AUTH, OPENID_CONNECT_LOGOUT) : null;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {

        if (oauth2UserLogoutUri == null) {
            log.warn("Logout URI is not configured. Redirecting to home.");
            redirectStrategy.sendRedirect(request, response, "/");
            return;
        }

        if (authentication == null || !(authentication.getPrincipal() instanceof OidcUser oidcUser)) {
            log.warn("No valid authentication found for logout.");
            redirectStrategy.sendRedirect(request, response, "/");
            return;
        }

        String idToken = oidcUser.getIdToken().getTokenValue();

        if (!StringUtils.hasText(idToken)) {
            log.warn("ID Token is missing for logout process.");
            redirectStrategy.sendRedirect(request, response, "/");
            return;
        }

        String requestUrl = request.getRequestURL().toString();
        String baseUrl = requestUrl.substring(0, requestUrl.indexOf(APP));
        String encodedRedirectUri = URLEncoder.encode(baseUrl, StandardCharsets.UTF_8);

        String logoutUrl = String.format(POST_LOGOUT_REDIRECT_URI_S_ID_TOKEN_HINT_S,
                oauth2UserLogoutUri, encodedRedirectUri, idToken);

        log.info("Redirecting user to Keycloak logout URL: {}", logoutUrl);
        redirectStrategy.sendRedirect(request, response, logoutUrl);
    }
}