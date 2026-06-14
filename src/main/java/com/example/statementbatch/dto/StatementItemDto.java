package com.example.statementbatch.dto;

import lombok.*;

// DTO passed from Reader → Processor → Writer
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class StatementItemDto {

    private Long processorLogId;
    private String accountNo;
    private String customerId;
    private String cardNo;
    private String statementCycle;

    // Set by Processor after validation
    private String statementNumber;
    private boolean valid;
    private String failureReason;
}
