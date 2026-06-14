-- ================================================
-- Credit Card Statement Batch - Database Schema
-- PostgreSQL
-- ================================================

-- 1. Processor Log Table (Input source for batch)
CREATE TABLE IF NOT EXISTS msr_processorlog (
    id               BIGSERIAL PRIMARY KEY,
    account_no       VARCHAR(20)  NOT NULL,
    customer_id      VARCHAR(20)  NOT NULL,
    card_no          VARCHAR(20)  NOT NULL,
    statement_cycle  VARCHAR(6)   NOT NULL,   -- e.g. 202606
    status           VARCHAR(20)  NOT NULL DEFAULT 'NEW',
    created_time     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_time     TIMESTAMP,
    CONSTRAINT uq_account_cycle UNIQUE (account_no, statement_cycle)
);

-- 2. Statement Table (Output of batch)
CREATE TABLE IF NOT EXISTS statement (
    id               BIGSERIAL PRIMARY KEY,
    account_no       VARCHAR(20)  NOT NULL,
    customer_id      VARCHAR(20)  NOT NULL,
    card_no          VARCHAR(20)  NOT NULL,
    statement_cycle  VARCHAR(6)   NOT NULL,
    statement_number VARCHAR(20)  NOT NULL UNIQUE,
    generated_date   TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- 3. Batch Error Table (Feature 9 - Error Handling)
CREATE TABLE IF NOT EXISTS batch_error (
    id               BIGSERIAL PRIMARY KEY,
    record_id        BIGINT       NOT NULL,
    account_no       VARCHAR(20),
    reason           TEXT         NOT NULL,
    stack_trace      TEXT,
    created_time     TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- 4. Batch Audit Table (Feature 13 - Job Execution Audit)
CREATE TABLE IF NOT EXISTS batch_audit (
    id               BIGSERIAL PRIMARY KEY,
    job_name         VARCHAR(100) NOT NULL,
    job_execution_id BIGINT,
    statement_cycle  VARCHAR(6),
    start_time       TIMESTAMP,
    end_time         TIMESTAMP,
    status           VARCHAR(20),
    total_records    INT          DEFAULT 0,
    success_count    INT          DEFAULT 0,
    failure_count    INT          DEFAULT 0,
    skipped_count    INT          DEFAULT 0,
    created_time     TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ================================================
-- Sample Data for Testing
-- ================================================
INSERT INTO msr_processorlog (account_no, customer_id, card_no, statement_cycle, status)
VALUES
    ('ACC001', 'CUST001', 'CARD001', '202606', 'NEW'),
    ('ACC002', 'CUST002', 'CARD002', '202606', 'NEW'),
    ('ACC003', 'CUST003', 'CARD003', '202606', 'NEW'),
    ('ACC004', 'CUST004', 'CARD004', '202606', 'NEW'),
    ('ACC005', 'CUST005', 'CARD005', '202606', 'NEW');
