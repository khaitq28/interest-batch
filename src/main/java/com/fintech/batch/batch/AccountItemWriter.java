package com.fintech.batch.batch;

import com.fintech.batch.entity.Account;
import com.fintech.batch.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class AccountItemWriter implements ItemWriter<Account> {

    private static final Logger log = LoggerFactory.getLogger(AccountItemWriter.class);
    private final AccountRepository accountRepository;

    public AccountItemWriter(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void write(Chunk<? extends Account> chunk) {
        accountRepository.saveAll(chunk.getItems());
        log.info("Saved chunk of {} accounts", chunk.getItems().size());
    }
}
