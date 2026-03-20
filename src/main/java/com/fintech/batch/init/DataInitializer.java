package com.fintech.batch.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Order(1)
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final int TOTAL_ACCOUNTS = 100_000;
    private static final Random RANDOM = new Random(42);

    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Initializing {} accounts...", TOTAL_ACCOUNTS);
        long start = System.currentTimeMillis();

        String sql = """
                INSERT INTO account
                (account_number, owner_name, balance, annual_interest_rate,
                 accrued_interest, last_interest_calculated_at, created_at)
                VALUES (?, ?, ?, ?, ?, NULL, ?)
                """;

        List<Object[]> batchArgs = new ArrayList<>(TOTAL_ACCOUNTS);
        LocalDateTime now = LocalDateTime.now();

        for (int i = 1; i <= TOTAL_ACCOUNTS; i++) {
            double rawBalance = 1_000 + RANDOM.nextDouble() * 99_000;
            BigDecimal balance = BigDecimal.valueOf(rawBalance).setScale(2, RoundingMode.HALF_UP);

            double rate = 1.0 + RANDOM.nextDouble() * 4.0;
            rate = Math.round(rate * 100.0) / 100.0;

            batchArgs.add(new Object[]{
                    String.format("ACC-%06d", i),
                    "User_" + i,
                    balance,
                    rate,
                    BigDecimal.ZERO,
                    now
            });
        }

        jdbcTemplate.batchUpdate(sql, batchArgs);

        log.info("{} accounts inserted in {} ms", TOTAL_ACCOUNTS, System.currentTimeMillis() - start);
    }
}
