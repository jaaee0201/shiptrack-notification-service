package com.shiptrack.notification_service.mapper;


import com.shiptrack.notification_service.dto.response.NotificationResponse;
import com.shiptrack.notification_service.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
            .id(notification.getId())
            .shipmentId(notification.getShipmentId())
            .trackingNumber(notification.getTrackingNumber())
            .eventType(notification.getEventType())
            .notificationStatus(notification.getNotificationStatus())
            .payload(notification.getPayload())
            .eventTimestamp(notification.getEventTimestamp())
            .processedAt(notification.getProcessedAt())
            .failureReason(notification.getFailureReason())
            .retryCount(notification.getRetryCount())
            .build();
    }
}
