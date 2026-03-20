package com.fintech.batch.config;

import com.fintech.batch.batch.AccountItemWriter;
import com.fintech.batch.batch.InterestProcessor;
import com.fintech.batch.entity.Account;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class InterestBatchConfig {

    private static final int CHUNK_SIZE = 1000;

    private final EntityManagerFactory entityManagerFactory;
    private final InterestProcessor interestProcessor;
    private final AccountItemWriter accountItemWriter;
    private final JobCompletionListener listener;

    public InterestBatchConfig(EntityManagerFactory entityManagerFactory,
                               InterestProcessor interestProcessor,
                               AccountItemWriter accountItemWriter,
                               JobCompletionListener listener) {
        this.entityManagerFactory = entityManagerFactory;
        this.interestProcessor = interestProcessor;
        this.accountItemWriter = accountItemWriter;
        this.listener = listener;
    }

    @Bean
    public Job calculateInterestJob(JobRepository jobRepository, Step calculateInterestStep) {
        return new JobBuilder("calculateInterestJob", jobRepository)
                .listener(listener)
                .start(calculateInterestStep)
                .build();
    }

    @Bean
    public Step calculateInterestStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {

        return new StepBuilder("calculateInterestStep", jobRepository)
                .<Account, Account>chunk(CHUNK_SIZE, transactionManager)
                .reader(synchronizedReader())
                .processor(interestProcessor)
                .writer(accountItemWriter)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.initialize();
        return executor;
    }
    @Bean
    public SynchronizedItemStreamReader<Account> synchronizedReader() {
        SynchronizedItemStreamReader<Account> reader = new SynchronizedItemStreamReader<>();
        reader.setDelegate(accountReader());
        return reader;
    }

    @Bean
    public JpaPagingItemReader<Account> accountReader() {
        return new JpaPagingItemReaderBuilder<Account>()
                .name("accountReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT a FROM Account a ORDER BY a.id")
                .pageSize(CHUNK_SIZE)
                .build();
    }
}
