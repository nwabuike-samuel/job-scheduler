package com.example.jobscheduler.dto;

import com.example.jobscheduler.model.Job;
import com.example.jobscheduler.model.JobStatus;

import java.time.Instant;

public class JobResponse {
    public String id;
    public String type;
    public JobStatus status;
    public int priority;
    public int attempts;
    public int maxAttempts;
    public Instant createdAt;
    public Instant updatedAt;
    public Instant nextRetryAt;
    public String result;
    public String errorMessage;

    public static JobResponse from(Job job) {
        JobResponse r = new JobResponse();
        r.id = job.getId();
        r.type = job.getType();
        r.status = job.getStatus();
        r.priority = job.getPriority();
        r.attempts = job.getAttempts();
        r.maxAttempts = job.getMaxAttempts();
        r.createdAt = job.getCreatedAt();
        r.updatedAt = job.getUpdatedAt();
        r.nextRetryAt = job.getNextRetryAt();
        r.result = job.getResult();
        r.errorMessage = job.getErrorMessage();
        return r;
    }
}
