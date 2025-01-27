package com.keycloak.example.config.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

@Configuration
public class ObjectMapperConfig {

    @Bean
    @Primary
    public ObjectMapper customObjectMapper() {
        return createObjectMapper();
    }

    public static ObjectMapper createObjectMapper() {
        return new ObjectMapper()
                .setSerializationInclusion(NON_NULL)
                .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule())
                .registerModule(new Jdk8Module())
                .registerModule(new SimpleModule())
                .disable(WRITE_DATES_AS_TIMESTAMPS);
    }
}
