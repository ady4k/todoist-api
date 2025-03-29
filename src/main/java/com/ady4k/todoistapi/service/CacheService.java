package com.ady4k.todoistapi.service;

public interface CacheService<T> {
    void put(String key, T value);
    T get(String key);
    void evict(String key);
    void clear();
}
