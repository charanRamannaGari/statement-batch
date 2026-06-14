package com.example.statementbatch.writer;

import com.example.statementbatch.dto.StatementItemDto;
import com.example.statementbatch.entity.Statement;
import com.example.statementbatch.repository.ProcessorLogRepository;
import com.example.statementbatch.repository.StatementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Feature 7: Write into statement table
 * Feature 8: Update msr_processorlog status to COMPLETED
 * Feature 16: Log each write
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatementWriter implements ItemWriter<StatementItemDto> {

    private final StatementRepository statementRepository;
    private final ProcessorLogRepository processorLogRepository;

    @Override
    @Transactional
    public void write(Chunk<? extends StatementItemDto> chunk) throws Exception {
        List<Statement> statements = new ArrayList<>();

        for (StatementItemDto item : chunk.getItems()) {
            if (!item.isValid() || item.getStatementNumber() == null) {
                log.warn("Skipping invalid item in writer: id={}", item.getProcessorLogId());
                continue;
            }

            log.info("Writing statement: id={}, accountNo={}, statementNumber={}",
                    item.getProcessorLogId(), item.getAccountNo(), item.getStatementNumber());

            // Feature 7: Build Statement entity
            Statement statement = Statement.builder()
                    .accountNo(item.getAccountNo())
                    .customerId(item.getCustomerId())
                    .cardNo(item.getCardNo())
                    .statementCycle(item.getStatementCycle())
                    .statementNumber(item.getStatementNumber())
                    .generatedDate(LocalDateTime.now())
                    .build();

            statements.add(statement);
        }

        // Batch insert all statements in one shot
        if (!statements.isEmpty()) {
            statementRepository.saveAll(statements);
            log.info("Saved {} statements in this chunk", statements.size());
        }

        // Feature 8: Update processor log to COMPLETED for each written item
        for (StatementItemDto item : chunk.getItems()) {
            if (item.isValid()) {
                processorLogRepository.updateStatus(item.getProcessorLogId(), "COMPLETED", LocalDateTime.now());
                log.debug("Updated processorlog id={} to COMPLETED", item.getProcessorLogId());
            }
        }
    }
}
