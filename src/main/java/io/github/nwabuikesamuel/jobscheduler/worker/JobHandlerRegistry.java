package io.github.nwabuikesamuel.jobscheduler.worker;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JobHandlerRegistry {

    private final Map<String, JobHandler> handlers = new ConcurrentHashMap<>();

    // Spring injects every JobHandler bean in the context here automatically.
    public JobHandlerRegistry(List<JobHandler> allHandlers) {
        for (JobHandler handler : allHandlers) {
            handlers.put(handler.getType(), handler);
        }
    }

    public JobHandler get(String type) {
        JobHandler handler = handlers.get(type);
        if (handler == null) {
            throw new IllegalArgumentException("No JobHandler registered for type: " + type);
        }
        return handler;
    }

    public boolean isRegistered(String type) {
        return handlers.containsKey(type);
    }
}
