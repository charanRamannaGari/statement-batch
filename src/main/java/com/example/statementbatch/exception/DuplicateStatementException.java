package com.example.statementbatch.exception;

// Thrown when a statement already exists for account + cycle
public class DuplicateStatementException extends RuntimeException {

    private final String accountNo;
    private final String statementCycle;

    public DuplicateStatementException(String accountNo, String statementCycle) {
        super(String.format("Statement already exists for account=%s, cycle=%s", accountNo, statementCycle));
        this.accountNo = accountNo;
        this.statementCycle = statementCycle;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public String getStatementCycle() {
        return statementCycle;
    }
}
