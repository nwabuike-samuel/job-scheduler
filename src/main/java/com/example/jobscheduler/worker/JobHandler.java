package com.example.jobscheduler.worker;

/**
 * Implement this and register as a Spring bean to add a new job type.
 * getType() must be unique and matches the "type" field submitted via the API.
 */
public interface JobHandler {

    String getType();

    /**
     * Do the work. Return a short human-readable result on success.
     * Throw any exception to signal failure -> triggers the retry/backoff flow.
     */
    String execute(String payload) throws Exception;
}
