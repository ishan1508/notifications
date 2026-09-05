package com.ishan.notifications.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.ishan.notifications.domain.Notification;
import com.ishan.notifications.domain.NotificationStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class InMemoryNotificationRepositoryTest {

    @Test
    void handlesConcurrentMarkReadRequestsIdempotently() throws Exception {
        InMemoryNotificationRepository repository = new InMemoryNotificationRepository();
        UUID id = UUID.randomUUID();
        repository.save(
                new Notification(id, "user-1", "WELCOME", "Welcome", NotificationStatus.UNREAD, Instant.EPOCH, null));

        int requestCount = 32;
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(8);
        List<Future<Notification>> futures = new ArrayList<>();

        try {
            for (int index = 0; index < requestCount; index++) {
                Instant requestedReadAt = Instant.ofEpochSecond(index + 1L);
                futures.add(executor.submit(() -> {
                    start.await();
                    return repository
                            .updateStatus(id, NotificationStatus.READ, requestedReadAt)
                            .orElseThrow();
                }));
            }

            start.countDown();
            List<Notification> results = new ArrayList<>();
            for (Future<Notification> future : futures) {
                results.add(future.get(5, TimeUnit.SECONDS));
            }

            Instant retainedReadAt = results.getFirst().readAt();
            assertNotNull(retainedReadAt);
            for (Notification result : results) {
                assertEquals(NotificationStatus.READ, result.status());
                assertEquals(retainedReadAt, result.readAt());
            }
            assertEquals(retainedReadAt, repository.findById(id).orElseThrow().readAt());
        } finally {
            executor.shutdownNow();
        }
    }
}
