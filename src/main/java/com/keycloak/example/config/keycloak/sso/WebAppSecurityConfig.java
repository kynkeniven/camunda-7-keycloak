package com.keycloak.example.config.keycloak.sso;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.webapp.impl.security.auth.ContainerBasedAuthenticationFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.filter.ForwardedHeaderFilter;

import java.util.Collections;

import static org.springframework.security.config.Customizer.withDefaults;

@Slf4j
@ConditionalOnMissingClass(WebAppSecurityConfig.ORG_SPRINGFRAMEWORK_TEST_CONTEXT_JUNIT_JUPITER_SPRING_EXTENSION)
@EnableWebSecurity
@Configuration
public class WebAppSecurityConfig {

    public static final int ORDER = 201;
    public static final String AUTHENTICATION_PROVIDER = "authentication-provider";
    public static final String ORG_SPRINGFRAMEWORK_TEST_CONTEXT_JUNIT_JUPITER_SPRING_EXTENSION = "org.springframework.test.context.junit.jupiter.SpringExtension";
    public static final String APP = "/app/";
    public static final String APP_LOGOUT = "/app/**/logout";
    public static final String API = "/api/**";
    public static final String ENGINE_REST = "/engine-rest/**";
    public static final String ASSETS = "/assets/**";
    public static final String LIB = "/lib/**";
    public static final String CAMUNDA_PATH = "/camunda";

    @Inject
    private KeycloakLogoutHandler keycloakLogoutHandler;

    @Bean
    @Order(2)
    @Profile("!local")
    public SecurityFilterChain httpSecurity(HttpSecurity http) throws Exception {
        log.debug("Configuring security filters");

        http.csrf(csrf -> csrf.ignoringRequestMatchers(
                        new AntPathRequestMatcher(CAMUNDA_PATH + API),
                        new AntPathRequestMatcher(CAMUNDA_PATH + ENGINE_REST)
                ))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                new AntPathRequestMatcher(CAMUNDA_PATH + ASSETS),
                                new AntPathRequestMatcher(CAMUNDA_PATH + APP + "**"),
                                new AntPathRequestMatcher(CAMUNDA_PATH + API),
                                new AntPathRequestMatcher(CAMUNDA_PATH + LIB)
                        )
                        .authenticated()
                        .anyRequest()
                        .permitAll()
                )
                .oauth2Login(withDefaults())
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher(CAMUNDA_PATH + APP_LOGOUT))
                        .logoutSuccessHandler(keycloakLogoutHandler)
                );
        return http.build();
    }

    @Bean
    @Profile("local")
    public SecurityFilterChain httpSecurityLocal(HttpSecurity http) throws Exception {
        log.debug("Configuring security filters local profile");

        http.csrf(csrf -> csrf.disable()
                )
                .authorizeHttpRequests(auth -> auth
                        .anyRequest()
                        .permitAll()
                );
        return http.build();
    }

    @Bean
    @Profile("!local")
    public FilterRegistrationBean<ContainerBasedAuthenticationFilter> containerBasedAuthenticationFilter() {
        FilterRegistrationBean<ContainerBasedAuthenticationFilter> filterRegistration = new FilterRegistrationBean<>();
        filterRegistration.setFilter(new ContainerBasedAuthenticationFilter());
        filterRegistration.setInitParameters(
                Collections.singletonMap(AUTHENTICATION_PROVIDER, KeycloakAuthenticationProvider.class.getName()));
        filterRegistration.setOrder(ORDER);
        filterRegistration.addUrlPatterns(CAMUNDA_PATH + APP + "*");
        return filterRegistration;
    }

    @Bean
    @Profile("!local")
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        FilterRegistrationBean<ForwardedHeaderFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new ForwardedHeaderFilter());
        filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return filterRegistrationBean;
    }

    @Bean
    @Order(0)
    @Profile("!local")
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }

    @Bean
    @Profile("!local")
    public HttpFirewall getHttpFirewall() {
        StrictHttpFirewall strictHttpFirewall = new StrictHttpFirewall();
        strictHttpFirewall.setAllowUrlEncodedPercent(true);
        strictHttpFirewall.setAllowUrlEncodedSlash(true);
        return strictHttpFirewall;
    }
}