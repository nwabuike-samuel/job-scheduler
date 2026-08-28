package io.github.nwabuikesamuel.jobscheduler.service;

import io.github.nwabuikesamuel.jobscheduler.model.Job;
import io.github.nwabuikesamuel.jobscheduler.model.JobStatus;
import io.github.nwabuikesamuel.jobscheduler.repository.JobRepository;
import io.github.nwabuikesamuel.jobscheduler.worker.JobHandler;
import io.github.nwabuikesamuel.jobscheduler.worker.JobHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
public class JobExecutionService {

    private static final Logger log = LoggerFactory.getLogger(JobExecutionService.class);
    private static final Duration BASE_BACKOFF = Duration.ofSeconds(5);

    private final JobRepository jobRepository;
    private final JobHandlerRegistry handlerRegistry;

    public JobExecutionService(JobRepository jobRepository, JobHandlerRegistry handlerRegistry) {
        this.jobRepository = jobRepository;
        this.handlerRegistry = handlerRegistry;
    }

    /**
     * Called by a worker thread after it dequeues a job id. Loads the job,
     * runs the matching handler, and applies the resulting state transition.
     * Any concurrent-update conflict (two workers grabbing the same job)
     * surfaces as an OptimisticLockingFailureException, which we simply log
     * and skip -- the other worker won the race.
     */
    public void runJob(String jobId) {
        Job job = jobRepository.findById(jobId).orElse(null);
        if (job == null || job.getStatus() == JobStatus.SUCCESS || job.getStatus() == JobStatus.DEAD_LETTER) {
            return; // already handled, cancelled, or deleted
        }

        markRunning(job);

        try {
            JobHandler handler = handlerRegistry.get(job.getType());
            String result = handler.execute(job.getPayload());
            markSuccess(job, result);
        } catch (Exception ex) {
            markFailedOrRetry(job, ex);
        }
    }

    @Transactional
    protected void markRunning(Job job) {
        job.setAttempts(job.getAttempts() + 1);
        job.setStatus(JobStatus.RUNNING);
        jobRepository.save(job);
    }

    @Transactional
    protected void markSuccess(Job job, String result) {
        job.setResult(result);
        job.setErrorMessage(null);
        job.setStatus(JobStatus.SUCCESS);
        jobRepository.save(job);
        log.info("Job {} succeeded on attempt {}", job.getId(), job.getAttempts());
    }

    @Transactional
    protected void markFailedOrRetry(Job job, Exception ex) {
        job.setErrorMessage(ex.getMessage());
        job.setStatus(JobStatus.FAILED);

        if (job.getAttempts() >= job.getMaxAttempts()) {
            job.setStatus(JobStatus.DEAD_LETTER);
            log.warn("Job {} moved to DEAD_LETTER after {} attempts", job.getId(), job.getAttempts());
        } else {
            long backoffSeconds = BASE_BACKOFF.getSeconds() * (1L << (job.getAttempts() - 1)); // exponential
            job.setNextRetryAt(Instant.now().plusSeconds(backoffSeconds));
            job.setStatus(JobStatus.RETRYING);
            log.info("Job {} failed (attempt {}), retrying at {}", job.getId(), job.getAttempts(), job.getNextRetryAt());
        }

        jobRepository.save(job);
    }
}
