package com.example.statementbatch.processor;

import com.example.statementbatch.dto.StatementItemDto;
import com.example.statementbatch.exception.DuplicateStatementException;
import com.example.statementbatch.exception.StatementValidationException;
import com.example.statementbatch.repository.BatchErrorRepository;
import com.example.statementbatch.repository.ProcessorLogRepository;
import com.example.statementbatch.repository.StatementRepository;
import com.example.statementbatch.entity.BatchError;
import com.example.statementbatch.util.StatementNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

/**
 * Feature 3: Validate record (customer, card, account, cycle)
 * Feature 4: Duplicate statement check
 * Feature 5: Status transition NEW → IN_PROGRESS
 * Feature 6: Generate statement number
 * Feature 9: Save error to batch_error on failure
 * Feature 10: Return null to skip invalid records (Spring Batch skips null returns)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatementProcessor implements ItemProcessor<StatementItemDto, StatementItemDto> {

    private final StatementRepository statementRepository;
    private final ProcessorLogRepository processorLogRepository;
    private final BatchErrorRepository batchErrorRepository;
    private final StatementNumberUtil statementNumberUtil;

    @Override
    @Transactional
    public StatementItemDto process(StatementItemDto item) throws Exception {
        log.debug("Processing id={}, accountNo={}", item.getProcessorLogId(), item.getAccountNo());

        try {
            // Feature 5: Mark as IN_PROGRESS
            processorLogRepository.updateStatus(item.getProcessorLogId(), "IN_PROGRESS", LocalDateTime.now());

            // Feature 3: Validate record
            validate(item);

            // Feature 4: Duplicate statement check
            checkDuplicate(item);

            // Feature 6: Generate statement number
            String statementNumber = statementNumberUtil.generate(item.getStatementCycle());
            item.setStatementNumber(statementNumber);
            item.setValid(true);

            log.info("Statement generated: id={}, statementNumber={}", item.getProcessorLogId(), statementNumber);
            return item;

        } catch (StatementValidationException | DuplicateStatementException ex) {
            log.warn("Skipping record id={}: {}", item.getProcessorLogId(), ex.getMessage());

            // Feature 9: Store error
            saveError(item, ex.getMessage(), ex);

            // Feature 5: Mark FAILED
            processorLogRepository.updateStatus(item.getProcessorLogId(), "FAILED", LocalDateTime.now());

            // Feature 10: Return null → Spring Batch skips this item
            return null;
        }
    }

    // Feature 3: Validation logic
    private void validate(StatementItemDto item) {
        if (item.getAccountNo() == null || item.getAccountNo().isBlank()) {
            throw new StatementValidationException(item.getProcessorLogId(), item.getAccountNo(),
                    "Account number is missing");
        }
        if (item.getCustomerId() == null || item.getCustomerId().isBlank()) {
            throw new StatementValidationException(item.getProcessorLogId(), item.getAccountNo(),
                    "Customer ID is missing");
        }
        if (item.getCardNo() == null || item.getCardNo().isBlank()) {
            throw new StatementValidationException(item.getProcessorLogId(), item.getAccountNo(),
                    "Card number is missing");
        }
        if (item.getStatementCycle() == null || item.getStatementCycle().isBlank()) {
            throw new StatementValidationException(item.getProcessorLogId(), item.getAccountNo(),
                    "Statement cycle is missing");
        }
        if (!item.getStatementCycle().matches("\\d{6}")) {
            throw new StatementValidationException(item.getProcessorLogId(), item.getAccountNo(),
                    "Statement cycle format invalid, expected YYYYMM: " + item.getStatementCycle());
        }
    }

    // Feature 4: Duplicate check
    private void checkDuplicate(StatementItemDto item) {
        long count = statementRepository.countByAccountNoAndStatementCycle(
                item.getAccountNo(), item.getStatementCycle());
        if (count > 0) {
            throw new DuplicateStatementException(item.getAccountNo(), item.getStatementCycle());
        }
    }

    // Feature 9: Save to batch_error
    private void saveError(StatementItemDto item, String reason, Exception ex) {
        String stackTrace = getStackTrace(ex);
        BatchError error = BatchError.builder()
                .recordId(item.getProcessorLogId())
                .accountNo(item.getAccountNo())
                .reason(reason)
                .stackTrace(stackTrace)
                .createdTime(LocalDateTime.now())
                .build();
        batchErrorRepository.save(error);
        log.debug("Error saved to batch_error for id={}", item.getProcessorLogId());
    }

    private String getStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
