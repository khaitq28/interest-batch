package com.fintech.batch.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private static final Logger log = LoggerFactory.getLogger(JobController.class);

    private final JobLauncher jobLauncher;
    private final Job calculateInterestJob;

    public JobController(JobLauncher jobLauncher, Job calculateInterestJob) {
        this.jobLauncher = jobLauncher;
        this.calculateInterestJob = calculateInterestJob;
    }

    @PostMapping("/interest/run")
    public ResponseEntity<Map<String, Object>> triggerJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("startAt", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(calculateInterestJob, params);

            return ResponseEntity.ok(Map.of(
                    "jobName", execution.getJobInstance().getJobName(),
                    "executionId", execution.getId(),
                    "status", execution.getStatus().toString(),
                    "startTime", execution.getStartTime().toString()
            ));

        } catch (Exception e) {
            log.error("Failed to trigger job", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
