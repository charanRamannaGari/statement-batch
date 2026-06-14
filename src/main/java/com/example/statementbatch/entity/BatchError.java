package com.example.statementbatch.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "batch_error")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_id", nullable = false)
    private Long recordId;

    @Column(name = "account_no")
    private String accountNo;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    @Column(name = "created_time")
    private LocalDateTime createdTime;
}
