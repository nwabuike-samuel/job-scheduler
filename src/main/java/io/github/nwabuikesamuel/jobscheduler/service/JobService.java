package io.github.nwabuikesamuel.jobscheduler.service;

import io.github.nwabuikesamuel.jobscheduler.dto.JobSubmitRequest;
import io.github.nwabuikesamuel.jobscheduler.exception.JobNotFoundException;
import io.github.nwabuikesamuel.jobscheduler.model.Job;
import io.github.nwabuikesamuel.jobscheduler.model.JobStatus;
import io.github.nwabuikesamuel.jobscheduler.repository.JobRepository;
import io.github.nwabuikesamuel.jobscheduler.worker.JobHandlerRegistry;
import io.github.nwabuikesamuel.jobscheduler.worker.JobQueueService;
import io.github.nwabuikesamuel.jobscheduler.worker.QueuedJobRef;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final JobQueueService jobQueueService;
    private final JobHandlerRegistry handlerRegistry;

    public JobService(JobRepository jobRepository, JobQueueService jobQueueService, JobHandlerRegistry handlerRegistry) {
        this.jobRepository = jobRepository;
        this.jobQueueService = jobQueueService;
        this.handlerRegistry = handlerRegistry;
    }

    public Job submit(JobSubmitRequest request) {
        if (!handlerRegistry.isRegistered(request.getType())) {
            throw new IllegalArgumentException("Unknown job type: " + request.getType()
                    + ". Register a JobHandler bean for it first.");
        }

        Job job = new Job();
        job.setType(request.getType());
        job.setPayload(request.getPayload());
        job.setPriority(request.getPriority());
        job.setMaxAttempts(request.getMaxAttempts());
        job.setStatus(JobStatus.PENDING);

        job = jobRepository.save(job);
        jobQueueService.enqueue(new QueuedJobRef(job.getId(), job.getPriority(), job.getCreatedAt()));
        return job;
    }

    public Job getOrThrow(String id) {
        return jobRepository.findById(id).orElseThrow(() -> new JobNotFoundException(id));
    }

    public List<Job> list(JobStatus statusFilter) {
        return statusFilter == null ? jobRepository.findAll() : jobRepository.findByStatus(statusFilter);
    }

    public void cancel(String id) {
        Job job = getOrThrow(id);
        if (job.getStatus() != JobStatus.PENDING) {
            throw new IllegalStateException("Only PENDING jobs can be cancelled (current status: " + job.getStatus() + ")");
        }
        jobRepository.delete(job);
    }
}
