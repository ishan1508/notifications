package com.ishan.notifications.dto;

import com.ishan.notifications.domain.NotificationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateNotificationStatusRequest(@NotNull NotificationStatus status) {}
