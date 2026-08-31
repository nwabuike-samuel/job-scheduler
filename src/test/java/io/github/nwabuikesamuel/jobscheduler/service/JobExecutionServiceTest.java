package io.github.nwabuikesamuel.jobscheduler.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.nwabuikesamuel.jobscheduler.model.Job;
import io.github.nwabuikesamuel.jobscheduler.model.JobStatus;
import io.github.nwabuikesamuel.jobscheduler.repository.JobRepository;
import io.github.nwabuikesamuel.jobscheduler.worker.JobHandler;
import io.github.nwabuikesamuel.jobscheduler.worker.JobHandlerRegistry;

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

    @Mock
    JobExecutionService executionService;

    @BeforeEach
    void setUp() {
        executionService = new JobExecutionService(jobRepository, handlerRegistry, jobStateService);
    }

    // --- A6: retry and backoff ---

    @Test
    void failedJob_callsMarkFailedOrRetry() throws Exception {

        Job job = new Job();
        job.setId("job1");
        job.setType("SOME_TYPE");
        job.setPayload("test");
        job.setAttempts(0);
        job.setMaxAttempts(3);
        job.setStatus(JobStatus.PENDING);

        when(jobRepository.findById("job1")).thenReturn(java.util.Optional.of(job));

        JobHandler handler = mock(JobHandler.class);

        when(handlerRegistry.get("SOME_TYPE")).thenReturn(handler);

        when(handler.execute("test")).thenThrow(new RuntimeException("Test failure"));

        executionService.runJob("job1");

        verify(jobStateService).markRunning("job1");

        verify(jobStateService).markFailedOrRetry(eq("job1"), any(RuntimeException.class));

        verify(jobStateService, never()).markSuccess(anyString(), anyString());
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
