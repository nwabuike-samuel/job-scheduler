package io.github.nwabuikesamuel.jobscheduler.service;

import io.github.nwabuikesamuel.jobscheduler.model.Job;
import io.github.nwabuikesamuel.jobscheduler.model.JobStatus;
import io.github.nwabuikesamuel.jobscheduler.repository.JobRepository;
import io.github.nwabuikesamuel.jobscheduler.worker.JobHandler;
import io.github.nwabuikesamuel.jobscheduler.worker.JobHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JobExecutionService {

    private static final Logger log = LoggerFactory.getLogger(JobExecutionService.class);

    private final JobRepository jobRepository;
    private final JobHandlerRegistry handlerRegistry;
    private final JobStateService jobStateService;

    public JobExecutionService(JobRepository jobRepository, JobHandlerRegistry handlerRegistry, JobStateService jobStateService) {
        this.jobRepository = jobRepository;
        this.handlerRegistry = handlerRegistry;
        this.jobStateService = jobStateService;
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

        try {
            jobStateService.markRunning(jobId);
            // Reload the job after markRunning to ensure we have the latest state and attempts count
            job = jobRepository.findById(jobId).orElse(null);
            if (job == null) {
                return; // job might have been deleted after marking running
            }
            JobHandler handler = handlerRegistry.get(job.getType());
            String result = handler.execute(job.getPayload());
            jobStateService.markSuccess(jobId, result);
        } catch (Exception ex) {
            jobStateService.markFailedOrRetry(jobId, ex);
        }
    }
}
