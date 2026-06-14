package com.example.statementbatch;

import com.example.statementbatch.dto.StatementItemDto;
import com.example.statementbatch.entity.BatchError;
import com.example.statementbatch.processor.StatementProcessor;
import com.example.statementbatch.repository.BatchErrorRepository;
import com.example.statementbatch.repository.ProcessorLogRepository;
import com.example.statementbatch.repository.StatementRepository;
import com.example.statementbatch.util.StatementNumberUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatementProcessorTest {

    @Mock ProcessorLogRepository processorLogRepository;
    @Mock StatementRepository statementRepository;
    @Mock BatchErrorRepository batchErrorRepository;
    @Mock StatementNumberUtil statementNumberUtil;

    @InjectMocks
    StatementProcessor processor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldProcessValidRecord() throws Exception {
        // given
        StatementItemDto item = buildValidItem();
        when(statementRepository.countByAccountNoAndStatementCycle("ACC001", "202606")).thenReturn(0L);
        when(statementNumberUtil.generate("202606")).thenReturn("ST202606000001");

        // when
        StatementItemDto result = processor.process(item);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getStatementNumber()).isEqualTo("ST202606000001");
    }

    @Test
    void shouldReturnNullForDuplicateRecord() throws Exception {
        // given
        StatementItemDto item = buildValidItem();
        when(statementRepository.countByAccountNoAndStatementCycle("ACC001", "202606")).thenReturn(1L);

        // when
        StatementItemDto result = processor.process(item);

        // then
        assertThat(result).isNull(); // Feature 10: null = skip
        verify(batchErrorRepository).save(any(BatchError.class));
    }

    @Test
    void shouldReturnNullForMissingAccountNo() throws Exception {
        // given
        StatementItemDto item = buildValidItem();
        item.setAccountNo(null);

        // when
        StatementItemDto result = processor.process(item);

        // then
        assertThat(result).isNull();
    }

    @Test
    void shouldReturnNullForInvalidCycleFormat() throws Exception {
        // given
        StatementItemDto item = buildValidItem();
        item.setStatementCycle("20266"); // wrong format

        // when
        StatementItemDto result = processor.process(item);

        // then
        assertThat(result).isNull();
    }

    private StatementItemDto buildValidItem() {
        return StatementItemDto.builder()
                .processorLogId(1L)
                .accountNo("ACC001")
                .customerId("CUST001")
                .cardNo("CARD001")
                .statementCycle("202606")
                .build();
    }
}
