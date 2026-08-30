package io.github.nwabuikesamuel.jobscheduler.service;

import io.github.nwabuikesamuel.jobscheduler.model.Job;
import io.github.nwabuikesamuel.jobscheduler.model.JobStatus;
import io.github.nwabuikesamuel.jobscheduler.repository.JobRepository;
import io.github.nwabuikesamuel.jobscheduler.worker.JobHandler;
import io.github.nwabuikesamuel.jobscheduler.worker.JobHandlerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Part A6 and A8 from test-scenarios.md.
 * These mock JobRepository and JobHandlerRegistry so we can test the state
 * machine transitions in isolation, without a real database or Spring context.
 */
@ExtendWith(MockitoExtension.class)
class JobExecutionServiceTest {

    @Mock
    JobRepository jobRepository;

    @Mock
    JobHandlerRegistry handlerRegistry;

    @Mock
    JobStateService jobStateService;

    JobExecutionService executionService;

    @BeforeEach
    void setUp() {
        executionService = new JobExecutionService(jobRepository, handlerRegistry, jobStateService);
    }

    // --- A6: retry and backoff ---

    @Test
    void jobBelowMaxAttempts_thatThrows_movesToRetrying_notDeadLetter() {
        // TODO: build a Job with attempts=0, maxAttempts=3, status=PENDING.
        //       when(jobRepository.findById(...)).thenReturn(Optional.of(job));
        //       when(jobRepository.save(any())).thenReturn(job); // or thenAnswer to return the arg
        //       Make handlerRegistry.get("SOME_TYPE") return a JobHandler whose execute() throws.
        //       Call executionService.runJob(job.getId());
        //       assertThat(job.getStatus()).isEqualTo(JobStatus.RETRYING);
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Test
    void jobAtMaxAttempts_thatThrows_movesToDeadLetter_notRetrying() {
        // TODO: same setup as above but attempts == maxAttempts - 1 before this run
        //       (so after markRunning increments it, attempts == maxAttempts).
        //       assertThat(job.getStatus()).isEqualTo(JobStatus.DEAD_LETTER);
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Test
    void secondFailure_hasLaterNextRetryAt_thanFirstFailure_exponentialBackoff() {
        // TODO: run two separate jobs (or the same job twice), one failing on
        //       attempt 1 and one on attempt 2. Capture nextRetryAt on each
        //       and assert the attempt-2 value is further in the future,
        //       proving backoff grows rather than staying fixed.
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Test
    void successfulRun_clearsPreviousErrorMessage() {
        // TODO: build a Job that already has errorMessage set from a prior
        //       failed attempt, make the handler succeed this time, assert
        //       job.getErrorMessage() is null after runJob().
        throw new UnsupportedOperationException("not implemented yet");
    }

    // --- A8: concurrency safety ---

    @Test
    void concurrentRunJobCalls_onSameId_onlyExecuteOnce() throws InterruptedException {
        // TODO: this one is trickier. Ideas:
        //   1. Use a test-only JobHandler with an AtomicInteger counter incremented
        //      inside execute(), run two threads calling runJob(sameId) concurrently
        //      (e.g. via ExecutorService + CountDownLatch to line them up), join both,
        //      then assert the counter == 1.
        //   2. Alternative: this is easier to test with a real (non-mocked)
        //      JobRepository against the test H2 database - consider moving
        //      this specific test into an @SpringBootTest / @DataJpaTest instead
        //      if pure mocking makes it awkward to simulate the race.
        throw new UnsupportedOperationException("not implemented yet");
    }
}
