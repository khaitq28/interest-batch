# Interest Batch — Spring Batch Demo

Spring Boot 3 / Java 21 project demonstrating batch processing of bank accounts
with daily interest calculation using Spring Batch.

---

## Stack

- Java 21
- Spring Boot 3.2
- Spring Batch 5
- Spring Data JPA
- H2 in-memory database
- Spring Web (REST trigger endpoint)

---

## How to run

```bash
mvn spring-boot:run
```

- App starts on port 8080
- 100,000 accounts are inserted automatically at startup
- Batch job triggers 5 seconds after startup (scheduler)
- H2 console: http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:fintechdb)

### Trigger job manually via REST

```bash
curl -X POST http://localhost:8080/api/jobs/interest/run
```

---

## Project structure

```
src/main/java/com/fintech/batch/
├── entity/Account.java              — JPA entity (balance, rate, accruedInterest...)
├── repository/AccountRepository.java
├── batch/
│   ├── InterestProcessor.java       — calculates daily interest per account
│   └── AccountItemWriter.java       — bulk saveAll() per chunk
├── config/
│   ├── InterestBatchConfig.java     — Job + Step + Reader + TaskExecutor
│   └── JobCompletionListener.java   — logs stats after job finishes
├── init/DataInitializer.java        — inserts 100k accounts at startup
├── runner/JobRunner.java            — @Scheduled trigger
└── controller/JobController.java    — REST endpoint POST /api/jobs/interest/run
```

---

## Key concepts

### 1. Why batch and not a simple loop ?

The naive approach:
```java
List<Account> accounts = accountRepository.findAll(); // loads ALL into memory — OOM risk
for (Account a : accounts) {
    interestService.calculate(a);
    accountRepository.save(a);  // 1 DB write per account — very slow
}
```

Problems:
- `findAll()` on 1M rows = OutOfMemoryError
- 1M individual `save()` calls = 1M DB round trips

Spring Batch solves both with chunk-oriented processing.

---

### 2. Chunk-oriented processing

The core of Spring Batch. Configured in `InterestBatchConfig`:

```java
.<Account, Account>chunk(1000, transactionManager)
.reader(...)
.processor(...)
.writer(...)
```

Spring Batch runs this loop internally:
```
loop:
  read 1000 items (1 paginated DB query)
  process each item (calculate interest)
  write 1000 items (1 bulk saveAll)
  commit transaction
  repeat until reader returns null
```

- Never loads the full table into memory
- 1 DB read + 1 DB write per 1000 accounts (not per account)
- If the job fails at chunk 50, it restarts from chunk 50 — not from the beginning

---

### 3. JpaPagingItemReader — paginated DB reads

```java
new JpaPagingItemReaderBuilder<Account>()
    .queryString("SELECT a FROM Account a ORDER BY a.id")
    .pageSize(1000)
    .build();
```

Internally generates:
```sql
SELECT ... LIMIT 1000 OFFSET 0      -- chunk 1
SELECT ... LIMIT 1000 OFFSET 1000   -- chunk 2
...
SELECT ... LIMIT 1000 OFFSET 99000  -- chunk 100
SELECT ... LIMIT 1000 OFFSET 100000 -- returns 0 rows → null → job ends
```

The reader returns items one by one to Spring Batch.
When a page is exhausted it fires the next SQL query to load the next page.

---

### 4. Single thread vs parallel with TaskExecutor

#### Single thread (default)

```
Thread main:
  chunk 1: read 1000 → process 1000 → write 1000 → commit
  chunk 2: read 1000 → process 1000 → write 1000 → commit
  ...strictly sequential
```

#### Parallel with TaskExecutor (10 threads)

```java
@Bean
public TaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(10);
    executor.initialize();
    return executor;
}
```

```
Thread-1: chunk 1  → read → process → write ┐
Thread-2: chunk 2  → read → process → write  │ parallel
...                                           │
Thread-10: chunk 10 → read → process → write ┘

When chunk 1 done → Thread-1 picks up chunk 11
...
```

#### Why SynchronizedItemStreamReader is required with parallel

Without it, multiple threads call `read()` simultaneously and get the same page offset:
```
Thread-1 read() → offset=0 → accounts 1-1000
Thread-2 read() → offset=0 → accounts 1-1000  ← collision!
```

With `SynchronizedItemStreamReader`:
```
Thread-1 locks → read offset=0    → accounts 1-1000    → releases
Thread-2 locks → read offset=1000 → accounts 1001-2000 → releases
```

The reader is always called by only 1 thread at a time.
Only the processor + writer steps run in parallel.

```java
.reader(synchronizedReader())   // sequential — 1 thread at a time
.processor(interestProcessor)   // parallel — each thread owns its 1000 items
.writer(accountItemWriter)      // parallel — each thread calls saveAll()
```

---

### 5. Interest calculation formula

```
dailyInterest = balance × (annualRate / 100) / 365
newBalance    = balance + dailyInterest
```

Uses `BigDecimal` — never `double` or `float` for money (precision loss).

---

## Benchmark results

Machine: MacBook, H2 in-memory database, chunk size = 1000

| Mode | Accounts | Chunks | Duration |
|---|---|---|---|
| Single thread (no TaskExecutor) | 100,000 | 100 | **10,828 ms** |
| 10 threads (TaskExecutor) | 100,000 | 100 | **6,631 ms** |
| Speedup | | | **~1.6×** |

### Why only 1.6× and not 10× ?

Theoretical max speedup with 10 threads = 10×.
Actual speedup = ~1.6× because:

1. **Reader is sequential** — `SynchronizedItemStreamReader` allows only 1 thread
   to read at a time. The read phase does not benefit from parallelism.
2. **DB write contention** — 10 threads calling `saveAll()` simultaneously compete
   for the same H2 database locks, reducing write throughput.
3. **H2 in-memory** — H2 is already extremely fast. With PostgreSQL (disk I/O,
   network latency), the parallel speedup would be significantly higher (~4-6×).

### Extrapolation to 1M accounts (estimated, PostgreSQL)

| Mode | Estimated duration |
|---|---|
| Single thread | ~10-15 minutes |
| 10 threads | ~2-4 minutes |

For 10M+ accounts, use Spring Batch **partitioning** — splits the table by ID range,
each partition runs as an independent step in parallel.

---

## Scheduler configuration

In `JobRunner.java` — change the `@Scheduled` annotation to control when the job runs:

```java
// Production — every night at 2am:
@Scheduled(cron = "0 0 2 * * *")

// Demo — once, 5 seconds after startup:
@Scheduled(initialDelay = 5_000, fixedDelay = Long.MAX_VALUE)

// Test — every minute:
@Scheduled(fixedDelay = 60_000)
```

Or trigger manually via REST endpoint:
```bash
curl -X POST http://localhost:8080/api/jobs/interest/run
```
