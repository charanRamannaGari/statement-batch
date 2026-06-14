package com.example.statementbatch.util;

import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility to generate unique statement numbers.
 * Format: ST{cycle}{sequence}  e.g. ST202606000001
 */
@Component
public class StatementNumberUtil {

    private final AtomicLong sequence = new AtomicLong(0);

    public String generate(String statementCycle) {
        long seq = sequence.incrementAndGet();
        return String.format("ST%s%06d", statementCycle, seq);
    }

    public void reset() {
        sequence.set(0);
    }
}
