package com.example.demo;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("customCheck")
public class CustomReadinessCheck implements HealthIndicator {

    private boolean databaseConnected = true; // example condition

    @Override
    public Health health() {
        if (databaseConnected) {
            return Health.up().withDetail("database", "Connected").build();
        }
        return Health.down().withDetail("database", "Unreachable").build();
    }
}

