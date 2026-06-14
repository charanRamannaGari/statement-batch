package com.example.statementbatch.exception;

// Thrown when validation fails for a record
public class StatementValidationException extends RuntimeException {

    private final Long recordId;
    private final String accountNo;

    public StatementValidationException(Long recordId, String accountNo, String message) {
        super(message);
        this.recordId = recordId;
        this.accountNo = accountNo;
    }

    public Long getRecordId() {
        return recordId;
    }

    public String getAccountNo() {
        return accountNo;
    }
}
