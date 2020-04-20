package com.mycompany.simpleservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class KeycloakIndicator implements HealthIndicator {

    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;

    private final RestTemplate restTemplate;

    public KeycloakIndicator(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Health health() {
        int errorCode = check();
        if (errorCode != 0) {
            return Health.down().withDetail("Error Code", errorCode).build();
        }
        return Health.up().build();
    }

    private int check() {
        try {
            restTemplate.getForObject(keycloakUrl, String.class);
        } catch (Exception e) {
            log.error("Unable to access Keycloak at {}. {}", keycloakUrl, e.getMessage());
            return 1;
        }
        return 0;
    }

}