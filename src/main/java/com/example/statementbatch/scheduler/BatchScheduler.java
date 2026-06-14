package com.example.statementbatch.scheduler;

import com.example.statementbatch.service.BatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Feature 20: Automatically trigger batch job on schedule.
 *
 * Cron: "0 0 2 1 * ?" = 2:00 AM on the 1st day of every month
 *
 * Format: second minute hour dayOfMonth month dayOfWeek
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final BatchService batchService;

    /**
     * Runs at 02:00 AM on the 1st of every month.
     * Automatically derives the current month's statement cycle (YYYYMM).
     *
     * To test immediately, change cron to: "0 * * * * ?" (every minute)
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    public void runStatementBatch() {
        // Derive cycle as YYYYMM for the current month
        String statementCycle = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyyMM"));

        log.info("Scheduler triggered: Running statement batch for cycle={}", statementCycle);

        try {
            var response = batchService.startBatch(statementCycle);
            log.info("Scheduler: Batch started jobId={}, status={}", response.getJobId(), response.getStatus());
        } catch (Exception e) {
            log.error("Scheduler: Failed to start batch for cycle={}: {}", statementCycle, e.getMessage(), e);
        }
    }
}
