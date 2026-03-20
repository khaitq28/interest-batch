package com.fintech.batch.batch;

import com.fintech.batch.entity.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Component
public class InterestProcessor implements ItemProcessor<Account, Account> {

    private static final Logger log = LoggerFactory.getLogger(InterestProcessor.class);
    private static final BigDecimal DAYS_IN_YEAR = new BigDecimal("365");

    @Override
    public Account process(Account account) {
        // Daily interest = balance * (annualRate / 100) / 365
        BigDecimal rate = BigDecimal.valueOf(account.getAnnualInterestRate())
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        BigDecimal dailyInterest = account.getBalance()
                .multiply(rate)
                .divide(DAYS_IN_YEAR, 6, RoundingMode.HALF_UP);

        account.setBalance(account.getBalance().add(dailyInterest).setScale(2, RoundingMode.HALF_UP));
        account.setAccruedInterest(account.getAccruedInterest().add(dailyInterest));
        account.setLastInterestCalculatedAt(LocalDateTime.now());

        return account;
    }
}
