package com.ishan.notifications.dto;

import com.ishan.notifications.domain.Notification;
import com.ishan.notifications.domain.NotificationStatus;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String recipientId,
        String type,
        String message,
        NotificationStatus status,
        Instant createdAt,
        Instant readAt) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.id(),
                notification.recipientId(),
                notification.type(),
                notification.message(),
                notification.status(),
                notification.createdAt(),
                notification.readAt());
    }
}
