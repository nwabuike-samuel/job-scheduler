package io.github.nwabuikesamuel.jobscheduler.worker;
import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ConcurrencyTestHandler implements JobHandler {

    private final AtomicInteger executionCount =
            new AtomicInteger(0);

    @Override
    public String getType() {
        return "CONCURRENCY_TEST";
    }

    @Override
    public String execute(String payload) {
        executionCount.incrementAndGet();
        try {
            Thread.sleep(500); // simulate work
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "success";
    }

    public int getExecutionCount() {
        return executionCount.get();
    }
}
