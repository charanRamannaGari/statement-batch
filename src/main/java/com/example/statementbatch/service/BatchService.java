package com.example.statementbatch.service;

import com.example.statementbatch.dto.BatchStartResponse;
import com.example.statementbatch.dto.BatchStatusResponse;
import com.example.statementbatch.entity.BatchAudit;
import com.example.statementbatch.repository.BatchAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Feature 19: Service layer to trigger batch job and check status
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchService {

    private final JobLauncher jobLauncher;
    private final Job statementBatchJob;
    private final BatchAuditRepository batchAuditRepository;

    /**
     * Trigger the batch job with a given statementCycle.
     * Uses timestamp to ensure unique JobInstance per trigger.
     */
    public BatchStartResponse startBatch(String statementCycle) {
        log.info("Triggering statementBatchJob for cycle={}", statementCycle);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("statementCycle", statementCycle)
                .addLong("runTimestamp", System.currentTimeMillis()) // ensures unique job instance
                .toJobParameters();

        try {
            JobExecution jobExecution = jobLauncher.run(statementBatchJob, jobParameters);
            log.info("Job started: executionId={}, status={}", jobExecution.getId(), jobExecution.getStatus());

            return BatchStartResponse.builder()
                    .jobId(jobExecution.getId())
                    .status(jobExecution.getStatus().name())
                    .statementCycle(statementCycle)
                    .message("Batch job triggered successfully")
                    .build();

        } catch (JobExecutionAlreadyRunningException e) {
            log.warn("Job already running for cycle={}", statementCycle);
            return BatchStartResponse.builder()
                    .status("ALREADY_RUNNING")
                    .statementCycle(statementCycle)
                    .message("A batch job is already running for this cycle")
                    .build();

        } catch (JobInstanceAlreadyCompleteException e) {
            log.warn("Job already completed for cycle={}", statementCycle);
            return BatchStartResponse.builder()
                    .status("ALREADY_COMPLETED")
                    .statementCycle(statementCycle)
                    .message("Batch already completed for this cycle. Use a new runTimestamp to rerun.")
                    .build();

        } catch (JobRestartException | JobParametersInvalidException e) {
            log.error("Job failed to start for cycle={}: {}", statementCycle, e.getMessage());
            return BatchStartResponse.builder()
                    .status("FAILED_TO_START")
                    .statementCycle(statementCycle)
                    .message("Failed to start job: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Get batch status by job execution ID from audit table
     */
    public BatchStatusResponse getStatus(Long jobExecutionId) {
        return batchAuditRepository.findByJobExecutionId(jobExecutionId)
                .map(this::toStatusResponse)
                .orElse(BatchStatusResponse.builder()
                        .jobExecutionId(jobExecutionId)
                        .status("NOT_FOUND")
                        .build());
    }

    private BatchStatusResponse toStatusResponse(BatchAudit audit) {
        return BatchStatusResponse.builder()
                .jobExecutionId(audit.getJobExecutionId())
                .jobName(audit.getJobName())
                .status(audit.getStatus())
                .statementCycle(audit.getStatementCycle())
                .startTime(audit.getStartTime())
                .endTime(audit.getEndTime())
                .totalRecords(audit.getTotalRecords())
                .successCount(audit.getSuccessCount())
                .failureCount(audit.getFailureCount())
                .skippedCount(audit.getSkippedCount())
                .build();
    }
}
