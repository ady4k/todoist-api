package com.ady4k.todoistapi.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class CacheService<K, V> {
    private final Cache<K, V> inMemoryCache;
    private final RedisTemplate<K, V> redisTemplate;

    public CacheService(RedisTemplate<K, V> redisTemplate) {
        this.inMemoryCache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(10000)
                .build();
        this.redisTemplate = redisTemplate;
    }

    public void addToCache(K key, V value, Duration ttl) {
        inMemoryCache.put(key, value);
        redisTemplate.opsForValue().setIfAbsent(key, value, ttl.toMillis(), TimeUnit.MILLISECONDS);
    }

    public V getFromCache(K key) {
        V value = inMemoryCache.getIfPresent(key);
        if (value == null) {
            value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            inMemoryCache.put(key, value);
        }
        return value;
    }

    public void removeFromCache(K key) {
        inMemoryCache.invalidate(key);
        redisTemplate.delete(key);
    }
}
