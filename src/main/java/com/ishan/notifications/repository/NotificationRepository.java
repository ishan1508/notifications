package com.ishan.notifications.repository;

import com.ishan.notifications.domain.Notification;
import com.ishan.notifications.domain.NotificationStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(UUID id);

    List<Notification> findAll();

    Optional<Notification> updateStatus(UUID id, NotificationStatus status, Instant readAt);

    boolean deleteById(UUID id);
}
