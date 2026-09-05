package com.ishan.notifications.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateNotificationRequest(
        @NotBlank @Size(max = 100) String recipientId,
        @NotBlank @Size(max = 100) String type,
        @NotBlank @Size(max = 2000) String message) {}
