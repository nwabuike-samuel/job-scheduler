package com.example.jobscheduler.worker;

import org.springframework.stereotype.Service;

import java.util.concurrent.PriorityBlockingQueue;

@Service
public class JobQueueService {

    private final PriorityBlockingQueue<QueuedJobRef> queue = new PriorityBlockingQueue<>();

    public void enqueue(QueuedJobRef ref) {
        queue.put(ref);
    }

    /** Blocks until a job is available. Called by worker threads in a loop. */
    public QueuedJobRef take() throws InterruptedException {
        return queue.take();
    }

    public int size() {
        return queue.size();
    }
}
