package com.example.idempotency.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "redis")
public record RedisProperties(String password, Master master) {

    public record Master(String host, int port) {
    }
}
