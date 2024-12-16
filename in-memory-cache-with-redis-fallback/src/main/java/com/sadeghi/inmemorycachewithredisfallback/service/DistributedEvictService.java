package com.sadeghi.inmemorycachewithredisfallback.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

/**
 * @author Ali Sadeghi
 * Created at 12/1/24 - 9:18 AM
 */

@Log4j2
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Service
public class DistributedEvictService {

    final RedisTemplate<String, String> redisTemplate;
    final ChannelTopic topic;
    final CacheManager redisCacheManager;
    /**
     * This method does not directly evict local cache, Instead it send a pub/sub message to Redis to notify all subscribers.
     * Then It can be seen that the cache is cleared in `CacheMessageSubscriber.onMessage()`
     */
    public void evictCache() {
        redisTemplate.convertAndSend(topic.getTopic(), "banks"); // Notify other instances
        redisCacheManager.getCache("banks").clear();
    }

}
