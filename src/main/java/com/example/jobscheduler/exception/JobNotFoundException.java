package com.example.jobscheduler.exception;

public class JobNotFoundException extends RuntimeException {
    public JobNotFoundException(String id) {
        super("No job found with id: " + id);
    }
}
