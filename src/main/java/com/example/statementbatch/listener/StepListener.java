package com.example.statementbatch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

/**
 * Feature 15: Log read/processed/written counts per step
 * Feature 16: Detailed logging per step lifecycle
 */
@Slf4j
@Component
public class StepListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("---------------------------------------------------");
        log.info("  Step STARTED: {}", stepExecution.getStepName());
        log.info("  Job Execution ID: {}", stepExecution.getJobExecutionId());
        log.info("---------------------------------------------------");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("---------------------------------------------------");
        log.info("  Step COMPLETED: {}", stepExecution.getStepName());
        log.info("  Read Count     : {}", stepExecution.getReadCount());
        log.info("  Process Skip   : {}", stepExecution.getProcessSkipCount());
        log.info("  Write Count    : {}", stepExecution.getWriteCount());
        log.info("  Write Skip     : {}", stepExecution.getWriteSkipCount());
        log.info("  Commit Count   : {}", stepExecution.getCommitCount());
        log.info("  Rollback Count : {}", stepExecution.getRollbackCount());
        log.info("  Exit Status    : {}", stepExecution.getExitStatus().getExitCode());
        log.info("---------------------------------------------------");
        return stepExecution.getExitStatus();
    }
}
