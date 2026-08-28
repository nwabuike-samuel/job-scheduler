package io.github.nwabuikesamuel.jobscheduler.worker;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.PriorityBlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Part A1 from test-scenarios.md.
 * Pure unit tests - no Spring context, no database. These should be fast
 * and are a good first target since QueuedJobRef has zero dependencies.
 */
class QueuedJobRefTest {

    @Test
    void higherPriorityRef_sortsBeforeLowerPriorityRef() {
        // priority 1 = highest priority (see the field comment on Job.priority)
        // TODO: create two QueuedJobRef instances with priority 1 and priority 5,
        //       same createdAt, and assert compareTo() puts priority 1 first.
        QueuedJobRef highPriority = new QueuedJobRef("1",1, Instant.now());
        QueuedJobRef lowPriority = new QueuedJobRef("2",5, Instant.now());
        assertThat(highPriority.compareTo(lowPriority)).isLessThan(0);
    }

    @Test
    void equalPriority_sortsByCreatedAtAscending_olderFirst() {
        // TODO: create two refs with the same priority but different createdAt
        //       (e.g. Instant.now() and Instant.now().plusSeconds(10)),
        //       assert the older one sorts first.
        QueuedJobRef older = new QueuedJobRef("3", 3, Instant.now());
        QueuedJobRef newer = new QueuedJobRef("4", 3, Instant.now().plusSeconds(10));
        assertThat(older.compareTo(newer)).isLessThan(0);
        System.out.println("Older createdAt: " + older.createdAt() + ", Newer createdAt: " + newer.createdAt());
    }

    @Test
    void priorityBlockingQueue_drainsInExpectedOrder_whenInsertedOutOfOrder() throws InterruptedException {
        PriorityBlockingQueue<QueuedJobRef> queue = new PriorityBlockingQueue<>();

        // TODO: insert refs in a deliberately scrambled order (e.g. priority 5, then 1, then 3),
        //       then queue.take() three times and assert they come out 1, 3, 5.
        //       assertThat(...) from AssertJ is already imported for you above.
        queue.add(new QueuedJobRef("45", 5, Instant.now()));
        queue.add(new QueuedJobRef("125", 1, Instant.now()));
        queue.add(new QueuedJobRef("93", 3, Instant.now()));
        assertThat(queue.take().priority()).isEqualTo(1);
        assertThat(queue.take().priority()).isEqualTo(3);
        assertThat(queue.take().priority()).isEqualTo(5);
    }
}
