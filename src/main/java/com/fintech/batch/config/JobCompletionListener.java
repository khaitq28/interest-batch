package com.fintech.batch.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class JobCompletionListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(JobCompletionListener.class);

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("====================================================");
        log.info(" JOB STARTED: {}", jobExecution.getJobInstance().getJobName());
        log.info("====================================================");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        Duration duration = Duration.between(
                jobExecution.getStartTime(),
                jobExecution.getEndTime()
        );
        log.info("====================================================");
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info(" JOB COMPLETED SUCCESSFULLY");
            log.info(" Duration     : {} ms", duration.toMillis());
            log.info(" Read count   : {}", jobExecution.getStepExecutions()
                    .stream().mapToLong(s -> s.getReadCount()).sum());
            log.info(" Write count  : {}", jobExecution.getStepExecutions()
                    .stream().mapToLong(s -> s.getWriteCount()).sum());
        } else {
            log.error(" JOB FAILED with status: {}", jobExecution.getStatus());
        }
        log.info("====================================================");
    }
}
