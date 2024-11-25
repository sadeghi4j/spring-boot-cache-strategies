package com.sadeghi.cache;

import com.sadeghi.cache.entity.Bank;
import com.sadeghi.cache.reposiroty.BankRepository;
import com.sadeghi.cache.service.BankService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.mockito.Mockito.*;

@SpringBootTest
class RedisCacheApplicationTests {

    static {
        GenericContainer<?> redis =
                new GenericContainer<>(DockerImageName.parse("redis:7.2.6")).withExposedPorts(6379);
        redis.start();
        System.setProperty("spring.data.redis.host", redis.getHost());
        System.setProperty("spring.data.redis.port", redis.getMappedPort(6379).toString());
    }

    @MockitoSpyBean
    private BankRepository bankRepository;
    @Autowired
    private BankService bankService;
    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void init() {
        bankRepository.deleteAll();
        bankRepository.save(new Bank(1L, "BankName", "1"));

        // ensures that the cache is reset before each test.
        // This isolates the tests and avoids any unwanted interactions caused by residual cache data from previous tests.
        cacheManager.getCache("banks").clear();

        // clears any recorded interactions with bankRepository
        Mockito.reset(bankRepository);
    }

    @Test
    void testCachingForBank() {
        List<Bank> banks = bankRepository.findAll();
        Assertions.assertFalse(banks.isEmpty());

        List<Bank> sameBank = bankRepository.findAll();
        Assertions.assertFalse(sameBank.isEmpty());

        verify(bankRepository, times(1)).findAll();
    }

    @Test
    void testBankCaching() {
        Bank bank = bankService.findByCode("1");
        Assertions.assertNotNull(bank);

        Bank sameBank = bankService.findByCode("1");
        Assertions.assertNotNull(sameBank);

        verify(bankRepository, times(1)).findAll();
    }

}
