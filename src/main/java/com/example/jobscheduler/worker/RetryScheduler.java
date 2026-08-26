package com.example.jobscheduler.worker;

import com.example.jobscheduler.model.Job;
import com.example.jobscheduler.model.JobStatus;
import com.example.jobscheduler.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class RetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(RetryScheduler.class);

    private final JobRepository jobRepository;
    private final JobQueueService jobQueueService;

    public RetryScheduler(JobRepository jobRepository, JobQueueService jobQueueService) {
        this.jobRepository = jobRepository;
        this.jobQueueService = jobQueueService;
    }

    @Scheduled(fixedDelay = 2000)
    public void reQueueDueRetries() {
        List<Job> due = jobRepository.findByStatusAndNextRetryAtBefore(JobStatus.RETRYING, Instant.now());
        for (Job job : due) {
            jobQueueService.enqueue(new QueuedJobRef(job.getId(), job.getPriority(), job.getCreatedAt()));
            log.debug("Re-queued job {} for retry attempt {}", job.getId(), job.getAttempts() + 1);
        }
    }
}
