package com.ady4k.todoistapi.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

import java.io.IOException;

@Configuration
@Profile("debug")
public class EmbeddedRedisConfig {
    private static final Logger log = LoggerFactory.getLogger(EmbeddedRedisConfig.class.getName());

    @Value("${spring.redis.port:6379}") // Use the same port as configured for Redis connection
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() {
        try {
            log.info("Starting embedded Redis server on port {}", redisPort);
            redisServer = RedisServer.newRedisServer()
                    .port(redisPort)
                    .build();
            redisServer.start();
            log.info("Embedded Redis server started successfully.");
        } catch (Exception e) {
            log.error("Failed to start embedded Redis server on port {}: {}", redisPort, e.getMessage());
            throw new RuntimeException("Could not start embedded Redis", e);
        }
    }

    @PreDestroy
    public void stopRedis() throws IOException {
        if (this.redisServer != null && this.redisServer.isActive()) {
            log.info("Stopping embedded Redis server.");
            this.redisServer.stop();
            log.info("Embedded Redis server stopped.");
        }
    }
}
