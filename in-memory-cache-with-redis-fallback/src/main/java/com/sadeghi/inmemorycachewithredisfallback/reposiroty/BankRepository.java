package com.sadeghi.inmemorycachewithredisfallback.reposiroty;

import com.sadeghi.inmemorycachewithredisfallback.entity.Bank;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Ali Sadeghi
 * Created at 11/24/24 - 10:20 AM
 */

@Repository
public interface BankRepository extends JpaRepository<Bank, Long> {

    @Cacheable("banks")
//    @Caching(cacheable = {
//            @Cacheable(cacheNames = "banks", cacheManager = "caffeineCacheManager"),
//            @Cacheable(cacheNames = "banks", cacheManager = "redisCacheManager")
//    })
    @Override
    List<Bank> findAll();

}