package com.example.statementbatch.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchStatusResponse {
    private Long jobExecutionId;
    private String jobName;
    private String status;
    private String statementCycle;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalRecords;
    private Integer successCount;
    private Integer failureCount;
    private Integer skippedCount;
}
