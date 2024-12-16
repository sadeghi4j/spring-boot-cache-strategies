package com.sadeghi.inmemorycachewithredisfallback.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.Priority;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.AnnotationCacheOperationSource;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.annotation.ProxyCachingConfiguration;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Ali Sadeghi
 * Created at 11/2/24 - 9:55 AM
 */
@Configuration
@EnableCaching
public class InMemoryCacheWithRedisFallbackCacheConfig {

    // List of cache names to create dynamically
    private final List<String> cacheNames = List.of("banks");

    @Bean("caffeineCacheManager")
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager(cacheNames.toArray(new String[0]));
        caffeineCacheManager.setCaffeine(Caffeine.newBuilder()
//                .maximumSize(50)
                .expireAfterWrite(1, TimeUnit.MINUTES) // Example TTL, customize as needed
        );
        return caffeineCacheManager;
    }

    @Bean("redisCacheManager")
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .prefixCacheNameWith("ServiceName::")
                .disableCachingNullValues()
                .entryTtl(Duration.ofDays(1)); // Example TTL, customize as needed

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfig)
                .withInitialCacheConfigurations(
                        Map.of(
                                "banks", cacheConfig.entryTtl(Duration.ofMinutes(1))
                        )
                )
                .build();
    }

    @Primary
    @Bean
    public CacheManager cacheManager(@Qualifier("caffeineCacheManager") CacheManager caffeineCacheManager,
                                     @Qualifier("redisCacheManager") CacheManager redisCacheManager) {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        // Create TwoLevelCache instances for each cache name
        List<Cache> caches = cacheNames.stream()
                .map(name -> new TwoLevelCache(name, caffeineCacheManager, redisCacheManager))
                .collect(Collectors.toList());

        cacheManager.setCaches(caches);
        return cacheManager;
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

    @Primary
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

}
