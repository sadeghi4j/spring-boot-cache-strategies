package com.sadeghi.distributedinmemorycache.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

/**
 * @author Ali Sadeghi
 * Created at 11/2/24 - 9:55 AM
 */
@Configuration
@EnableCaching
public class DistributedInMemoryCacheConfig {

    // List of cache names to create dynamically
    private final List<String> cacheNames = List.of("banks", "babats", "appConfigs");

    @Bean("caffeineCacheManager")
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager(cacheNames.toArray(new String[0]));
        caffeineCacheManager.setCaffeine(Caffeine.newBuilder()
//                .maximumSize(50)
//                .expireAfterWrite(30, TimeUnit.SECONDS)
        );

        return caffeineCacheManager;
    }

    // Redis Listener for Pub/Sub
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter,
            ChannelTopic topic) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, topic);
        return container;
    }

    // MessageListenerAdapter for handling cache eviction messages
    @Bean
    public MessageListenerAdapter listenerAdapter(CacheMessageSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber);
    }

    // ChannelTopic to specify the topic for cache evictions
    @Bean
    public ChannelTopic topic() {
        return new ChannelTopic("cache:evictions");
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

}
