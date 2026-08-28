package io.github.nwabuikesamuel.jobscheduler.controller;

import io.github.nwabuikesamuel.jobscheduler.dto.JobResponse;
import io.github.nwabuikesamuel.jobscheduler.dto.JobSubmitRequest;
import io.github.nwabuikesamuel.jobscheduler.model.Job;
import io.github.nwabuikesamuel.jobscheduler.model.JobStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Part A2-A5 and A7 from test-scenarios.md.
 * Runs the real Spring context on a random port against the in-memory
 * "test" profile database (see application-test.yml), so these never touch
 * your dev H2 file. Uses TestRestTemplate to hit the actual HTTP endpoints -
 * closest thing to testing what a real client would experience.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
class JobControllerIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    // --- A2: job submission ---

    @Test
    void submittingValidEchoJob_returns201_withPendingStatus() {
        // TODO: build a JobSubmitRequest(type="ECHO", payload="hello", priority=3, maxAttempts=3),
        //       POST to url("/api/jobs") via restTemplate.postForEntity(...),
        //       assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED),
        //       assertThat(response.getBody().status).isEqualTo(JobStatus.PENDING).
        JobSubmitRequest request = new JobSubmitRequest();
        request.setType("ECHO");    
        request.setPayload("hello");
        request.setPriority(3);
        request.setMaxAttempts(3);
        ResponseEntity<JobResponse> response = restTemplate.postForEntity(url("/api/jobs"), request, JobResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().status).isEqualTo(JobStatus.PENDING);
    }

    @Test
    void submittingUnknownJobType_returns400() {
        // TODO: POST with type="NOT_REAL", assert 400.
        JobSubmitRequest request = new JobSubmitRequest();
        request.setType("NOT_REAL");
        request.setPayload("test");
        ResponseEntity<JobResponse> response = restTemplate.postForEntity(url("/api/jobs"), request, JobResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void submittingBlankType_returns400_validationFailure() {
        // TODO: POST with type="" (or null), assert 400.
        JobSubmitRequest request = new JobSubmitRequest();
        request.setType("");
        request.setPayload("test");
        ResponseEntity<String> response = restTemplate.postForEntity(url("/api/jobs"), request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void submittingPriorityOutOfRange_returns400() {
        // TODO: POST with priority=0 and separately priority=11, assert both 400.
        JobSubmitRequest requestLow = new JobSubmitRequest();
        requestLow.setType("ECHO");
        requestLow.setPayload("test");
        requestLow.setPriority(0);
        ResponseEntity<String> responseLow = restTemplate.postForEntity(url("/api/jobs"), requestLow, String.class);
        assertThat(responseLow.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        JobSubmitRequest requestHigh = new JobSubmitRequest();
        requestHigh.setType("ECHO");
        requestHigh.setPayload("test");
        requestHigh.setPriority(11);
        ResponseEntity<String> responseHigh = restTemplate.postForEntity(url("/api/jobs"), requestHigh, String.class);
        assertThat(responseHigh.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST); 
    }

    // --- A3: retrieval and listing ---

    @Test
    void gettingJobById_afterSubmission_returnsMatchingFields() {
        // TODO: submit a job, take its id from the response, GET /api/jobs/{id},
        //       assert the returned type/payload/priority match what was submitted.
        JobSubmitRequest request = new JobSubmitRequest();
        request.setType("ECHO");
        request.setPayload("hello");
        request.setPriority(3);
        ResponseEntity<JobResponse> submitResponse = restTemplate.postForEntity(url("/api/jobs"), request, JobResponse.class);
        String jobId = submitResponse.getBody().id;
        ResponseEntity<JobResponse> getResponse = restTemplate.getForEntity(url("/api/jobs/" + jobId), JobResponse.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().type).isEqualTo(request.getType  ());
        // assertThat(getResponse.getBody().payload).isEqualTo(request.getPayload()); // Note: JobResponse doesn't have payload field, so this line is commented out.
        assertThat(getResponse.getBody().priority).isEqualTo(request.getPriority());
    }

    @Test
    void gettingNonexistentJobId_returns404() {
        // TODO: GET /api/jobs/00000000-0000-0000-0000-000000000000, assert 404.
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Test
    void listingAllJobs_includesJustSubmittedJob() {
        // TODO: submit a job, GET /api/jobs, assert the list contains that job's id.
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Test
    void listingJobsFilteredByStatus_excludesOtherStatuses() {
        // TODO: submit a job (starts PENDING), immediately GET /api/jobs?status=SUCCESS,
        //       assert the freshly-submitted job's id is NOT in that list.
        throw new UnsupportedOperationException("not implemented yet");
    }

    // --- A4: cancellation ---

    @Test
    void cancellingPendingJob_returns204_andJobIsGone() {
        // TODO: submit a job fast enough to catch it PENDING (or use a job type
        //       with no registered handler thread contention - simplest is to
        //       submit and immediately DELETE before a worker could plausibly
        //       pick it up), assert 204, then GET the same id and assert 404.
        //       Note: this is timing-sensitive against the real worker pool -
        //       if it's flaky, consider adding a way to pause workers in tests.
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Test
    void cancellingNonPendingJob_returns400() {
        // TODO: submit a job, poll until it's RUNNING or terminal, then try to
        //       DELETE it, assert 400.
        throw new UnsupportedOperationException("not implemented yet");
    }

    // --- A5: end-to-end execution ---

    @Test
    void echoJob_eventuallyReachesSuccess_withCorrectResult() throws InterruptedException {
        // TODO: submit an ECHO job with a distinctive payload, poll GET /api/jobs/{id}
        //       every ~200ms up to a few seconds until status is SUCCESS or DEAD_LETTER,
        //       assert SUCCESS and that result contains the payload text.
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Test
    void alwaysFailingJob_withMaxAttemptsOne_reachesDeadLetter() throws InterruptedException {
        // TODO: you'll need a handler that always fails - FLAKY is only ~50%,
        //       so either submit several FLAKY jobs and check at least one
        //       dead-letters over enough attempts, or add a small "ALWAYS_FAIL"
        //       test-only handler. Poll until terminal, assert DEAD_LETTER
        //       and attempts == 1.
        throw new UnsupportedOperationException("not implemented yet");
    }

    // --- A7: crash recovery (manual test - see notes in test-scenarios.md) ---
    // No automated test here by design. To verify manually:
    //   1. Submit a slow job, stop the app mid-execution (Ctrl+C).
    //   2. Confirm its row in H2 console still shows RUNNING.
    //   3. Restart the app, confirm it gets re-queued and reaches a terminal state.
}
