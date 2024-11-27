package com.sadeghi.distributedinmemorycache.reposiroty;

import com.sadeghi.distributedinmemorycache.entity.Bank;
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

    @Cacheable(value = "banks")
    @Override
    List<Bank> findAll();

}