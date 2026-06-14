package com.example.statementbatch.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "statement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Statement {

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

    @Column(name = "statement_number", nullable = false, unique = true)
    private String statementNumber;

    @Column(name = "generated_date")
    private LocalDateTime generatedDate;
}
