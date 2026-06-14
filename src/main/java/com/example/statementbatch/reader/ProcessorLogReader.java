package com.example.statementbatch.reader;

import com.example.statementbatch.dto.StatementItemDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.PostgresPagingQueryProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Feature 1: Read NEW records from msr_processorlog using JdbcPagingItemReader
 * Feature 2: Filter by statementCycle job parameter
 *
 * Why PagingReader instead of CursorReader?
 * - CursorReader holds an open DB cursor for the full job duration → resource heavy
 * - PagingReader fetches page-by-page (e.g. 100 rows), releases connection between pages
 * - PagingReader is restartable: on restart it can skip already-processed pages
 * - Safer for large datasets and concurrent environments
 */
@Slf4j
@Configuration
public class ProcessorLogReader {

    private static final int PAGE_SIZE = 100; // Feature 12: chunk size

    @Bean
    @StepScope
    public JdbcPagingItemReader<StatementItemDto> processorLogItemReader(
            DataSource dataSource,
            @Value("#{jobParameters['statementCycle']}") String statementCycle) {

        log.info("Initializing ProcessorLogReader for statementCycle={}", statementCycle);

        // Query provider for PostgreSQL paging
        PostgresPagingQueryProvider queryProvider = new PostgresPagingQueryProvider();
        queryProvider.setSelectClause("SELECT id, account_no, customer_id, card_no, statement_cycle, status");
        queryProvider.setFromClause("FROM msr_processorlog");

        // Feature 2: filter by statementCycle if provided, else read all NEW
        if (statementCycle != null && !statementCycle.isBlank()) {
            queryProvider.setWhereClause("WHERE status = 'NEW' AND statement_cycle = :statementCycle");
        } else {
            queryProvider.setWhereClause("WHERE status = 'NEW'");
        }

        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("id", Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);

        // Parameter map
        Map<String, Object> parameterValues = new HashMap<>();
        if (statementCycle != null && !statementCycle.isBlank()) {
            parameterValues.put("statementCycle", statementCycle);
        }

        JdbcPagingItemReader<StatementItemDto> reader = new JdbcPagingItemReader<>();
        reader.setName("processorLogItemReader");
        reader.setDataSource(dataSource);
        reader.setQueryProvider(queryProvider);
        reader.setParameterValues(parameterValues);
        reader.setPageSize(PAGE_SIZE);
        reader.setRowMapper(new ProcessorLogRowMapper());

        log.info("ProcessorLogReader configured with pageSize={}", PAGE_SIZE);
        return reader;
    }

    // RowMapper: maps ResultSet → StatementItemDto
    static class ProcessorLogRowMapper implements RowMapper<StatementItemDto> {
        @Override
        public StatementItemDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            return StatementItemDto.builder()
                    .processorLogId(rs.getLong("id"))
                    .accountNo(rs.getString("account_no"))
                    .customerId(rs.getString("customer_id"))
                    .cardNo(rs.getString("card_no"))
                    .statementCycle(rs.getString("statement_cycle"))
                    .build();
        }
    }
}
