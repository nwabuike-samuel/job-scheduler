package io.github.nwabuikesamuel.jobscheduler.worker;

import org.springframework.stereotype.Component;

@Component
public class AlwaysFailHandler implements JobHandler {

    @Override
    public String getType() {
        return "ALWAYS_FAIL";
    }

    @Override
    public String execute(String payload) throws Exception {
        throw new RuntimeException("Intentional test failure");
    }
}