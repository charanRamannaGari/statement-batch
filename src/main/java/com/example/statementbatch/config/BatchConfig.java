package com.example.statementbatch.config;

import com.example.statementbatch.dto.StatementItemDto;
import com.example.statementbatch.exception.DuplicateStatementException;
import com.example.statementbatch.exception.StatementValidationException;
import com.example.statementbatch.listener.JobListener;
import com.example.statementbatch.listener.StepListener;
import com.example.statementbatch.processor.StatementProcessor;
import com.example.statementbatch.writer.StatementWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Main Batch Configuration
 *
 * Features wired here:
 * Feature 10: skip() + skipLimit() → skips invalid records
 * Feature 11: retry() + retryLimit() → retries on transient DB errors
 * Feature 12: chunk(100) → processes 100 records per transaction
 * Feature 14: jobListener
 * Feature 15: stepListener
 * Feature 18: restartability (Spring Batch handles via JobRepository automatically)
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private static final int CHUNK_SIZE   = 100;   // Feature 12
    private static final int SKIP_LIMIT   = 100;   // Feature 10: max records to skip
    private static final int RETRY_LIMIT  = 3;     // Feature 11: retry transient DB errors

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobListener jobListener;
    private final StepListener stepListener;
    private final StatementProcessor statementProcessor;
    private final StatementWriter statementWriter;

    // =============================================
    // JOB DEFINITION
    // Feature 18: Restartability is automatic when
    //             using JobRepository (Spring Batch
    //             tracks completed chunks and skips them on restart)
    // =============================================
    @Bean
    public Job statementBatchJob(Step statementStep) {
        return new JobBuilder("statementBatchJob", jobRepository)
                .listener(jobListener)                  // Feature 14
                .start(statementStep)
                .build();
    }

    // =============================================
    // STEP DEFINITION
    // =============================================
    @Bean
    public Step statementStep(JdbcPagingItemReader<StatementItemDto> processorLogItemReader) {
        return new StepBuilder("statementStep", jobRepository)
                .<StatementItemDto, StatementItemDto>chunk(CHUNK_SIZE, transactionManager) // Feature 12
                .reader(processorLogItemReader)
                .processor(statementProcessor)
                .writer(statementWriter)

                // Feature 10: Skip invalid records instead of stopping the job
                .faultTolerant()
                .skip(StatementValidationException.class)
                .skip(DuplicateStatementException.class)
                .skipLimit(SKIP_LIMIT)

                // Feature 11: Retry on transient DB failures (e.g. connection timeout)
                .retry(TransientDataAccessException.class)
                .retryLimit(RETRY_LIMIT)

                .listener(stepListener)                 // Feature 15
                .build();
    }
}
