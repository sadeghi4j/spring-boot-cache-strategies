package com.sadeghi.distributedinmemorycache.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author Ali Sadeghi
 * Created at 11/2/24 - 11:14 AM
 */
@Log4j2
@Service
public class CacheMessageSubscriber implements MessageListener {

    private final CacheManager caffeineCacheManager;

    public CacheMessageSubscriber(CacheManager caffeineCacheManager) {
        this.caffeineCacheManager = caffeineCacheManager;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String cacheName = message.toString(); // Extract cache name from message
        log.info("Cache clear message received for: {} ", message);
        Objects.requireNonNull(caffeineCacheManager.getCache(cacheName)).clear();
    }

}
