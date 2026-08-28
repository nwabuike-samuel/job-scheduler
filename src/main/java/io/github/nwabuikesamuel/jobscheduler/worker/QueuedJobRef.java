package io.github.nwabuikesamuel.jobscheduler.worker;

import java.time.Instant;

/**
 * We only put the id + ordering fields on the in-memory queue, not the whole
 * Job entity. The worker re-fetches the full row from the DB when it dequeues,
 * which keeps the queue cheap and avoids holding stale entity state.
 */
public record QueuedJobRef(String jobId, int priority, Instant createdAt) implements Comparable<QueuedJobRef> {

    @Override
    public int compareTo(QueuedJobRef other) {
        int byPriority = Integer.compare(this.priority, other.priority); // 1 = highest priority
        if (byPriority != 0) return byPriority;
        return this.createdAt.compareTo(other.createdAt); // older first (FIFO within same priority)
    }
}
