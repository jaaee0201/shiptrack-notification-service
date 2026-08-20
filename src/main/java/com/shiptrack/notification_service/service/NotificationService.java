package com.shiptrack.notification_service.service;

import com.shiptrack.notification_service.dto.response.NotificationResponse;
import com.shiptrack.notification_service.entity.Notification;
import com.shiptrack.notification_service.entity.NotificationStatus;
import com.shiptrack.notification_service.entity.ShipmentEventType;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.Instant;
import java.util.List;

public interface NotificationService {
    List<NotificationResponse> getAllNotifications();
    NotificationResponse getNotificationById(@PathVariable String id);
    List<Notification> findByNotificationStatus(NotificationStatus status);
    List<Notification> findByEventType(ShipmentEventType eventType);
    List<Notification> findByEventTimestampBetween(Instant start, Instant end);
    List<Notification> getAllNotificationWtMapping();
    }
