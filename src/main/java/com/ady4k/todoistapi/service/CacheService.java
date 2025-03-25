package com.ady4k.todoistapi.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class CacheService<T> {
    private final Cache<String, T> inMemoryCache;
    private final RedisTemplate<String, T> redisTemplate;

    public CacheService(RedisTemplate<String, T> redisTemplate) {
        this.inMemoryCache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(10000)
                .build();
        this.redisTemplate = redisTemplate;
    }

    public void addToCache(String key, T value, Duration ttl) {
        inMemoryCache.put(key, value);
        redisTemplate.opsForValue().set(key, value, ttl.toMillis(), TimeUnit.MILLISECONDS);
    }

    public T getFromCache(String key) {
        T value = inMemoryCache.getIfPresent(key);
        return (value != null) ? redisTemplate.opsForValue().get(key) : null;
    }

    public void removeFromCache(String key) {
        inMemoryCache.invalidate(key);
        redisTemplate.delete(key);
    }
}
