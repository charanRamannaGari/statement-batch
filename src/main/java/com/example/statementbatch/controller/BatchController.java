package com.example.statementbatch.controller;

import com.example.statementbatch.dto.BatchStartResponse;
import com.example.statementbatch.dto.BatchStatusResponse;
import com.example.statementbatch.service.BatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Feature 19: REST API to trigger batch and check status
 *
 * POST /batch/statement/start?statementCycle=202606
 * GET  /batch/statement/status/{jobId}
 */
@Slf4j
@RestController
@RequestMapping("/batch/statement")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;

    /**
     * Start the statement batch job.
     *
     * Example:
     *   POST http://localhost:8080/batch/statement/start?statementCycle=202606
     *
     * Response:
     *   { "jobId": 1, "status": "STARTED", "statementCycle": "202606" }
     */
    @PostMapping("/start")
    public ResponseEntity<BatchStartResponse> startBatch(
            @RequestParam(defaultValue = "") String statementCycle) {

        log.info("REST: Start batch request received for cycle={}", statementCycle);
        BatchStartResponse response = batchService.startBatch(statementCycle);
        return ResponseEntity.ok(response);
    }

    /**
     * Get batch status by job execution ID.
     *
     * Example:
     *   GET http://localhost:8080/batch/statement/status/1
     */
    @GetMapping("/status/{jobId}")
    public ResponseEntity<BatchStatusResponse> getStatus(@PathVariable Long jobId) {
        log.info("REST: Status request for jobId={}", jobId);
        BatchStatusResponse response = batchService.getStatus(jobId);
        return ResponseEntity.ok(response);
    }
}
