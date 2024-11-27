package com.sadeghi.distributedinmemorycache.service;

import com.sadeghi.distributedinmemorycache.entity.Bank;
import com.sadeghi.distributedinmemorycache.reposiroty.BankRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

/**
 * @author Ali Sadeghi
 * Created at 11/24/24 - 10:22 AM
 */

@Log4j2
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Service
public class BankService {

    final RedisTemplate<String, String> redisTemplate;
    private final ChannelTopic topic;

    final BankRepository bankRepository;

    public Bank findByCode(String code) {
        return bankRepository.findAll()
                .stream()
                .filter(bank -> bank.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Bank not found!"));
    }

    /**
     * This method does not directly evict local cache, Instead it send a pub/sub message to Redis to notify all subscribers.
     * Then It can be seen that the cache is cleared in `CacheMessageSubscriber.onMessage()`
     */
    public void evictCache() {
        redisTemplate.convertAndSend(topic.getTopic(), "banks"); // Notify other instances
    }

}
