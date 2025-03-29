package com.ady4k.todoistapi.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisCacheService<T> implements CacheService<T> {
    private final RedisTemplate<String, T> redisTemplate;

    public RedisCacheService(RedisTemplate<String, T> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void put(String key, T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void put(String key, T value, Duration timeout) {
        redisTemplate.opsForValue().set(key, value, timeout);
    }

    @Override
    public T get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void evict(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void clear() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }
}
