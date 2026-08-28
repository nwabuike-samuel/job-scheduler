package io.github.nwabuikesamuel.jobscheduler.worker;

import org.springframework.stereotype.Component;

/**
 * Demo handler for the "ECHO" job type. Use this to smoke-test the pipeline
 * end to end before wiring in real job types (send email, generate report...).
 */
@Component
public class EchoJobHandler implements JobHandler {

    @Override
    public String getType() {
        return "ECHO";
    }

    @Override
    public String execute(String payload) throws Exception {
        Thread.sleep(500); // simulate work
        return "Echoed: " + payload;
    }
}
