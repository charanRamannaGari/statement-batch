package com.example.statementbatch.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "batch_audit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_name", nullable = false)
    private String jobName;

    @Column(name = "job_execution_id")
    private Long jobExecutionId;

    @Column(name = "statement_cycle")
    private String statementCycle;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "status")
    private String status;

    @Column(name = "total_records")
    private Integer totalRecords;

    @Column(name = "success_count")
    private Integer successCount;

    @Column(name = "failure_count")
    private Integer failureCount;

    @Column(name = "skipped_count")
    private Integer skippedCount;

    @Column(name = "created_time")
    private LocalDateTime createdTime;
}
