package com.example.statementbatch.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "msr_processorlog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProcessorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_no", nullable = false)
    private String accountNo;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "card_no", nullable = false)
    private String cardNo;

    @Column(name = "statement_cycle", nullable = false)
    private String statementCycle;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}
