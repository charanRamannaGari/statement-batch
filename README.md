# Credit Card Statement Batch

Spring Batch application for generating credit card statements from `msr_processorlog`.

---

## Tech Stack

| Layer        | Technology                  |
|--------------|-----------------------------|
| Framework    | Spring Boot 3.2.5           |
| Batch        | Spring Batch 5              |
| Database     | PostgreSQL                  |
| ORM          | Spring Data JPA / Hibernate |
| Build        | Maven                       |
| Java         | Java 17                     |
| Logging      | SLF4J + Logback             |

---

## Project Structure

```
statement-batch/
├── config/         BatchConfig.java         ← Job + Step wiring
├── reader/         ProcessorLogReader.java  ← JdbcPagingItemReader
├── processor/      StatementProcessor.java  ← Validate + Generate
├── writer/         StatementWriter.java     ← Insert + Update status
├── listener/       JobListener.java         ← Before/After Job
│                   StepListener.java        ← Before/After Step
├── scheduler/      BatchScheduler.java      ← Monthly cron trigger
├── controller/     BatchController.java     ← REST API
├── service/        BatchService.java        ← Job launcher logic
├── repository/     *Repository.java         ← JPA repositories
├── entity/         ProcessorLog, Statement, BatchError, BatchAudit
├── dto/            StatementItemDto, BatchStartResponse, BatchStatusResponse
├── exception/      StatementValidationException, DuplicateStatementException
└── util/           StatementNumberUtil.java ← ST{cycle}{seq} generator
```

---

## Database Setup

```sql
-- Run schema.sql in your PostgreSQL database
psql -U postgres -d statementdb -f src/main/resources/schema.sql
```

Tables created:
- `msr_processorlog` — Input: records to process
- `statement`        — Output: generated statements
- `batch_error`      — Error details per failed record
- `batch_audit`      — Job-level summary per execution

---

## Configuration

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/statementdb
spring.datasource.username=postgres
spring.datasource.password=postgres
```

---

## Running the Application

```bash
mvn clean package -DskipTests
java -jar target/statement-batch-1.0.0.jar
```

---

## REST API (Feature 19)

### Trigger Batch
```http
POST http://localhost:8080/batch/statement/start?statementCycle=202606
```
Response:
```json
{
  "jobId": 1,
  "status": "STARTED",
  "statementCycle": "202606",
  "message": "Batch job triggered successfully"
}
```

### Check Status
```http
GET http://localhost:8080/batch/statement/status/1
```
Response:
```json
{
  "jobExecutionId": 1,
  "jobName": "statementBatchJob",
  "status": "COMPLETED",
  "statementCycle": "202606",
  "totalRecords": 1000,
  "successCount": 980,
  "failureCount": 15,
  "skippedCount": 5
}
```

---

## Feature Map

| Feature | Description                         | Location                          |
|---------|-------------------------------------|-----------------------------------|
| 1       | Read NEW records (Paging)           | ProcessorLogReader                |
| 2       | Job parameter: statementCycle       | ProcessorLogReader + BatchService |
| 3       | Record validation                   | StatementProcessor.validate()     |
| 4       | Duplicate statement check           | StatementProcessor.checkDuplicate()|
| 5       | Status flow: NEW→IN_PROGRESS→DONE   | StatementProcessor + StatementWriter|
| 6       | Statement number generation         | StatementNumberUtil               |
| 7       | Write to statement table            | StatementWriter                   |
| 8       | Update processorlog to COMPLETED    | StatementWriter                   |
| 9       | Error stored in batch_error         | StatementProcessor.saveError()    |
| 10      | Skip invalid records (.skip())      | BatchConfig                       |
| 11      | Retry on DB failure (.retry())      | BatchConfig                       |
| 12      | Chunk processing (100/chunk)        | BatchConfig chunk(100)            |
| 13      | Audit in batch_audit table          | JobListener                       |
| 14      | Job lifecycle logging               | JobListener                       |
| 15      | Step lifecycle logging              | StepListener                      |
| 16      | Detailed per-record logging         | Processor + Writer (log.info)     |
| 17      | Batch summary after completion      | JobListener.afterJob()            |
| 18      | Restartability                      | Spring Batch JobRepository (auto) |
| 19      | REST API trigger + status           | BatchController + BatchService    |
| 20      | Monthly scheduler (1st, 02:00 AM)   | BatchScheduler                    |

---

## Interview Q&A

**Q: Why PagingReader instead of CursorReader?**

CursorReader holds an open DB connection/cursor for the entire job duration — risky for large datasets.
PagingReader fetches a page (e.g., 100 rows) at a time, releases the connection between pages, and is fully restartable since Spring Batch tracks the last processed page.

**Q: How does restartability work?**

Spring Batch stores job state in its metadata tables (BATCH_JOB_EXECUTION, BATCH_STEP_EXECUTION). On restart, it reads the last committed chunk position and resumes from there — already-processed chunks are skipped automatically.

**Q: How does skip work?**

When `processor.process()` returns `null`, Spring Batch increments the filter count and does not pass that item to the writer. Combined with `.skip(ExceptionClass.class)`, exceptions thrown in the processor/writer are also skipped up to `skipLimit`.

**Q: What happens if skipLimit is exceeded?**

The job fails with a `SkipLimitExceededException`. This prevents a runaway situation where most records are invalid.
