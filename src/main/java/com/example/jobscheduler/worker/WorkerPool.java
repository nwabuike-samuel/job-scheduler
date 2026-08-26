package com.example.jobscheduler.worker;

import com.example.jobscheduler.model.Job;
import com.example.jobscheduler.model.JobStatus;
import com.example.jobscheduler.repository.JobRepository;
import com.example.jobscheduler.service.JobExecutionService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class WorkerPool {

    private static final Logger log = LoggerFactory.getLogger(WorkerPool.class);

    private final JobQueueService jobQueueService;
    private final JobExecutionService jobExecutionService;
    private final JobRepository jobRepository;

    @Value("${scheduler.worker-count:4}")
    private int workerCount;

    private ExecutorService executor;
    private volatile boolean running = true;

    public WorkerPool(JobQueueService jobQueueService, JobExecutionService jobExecutionService, JobRepository jobRepository) {
        this.jobQueueService = jobQueueService;
        this.jobExecutionService = jobExecutionService;
        this.jobRepository = jobRepository;
    }

    @PostConstruct
    public void start() {
        recoverInFlightJobs();

        executor = Executors.newFixedThreadPool(workerCount);
        for (int i = 0; i < workerCount; i++) {
            executor.submit(this::workerLoop);
        }
        log.info("Started {} worker threads", workerCount);
    }

    private void workerLoop() {
        while (running) {
            try {
                QueuedJobRef ref = jobQueueService.take();
                jobExecutionService.runJob(ref.jobId());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.error("Worker loop error", e);
            }
        }
    }

    /**
     * If the app crashed mid-flight, jobs may be stuck in PENDING (never
     * picked up) or RUNNING (killed mid-execution). Re-queue them on startup
     * rather than losing them silently.
     */
    private void recoverInFlightJobs() {
        List<Job> stuck = jobRepository.findByStatusIn(List.of(JobStatus.PENDING, JobStatus.RUNNING));
        for (Job job : stuck) {
            job.setStatus(JobStatus.PENDING);
            jobRepository.save(job);
            jobQueueService.enqueue(new QueuedJobRef(job.getId(), job.getPriority(), job.getCreatedAt()));
        }
        if (!stuck.isEmpty()) {
            log.info("Recovered {} in-flight jobs after restart", stuck.size());
        }
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
