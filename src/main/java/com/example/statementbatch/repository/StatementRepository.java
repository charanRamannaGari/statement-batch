package com.example.statementbatch.repository;

import com.example.statementbatch.entity.Statement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StatementRepository extends JpaRepository<Statement, Long> {

    // Feature 4: Duplicate check
    @Query("SELECT COUNT(s) FROM Statement s WHERE s.accountNo = :accountNo AND s.statementCycle = :cycle")
    long countByAccountNoAndStatementCycle(@Param("accountNo") String accountNo,
                                           @Param("cycle") String statementCycle);
}
