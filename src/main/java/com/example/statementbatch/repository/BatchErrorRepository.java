package com.example.statementbatch.repository;

import com.example.statementbatch.entity.BatchError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchErrorRepository extends JpaRepository<BatchError, Long> {
}
