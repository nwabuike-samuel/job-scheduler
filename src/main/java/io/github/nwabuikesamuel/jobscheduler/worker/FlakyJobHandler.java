package io.github.nwabuikesamuel.jobscheduler.worker;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Demo handler for the "FLAKY" job type. Fails roughly half the time so you
 * can watch jobs move PENDING -> RUNNING -> FAILED -> RETRYING -> ... -> SUCCESS/DEAD_LETTER.
 */
@Component
public class FlakyJobHandler implements JobHandler {

    @Override
    public String getType() {
        return "FLAKY";
    }

    @Override
    public String execute(String payload) throws Exception {
        Thread.sleep(300);
        if (ThreadLocalRandom.current().nextBoolean()) {
            throw new RuntimeException("Simulated transient failure for payload: " + payload);
        }
        return "Succeeded on this attempt: " + payload;
    }
}
