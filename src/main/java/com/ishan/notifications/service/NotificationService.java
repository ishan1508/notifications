package com.ishan.notifications.service;

import com.ishan.notifications.domain.Notification;
import com.ishan.notifications.domain.NotificationStatus;
import com.ishan.notifications.dto.CreateNotificationRequest;
import com.ishan.notifications.dto.NotificationPageResponse;
import com.ishan.notifications.dto.NotificationResponse;
import com.ishan.notifications.exception.ResourceNotFoundException;
import com.ishan.notifications.repository.NotificationRepository;
import com.ishan.notifications.service.NotificationCursorCodec.Cursor;
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
    private final NotificationCursorCodec cursorCodec;

    public NotificationService(NotificationRepository repository, Clock clock, NotificationCursorCodec cursorCodec) {
        this.repository = repository;
        this.clock = clock;
        this.cursorCodec = cursorCodec;
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

    public NotificationPageResponse find(
            String recipientId, Optional<NotificationStatus> status, Optional<String> cursor, int limit) {
        Cursor position = cursor.map(cursorCodec::decode).orElse(null);
        List<Notification> matches = repository.findAll().stream()
                .filter(notification -> recipientId.trim().equals(notification.recipientId()))
                .filter(notification ->
                        status.map(value -> value == notification.status()).orElse(true))
                .sorted(NEWEST_FIRST)
                .filter(notification -> isAfterCursor(notification, position))
                .limit((long) limit + 1)
                .toList();

        boolean hasMore = matches.size() > limit;
        List<Notification> page = hasMore ? matches.subList(0, limit) : matches;
        String nextCursor = hasMore ? cursorCodec.encode(page.getLast()) : null;
        List<NotificationResponse> items =
                page.stream().map(NotificationResponse::from).toList();
        return new NotificationPageResponse(items, nextCursor, hasMore);
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

    private boolean isAfterCursor(Notification notification, Cursor cursor) {
        if (cursor == null) {
            return true;
        }
        int timeComparison = notification.createdAt().compareTo(cursor.createdAt());
        return timeComparison < 0 || timeComparison == 0 && notification.id().compareTo(cursor.id()) > 0;
    }
}
