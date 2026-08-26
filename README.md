# MiniQuartz — Job Scheduler

A Spring Boot job scheduler with a priority queue, thread pool workers,
persistence, exponential-backoff retries, and pluggable job types.

## Run it

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080`. An H2 console is available at
`http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/jobscheduler`).

## Try it

Submit a reliable demo job:

```bash
curl -X POST http://localhost:8080/api/jobs \
  -H "Content-Type: application/json" \
  -d '{"type": "ECHO", "payload": "hello world", "priority": 3}'
```

Submit a flaky job to watch the retry/backoff/dead-letter path in action:

```bash
curl -X POST http://localhost:8080/api/jobs \
  -H "Content-Type: application/json" \
  -d '{"type": "FLAKY", "payload": "test-1", "maxAttempts": 4}'
```

Check status (swap in the id returned above):

```bash
curl http://localhost:8080/api/jobs/{id}
```

List all jobs, or filter by status:

```bash
curl http://localhost:8080/api/jobs
curl "http://localhost:8080/api/jobs?status=DEAD_LETTER"
```

## Adding a new job type

Implement `JobHandler`, annotate with `@Component`, and it's auto-registered:

```java
@Component
public class SendEmailJobHandler implements JobHandler {
    public String getType() { return "SEND_EMAIL"; }
    public String execute(String payload) throws Exception {
        // ... send the email, parse payload as needed
        return "sent";
    }
}
```

No other wiring needed — `JobHandlerRegistry` picks it up via Spring DI.

## What's implemented so far (milestones 1–5)

- REST API: submit / get / list / cancel jobs
- Priority queue (`PriorityBlockingQueue`, priority + FIFO tiebreak)
- Thread pool workers consuming the queue concurrently
- Persistence via JPA/H2 — jobs and their state survive a restart
- Startup recovery of PENDING/RUNNING jobs left over from a crash
- Exponential backoff retries + dead-lettering after max attempts
- Optimistic locking (`@Version`) to guard against double-processing
- Pluggable job types via the Strategy pattern (`JobHandler`)

## Not yet implemented (see the checklist below for next steps)

- Distributed workers (RabbitMQ/Kafka)
- Auth / API security
- Metrics / Actuator dashboard
