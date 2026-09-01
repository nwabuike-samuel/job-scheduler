package io.github.nwabuikesamuel.jobscheduler.service;

import io.github.nwabuikesamuel.jobscheduler.repository.JobRepository;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.nwabuikesamuel.jobscheduler.model.Job;
import io.github.nwabuikesamuel.jobscheduler.model.JobStatus;

@ExtendWith(MockitoExtension.class)
public class JobStateServiceTest {
    
    @Mock
    JobRepository jobRepository;

    @Mock
    JobStateService jobStateService;

    @Mock
    JobExecutionService jobExecutionService;

    @BeforeEach
    void setUp() {
        jobStateService = new JobStateService(jobRepository);
    }

    @Test
    void markFailedOrRetry_belowMaxAttempts_movesToRetrying() {

        Job job = new Job();
        job.setId("job1");
        job.setAttempts(1);
        job.setMaxAttempts(3);
        job.setStatus(JobStatus.RUNNING);

        when(jobRepository.findById("job1")).thenReturn(Optional.of(job));

        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        jobStateService.markFailedOrRetry("job1", new RuntimeException("Test failure"));

        assertThat(job.getStatus()).isEqualTo(JobStatus.RETRYING);

        assertThat(job.getNextRetryAt()).isNotNull();
    }

    @Test
    void markFailedOrRetry_atMaxAttempts_movesToDeadLetter() {

        Job job = new Job();
        job.setId("job2");
        job.setAttempts(3);
        job.setMaxAttempts(3);
        job.setStatus(JobStatus.RUNNING);

        when(jobRepository.findById("job2")).thenReturn(Optional.of(job));

        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        jobStateService.markFailedOrRetry("job2", new RuntimeException("Test failure"));

        assertThat(job.getStatus()).isEqualTo(JobStatus.DEAD_LETTER);
    }

    @Test
    void secondFailure_hasLaterNextRetryAt_thanFirstFailure_exponentialBackoff() throws Exception {
        // TODO: run two separate jobs (or the same job twice), one failing on
        //       attempt 1 and one on attempt 2. Capture nextRetryAt on each
        //       and assert the attempt-2 value is further in the future,
        //       proving backoff grows rather than staying fixed.
        Job job1 = new Job();
        job1.setId("job1");
        job1.setType("SOME_TYPE");
        job1.setAttempts(1);
        job1.setMaxAttempts(3);
        job1.setStatus(JobStatus.RUNNING);
        when(jobRepository.findById("job1")).thenReturn(java.util.Optional.of(job1));

        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        jobStateService.markFailedOrRetry("job1", new RuntimeException("Test failure"));
        Instant firstRetryAt = job1.getNextRetryAt();
        // Simulate a second failure on the same job
        job1.setAttempts(2); // increment attempts to simulate second failure
        job1.setStatus(JobStatus.RUNNING); // set status back to RUNNING for the second failure
        jobStateService.markFailedOrRetry("job1", new RuntimeException("Second failure"));

        Instant secondRetryAt = job1.getNextRetryAt();
        assertThat(secondRetryAt).isAfter(firstRetryAt);
    }

    @Test
    void successfulRun_clearsPreviousErrorMessage_andSetsResult() {
        // TODO: build a Job that already has errorMessage set from a prior
        //       failed attempt, make the handler succeed this time, assert
        //       job.getErrorMessage() is null after runJob().
        Job job = new Job();
        job.setId("job3");
        job.setAttempts(2);
        job.setMaxAttempts(3);
        job.setStatus(JobStatus.RUNNING);
        job.setErrorMessage("Previous error");
        job.setResult(null);

        when(jobRepository.findById("job3")).thenReturn(java.util.Optional.of(job));

        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));
        jobStateService.markSuccess("job3", "Successful result");
        assertThat(job.getStatus()).isEqualTo(JobStatus.SUCCESS);
        assertThat(job.getErrorMessage()).isNull();
        assertThat(job.getResult()).isEqualTo("Successful result");
    }
    
}
