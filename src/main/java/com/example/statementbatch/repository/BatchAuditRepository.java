package com.example.statementbatch.repository;

import com.example.statementbatch.entity.BatchAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BatchAuditRepository extends JpaRepository<BatchAudit, Long> {

    Optional<BatchAudit> findByJobExecutionId(Long jobExecutionId);
}
