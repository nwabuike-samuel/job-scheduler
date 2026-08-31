package io.github.nwabuikesamuel.jobscheduler.service;
import io.github.nwabuikesamuel.jobscheduler.model.Job;
import io.github.nwabuikesamuel.jobscheduler.model.JobStatus;
import io.github.nwabuikesamuel.jobscheduler.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;

@Service
public class JobStateService {

    private static final Logger log =
            LoggerFactory.getLogger(JobStateService.class);

    private static final Duration BASE_BACKOFF =
            Duration.ofSeconds(5);

    private final JobRepository jobRepository;

    public JobStateService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional
    public void markRunning(String jobId) {

        Job job = jobRepository.findById(jobId).orElse(null);

        if (job == null) {
            return;
        }

        job.setAttempts(job.getAttempts() + 1);
        job.setStatus(JobStatus.RUNNING);

        jobRepository.save(job);
    }

    @Transactional
    public void markSuccess(String jobId, String result) {

        Job job = jobRepository.findById(jobId).orElse(null);

        if (job == null) {
            return;
        }

        job.setResult(result);
        job.setErrorMessage(null);
        job.setStatus(JobStatus.SUCCESS);

        jobRepository.save(job);

        log.info(
                "Job {} succeeded on attempt {}",
                job.getId(),
                job.getAttempts()
        );
    }

    @Transactional
    public void markFailedOrRetry(String jobId, Exception ex) {

        Job job = jobRepository.findById(jobId).orElse(null);

        if (job == null) {
            return;
        }

        job.setErrorMessage(ex.getMessage());
        job.setStatus(JobStatus.FAILED);

        if (job.getAttempts() >= job.getMaxAttempts()) {

            job.setStatus(JobStatus.DEAD_LETTER);

            log.warn(
                    "Job {} moved to DEAD_LETTER after {} attempts",
                    job.getId(),
                    job.getAttempts()
            );

        } else {

            long backoffSeconds = BASE_BACKOFF.getSeconds() * (1L << (job.getAttempts() - 1));

            job.setNextRetryAt(Instant.now().plusSeconds(backoffSeconds));

            job.setStatus(JobStatus.RETRYING);

            log.info(
                    "Job {} failed (attempt {}), retrying at {}",
                    job.getId(),
                    job.getAttempts(),
                    job.getNextRetryAt()
            );
        }

        jobRepository.save(job);
    }
}
