package com.ady4k.todoistapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
public class CacheConfig {
    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    @Bean
    @Profile("debug")
    public RedisConnectionFactory debugRedisConnectionFactory(
            @Value("${spring.redis.host:localhost}") String redisHost,
            @Value("${spring.redis.port:6379}") int redisPort) {
        log.warn("--- Creating DEBUG Redis Connection Factory (Connecting to Embedded at {}:{}) ---", redisHost, redisPort);
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    @Profile("dev")
    public RedisConnectionFactory devRedisConnectionFactory(
            @Value("${spring.redis.host}") String redisHost,
            @Value("${spring.redis.port:6379}") int redisPort,
            @Value("${spring.redis.password:#{null}}") String redisPassword) {
        log.info("--- Creating DEV Redis Connection Factory (Connecting to Server at {}:{}) ---", redisHost, redisPort);
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.setPassword(redisPassword);
        }
        return new LettuceConnectionFactory(config);
    }
}
