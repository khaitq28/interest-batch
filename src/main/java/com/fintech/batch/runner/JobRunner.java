package com.fintech.batch.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobRunner {

    private static final Logger log = LoggerFactory.getLogger(JobRunner.class);

    private final JobLauncher jobLauncher;
    private final Job calculateInterestJob;

    public JobRunner(JobLauncher jobLauncher, Job calculateInterestJob) {
        this.jobLauncher = jobLauncher;
        this.calculateInterestJob = calculateInterestJob;
    }

    // -------------------------------------------------------
    // SCHEDULER — change cron here to control when job runs
    //
    // Every night at 2am (production):
    //   @Scheduled(cron = "0 0 2 * * *")
    //
    // Every minute (demo/test):
    //   @Scheduled(fixedDelay = 60_000)
    //
    // Once, 5 seconds after startup (demo):
    //   @Scheduled(initialDelay = 5_000, fixedDelay = Long.MAX_VALUE)
    // -------------------------------------------------------
    @Scheduled(initialDelay = 5_000, fixedDelay = Long.MAX_VALUE)
    public void run() throws Exception {
        log.info("Triggering interest calculation batch job...");

        JobParameters params = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(calculateInterestJob, params);
    }
}
