package com.mycompany.simpleservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class KeycloakIndicator implements HealthIndicator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${keycloak.auth-server-url}")
    private String KEYCLOAK_URL;

    @Autowired
    private RestTemplate restTemplate;

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
            restTemplate.getForObject(KEYCLOAK_URL, String.class);
        } catch (Exception e) {
            logger.error("Unable to access Keycloak. {}", e.getMessage());
            return 1;
        }
        return 0;
    }

}