package com.example.statementbatch.listener;

import com.example.statementbatch.entity.BatchAudit;
import com.example.statementbatch.repository.BatchAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Feature 14: Log before/after job execution
 * Feature 13: Write to batch_audit table
 * Feature 17: Print batch summary after completion
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobListener implements JobExecutionListener {

    private final BatchAuditRepository batchAuditRepository;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("===================================================");
        log.info("  Statement Batch STARTED");
        log.info("  Job Name     : {}", jobExecution.getJobInstance().getJobName());
        log.info("  Execution ID : {}", jobExecution.getId());
        log.info("  Parameters   : {}", jobExecution.getJobParameters());
        log.info("===================================================");

        // Feature 13: Save initial audit record
        String cycle = jobExecution.getJobParameters().getString("statementCycle");
        BatchAudit audit = BatchAudit.builder()
                .jobName(jobExecution.getJobInstance().getJobName())
                .jobExecutionId(jobExecution.getId())
                .statementCycle(cycle)
                .startTime(LocalDateTime.now())
                .status("STARTED")
                .totalRecords(0)
                .successCount(0)
                .failureCount(0)
                .skippedCount(0)
                .createdTime(LocalDateTime.now())
                .build();
        batchAuditRepository.save(audit);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        // Collect counts from step executions
        int totalRead = 0, totalWrite = 0, totalSkip = 0;
        for (var stepExecution : jobExecution.getStepExecutions()) {
            totalRead  += stepExecution.getReadCount();
            totalWrite += stepExecution.getWriteCount();
            totalSkip  += stepExecution.getProcessSkipCount() + stepExecution.getWriteSkipCount();
        }

        // Make effectively final for lambda use
        final int readCount     = totalRead;
        final int writeCount    = totalWrite;
        final int skipCount     = totalSkip;
        final int failureCount  = Math.max(0, readCount - writeCount - skipCount);
        final long durationSec  = ChronoUnit.SECONDS.between(
                jobExecution.getStartTime(), LocalDateTime.now());

        // Feature 17: Batch Summary
        log.info("");
        log.info("==================================");
        log.info("  Statement Batch Summary");
        log.info("==================================");
        log.info("  Input Records  : {}", readCount);
        log.info("  Success        : {}", writeCount);
        log.info("  Failed         : {}", failureCount);
        log.info("  Skipped        : {}", skipCount);
        log.info("  Execution Time : {} sec", durationSec);
        log.info("  Job Status     : {}", jobExecution.getStatus());
        log.info("==================================");

        // Feature 13: Update audit record
        batchAuditRepository.findByJobExecutionId(jobExecution.getId()).ifPresent(audit -> {
            audit.setEndTime(LocalDateTime.now());
            audit.setStatus(jobExecution.getStatus().name());
            audit.setTotalRecords(readCount);
            audit.setSuccessCount(writeCount);
            audit.setFailureCount(failureCount);
            audit.setSkippedCount(skipCount);
            batchAuditRepository.save(audit);
        });
    }
}
