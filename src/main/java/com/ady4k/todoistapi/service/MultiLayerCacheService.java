package com.ady4k.todoistapi.service;

import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class MultiLayerCacheService<T> implements CacheService<T> {
    private final CaffeineCacheService<T> caffeineCacheService;
    private final RedisCacheService<T> redisCacheService;

    public MultiLayerCacheService(CaffeineCacheService<T> caffeineCacheService, RedisCacheService<T> redisCacheService) {
        this.caffeineCacheService = caffeineCacheService;
        this.redisCacheService = redisCacheService;
    }

    @Override
    public void put(String key, T value) {
        caffeineCacheService.put(key, value);
        redisCacheService.put(key, value);
    }

    public void put(String key, T value, Duration ttl) {
        caffeineCacheService.put(key, value);
        redisCacheService.put(key, value, ttl);
    }

    @Override
    public T get(String key) {
        T value = caffeineCacheService.get(key);
        if (value == null) {
            value = redisCacheService.get(key);
            if (value == null) {
                return null;
            }
            caffeineCacheService.put(key, value);
        }
        return value;
    }

    @Override
    public void evict(String key) {
        caffeineCacheService.evict(key);
        redisCacheService.evict(key);
    }

    @Override
    public void clear() {
        caffeineCacheService.clear();
        redisCacheService.clear();
    }
}
