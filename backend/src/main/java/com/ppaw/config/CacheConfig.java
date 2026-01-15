package com.ppaw.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
public class CacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        List<Cache> caches = new ArrayList<>();
        
        // Create named caches with logging
        caches.add(new LoggingCache("users"));
        caches.add(new LoggingCache("history"));
        caches.add(new LoggingCache("subscriptions"));
        
        cacheManager.setCaches(caches);
        log.info("Cache Manager initialized with caches: users, history, subscriptions");
        return cacheManager;
    }

    /**
     * Custom Cache wrapper that logs all cache operations
     */
    private static class LoggingCache implements Cache {
        private final Cache delegate;
        private final String name;

        public LoggingCache(String name) {
            this.name = name;
            this.delegate = new ConcurrentMapCache(name);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Object getNativeCache() {
            return delegate.getNativeCache();
        }

        @Override
        public ValueWrapper get(Object key) {
            ValueWrapper value = delegate.get(key);
            if (value != null) {
                log.info("[CACHE HIT] Cache '{}' - Key: {}", name, key);
            } else {
                log.info("[CACHE MISS] Cache '{}' - Key: {} (will fetch from database)", name, key);
            }
            return value;
        }

        @Override
        public <T> T get(Object key, Class<T> type) {
            T value = delegate.get(key, type);
            if (value != null) {
                log.info("[CACHE HIT] Cache '{}' - Key: {}", name, key);
            } else {
                log.info("[CACHE MISS] Cache '{}' - Key: {} (will fetch from database)", name, key);
            }
            return value;
        }

        @Override
        public <T> T get(Object key, java.util.concurrent.Callable<T> valueLoader) {
            T value = delegate.get(key, valueLoader);
            if (value != null) {
                log.info("[CACHE HIT] Cache '{}' - Key: {}", name, key);
            } else {
                log.info("[CACHE MISS] Cache '{}' - Key: {} (will fetch from database)", name, key);
            }
            return value;
        }

        @Override
        public void put(Object key, Object value) {
            delegate.put(key, value);
            log.info("[CACHE PUT] Cache '{}' - Key: {} (stored in memory)", name, key);
        }

        @Override
        public void evict(Object key) {
            delegate.evict(key);
            log.info("[CACHE EVICT] Cache '{}' - Key: {} (removed from cache)", name, key);
        }

        @Override
        public void clear() {
            delegate.clear();
            log.info("[CACHE CLEAR] Cache '{}' (all entries removed)", name);
        }
    }
}
