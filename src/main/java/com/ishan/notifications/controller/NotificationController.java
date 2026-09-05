package com.ishan.notifications.controller;

import com.ishan.notifications.domain.NotificationStatus;
import com.ishan.notifications.dto.CreateNotificationRequest;
import com.ishan.notifications.dto.NotificationResponse;
import com.ishan.notifications.dto.UpdateNotificationStatusRequest;
import com.ishan.notifications.service.NotificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> create(@Valid @RequestBody CreateNotificationRequest request) {
        NotificationResponse response = NotificationResponse.from(service.create(request));
        return ResponseEntity.created(URI.create("/notifications/" + response.id()))
                .body(response);
    }

    @GetMapping("/{notificationId}")
    public NotificationResponse get(@PathVariable UUID notificationId) {
        return NotificationResponse.from(service.get(notificationId));
    }

    @GetMapping
    public List<NotificationResponse> find(
            @RequestParam(required = false)
                    @Size(max = 100) @Pattern(regexp = "\\S(?:.*\\S)?", message = "must not be blank")
                    String recipientId,
            @RequestParam(required = false) NotificationStatus status) {
        return service.find(Optional.ofNullable(recipientId), Optional.ofNullable(status)).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @PatchMapping("/{notificationId}")
    public NotificationResponse updateStatus(
            @PathVariable UUID notificationId, @Valid @RequestBody UpdateNotificationStatusRequest request) {
        return NotificationResponse.from(service.updateStatus(notificationId, request.status()));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> delete(@PathVariable UUID notificationId) {
        service.delete(notificationId);
        return ResponseEntity.noContent().build();
    }
}
