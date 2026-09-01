package io.github.nwabuikesamuel.jobscheduler.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.github.nwabuikesamuel.jobscheduler.model.Job;
import io.github.nwabuikesamuel.jobscheduler.repository.JobRepository;
import io.github.nwabuikesamuel.jobscheduler.model.JobStatus;
import io.github.nwabuikesamuel.jobscheduler.worker.ConcurrencyTestHandler;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

@SpringBootTest
public class JobExecutionConcurrencyTest {

    @Autowired
    private JobExecutionService jobExecutionService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ConcurrencyTestHandler concurrencyTestHandler;
    // --- A8: concurrency safety ---

    @Test
    void concurrentRunJobCalls_onSameId_onlyExecuteOnce() throws Exception {
        // This test would ideally simulate concurrent calls to runJob with the same jobId
        // and assert that only one execution occurs. However, implementing such a test
        // requires a more complex setup with threads or async execution, which is beyond
        // the scope of this simple example.

        Job job = new Job();
        job.setId("concurrentJob3");
        job.setType("CONCURRENCY_TEST");
        job.setPayload("testPayload");
        job.setStatus(JobStatus.PENDING);
        job.setAttempts(0);
        job.setMaxAttempts(3);

        jobRepository.save(job);

        int numberOfThreads = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        Runnable task = () -> {
            try {
                startLatch.await(); // Wait for all threads to be ready
                jobExecutionService.runJob("concurrentJob3");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                doneLatch.countDown();
            }
        };

        executorService.submit(task);
        executorService.submit(task);

        // Release both threads at approximately the same time
        startLatch.countDown();
        // Wait for both threads to finish
        doneLatch.await();
        executorService.shutdown();
        assertThat(concurrencyTestHandler.getExecutionCount()).isEqualTo(1);
    }
    
}
