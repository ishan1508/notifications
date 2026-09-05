package com.ishan.notifications.domain;

import java.time.Instant;
import java.util.UUID;

public record Notification(
        UUID id,
        String recipientId,
        String type,
        String message,
        NotificationStatus status,
        Instant createdAt,
        Instant readAt) {}
