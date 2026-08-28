package io.github.nwabuikesamuel.jobscheduler.controller;

import io.github.nwabuikesamuel.jobscheduler.dto.JobResponse;
import io.github.nwabuikesamuel.jobscheduler.dto.JobSubmitRequest;
import io.github.nwabuikesamuel.jobscheduler.model.Job;
import io.github.nwabuikesamuel.jobscheduler.model.JobStatus;
import io.github.nwabuikesamuel.jobscheduler.service.JobService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<JobResponse> submit(@Valid @RequestBody JobSubmitRequest request) {
        Job job = jobService.submit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(JobResponse.from(job));
    }

    @GetMapping("/{id}")
    public JobResponse get(@PathVariable String id) {
        return JobResponse.from(jobService.getOrThrow(id));
    }

    @GetMapping
    public List<JobResponse> list(@RequestParam(required = false) JobStatus status) {
        return jobService.list(status).stream().map(JobResponse::from).toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable String id) {
        jobService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
