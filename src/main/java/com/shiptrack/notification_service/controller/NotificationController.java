package com.shiptrack.notification_service.controller;

import com.shiptrack.notification_service.dto.response.NotificationResponse;
import com.shiptrack.notification_service.service.NotificationServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Notifications", description = "Notification history and processing status")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationServiceImpl notificationService;


    @Operation(summary = "List all notifications (optionally filter by shipmentId)")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','CUSTOMER')")
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @Operation(summary = "Get a single notification by id")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','CUSTOMER')")
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotification(@PathVariable String id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }
}
