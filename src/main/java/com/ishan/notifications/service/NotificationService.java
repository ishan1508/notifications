package com.ishan.notifications.service;

import com.ishan.notifications.domain.Notification;
import com.ishan.notifications.domain.NotificationStatus;
import com.ishan.notifications.dto.CreateNotificationRequest;
import com.ishan.notifications.exception.ResourceNotFoundException;
import com.ishan.notifications.repository.NotificationRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Comparator<Notification> NEWEST_FIRST =
            Comparator.comparing(Notification::createdAt).reversed().thenComparing(Notification::id);

    private final NotificationRepository repository;
    private final Clock clock;

    public NotificationService(NotificationRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public Notification create(CreateNotificationRequest request) {
        Notification notification = new Notification(
                UUID.randomUUID(),
                request.recipientId().trim(),
                request.type().trim(),
                request.message().trim(),
                NotificationStatus.UNREAD,
                clock.instant(),
                null);
        return repository.save(notification);
    }

    public Notification get(UUID id) {
        return repository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification " + id + " was not found"));
    }

    public List<Notification> find(Optional<String> recipientId, Optional<NotificationStatus> status) {
        Optional<String> normalizedRecipientId = recipientId.map(String::trim);
        return repository.findAll().stream()
                .filter(notification -> normalizedRecipientId
                        .map(value -> value.equals(notification.recipientId()))
                        .orElse(true))
                .filter(notification ->
                        status.map(value -> value == notification.status()).orElse(true))
                .sorted(NEWEST_FIRST)
                .toList();
    }

    public Notification updateStatus(UUID id, NotificationStatus status) {
        Instant readAt = status == NotificationStatus.READ ? clock.instant() : null;
        return repository
                .updateStatus(id, status, readAt)
                .orElseThrow(() -> new ResourceNotFoundException("Notification " + id + " was not found"));
    }

    public void delete(UUID id) {
        if (!repository.deleteById(id)) {
            throw new ResourceNotFoundException("Notification " + id + " was not found");
        }
    }
}
