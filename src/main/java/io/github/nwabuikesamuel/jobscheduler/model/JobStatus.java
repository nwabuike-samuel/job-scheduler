package io.github.nwabuikesamuel.jobscheduler.model;

/**
 * Job lifecycle:
 * PENDING -> RUNNING -> SUCCESS
 *                    -> FAILED -> RETRYING -> RUNNING (loop, while attempts < maxAttempts)
 *                                          -> DEAD_LETTER (once attempts >= maxAttempts)
 */
public enum JobStatus {
    PENDING,
    RUNNING,
    SUCCESS,
    FAILED,
    RETRYING,
    DEAD_LETTER
}
