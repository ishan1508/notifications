package com.ishan.notifications.service;

import com.ishan.notifications.domain.Notification;
import com.ishan.notifications.exception.InvalidCursorException;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class NotificationCursorCodec {

    private static final String SEPARATOR = "|";

    public String encode(Notification notification) {
        String value = notification.createdAt() + SEPARATOR + notification.id();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    public Cursor decode(String cursor) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = decoded.split("\\|", -1);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Unexpected cursor structure");
            }
            return new Cursor(Instant.parse(parts[0]), UUID.fromString(parts[1]));
        } catch (IllegalArgumentException | DateTimeException exception) {
            throw new InvalidCursorException("The pagination cursor is invalid");
        }
    }

    public record Cursor(Instant createdAt, UUID id) {}
}
