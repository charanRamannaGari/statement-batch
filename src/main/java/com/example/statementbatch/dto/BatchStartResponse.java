package com.example.statementbatch.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchStartResponse {
    private Long jobId;
    private String status;
    private String statementCycle;
    private String message;
}
