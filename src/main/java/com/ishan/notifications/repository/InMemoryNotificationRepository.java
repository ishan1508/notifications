package com.ishan.notifications.repository;

import com.ishan.notifications.domain.Notification;
import com.ishan.notifications.domain.NotificationStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryNotificationRepository implements NotificationRepository {

    private final ConcurrentMap<UUID, Notification> notifications = new ConcurrentHashMap<>();

    @Override
    public Notification save(Notification notification) {
        notifications.put(notification.id(), notification);
        return notification;
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return Optional.ofNullable(notifications.get(id));
    }

    @Override
    public List<Notification> findAll() {
        return List.copyOf(notifications.values());
    }

    @Override
    public Optional<Notification> updateStatus(UUID id, NotificationStatus status, Instant readAt) {
        Notification updated = notifications.computeIfPresent(id, (ignored, existing) -> {
            Instant effectiveReadAt =
                    status == NotificationStatus.READ && existing.readAt() != null ? existing.readAt() : readAt;
            return new Notification(
                    existing.id(),
                    existing.recipientId(),
                    existing.type(),
                    existing.message(),
                    status,
                    existing.createdAt(),
                    effectiveReadAt);
        });
        return Optional.ofNullable(updated);
    }

    @Override
    public boolean deleteById(UUID id) {
        return notifications.remove(id) != null;
    }
}
