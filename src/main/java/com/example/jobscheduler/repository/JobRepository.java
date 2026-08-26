package com.example.jobscheduler.repository;

import com.example.jobscheduler.model.Job;
import com.example.jobscheduler.model.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, String> {

    List<Job> findByStatus(JobStatus status);

    // Used on startup to re-queue jobs that were mid-flight when the app died.
    List<Job> findByStatusIn(List<JobStatus> statuses);

    // Used by the retry scheduler to find jobs whose backoff window has elapsed.
    List<Job> findByStatusAndNextRetryAtBefore(JobStatus status, Instant time);
}
