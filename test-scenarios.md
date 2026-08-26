# Test Scenarios — work through these in order

How to use this: write each test as a JUnit method (it will fail or not
compile at first — that's correct, that's TDD). Only write the implementation
code needed to make that one test pass, then move to the next. Don't skip
ahead and implement several at once.

Suggested test file locations:
- `src/test/java/.../service/JobExecutionServiceTest.java` (unit, Mockito)
- `src/test/java/.../worker/QueuedJobRefTest.java` (unit, no Spring context)
- `src/test/java/.../controller/JobControllerIntegrationTest.java` (`@SpringBootTest` + `MockMvc` or `TestRestTemplate`)

---

## Part A — Lock in the existing scaffold (milestones 1–5)

These test the code you already have. Writing them first catches anything
that's subtly broken before you build on top of it.

### A1. Priority queue ordering (unit test, no Spring needed)
- [ ] `QueuedJobRef` with priority 1 sorts before priority 5
- [ ] Two refs with the same priority sort by `createdAt` ascending (older first)
- [ ] A `PriorityBlockingQueue<QueuedJobRef>` drains in the expected order when refs are inserted out of order

### A2. Job submission (integration test via MockMvc/TestRestTemplate)
- [ ] `POST /api/jobs` with a valid `ECHO` request returns `201 Created` and a body containing `status: PENDING`
- [ ] `POST /api/jobs` with an unregistered `type` returns `400 Bad Request`
- [ ] `POST /api/jobs` with a blank `type` field returns `400 Bad Request` (validation)
- [ ] `POST /api/jobs` with `priority: 0` or `priority: 11` returns `400 Bad Request` (out of the 1–10 range)

### A3. Job retrieval and listing
- [ ] `GET /api/jobs/{id}` for a real id returns `200` with matching fields
- [ ] `GET /api/jobs/{id}` for a nonexistent id returns `404`
- [ ] `GET /api/jobs` returns a list including a job you just submitted
- [ ] `GET /api/jobs?status=PENDING` excludes jobs in other statuses

### A4. Job cancellation
- [ ] `DELETE /api/jobs/{id}` on a `PENDING` job returns `204` and the job is gone from `GET /api/jobs/{id}` (404 after)
- [ ] `DELETE /api/jobs/{id}` on a job already `RUNNING` or `SUCCESS` returns `400` (only PENDING is cancellable)

### A5. End-to-end execution (integration, allow real async time to pass)
- [ ] Submit an `ECHO` job, poll `GET /api/jobs/{id}` until status is no longer `PENDING`/`RUNNING`, assert final status is `SUCCESS` and `result` contains the echoed payload
- [ ] Submit a job with `maxAttempts: 1` and a handler that always throws, poll until terminal, assert status is `DEAD_LETTER` and `attempts == 1`
- [ ] Submit two jobs, priority 1 and priority 9, both `ECHO` with an artificial delay — assert the priority-1 job's `updatedAt` (start of RUNNING) is earlier

### A6. Retry and backoff (unit test on `JobExecutionService` directly, mock the repository/handler)
- [ ] A job with `attempts < maxAttempts` that throws moves to `RETRYING`, not `DEAD_LETTER`
- [ ] A job with `attempts == maxAttempts` that throws moves to `DEAD_LETTER`, not `RETRYING`
- [ ] `nextRetryAt` is set further into the future on the 2nd failure than the 1st (confirms exponential, not fixed, backoff)
- [ ] A successful run clears `errorMessage` (in case it was previously set by an earlier failed attempt)

### A7. Crash recovery
- [ ] Manually insert a `Job` row with status `RUNNING` directly via the repository (simulating a crash mid-execution), restart the Spring context, assert the job is re-queued and eventually reaches a terminal state — this one is fiddly to automate; a manual test (kill the app mid-job, restart, observe) is an acceptable substitute if the automated version is too much setup right now

### A8. Concurrency safety
- [ ] Two threads calling `JobExecutionService.runJob(sameId)` concurrently — assert the job is only executed once (check a counter inside a test-only handler, or assert `attempts` never exceeds what one run would produce)

---

## Part B — Milestone 6: Observability

Write these before adding Actuator/logging/stats.

### B1. Health and metrics
- [ ] `GET /actuator/health` returns `200` with `status: UP` (once Actuator is added)
- [ ] `GET /actuator/metrics` lists at least one custom metric related to job processing

### B2. Stats endpoint
- [ ] `GET /api/jobs/stats` returns counts per status that match what's actually in the database (submit known jobs, assert the numbers add up)
- [ ] Stats endpoint on an empty database returns all-zero counts, not an error

### B3. Correlation logging
- [ ] (Manual/log-inspection test) Submit a job, grep the log output for its job id, confirm every log line for that job's lifecycle (RUNNING, SUCCESS/FAILED) includes the id

---

## Part C — Milestone 7: broader test coverage

You'll have already written most of Part A by the time you get here — this
section is about closing remaining gaps and adding load/failure testing.

- [ ] Load test: submit 1,000 `ECHO` jobs in a loop, assert all reach `SUCCESS` within a reasonable timeout, and assert no job id appears twice in a "completed" log/counter (no double-processing)
- [ ] Load test: submit 1,000 jobs, kill the app halfway through (manual), restart, assert the remainder eventually complete

---

## Part D — Milestone 8: API hardening

Write these first, they'll fail against the current wide-open API.

### D1. Pagination
- [ ] `GET /api/jobs?page=0&size=10` returns at most 10 results even with 50+ jobs in the database
- [ ] `GET /api/jobs?page=1&size=10` returns a different page of results than page 0

### D2. Auth
- [ ] `POST /api/jobs` without an API key/token returns `401`
- [ ] `POST /api/jobs` with a valid key/token returns `201` as before
- [ ] `GET /actuator/health` remains accessible without auth (health checks usually should be)

### D3. Rate limiting
- [ ] Submitting more than N jobs in a short window from the same client returns `429 Too Many Requests` for the excess

---

## Part E — Milestone 9: distributed workers

These are harder to unit test — mostly integration tests against a real
broker running in Docker (which is exactly why Docker Desktop will finally
earn its keep).

- [ ] With RabbitMQ/Kafka running locally via `docker-compose`, submitting a job results in a message appearing on the expected queue/topic
- [ ] Run two instances of the app pointed at the same broker + database, submit 100 jobs, assert every job completes exactly once (no double-processing across instances, no job lost)
- [ ] Kill one instance mid-processing, assert the in-flight job is eventually picked up and completed by the surviving instance
- [ ] Submit a high-priority and low-priority job under load — assert (or document, if the broker doesn't guarantee it) which completes first, matching whatever tradeoff you chose in the RabbitMQ vs Kafka decision

---

## Part F — Milestone 10: polish

- [ ] `docker-compose up` brings up the app + Postgres and `GET /api/jobs` responds `200`
- [ ] `GET /v3/api-docs` or `/swagger-ui.html` loads once springdoc-openapi is added
