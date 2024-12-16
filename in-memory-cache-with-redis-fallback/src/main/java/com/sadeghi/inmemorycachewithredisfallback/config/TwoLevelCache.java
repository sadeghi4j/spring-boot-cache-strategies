package com.sadeghi.inmemorycachewithredisfallback.config;

/**
 * @author Ali Sadeghi
 * Created at 11/5/24 - 1:35 PM
 */

import lombok.extern.log4j.Log4j2;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.concurrent.Callable;

@Log4j2
public class TwoLevelCache implements Cache {

    private final String name;
    private final Cache caffeineCache;
    private final Cache redisCache;

    public TwoLevelCache(String name, CacheManager caffeineCacheManager, CacheManager redisCacheManager) {
        this.name = name;
        this.caffeineCache = caffeineCacheManager.getCache(name);  // L1 cache
        this.redisCache = redisCacheManager.getCache(name);        // L2 cache
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Object getNativeCache() {
        return this;
    }

    @Override
    public ValueWrapper get(Object key) {
        // First check in-memory (L1) cache
        ValueWrapper value = caffeineCache.get(key);
        if (value != null) {
            return value;
        }

        // If not in L1, check Redis (L2)
        value = redisCache.get(key);
        if (value != null) {
            // Update L1 cache to improve future access speed
            caffeineCache.put(key, value.get());
            return value;
        }

        // If not found in either cache, return null to fall back to DB or other source
        return null;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        ValueWrapper valueWrapper = this.get(key);
        if (valueWrapper != null && type.isInstance(valueWrapper.get())) {
            return type.cast(valueWrapper.get());
        }
        return null;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        try {
            ValueWrapper valueWrapper = this.get(key);
            if (valueWrapper != null) {
                return (T) valueWrapper.get();
            }

            // If not found in caches, load using valueLoader (e.g., fetch from DB)
            T value = valueLoader.call();

            // Store in both L1 and L2 caches
            this.put(key, value);

            return value;
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }
    }

    @Override
    public void put(Object key, Object value) {
        caffeineCache.put(key, value);
        redisCache.put(key, value);
    }

    @Override
    public void evict(Object key) {
        log.info("TwoLevelCache evict called for key: {}", key);
        caffeineCache.evict(key);
        redisCache.evict(key);
    }

    @Override
    public void clear() {
        log.info("TwoLevelCache clear called");
        caffeineCache.clear();
        redisCache.clear();
    }
}
