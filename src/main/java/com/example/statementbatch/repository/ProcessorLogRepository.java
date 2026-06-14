package com.example.statementbatch.repository;

import com.example.statementbatch.entity.ProcessorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProcessorLogRepository extends JpaRepository<ProcessorLog, Long> {

    List<ProcessorLog> findByStatus(String status);

    @Modifying
    @Query("UPDATE ProcessorLog p SET p.status = :status, p.updatedTime = :updatedTime WHERE p.id = :id")
    int updateStatus(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("updatedTime") LocalDateTime updatedTime);
}
