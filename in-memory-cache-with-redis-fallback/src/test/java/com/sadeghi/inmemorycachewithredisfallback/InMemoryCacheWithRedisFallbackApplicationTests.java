package com.sadeghi.inmemorycachewithredisfallback;

import com.sadeghi.inmemorycachewithredisfallback.entity.Bank;
import com.sadeghi.inmemorycachewithredisfallback.reposiroty.BankRepository;
import com.sadeghi.inmemorycachewithredisfallback.service.BankService;
import com.sadeghi.inmemorycachewithredisfallback.service.DistributedEvictService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

//@Import(TestcontainersConfiguration.class)
@SpringBootTest
class InMemoryCacheWithRedisFallbackApplicationTests {

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
	private DistributedEvictService distributedEvictService;
	@Autowired
	private CaffeineCacheManager caffeineCacheManager;
	@Autowired
	private RedisCacheManager redisCacheManager;

	@BeforeEach
	void init() {
		bankRepository.deleteAll();
		bankRepository.save(new Bank(1L, "BankName", "1"));

		// ensures that the cache is reset before each test.
		// This isolates the tests and avoids any unwanted interactions caused by residual cache data from previous tests.
		caffeineCacheManager.getCache("banks").clear();
		redisCacheManager.getCache("banks").clear();

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

	@SneakyThrows
	@Test
	void testEvictCache() {
		Bank bank = bankService.findByCode("1");
		Assertions.assertNotNull(bank);


		Cache.ValueWrapper banksValueWrapper = caffeineCacheManager.getCache("banks").get(SimpleKey.EMPTY);
		Assertions.assertNotNull(banksValueWrapper);
		List<Bank> banks = (List<Bank>) banksValueWrapper.get();
		Assertions.assertNotNull(banks);
		Assertions.assertEquals(1, banks.size());

		// evict cache by sending message
//		bankService.evictCache();
		distributedEvictService.evictCache();

		// We wait for a pub/sub message to receive and then clear local cache as you can see in `CacheMessageSubscriber.onMessage()`
		TimeUnit.SECONDS.sleep(1);

		// Here we expect that the local in memory cache is cleared.
		banksValueWrapper = caffeineCacheManager.getCache("banks").get(SimpleKey.EMPTY);
		Assertions.assertNull(banksValueWrapper);

		// Here we expect that the Redis cache is cleared.
		banksValueWrapper = redisCacheManager.getCache("banks").get(SimpleKey.EMPTY);
		Assertions.assertNull(banksValueWrapper);
	}

}
