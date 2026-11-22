package com.example.hospitalmanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hospital.seed.admin")
public record AdminSeedProperties(boolean enabled, String username, String password, String name) {
}
