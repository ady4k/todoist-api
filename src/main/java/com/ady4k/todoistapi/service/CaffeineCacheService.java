package com.ady4k.todoistapi.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CaffeineCacheService<T> implements CacheService<T> {
    private final Cache<String, T> inMemoryCache;

    public CaffeineCacheService() {
        this.inMemoryCache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(10000)
                .build();
    }

    @Override
    public void put(String key, T value) {
        inMemoryCache.put(key, value);
    }

    @Override
    public T get(String key) {
        return inMemoryCache.getIfPresent(key);
    }

    @Override
    public void evict(String key) {
        inMemoryCache.invalidate(key);
    }

    @Override
    public void clear() {
        inMemoryCache.invalidateAll();
    }
}
