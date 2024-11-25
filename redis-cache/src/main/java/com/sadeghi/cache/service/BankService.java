package com.sadeghi.cache.service;

import com.sadeghi.cache.entity.Bank;
import com.sadeghi.cache.reposiroty.BankRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
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

    final BankRepository bankRepository;

    public Bank findByCode(String code) {
        return bankRepository.findAll()
                .stream()
                .filter(bank -> bank.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Bank not found!"));
    }

}
